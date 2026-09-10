package com.idroid.stuido.mind_game.feature.game.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.feature.game.GameMode
import com.idroid.stuido.mind_game.feature.game.mvi.GameEvent
import com.idroid.stuido.mind_game.feature.game.mvi.GameViewModel
import com.idroid.stuido.mind_game.feature.game.engine.GameFactory

/**
 * App 壳（MainActivity/SudokuApp）引用的 VM 绑定路由：在 Compose 内创建/持有
 * [GameViewModel]，收集 State 并转发事件到纯展示的 [GameScreen]。
 *
 * @param sessionKey 由宿主每次开局传入的唯一值，用作该屏 ViewModel 的 key，
 *                   保证不同对局（不同 spec/difficulty/seed 或反复重开）永远各自
 *                   持有一个全新 ViewModel，互不复用旧盘面。
 */
@Composable
fun GameScreenRoute(
    onExit: () -> Unit,
    mode: GameMode = GameMode.FREE,
    spec: BoardSpec = BoardSpec.CLASSIC,
    difficulty: Difficulty = Difficulty.EASY,
    seed: Long? = null,
    sessionKey: String = "default-session",
) {
    val vm: GameViewModel = viewModel(key = sessionKey) {
        GameViewModel(gameFactory = GameFactory())
    }
    val state by vm.uiState.collectAsState()

    // 首次进入（本会话挂载）自动开局（生成 + 铺盘）；后续由 Restart / FinishPanel 驱动。
    LaunchedEffect(vm) {
        vm.dispatch(GameEvent.NewGame(mode, spec, difficulty, seed))
    }

    GameScreen(state = state, dispatch = vm::dispatch, onExit = onExit)
}
