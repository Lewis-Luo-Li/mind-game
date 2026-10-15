import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("mindgame.kotlin.library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
}
