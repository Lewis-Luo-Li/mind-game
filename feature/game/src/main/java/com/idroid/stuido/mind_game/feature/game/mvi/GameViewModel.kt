package com.idroid.stuido.mind_game.feature.game.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.feature.game.GameMode
import com.idroid.stuido.mind_game.feature.game.GameStatus
import com.idroid.stuido.mind_game.feature.game.reducers.GameReducer
import com.idroid.stuido.mind_game.feature.game.engine.GameFactory
import com.idroid.stuido.mind_game.feature.game.engine.GameSeed
import com.idroid.stuido.mind_game.feature.game.engine.HintProvider
import com.idroid.stuido.mind_game.feature.game.engine.InputValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 见 §9——MVI 的 ViewModel：唯一状态持有者 + `dispatch` 归一到 [GameEvent]。
 *
 * 职责分层：
 *  - **纯状态变换**交给 [GameReducer]；
 *  - **跨线程/耗时**（生成谜题、求解）放到 [Dispatchers.Default]；
 *  - **一次性副作用**用 Channel 型 [GameEffect] 发出（Snackbar / 完成 / 超限）；
 *  - **计时器**由本类用一个 1Hz 协程驱动 [GameEvent.Tick]。
 *
 * 构造参数均可注入（引擎默认实现便于直接使用；单测时可传入假件）。
 */
