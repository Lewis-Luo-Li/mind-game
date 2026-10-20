import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties

plugins {
    id("mindgame.android.application")
    id("mindgame.android.application.compose")
}

// Real AdMob IDs can be supplied via a root secret.properties (gitignored);
// Google's official test IDs are the fallback so debug/CI/emulator builds are safe.
val adMobProps = Properties().apply {
    val file = rootProject.file("secret.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun adMobId(key: String, testId: String): String = adMobProps.getProperty(key) ?: testId

val adMobAppId = adMobId("ADMOB_APP_ID", "ca-app-pub-3940256099942544~3347511713")
val adMobBannerUnitId = adMobId("ADMOB_BANNER_UNIT_ID", "ca-app-pub-3940256099942544/6300978111")
val adMobInterstitialUnitId = adMobId("ADMOB_INTERSTITIAL_UNIT_ID", "ca-app-pub-3940256099942544/1033173712")
val adMobRewardedUnitId = adMobId("ADMOB_REWARDED_UNIT_ID", "ca-app-pub-3940256099942544/5224354917")

extensions.configure<ApplicationExtension> {
    namespace = Config.APPLICATION_ID
    defaultConfig {
        applicationId = Config.APPLICATION_ID
        versionCode = Config.VERSION_CODE
        versionName = Config.VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "ADMOB_APP_ID", "\"$adMobAppId\"")
        buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"$adMobBannerUnitId\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_UNIT_ID", "\"$adMobInterstitialUnitId\"")
        buildConfigField("String", "ADMOB_REWARDED_UNIT_ID", "\"$adMobRewardedUnitId\"")
        manifestPlaceholders["admobAppId"] = adMobAppId
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