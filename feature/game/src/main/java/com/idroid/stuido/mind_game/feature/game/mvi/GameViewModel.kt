package com.idroid.stuido.mind_game.feature.game.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.core.logic.config.RewardsSection
import com.idroid.stuido.mind_game.feature.game.GameMode
import com.idroid.stuido.mind_game.feature.game.GameOutcome
import com.idroid.stuido.mind_game.feature.game.GameStatus
import com.idroid.stuido.mind_game.feature.game.reducers.GameReducer
import com.idroid.stuido.mind_game.feature.game.engine.GameFactory
import com.idroid.stuido.mind_game.feature.game.engine.GameSeed
import com.idroid.stuido.mind_game.feature.game.engine.HintProvider
import com.idroid.stuido.mind_game.feature.game.engine.InputValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher
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
 *  - **跨线程/耗时**（生成谜题、求解）放到可注入的 dispatcher（默认 [Dispatchers.Default]）；
 *  - **一次性副作用**用 Channel 型 [GameEffect] 发出（Snackbar / 完成 / 超限）；
 *  - **计时器**由本类用一个 1Hz 协程驱动 [GameEvent.Tick]。
 *
 * 构造参数均可注入（引擎默认实现便于直接使用；单测时可传入假件）。
 */
class GameViewModel(
    private val gameFactory: GameFactory = GameFactory(),
    private val hintProvider: HintProvider = HintProvider(),
    private val inputValidator: InputValidator = InputValidator(),
    private val generationDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _effect = Channel<GameEffect>(Channel.BUFFERED)
    val effect: Flow<GameEffect> = _effect.receiveAsFlow()

    /** 最近一次开局参数：供 [GameEvent.Restart] 原样复用。 */
    private var lastRequest: GameConfig? = null

    private var lastGame: Pair<GameConfig, GameSeed>? = null

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
                event.maxMistakes, event.maxHints, event.rewards,
            )

            GameEvent.Restart -> lastRequest?.let { cfg ->
                newGame(
                    cfg.mode, cfg.spec, cfg.difficulty, cfg.seed,
                    cfg.maxMistakes, cfg.maxHints, cfg.rewards,
                )
            }
            GameEvent.RetrySamePuzzle -> retrySamePuzzle()
            GameEvent.Exit -> Unit // 退出由宿主（onExit）完成，不在此处理

            is GameEvent.SelectCell -> ifRunning {
                _uiState.update { GameReducer.selectCell(it, event.index) }
            }
            is GameEvent.InputNumber -> ifRunning { inputNumber(event.index, event.value) }
            is GameEvent.ToggleNote -> ifRunning {
                _uiState.update { GameReducer.toggleNote(it, event.index, event.value) }
            }
            is GameEvent.KeypadDigit -> ifRunning { keypadDigit(event.digit) }
            GameEvent.Erase -> ifRunning { erase() }
            GameEvent.Undo -> ifRunning {
                _uiState.update { GameReducer.undo(it) }
            }

            GameEvent.ToggleNoteMode -> _uiState.update { GameReducer.toggleNoteMode(it) }
            GameEvent.RequestHint -> ifRunning { requestHint() }
            is GameEvent.GrantRewardedHints -> ifRunning {
                _uiState.update { GameReducer.grantRewardedHints(it, event.amount) }
            }
            is GameEvent.GrantRewardedLives -> {
                // 复活入口在 FINISHED(FAILED) 面板，不走 ifRunning。
                _uiState.update { GameReducer.grantRewardedLives(it, event.amount) }
            }
            GameEvent.RequestCheck -> ifRunning { requestCheck() }
            GameEvent.RequestSolve -> ifRunning { solve() }

            is GameEvent.Tick -> tick(event.elapsedSeconds)
            GameEvent.TogglePause -> onTogglePause()
        }
    }

    // ---------------------------------------------------------------- 启动 / 重开

    private fun newGame(
        mode: GameMode,
        spec: BoardSpec,
        difficulty: Difficulty,
        seed: Long?,
        maxMistakes: Int,
        maxHints: Int,
        rewards: RewardsSection,
    ) {
        if (_uiState.value.progress.isBusy) return
        val config = GameConfig(
            mode = mode,
            spec = spec,
            difficulty = difficulty,
            seed = seed,
            maxMistakes = maxMistakes,
            maxHints = maxHints,
            rewards = rewards,
        )
        lastRequest = config
        _uiState.update { GameReducer.setBusy(GameReducer.setGameConfig(it, config), true) }

        viewModelScope.launch {
            val result = try {
                withContext(generationDispatcher) {
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
        val config = _uiState.value.config
        lastGame = config to seed
        _uiState.update {
            GameReducer.startGame(GameReducer.setGameConfig(it, config), seed)
        }
        _uiState.update { GameReducer.setBusy(it, false) }
    }

    private fun retrySamePuzzle() {
        val (config, seed) = lastGame ?: return
        if (_uiState.value.progress.isBusy) return
        lastRequest = config
        _uiState.update {
            GameReducer.startGame(GameReducer.setGameConfig(it, config), seed)
        }
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

    /** 数字キー押下を dispatch 時点の最新 state で解決する（選択・given・入力モードはここで見る）。 */
    private fun keypadDigit(digit: Int) {
        val s = _uiState.value
        val idx = s.selection.index ?: return
        if (idx in s.board.givens) return
        if (s.selection.inputMode == InputMode.NOTE) {
            _uiState.update { GameReducer.toggleNote(it, idx, digit) }
        } else {
            inputNumber(idx, digit)
        }
    }

    private fun erase() {
        val index = _uiState.value.selection.index ?: return
        _uiState.update { GameReducer.erase(it, index) }
    }

    private fun registerMistake() {
        val state = _uiState.value
        // livesTotal 含激励广告补充的 bonusLives：复活后再错一次仍正常结束。
        val exceeds = state.progress.mistakes + 1 >= state.livesTotal
        _uiState.update { GameReducer.incrementMistakes(it) }
        _effect.trySend(GameEffect.ShowMessage("❌ Doesn't match the solution (+1)"))
        if (exceeds) {
            _uiState.update { GameReducer.finish(it, GameOutcome.FAILED) }
            _effect.trySend(GameEffect.GameOver(_uiState.value.progress.gameInstance))
        }
    }

    // ---------------------------------------------------------------- 工具

    private fun requestHint() {
        val state = _uiState.value
        if (state.hintsLeft == 0) {
            _effect.trySend(GameEffect.ShowMessage("No hints left"))
            return
        }
        val board = state.board
        val target = state.selection.index?.takeIf { index ->
            index in board.values.indices &&
                index !in board.givens &&
                board.values[index] != board.solution[index]
        } ?: board.values.indices.firstOrNull { index ->
            index !in board.givens && board.values[index] != board.solution[index]
        } ?: return
        val answer = board.solution[target]
        _uiState.update { GameReducer.useHint(it, target, answer) }
        _effect.trySend(GameEffect.HintFilled(target, answer))

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
        _uiState.update { GameReducer.finish(GameReducer.revealSolution(it), GameOutcome.REVEALED) }
        _effect.trySend(GameEffect.SolutionRevealed(_uiState.value.progress.gameInstance))
    }

    private fun finishAsSuccess() {
        _uiState.update { GameReducer.finish(it, GameOutcome.SOLVED) }
        _effect.trySend(GameEffect.PuzzleCompleted(_uiState.value.progress.gameInstance))
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

    private inline fun ifRunning(block: () -> Unit) {
        if (_uiState.value.progress.status == GameStatus.RUNNING) block()
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