class GameViewModel(
    private val gameFactory: GameFactory = GameFactory(),
    private val hintProvider: HintProvider = HintProvider(),
    private val inputValidator: InputValidator = InputValidator(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _effect = Channel<GameEffect>(Channel.BUFFERED)
    val effect: Flow<GameEffect> = _effect.receiveAsFlow()

    /** 最近一次开局参数：供 [GameEvent.Restart] 原样复用。 */
    private var lastRequest: GameConfig? = null

    /** 节奏驱动器：每秒若处于 RUNNING 则 tick＋1。 */
    private var tickerJob: kotlinx.coroutines.Job? = null

    init {
        startTicker()
    }

    /** 玩家在开局后应显式 dispatch([GameEvent.NewGame]); 屏幕可在进入时先发一局。 */
    fun dispatch(event: GameEvent) {
        when (event) {
            is GameEvent.NewGame -> newGame(
                event.mode, event.spec, event.difficulty, event.seed,
            )

            GameEvent.Restart -> lastRequest?.let { cfg -> newGame(cfg.mode, cfg.spec, cfg.difficulty, cfg.seed) }
            GameEvent.Exit -> Unit // 退出由宿主（onExit）完成，不在此处理

            is GameEvent.SelectCell -> _uiState.update { GameReducer.selectCell(it, event.index) }
            is GameEvent.InputNumber -> inputNumber(event.index, event.value)
            is GameEvent.ToggleNote -> _uiState.update { GameReducer.toggleNote(it, event.index, event.value) }
            GameEvent.Erase -> erase()

            GameEvent.ToggleNoteMode -> _uiState.update { GameReducer.toggleNoteMode(it) }
            GameEvent.RequestHint -> requestHint()
            GameEvent.RequestCheck -> requestCheck()
            GameEvent.RequestSolve -> solve()

            is GameEvent.Tick -> tick(event.elapsedSeconds)
            GameEvent.TogglePause -> onTogglePause()
        }
    }

    // ---------------------------------------------------------------- 启动 / 重开

    private fun newGame(mode: GameMode, spec: BoardSpec, difficulty: Difficulty, seed: Long?) {
        if (_uiState.value.progress.isBusy) return
        val config = GameConfig(mode = mode, spec = spec, difficulty = difficulty, seed = seed)
        lastRequest = config
        _uiState.update { GameReducer.setBusy(GameReducer.setGameConfig(it, config), true) }

        viewModelScope.launch {
            val result = try {
                withContext(Dispatchers.Default) {
                    gameFactory.newGame(mode, spec, difficulty, seed)
                }
            } catch (e: IllegalArgumentException) {
                val reason = e.message ?: "This board size/difficulty combination is unavailable."
                _effect.send(GameEffect.NewGameFailed(reason))
                null
            } catch (e: Exception) {
                _effect.send(GameEffect.NewGameFailed(e.message ?: "Failed to generate the puzzle."))
                null
            }

            if (result == null) {
                _uiState.update { GameReducer.setBusy(it, false) }
                return@launch
            }
            applySeed(result)
        }
    }

    /** 把生成成功的种子落盘：记录配置 → 铺盘 → 清除忙碌 → 清空判错高亮。 */
    private fun applySeed(seed: GameSeed) {
        _uiState.update {
            GameReducer.startGame(GameReducer.setGameConfig(it, it.config), seed)
        }
        _uiState.update { GameReducer.setBusy(it, false) }
    }

    // ---------------------------------------------------------------- 盘面操作

    /** 在 [index] 输入 [value]；非空格时做判错与完成检测。 */
    private fun inputNumber(index: Int, value: Int) {
        val before = _uiState.value
        val board = before.board
        val isGiven = index in board.givens
        if (isGiven) return

        val valueChanged = value != 0 && board.values.getOrNull(index) != value

        _uiState.update { GameReducer.inputNumber(it, index, value) }

        if (!valueChanged) return
        // 判错：与终盘不一致 => mistakes++
        if (value != 0 && hintProvider.isCorrect(index, value, board.solution).not()) {
            registerMistake()
            return
        }
        // 填对：若全盘填完 => 完成
        if (inputValidator.isComplete(_uiState.value.board)) {
            finishAsSuccess()
        }
    }

    private fun erase() {
        val index = _uiState.value.selection.index ?: return
        _uiState.update { GameReducer.erase(it, index) }
    }

    private fun registerMistake() {
        val state = _uiState.value
        val exceeds = state.progress.mistakes + 1 >= state.config.maxMistakes
        _uiState.update { GameReducer.incrementMistakes(it) }
        _effect.trySend(GameEffect.ShowMessage("❌ Doesn't match the solution (+1)"))
        if (exceeds) {
            _uiState.update { GameReducer.fail(it) }
            _effect.trySend(GameEffect.GameOver)
        }
    }

    // ---------------------------------------------------------------- 工具

    private fun requestHint() {
        val index = _uiState.value.selection.index ?: return
        if (index in _uiState.value.board.givens) return
        val answer = hintProvider.hintFor(index, _uiState.value.board.solution) ?: return
        _uiState.update { GameReducer.inputNumber(it, index, answer) }

        if (inputValidator.isComplete(_uiState.value.board)) finishAsSuccess()
    }

    /**
     * 判错：不改变盘值，仅把"当前已填但与终盘不符"的格子一次性标入
     * `Selection.conflicts` 供 UI 红显；同时给出提示文案。
     */
    private fun requestCheck() {
        val board = _uiState.value.board
        val wrong = board.values.indices
            .filter { i -> i !in board.givens && board.values[i] != 0 && board.values[i] != board.solution[i] }
            .toSet()

        _uiState.update { GameReducer.setConflicts(it, wrong) }
        _effect.trySend(
            GameEffect.ShowMessage(
                if (wrong.isEmpty()) "✅ All entered digits match the solution"
                else "⚠️ ${wrong.size} cell(s) don't match the solution"
            )
        )
    }

    /** 自动求解（演示）：铺满终盘后走完成流程。 */
    private fun solve() {
        _uiState.update { GameReducer.revealSolution(it) }
        finishAsSuccess()
    }

    private fun finishAsSuccess() {
        _uiState.update { GameReducer.finish(it) }
        _effect.trySend(GameEffect.PuzzleCompleted)
    }

    // ---------------------------------------------------------------- 暂停 / 计时

    private fun onTogglePause() {
        val st = _uiState.value.progress.status
        if (st == GameStatus.FINISHED) return
        _uiState.update { GameReducer.togglePause(it) }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                // 仅在 RUNNING 时自我推进 1 秒
                if (_uiState.value.progress.status == GameStatus.RUNNING) {
                    tick(_uiState.value.progress.timerSeconds + 1)
                }
            }
        }
    }

    private fun tick(elapsedSeconds: Int) {
        if (_uiState.value.progress.status == GameStatus.RUNNING) {
            _uiState.update { GameReducer.tick(it, elapsedSeconds) }
        }
    }

    override fun onCleared() {
        tickerJob?.cancel()
        super.onCleared()
    }
}

// 便于在 dispatch 分支内链式更新。
private fun MutableStateFlow<GameUiState>.update(transform: (GameUiState) -> GameUiState) {
    this.value = transform(this.value)
}
