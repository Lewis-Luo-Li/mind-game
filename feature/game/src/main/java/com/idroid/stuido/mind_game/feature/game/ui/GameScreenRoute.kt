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
 * 供 App 壳层（Phase 4 / MainActivity）引用的"VM 绑定路由"：
 * 在 Compose 内创建/持有 [GameViewModel]，收集 State 并转发事件到 [GameScreen]。
 *
 * 把真正的规格/难度选择交给上层阶段；此处暴露默认开一局的便捷调用。
 */
@Composable
fun GameScreenRoute(
    onExit: () -> Unit,
    mode: GameMode = GameMode.FREE,
    spec: BoardSpec = BoardSpec.CLASSIC,
    difficulty: Difficulty = Difficulty.EASY,
    seed: Long? = null,
) {
    val vm: GameViewModel = viewModel {
        GameViewModel(gameFactory = GameFactory())
    }
    val state by vm.uiState.collectAsState()

    // 首次进入自动开局（生成 + 铺盘）；后续由 Restart / FinishPanel 驱动.
    LaunchedEffect(vm) {
        vm.dispatch(GameEvent.NewGame(mode, spec, difficulty, seed))
    }

    GameScreen(state = state, dispatch = vm::dispatch, onExit = onExit)
}
