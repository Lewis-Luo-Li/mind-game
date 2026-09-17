import com.android.build.api.dsl.LibraryExtension

plugins {
    id("mindgame.android.library")
    id("mindgame.android.library.compose")
}

extensions.configure<LibraryExtension> {
    namespace = Config.Core.Ui.PACKAGE_NAME
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
}
