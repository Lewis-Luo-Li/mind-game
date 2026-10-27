import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.VariantDimension
import java.util.Properties

plugins {
    id("mindgame.android.application")
    id("mindgame.android.application.compose")
}

// Debug builds always use Google's official test ad IDs — live ads must never
// be tapped on dev devices (invalid traffic). Release builds read the real IDs
// from a root secret.properties (gitignored), falling back to test IDs.
val adMobTestAppId = "ca-app-pub-3940256099942544~3347511713"
val adMobTestBannerUnitId = "ca-app-pub-3940256099942544/6300978111"
val adMobTestInterstitialUnitId = "ca-app-pub-3940256099942544/1033173712"
val adMobTestRewardedUnitId = "ca-app-pub-3940256099942544/5224354917"

val adMobProps = Properties().apply {
    val file = rootProject.file("secret.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun adMobId(key: String, testId: String): String = adMobProps.getProperty(key) ?: testId

fun VariantDimension.adMobFields(appId: String, banner: String, interstitial: String, rewarded: String) {
    buildConfigField("String", "ADMOB_APP_ID", "\"$appId\"")
    buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"$banner\"")
    buildConfigField("String", "ADMOB_INTERSTITIAL_UNIT_ID", "\"$interstitial\"")
    buildConfigField("String", "ADMOB_REWARDED_UNIT_ID", "\"$rewarded\"")
    manifestPlaceholders["admobAppId"] = appId
}

extensions.configure<ApplicationExtension> {
    namespace = Config.APPLICATION_ID
    defaultConfig {
        applicationId = Config.APPLICATION_ID
        versionCode = Config.VERSION_CODE
        versionName = Config.VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        adMobFields(adMobTestAppId, adMobTestBannerUnitId, adMobTestInterstitialUnitId, adMobTestRewardedUnitId)
    }
    buildTypes {
        getByName("release") {
            // CI 用 -PrequireAdMobSecrets 强制校验：secret.properties 缺键时直接失败，
            // 防止 release APK 静默回退到 Google 测试广告 ID。
            val requireSecrets = providers.gradleProperty("requireAdMobSecrets")
                .map { it.toBoolean() }
                .getOrElse(false)
            val adMobKeys = listOf(
                "ADMOB_APP_ID",
                "ADMOB_BANNER_UNIT_ID",
                "ADMOB_INTERSTITIAL_UNIT_ID",
                "ADMOB_REWARDED_UNIT_ID",
            )
            val missingKeys = adMobKeys.filter { adMobProps.getProperty(it).isNullOrBlank() }
            if (requireSecrets && missingKeys.isNotEmpty()) {
                throw GradleException(
                    "secret.properties missing AdMob keys: ${missingKeys.joinToString()} — refusing to fall back to test IDs",
                )
            }
            if (adMobProps.getProperty("ADMOB_APP_ID") == null) {
                logger.warn("secret.properties missing ADMOB_* keys — release will serve TEST ads")
            }
            adMobFields(
                adMobId("ADMOB_APP_ID", adMobTestAppId),
                adMobId("ADMOB_BANNER_UNIT_ID", adMobTestBannerUnitId),
                adMobId("ADMOB_INTERSTITIAL_UNIT_ID", adMobTestInterstitialUnitId),
                adMobId("ADMOB_REWARDED_UNIT_ID", adMobTestRewardedUnitId),
            )
        }
    }
}

dependencies {
    implementation(project(":feature:game"))
    // feature:game 的公开 API（GameScreenRoute 等）参数使用了 core:logic 的类型
    // （BoardSpec / Difficulty），因此 app 需直接依赖之以便构造开局参数。
    implementation(project(":core:logic"))
    // 全局主题（Color/Typography/MindgameTheme）统一托管在 core:ui，app 顶层直接使用。
    implementation(project(":core:ui"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    // compose dependencies, don't need to declare the BOM here because it is in the AndroidCompose Plugin
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // AdMob SDK lives only in the app layer; :feature:game talks to interfaces only.
    implementation(libs.play.services.ads)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}