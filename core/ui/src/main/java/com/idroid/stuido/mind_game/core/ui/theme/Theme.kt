package com.idroid.stuido.mind_game.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

/**
 * App 顶层主题入口。
 *
 * - [AppThemeMode] 决定亮度：跟随系统 / 恒亮 / 恒暗；
 * - [AppThemeScheme] 决定选哪一套强调色板（默认紫调 + 三套自定义）；
 * - 默认值沿用旧行为：[null] 模式 -> 跟随系统亮度 + [AppThemeScheme.LILAC]，
 *   因此不传参调用仍能得到过去「跟随系统 + 紫调」的效果，所有 Preview 与既有
 *   调用点无需改动。
 *
 * @param scheme      所选的强调色板（默认 [AppThemeScheme.LILAC]）。
 * @param mode        亮度模式，null 表示跟随系统亮度。
 * @param content     主题作用域内内容。
 */
@Composable
fun MindgameTheme(
    scheme: AppThemeScheme = AppThemeScheme.LILAC,
    mode: AppThemeMode? = null,
    content: @Composable () -> Unit,
) {
    val dark: Boolean = when (mode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM, null -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = schemeFor(scheme, dark),
        typography = Typography,
        content = content,
    )
}
