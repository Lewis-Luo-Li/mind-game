package com.idroid.stuido.mind_game.feature.game.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.core.ui.theme.MindgameTheme
import com.idroid.stuido.mind_game.feature.game.GameMode
import com.idroid.stuido.mind_game.feature.game.GameStatus
import com.idroid.stuido.mind_game.feature.game.R
import com.idroid.stuido.mind_game.feature.game.mvi.BoardCells
import com.idroid.stuido.mind_game.feature.game.mvi.GameConfig
import com.idroid.stuido.mind_game.feature.game.mvi.GameEvent
import com.idroid.stuido.mind_game.feature.game.mvi.GameProgress
import com.idroid.stuido.mind_game.feature.game.mvi.GameUiState
import com.idroid.stuido.mind_game.feature.game.mvi.InputMode
import com.idroid.stuido.mind_game.feature.game.mvi.Selection
import com.idroid.stuido.mind_game.feature.game.ui.components.FinishPanel
import com.idroid.stuido.mind_game.feature.game.ui.components.PauseScrim

@Composable
private fun describeTitle(state: GameUiState): String {
    if (state.board.size == 0) return stringResource(R.string.game_not_started)
    val specName = when (state.config.spec.size) {
        4 -> "4×4"
        6 -> "6×6"
        else -> "9×9"
    }
    val mode = if (state.config.mode == GameMode.DAILY) "· ${stringResource(R.string.label_daily)}" else ""
    return "$specName Sudoku · ${state.config.difficulty.name}$mode"
}

/**
 * 纯展示的整屏 UI（MVI presentation）：输入 [GameUiState] 与事件回调，宿主负责接线。
 *
 * 兼容规格自动切换：难度/规格标题、计时、错误计数、数字键盘规模与棋盘完全由 [GameUiState] 驱动。
 */
