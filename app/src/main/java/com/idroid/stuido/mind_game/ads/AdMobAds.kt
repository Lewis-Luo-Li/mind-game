package com.idroid.stuido.mind_game.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.idroid.stuido.mind_game.BuildConfig
import com.idroid.stuido.mind_game.feature.game.ads.BannerAdSlot
import com.idroid.stuido.mind_game.feature.game.ads.InterstitialController
import com.idroid.stuido.mind_game.feature.game.ads.RewardKind
import com.idroid.stuido.mind_game.feature.game.ads.RewardedAdController

private const val TAG = "AdMob"

/** 沿 ContextThemeWrapper 链取宿主 Activity——全屏广告 show() 需要 Activity。 */
internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * AdMob 横幅（50dp 锚定槽内渲染）。[reloadKey] 变化触发新一轮 loadAd；
 * 随宿主 Activity 生命周期 pause/resume，离开组合即 destroy。
 */
@Composable
fun AdMobBanner(modifier: Modifier, reloadKey: Any?) {
    var adView by remember { mutableStateOf<AdView?>(null) }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = BuildConfig.ADMOB_BANNER_UNIT_ID
                adListener = object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.w(TAG, "Banner failed to load: ${error.message}")
                    }
                }
            }.also { adView = it }
        },
        // 销毁交给 AndroidView 生命周期：离开组合时统一 onRelease，
        // 不在 DisposableEffect 的 onDispose 里读共享 adView 状态。
        onRelease = { it.destroy() },
    )
    LaunchedEffect(adView, reloadKey) {
        adView?.loadAd(AdRequest.Builder().build())
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(adView, lifecycleOwner) {
        val view = adView
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> view?.pause()
                Lifecycle.Event.ON_RESUME -> view?.resume()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

/** 广告开启时传给 GameScreenRoute 的横幅插槽工厂。 */
fun adMobBannerSlot(): BannerAdSlot = BannerAdSlot { modifier, reloadKey ->
    AdMobBanner(modifier = modifier, reloadKey = reloadKey)
}

/**
 * 激励广告控制器：按 [RewardKind] 各持一条预载，创建即开始加载，
 * 每次 show 后重载。未就绪/无 Activity → 立即 onUnavailable 并补载。
 */
class AdMobRewardedAdController(context: Context) : RewardedAdController {
    private val appContext = context.applicationContext
    private val loaded = mutableMapOf<RewardKind, RewardedAd>()
    private val loading = mutableSetOf<RewardKind>()

    init {
        RewardKind.entries.forEach { load(it) }
    }

    // 两种奖励共用同一 ad unit；独立槽位保证连发（先提示后复活）各自有货。
    private fun unitId(kind: RewardKind): String = when (kind) {
        RewardKind.EXTRA_HINT, RewardKind.EXTRA_LIFE -> BuildConfig.ADMOB_REWARDED_UNIT_ID
    }

    private fun load(kind: RewardKind) {
        if (kind in loading) return
        loading += kind
        RewardedAd.load(
            appContext,
            unitId(kind),
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    loading -= kind
                    loaded[kind] = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading -= kind
                    loaded.remove(kind)
                    Log.w(TAG, "Rewarded($kind) failed to load: ${error.message}")
                }
            },
        )
    }

    override fun isAvailable(kind: RewardKind): Boolean = loaded[kind] != null

    override fun show(
        context: Context,
        kind: RewardKind,
        onReward: () -> Unit,
        onUnavailable: () -> Unit,
    ) {
        val ad = loaded.remove(kind)
        val activity = context.findActivity()
        if (ad == null || activity == null) {
            load(kind)
            onUnavailable()
            return
        }
        var rewarded = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                load(kind)
                if (!rewarded) onUnavailable()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                load(kind)
                onUnavailable()
            }
        }
        ad.show(activity) {
            rewarded = true
            onReward()
        }
    }
}

/**
 * 插页广告控制器：创建即预载，每次展示后自动预载下一条。
 * 未预载完成时 [maybeShow] 返回 false 并补载（静默跳过，不打断流程）。
 */
class AdMobInterstitialController(context: Context) : InterstitialController {
    private val appContext = context.applicationContext
    private var ad: InterstitialAd? = null
    private var loading = false

    init {
        preload()
    }

    override fun preload() {
        if (ad != null || loading) return
        loading = true
        InterstitialAd.load(
            appContext,
            BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    loading = false
                    ad = interstitialAd
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                    ad = null
                    Log.w(TAG, "Interstitial failed to load: ${error.message}")
                }
            },
        )
    }

    override fun maybeShow(context: Context, onShown: () -> Unit): Boolean {
        val activity = context.findActivity() ?: return false
        val current = ad ?: run {
            preload()
            return false
        }
        ad = null
        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            // 只有 SDK 确认画面真正弹出才算一次展示（频控计时以此为原点）。
            override fun onAdShowedFullScreenContent() = onShown()

            override fun onAdDismissedFullScreenContent() = preload()

            override fun onAdFailedToShowFullScreenContent(error: AdError) = preload()
        }
        current.show(activity)
        return true
    }
}
