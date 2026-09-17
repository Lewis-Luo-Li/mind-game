import com.android.build.api.dsl.ApplicationExtension
plugins {
    id("mindgame.android.application")
    id("mindgame.android.application.compose")
}

extensions.configure<ApplicationExtension> {
    namespace = Config.APPLICATION_ID
    defaultConfig {
        applicationId = Config.APPLICATION_ID
        versionCode = Config.VERSION_CODE
        versionName = Config.VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}