@Composable
fun GameScreen(
    state: GameUiState,
    dispatch: (GameEvent) -> Unit,
    onExit: () -> Unit,
) {
    val status = state.progress.status
    val finished = status == GameStatus.FINISHED
    val paused = status == GameStatus.PAUSED

    fun pressDigit(digit: Int) {
        val idx = state.selection.index ?: return
        if (idx in state.board.givens) return
        val event = if (state.selection.inputMode == InputMode.NOTE) {
            GameEvent.ToggleNote(idx, digit)
        } else {
            GameEvent.InputNumber(idx, digit)
        }
        dispatch(event)
    }

    Scaffold { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GameTopBar(
                    title = describeTitle(state),
                    timerSeconds = state.progress.timerSeconds,
                    mistakes = state.progress.mistakes,
                    maxMistakes = state.config.maxMistakes,
                    onExit = onExit,
                    modifier = Modifier.fillMaxWidth(),
                )

                when {
                    state.progress.isBusy -> BusyContent()
                    state.board.size == 0 -> IdleContent()
                    else -> {
                        GameBoard(
                            board = state.board,
                            selectedIndex = state.selection.index,
                            checkFlags = state.selection.conflicts,
                            onCellClick = { dispatch(GameEvent.SelectCell(it)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        GameToolBar(
                            noteMode = state.selection.inputMode == InputMode.NOTE,
                            isPaused = status == GameStatus.PAUSED,
                            canAct = state.selection.index?.let { it in state.board.givens }?.not() ?: true,
                            onNoteMode = { dispatch(GameEvent.ToggleNoteMode) },
                            onErase = { dispatch(GameEvent.Erase) },
                            onHint = { dispatch(GameEvent.RequestHint) },
                            onCheck = { dispatch(GameEvent.RequestCheck) },
                            onSolve = { dispatch(GameEvent.RequestSolve) },
                            onPause = { dispatch(GameEvent.TogglePause) },
                        )
                        NumberPad(
                            size = state.board.size,
                            selectedGiven = state.selection.index?.let { it in state.board.givens } ?: true,
                            onDigit = ::pressDigit,
                        )
                    }
                }
            }

            if (paused && state.board.size > 0) {
                PauseScrim(
                    onResume = { dispatch(GameEvent.TogglePause) },
                    onExit = onExit,
                )
            }
            if (finished && state.board.size > 0) {
                val victory = state.progress.mistakes < state.config.maxMistakes
                FinishPanel(
                    victory = victory,
                    mistakes = state.progress.mistakes,
                    timerSeconds = state.progress.timerSeconds,
                    onRetry = { dispatch(GameEvent.Restart) },
                    onExit = onExit,
                )
            }
        }
    }
}

@Composable
private fun IdleContent() {
    Column(modifier = Modifier.padding(top = 130.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.game_idle_welcome), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BusyContent() {
    Column(modifier = Modifier.padding(top = 130.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.game_busy_generating), style = MaterialTheme.typography.bodyMedium)
    }
}

// =====================================================================
// Preview（供 Android Studio 直接查看各游戏状态下的 UI 设计）
// =====================================================================

/** 供各 Preview 套上 core:ui 的统一主题，预览即为真实品牌配色（非 Material 默认）。 */
@Composable
private fun gamePreview(
    state: GameUiState,
    dispatch: (GameEvent) -> Unit = {},
    onExit: () -> Unit = {},
) {
    MindgameTheme() {
        GameScreen(state = state, dispatch = dispatch, onExit = onExit)
    }
}

/** 一个 4×4 的合法终盘，供构造各状态的示例盘面。 */
private val PREVIEW_SOLUTION_4 = listOf(
    1, 2, 3, 4,
    3, 4, 1, 2,
    2, 1, 4, 3,
    4, 3, 2, 1,
)

/** 若干"给定"格子（值与终盘一致、粗体不可改）。 */
private val PREVIEW_GIVENS_4 = setOf(0, 2, 5, 6, 9, 13, 14, 15)

private fun previewBoard4(holes: Set<Int>, notes: Map<Int, Set<Int>> = emptyMap()) = BoardCells(
    size = 4,
    values = PREVIEW_SOLUTION_4.mapIndexed { i, v -> if (i in holes) 0 else v },
    givens = PREVIEW_GIVENS_4,
    solution = PREVIEW_SOLUTION_4,
    notes = notes,
)

private fun previewConfig4() = GameConfig(
    mode = GameMode.FREE,
    spec = BoardSpec.MINI,
    difficulty = Difficulty.EASY,
    maxMistakes = 3,
)

/** 正常对局中：一空格被选中 + 另两空格带铅笔笔记 + 一个错误。 */
private fun runningMiniState() = GameUiState(
    config = previewConfig4(),
    board = previewBoard4(
        holes = setOf(7, 11),
        notes = mapOf(11 to setOf(2, 4), 12 to setOf(1, 3)),
    ),
    selection = Selection(index = 7, inputMode = InputMode.NUMBER),
    progress = GameProgress(status = GameStatus.RUNNING, mistakes = 1, timerSeconds = 42),
)

/** 铅笔模式：空格选中，数字键将写入该格的笔记。 */
private fun noteMiniState() = GameUiState(
    config = previewConfig4(),
    board = previewBoard4(holes = setOf(7, 11, 12)),
    selection = Selection(index = 7, inputMode = InputMode.NOTE),
    progress = GameProgress(status = GameStatus.RUNNING, mistakes = 0, timerSeconds = 7),
)

private fun pausedMiniState() = runningMiniState().copy(
    progress = GameProgress(status = GameStatus.PAUSED, mistakes = 1, timerSeconds = 90),
)

private fun victoryMiniState() = runningMiniState().copy(
    progress = GameProgress(status = GameStatus.FINISHED, mistakes = 0, timerSeconds = 135),
)

private fun failedMiniState() = runningMiniState().copy(
    progress = GameProgress(status = GameStatus.FINISHED, mistakes = 3, timerSeconds = 88),
)

@Preview(showBackground = true, name = "Game · Running (4x4)", widthDp = 420)
@Composable
private fun GamePreviewRunning() {
    gamePreview(runningMiniState())
}

@Preview(showBackground = true, name = "Game · Note mode (4x4)", widthDp = 420)
@Composable
private fun GamePreviewNote() {
    gamePreview(noteMiniState())
}

@Preview(showBackground = true, name = "Game · Paused (4x4)", widthDp = 420)
@Composable
private fun GamePreviewPaused() {
    gamePreview(pausedMiniState())
}

@Preview(showBackground = true, name = "Game · Victory (4x4)", widthDp = 420)
@Composable
private fun GamePreviewVictory() {
    gamePreview(victoryMiniState())
}

@Preview(showBackground = true, name = "Game · Failed (4x4)", widthDp = 420)
@Composable
private fun GamePreviewFailed() {
    gamePreview(failedMiniState())
}

// ---------------------------------------------------------------------
// 6×6 / 9×9 Preview 数据（合法终盘，随屏只作静态展示）
// ---------------------------------------------------------------------

/** 6×6 合法终盘（宫 = 2 行 × 3 列）。 */
private val PREVIEW_SOLUTION_6 = listOf(
    1, 2, 3, 4, 5, 6,
    4, 5, 6, 1, 2, 3,
    2, 3, 1, 5, 6, 4,
    5, 6, 4, 2, 3, 1,
    3, 1, 2, 6, 4, 5,
    6, 4, 5, 3, 1, 2,
)

private val PREVIEW_GIVENS_6 = setOf(0, 3, 8, 10, 13, 16, 22, 24, 29, 32)

/** 9×9 合法终盘（标准 3×3 宫）。 */
private val PREVIEW_SOLUTION_9 = listOf(
    5, 3, 4, 6, 7, 8, 9, 1, 2,
    6, 7, 2, 1, 9, 5, 3, 4, 8,
    1, 9, 8, 3, 4, 2, 5, 6, 7,
    8, 5, 9, 7, 6, 1, 4, 2, 3,
    4, 2, 6, 8, 5, 3, 7, 9, 1,
    7, 1, 3, 9, 2, 4, 8, 5, 6,
    9, 6, 1, 5, 3, 7, 2, 8, 4,
    2, 8, 7, 4, 1, 9, 6, 3, 5,
    3, 4, 5, 2, 8, 6, 1, 7, 9,
)

private val PREVIEW_GIVENS_9 = setOf(
    0, 3, 6, 9, 11, 13, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 39, 41, 44, 47, 49, 52, 54, 56, 58, 62, 65, 68, 70, 72, 75, 76, 78, 80,
)

/** 满盘的 running 状态（无冲突）：用于直观查看 6×6 / 9×9 的整盘样式与分区线。 */
private fun runningFullState(
    size: Int,
    solution: List<Int>,
    givens: Set<Int>,
    spec: BoardSpec,
    selIndex: Int,
) = GameUiState(
    config = GameConfig(mode = GameMode.FREE, spec = spec, difficulty = Difficulty.EASY, maxMistakes = 3),
    board = BoardCells(size = size, values = solution, givens = givens, solution = solution, notes = emptyMap()),
    selection = Selection(index = selIndex, inputMode = InputMode.NUMBER),
    progress = GameProgress(status = GameStatus.RUNNING, mistakes = 1, timerSeconds = 60),
)

@Preview(showBackground = true, name = "Game · Running (6x6)", widthDp = 420)
@Composable
private fun GamePreviewRunning6() {
    gamePreview(runningFullState(6, PREVIEW_SOLUTION_6, PREVIEW_GIVENS_6, BoardSpec.MEDIUM, selIndex = 13))
}

@Preview(showBackground = true, name = "Game · Running (9x9)", widthDp = 460)
@Composable
private fun GamePreviewRunning9() {
    gamePreview(runningFullState(9, PREVIEW_SOLUTION_9, PREVIEW_GIVENS_9, BoardSpec.CLASSIC, selIndex = 41))
}
