import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension
) {
    // 1. Apply the Kotlin 2.0 Compose Compiler plugin
    pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

    // 2. Enable Compose build features
    commonExtension.buildFeatures.compose = true

    // 3. Add default Compose dependencies to any module using this
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

    dependencies {
        val bom = libs.findLibrary("androidx-compose-bom").get()
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))

        // Add standard tooling you want in every Compose module
        add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
        add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
    }

}