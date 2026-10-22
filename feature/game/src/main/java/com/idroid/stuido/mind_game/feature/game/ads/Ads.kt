package com.idroid.stuido.mind_game.feature.game.ads

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.idroid.stuido.mind_game.core.logic.config.InterstitialRule

/**
 * 广告抽象层：游戏 UI/状态只面向这些接口，不 import 任何 AdMob 类型。
 * AdMob 实现位于 :app；无广告构建/预览/单测使用 [FakeRewardedAdController]
 * 与 [InterstitialController.NOOP]。
 */

/** 激励广告授予的奖励种类。 */
enum class RewardKind {
    EXTRA_HINT,
    EXTRA_LIFE,
}

/**
 * 激励广告控制器。实现方负责预载与重载；[show] 在广告未就绪/展示失败/
 * 未获得奖励即被关闭时回调 [onUnavailable]，获得奖励时回调 [onReward]。
 */
interface RewardedAdController {
    fun isAvailable(kind: RewardKind): Boolean

    fun show(
        context: Context,
        kind: RewardKind,
        onReward: () -> Unit,
        onUnavailable: () -> Unit,
    )
}

/**
 * 插页广告控制器。[maybeShow] 仅在已预载完成时返回 true 并开始展示；
 * [onShown] 在 SDK 确认画面真正弹出后才回调（频控应以此为准，展示失败不计入）。
 * 实现方在每次展示后自行预载下一条。
 */
interface InterstitialController {
    fun preload()

    fun maybeShow(context: Context, onShown: () -> Unit = {}): Boolean

    companion object {
        /** 广告关闭时的空实现：永不展示。 */
        val NOOP: InterstitialController = object : InterstitialController {
            override fun preload() = Unit
            override fun maybeShow(context: Context, onShown: () -> Unit): Boolean = false
        }
    }
}

/**
 * 底部锚定横幅的组合插槽。[reloadKey] 变化时实现方应重新加载广告
 * （本工程以 "sessionKey-开局序号" 传入，保证每次开局/重开都刷新横幅）。
 */
fun interface BannerAdSlot {
    @Composable
    operator fun invoke(modifier: Modifier, reloadKey: Any?)
}

/**
 * 立即发奖的假实现：设计约定 ads.enabled=false 时奖励按钮直接领奖
 * （占位按钮），也用于预览与单元测试。
 */
class FakeRewardedAdController(
    private val available: Boolean = true,
) : RewardedAdController {
    var showCount = 0
        private set

    override fun isAvailable(kind: RewardKind): Boolean = available

    override fun show(
        context: Context,
        kind: RewardKind,
        onReward: () -> Unit,
        onUnavailable: () -> Unit,
    ) {
        showCount++
        if (available) onReward() else onUnavailable()
    }
}

/**
 * 插页广告频控（纯 Kotlin，进程内内存态，不持久化）：
 * 统计"自上次展示以来完成的局数"与上次展示时间，由 [shouldShow] 判定。
 */
class InterstitialFrequencyGate(
    private val clockMs: () -> Long = { System.currentTimeMillis() },
) {
    var gamesCompletedSinceShow: Int = 0
        private set

    var lastShownAtMs: Long? = null
        private set

    /** 每完成一局调用一次（任何 outcome 都算一局；调用方按 gameInstance 去重）。 */
    fun recordGameFinished() {
        gamesCompletedSinceShow++
    }

    /** 距上次展示的局数与时间是否都满足 [rule] 的下限。 */
    fun shouldShow(rule: InterstitialRule, adsEnabled: Boolean): Boolean {
        if (!adsEnabled || !rule.enabled) return false
        if (gamesCompletedSinceShow < rule.minGamesBetween) return false
        val lastShown = lastShownAtMs ?: return true
        return clockMs() - lastShown >= rule.minSecondsBetween * 1_000L
    }

    /** 真正展示成功后调用：重置局数计数并记录展示时间。 */
    fun recordShown() {
        gamesCompletedSinceShow = 0
        lastShownAtMs = clockMs()
    }
}
