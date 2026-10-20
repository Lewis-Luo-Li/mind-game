package com.idroid.stuido.mind_game.feature.game.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.core.ui.components.button.IconButtonConfig
import com.idroid.stuido.mind_game.core.ui.components.button.IconButtonStyle
import com.idroid.stuido.mind_game.core.ui.components.button.IconButtonUiContent
import com.idroid.stuido.mind_game.core.ui.components.keypad.NumberKeypad
import com.idroid.stuido.mind_game.core.ui.components.keypad.NumberKeypadConfig
import com.idroid.stuido.mind_game.core.ui.components.keypad.NumberKeypadStyle
import com.idroid.stuido.mind_game.core.ui.components.keypad.NumberKeypadUiContent
import com.idroid.stuido.mind_game.core.ui.components.status.StatusChipStyle
import com.idroid.stuido.mind_game.core.ui.components.status.StatusChipUiContent
import com.idroid.stuido.mind_game.core.ui.components.text.TextConfig
import com.idroid.stuido.mind_game.core.ui.components.status.StatusInfoRow
import com.idroid.stuido.mind_game.core.ui.components.status.StatusInfoRowConfig
import com.idroid.stuido.mind_game.core.ui.components.status.StatusInfoRowStyle
import com.idroid.stuido.mind_game.core.ui.components.status.StatusInfoRowUiContent
import com.idroid.stuido.mind_game.core.ui.components.topbar.TopBar
import com.idroid.stuido.mind_game.core.ui.components.topbar.TopBarUiConfig
import com.idroid.stuido.mind_game.core.ui.components.topbar.TopBarUiContent
import com.idroid.stuido.mind_game.core.ui.components.topbar.TopBarUiStyle
import com.idroid.stuido.mind_game.core.ui.theme.MindgameTheme
import com.idroid.stuido.mind_game.core.ui.theme.Theme
import com.idroid.stuido.mind_game.core.ui.util.VisibilityConfig
import com.idroid.stuido.mind_game.feature.game.GameMode
import com.idroid.stuido.mind_game.feature.game.GameOutcome
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
    onOpenSettings: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    showDebugSolve: Boolean = false,
) {
    val status = state.progress.status
    val finished = status == GameStatus.FINISHED
    val paused = status == GameStatus.PAUSED
    var showSolveDialog by remember { mutableStateOf(false) }

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

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                // Scaffold 已发 top inset；消耗掉，TopBar 的 statusBarsPadding 不重复加。
                .consumeWindowInsets(inner),
        ) {
            val colorScheme = MaterialTheme.colorScheme
            // 顶部导航按钮与底部工具栏一致的圆形风格（surfaceVariant 圆底）。
            val navIconStyle = IconButtonStyle(
                shape = CircleShape,
                surfaceColor = colorScheme.surfaceVariant,
                iconTintColor = colorScheme.onSurfaceVariant,
                surfaceSize = 48.dp,
                iconSize = 24.dp,
            )
            val keypadStyle = NumberKeypadStyle(
                zoneColor = colorScheme.secondaryContainer,
                surfaceColor = colorScheme.surface,
                disabledSurfaceColor = colorScheme.onSurface.copy(alpha = 0.08f),
                digitColor = colorScheme.onSurface,
                disabledContentColor = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                badgeBackgroundColor = colorScheme.primary,
                badgeContentColor = colorScheme.onPrimary,
                keySize = 36.dp,
                digitStyle = MaterialTheme.typography.titleMedium,
                badgeStyle = MaterialTheme.typography.labelSmall,
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // mockup 顶部两分区（固定，不随棋盘滚动）：56dp 导航条 + 44dp 状态行。
                TopBar(
                    uiContent = TopBarUiContent(
                        onBackClick = onExit,
                        onSettingsClick = onOpenSettings,
                        topBarTitle = describeTitle(state),
                        backButtonUiContent = IconButtonUiContent(
                            icon = Theme.icons.back,
                            contentDescription = stringResource(R.string.cd_nav_back),
                            onClick = onExit,
                        ),
                        settingsButtonUiContent = IconButtonUiContent(
                            icon = Theme.icons.settings,
                            contentDescription = stringResource(R.string.cd_settings),
                            // 去设置页前先暂停，返回后保持暂停态由用户恢复。
                            onClick = {
                                if (state.progress.status == GameStatus.RUNNING) {
                                    dispatch(GameEvent.TogglePause)
                                }
                                onOpenSettings?.invoke()
                            },
                        ),
                    ),
                    uiConfig = TopBarUiConfig(
                        // 生成中/未开局时隐藏齿轮：此刻无法暂停，避免开出来的对局在设置页后继续计时。
                        settingsButtonConfig = IconButtonConfig(
                            visibility = if (
                                onOpenSettings != null && !state.progress.isBusy && state.board.size > 0
                            ) {
                                VisibilityConfig.VISIBLE
                            } else {
                                VisibilityConfig.HIDDEN
                            },
                        ),
                        titleConfig = TextConfig(maxLine = 1, overflow = TextOverflow.Ellipsis),
                    ),
                    uiStyle = TopBarUiStyle(
                        topbarBgColor = colorScheme.surface,
                        titleTextColor = colorScheme.onSurface,
                        backButtonStyle = navIconStyle,
                        settingsButtonStyle = navIconStyle,
                    ),
                )
                StatusInfoRow(
                    uiContent = StatusInfoRowUiContent(
                        items = listOf(
                            // D4：点击计时器切换暂停/继续。
                            StatusChipUiContent(
                                icon = Theme.icons.timer,
                                contentDescription = stringResource(R.string.game_status_timer),
                                text = formatTimer(state.progress.timerSeconds),
                                onClick = { dispatch(GameEvent.TogglePause) },
                            ),
                            // 图标纯装饰（文本自描述），contentDescription 传 null 避免读屏重复播报。
                            StatusChipUiContent(
                                icon = Theme.icons.error,
                                contentDescription = null,
                                text = stringResource(
                                    R.string.game_status_errors,
                                    state.progress.mistakes,
                                    state.config.maxMistakes,
                                ),
                            ),
                            StatusChipUiContent(
                                icon = Theme.icons.hint,
                                contentDescription = null,
                                text = stringResource(
                                    R.string.game_status_hints,
                                    state.hintsLeft,
                                    state.config.maxHints,
                                ),
                            ),
                        ),
                    ),
                    uiConfig = StatusInfoRowConfig(enabled = !finished),
                    uiStyle = StatusInfoRowStyle(
                        zoneColor = colorScheme.surface,
                        chipStyle = StatusChipStyle(
                            iconTintColor = colorScheme.onSurfaceVariant,
                            textColor = colorScheme.onSurface,
                            disabledContentColor = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            iconSize = 18.dp,
                            textStyle = MaterialTheme.typography.labelLarge,
                        ),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
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
                        }
                    }
                }

                // 底部固定区（mockup 的 thumb-easy zone）：工具行 + 数字键盘，不随棋盘滚动。
                if (!state.progress.isBusy && state.board.size > 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                    ) {
                        Spacer(Modifier.height(8.dp))
                        GameToolBar(
                            canUndo = state.canUndo && state.progress.isRunning,
                            noteMode = state.selection.inputMode == InputMode.NOTE,
                            canErase = state.selection.index?.let { it !in state.board.givens } ?: false,
                            hintsLeft = state.hintsLeft,
                            showDebugSolve = showDebugSolve,
                            onUndo = { dispatch(GameEvent.Undo) },
                            onNoteMode = { dispatch(GameEvent.ToggleNoteMode) },
                            onErase = { dispatch(GameEvent.Erase) },
                            onHint = { dispatch(GameEvent.RequestHint) },
                            onSolve = { showSolveDialog = true },
                        )
                        // 笔记模式下，选中格已有的候选数字即使剩余 0 也保持可点，便于再次点按移除。
                        val noteDigitsAtSelection = if (
                            state.selection.inputMode == InputMode.NOTE &&
                            state.selection.index != null
                        ) {
                            state.board.notes[state.selection.index].orEmpty()
                        } else {
                            emptySet()
                        }
                        NumberKeypad(
                            uiContent = NumberKeypadUiContent(
                                digits = state.board.size,
                                remaining = state.board.remainingCounts(),
                                zeroCountEnabled = noteDigitsAtSelection,
                                onDigit = ::pressDigit,
                            ),
                            uiConfig = NumberKeypadConfig(
                                enabled = state.selection.index?.let { it !in state.board.givens } ?: false,
                            ),
                            uiStyle = keypadStyle,
                            modifier = Modifier.fillMaxWidth(),
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
                FinishPanel(
                    outcome = state.progress.outcome ?: GameOutcome.SOLVED,
                    timerSeconds = state.progress.timerSeconds,
                    mistakes = state.progress.mistakes,
                    maxMistakes = state.config.maxMistakes,
                    hintsUsed = state.progress.hintsUsed,
                    maxHints = state.config.maxHints,
                    onRetrySamePuzzle = { dispatch(GameEvent.RetrySamePuzzle) },
                    onNewGame = { dispatch(GameEvent.Restart) },
                    onExit = onExit,
                )
            }
        }
    }
    if (showSolveDialog) {
        AlertDialog(
            onDismissRequest = { showSolveDialog = false },
            title = { Text(stringResource(R.string.solve_dialog_title)) },
            text = { Text(stringResource(R.string.solve_dialog_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSolveDialog = false
                        dispatch(GameEvent.RequestSolve)
                    },
                ) {
                    Text(stringResource(R.string.action_reveal))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSolveDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
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
        GameScreen(state = state, dispatch = dispatch, onExit = onExit, onOpenSettings = {})
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
    progress = GameProgress(
        status = GameStatus.FINISHED,
        mistakes = 0,
        timerSeconds = 135,
        outcome = GameOutcome.SOLVED,
    ),
)

private fun failedMiniState() = runningMiniState().copy(
    progress = GameProgress(
        status = GameStatus.FINISHED,
        mistakes = 3,
        timerSeconds = 88,
        outcome = GameOutcome.FAILED,
    ),
)

private fun revealedMiniState() = runningMiniState().copy(
    progress = GameProgress(
        status = GameStatus.FINISHED,
        mistakes = 1,
        timerSeconds = 78,
        outcome = GameOutcome.REVEALED,
    ),
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

@Preview(showBackground = true, name = "Game · Revealed (4x4)", widthDp = 420)
@Composable
private fun GamePreviewRevealed() {
    gamePreview(revealedMiniState())
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
