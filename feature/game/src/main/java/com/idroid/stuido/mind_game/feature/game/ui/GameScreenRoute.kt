package com.idroid.stuido.mind_game.feature.game.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.core.logic.config.DifficultyConfig
import com.idroid.stuido.mind_game.core.logic.config.RewardsSection
import com.idroid.stuido.mind_game.core.logic.generator.SudokuGenerator
import com.idroid.stuido.mind_game.feature.game.GameMode
import com.idroid.stuido.mind_game.feature.game.GameOutcome
import com.idroid.stuido.mind_game.feature.game.R
import com.idroid.stuido.mind_game.feature.game.ads.BannerAdSlot
import com.idroid.stuido.mind_game.feature.game.ads.FakeRewardedAdController
import com.idroid.stuido.mind_game.feature.game.ads.RewardKind
import com.idroid.stuido.mind_game.feature.game.ads.RewardedAdController
import com.idroid.stuido.mind_game.feature.game.mvi.GameEffect
import com.idroid.stuido.mind_game.feature.game.mvi.GameEvent
import com.idroid.stuido.mind_game.feature.game.mvi.GameViewModel
import com.idroid.stuido.mind_game.feature.game.engine.GameFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 终局面板出现到触发插页广告判定之间的小延迟，让玩家先看到结算结果。 */
private const val FINISH_CALLBACK_DELAY_MS = 1_200L

/**
 * App 壳（MainActivity/SudokuApp）引用的 VM 绑定路由：在 Compose 内创建/持有
 * [GameViewModel]，收集 State 并转发事件到纯展示的 [GameScreen]。
 *
 * @param sessionKey 由宿主每次开局传入的唯一值，用作该屏 ViewModel 的 key，
 *                   保证不同对局（不同 spec/difficulty/seed 或反复重开）永远各自
 *                   持有一个全新 ViewModel，互不复用旧盘面。
 * @param rewards    本局生效的奖励规则（extra_hint / extra_life），开局即固化。
 * @param rewardedAds 激励广告控制器；ads.enabled=false 时宿主传
 *                    [FakeRewardedAdController]（点按直接发奖）。
 * @param bannerAd   底部横幅插槽；null = 广告关闭，不预留高度。
 * @param onGameFinished 每次对局到达终局态（SOLVED/REVEALED/FAILED）回调给宿主，
 *                       用于插页广告频控与展示。[firstFinish] 标记同一局实例
 *                       首次到达终局（激励复活后再次终局时为 false）——频控局数
 *                       只在首次时计入，避免一局复活多次被算作多局。
 */
@Composable
fun GameScreenRoute(
    onExit: () -> Unit,
    onOpenSettings: (() -> Unit)? = null,
    mode: GameMode = GameMode.FREE,
    spec: BoardSpec = BoardSpec.CLASSIC,
    difficulty: Difficulty = Difficulty.EASY,
    seed: Long? = null,
    difficultyConfig: DifficultyConfig = DifficultyConfig.default(),
    maxMistakes: Int = 3,
    maxHints: Int = 3,
    rewards: RewardsSection = RewardsSection(),
    rewardedAds: RewardedAdController = FakeRewardedAdController(),
    bannerAd: BannerAdSlot? = null,
    onGameFinished: (outcome: GameOutcome, firstFinish: Boolean) -> Unit = { _, _ -> },
    showDebugSolve: Boolean = false,
    sessionKey: String = "default-session",
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val vm: GameViewModel = viewModel(key = sessionKey) {
        GameViewModel(
            gameFactory = GameFactory(
                generator = SudokuGenerator(maxRetry = difficultyConfig.maxRetry),
                difficultyConfig = difficultyConfig,
            ),
        )
    }
    val state by vm.uiState.collectAsState()

    // 同一局实例的终局只计一次局数：复活后再次到达终局不再重复计数。
    // 以 vm 为 key——新会话（新 ViewModel、gameInstance 归 1）重新计数。
    var countedGameInstance by remember(vm) { mutableStateOf(-1) }
    fun firstFinishOfInstance(instance: Int): Boolean {
        if (instance == countedGameInstance) return false
        countedGameInstance = instance
        return true
    }

    // 首次进入（本会话挂载）自动开局（生成 + 铺盘）；后续由 Restart / FinishPanel 驱动。
    LaunchedEffect(vm) {
        vm.dispatch(GameEvent.NewGame(mode, spec, difficulty, seed, maxMistakes, maxHints, rewards))
    }

    LaunchedEffect(vm) {
        vm.effect.collect { effect ->
            when (effect) {
                is GameEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                is GameEffect.NewGameFailed -> snackbarHostState.showSnackbar(effect.reason)
                is GameEffect.HintFilled -> snackbarHostState.showSnackbar(
                    context.getString(R.string.hint_filled, effect.value),
                )
                // 终局：先让玩家看到结算面板，再交给宿主做插页广告频控判定。
                is GameEffect.PuzzleCompleted -> {
                    delay(FINISH_CALLBACK_DELAY_MS)
                    onGameFinished(GameOutcome.SOLVED, firstFinishOfInstance(effect.gameInstance))
                }
                is GameEffect.SolutionRevealed -> {
                    delay(FINISH_CALLBACK_DELAY_MS)
                    onGameFinished(GameOutcome.REVEALED, firstFinishOfInstance(effect.gameInstance))
                }
                is GameEffect.GameOver -> onGameFinished(GameOutcome.FAILED, firstFinishOfInstance(effect.gameInstance))
                else -> Unit
            }
        }
    }

    /** 广告不可用/未完成时提示，按钮保持可再次点击。 */
    val adUnavailable: () -> Unit = {
        scope.launch {
            snackbarHostState.showSnackbar(context.getString(R.string.reward_ad_unavailable))
        }
    }

    GameScreen(
        state = state,
        dispatch = vm::dispatch,
        onExit = onExit,
        onOpenSettings = onOpenSettings,
        snackbarHostState = snackbarHostState,
        showDebugSolve = showDebugSolve,
        onHintAd = {
            rewardedAds.show(
                context = context,
                kind = RewardKind.EXTRA_HINT,
                onReward = {
                    // 先入账奖励配额，再立即消耗一次提示填格（设计文档 §4）。
                    vm.dispatch(GameEvent.GrantRewardedHints(state.config.rewards.extraHint.amount))
                    vm.dispatch(GameEvent.RequestHint)
                },
                onUnavailable = adUnavailable,
            )
        },
        onLifeAd = {
            rewardedAds.show(
                context = context,
                kind = RewardKind.EXTRA_LIFE,
                onReward = {
                    vm.dispatch(GameEvent.GrantRewardedLives(state.config.rewards.extraLife.amount))
                },
                onUnavailable = adUnavailable,
            )
        },
        bannerAd = bannerAd,
        // 每次开局（NewGame/Restart/RetrySamePuzzle）gameInstance 递增 → 横幅重载。
        bannerReloadKey = "$sessionKey-${state.progress.gameInstance}",
    )
}
