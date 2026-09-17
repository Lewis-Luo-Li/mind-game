import com.android.build.api.dsl.LibraryExtension

plugins {
    id("mindgame.android.library")
    id("mindgame.android.library.compose")
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

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

}
