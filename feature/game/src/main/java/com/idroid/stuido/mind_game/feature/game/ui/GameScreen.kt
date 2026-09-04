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
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.feature.game.GameMode
import com.idroid.stuido.mind_game.feature.game.GameStatus
import com.idroid.stuido.mind_game.feature.game.mvi.GameEvent
import com.idroid.stuido.mind_game.feature.game.mvi.GameUiState
import com.idroid.stuido.mind_game.feature.game.mvi.InputMode
import com.idroid.stuido.mind_game.feature.game.ui.components.FinishPanel
import com.idroid.stuido.mind_game.feature.game.ui.components.PauseScrim

private fun describeTitle(state: GameUiState): String {
    if (state.board.size == 0) return "未开始"
    val specName = when (state.config.spec.size) {
        4 -> "4×4"
        6 -> "6×6"
        else -> "9×9"
    }
    val mode = if (state.config.mode == GameMode.DAILY) "· Daily" else ""
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
        Text("欢迎来到数独。在上层选择规格/难度开始。", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BusyContent() {
    Column(modifier = Modifier.padding(top = 130.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text("正在生成谜题…", style = MaterialTheme.typography.bodyMedium)
    }
}
