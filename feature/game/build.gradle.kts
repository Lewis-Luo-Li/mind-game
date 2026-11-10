import com.android.build.api.dsl.LibraryExtension

plugins {
    id("mindgame.android.library")
    id("mindgame.android.library.compose")
    // 存档（SavedGame）序列化为 JSON，供宿主落盘与跨进程恢复。
    alias(libs.plugins.kotlin.serialization)
}

extensions.configure<LibraryExtension> {
    namespace = Config.Feature.Game.PACKAGE_NAME
}

dependencies {
    implementation(project(":core:logic"))
    implementation(project(":core:ui"))

    // Compose
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Lifecycle / ViewModel / Coroutines
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.kotlin)

}
