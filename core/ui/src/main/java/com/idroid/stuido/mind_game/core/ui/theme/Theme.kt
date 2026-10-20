package com.idroid.stuido.mind_game.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import com.idroid.stuido.mind_game.core.ui.primitives.COMPACT_TYPO_WIDTH_DP
import com.idroid.stuido.mind_game.core.ui.primitives.LocalFontSize
import com.idroid.stuido.mind_game.core.ui.primitives.LocalMindGameFontFamily
import com.idroid.stuido.mind_game.core.ui.primitives.LocalTypography
import com.idroid.stuido.mind_game.core.ui.primitives.MindGameFontFamily
import com.idroid.stuido.mind_game.core.ui.primitives.MindGameFontSize
import com.idroid.stuido.mind_game.core.ui.primitives.MindGameTypography
import com.idroid.stuido.mind_game.core.ui.primitives.compactFontSize
import com.idroid.stuido.mind_game.core.ui.primitives.defaultFontSize
import com.idroid.stuido.mind_game.core.ui.primitives.defaultMindGameFontFamily
import com.idroid.stuido.mind_game.core.ui.primitives.defaultTypography
import com.idroid.stuido.mind_game.core.ui.primitives.iconset.IconSet
import com.idroid.stuido.mind_game.core.ui.primitives.iconset.LocalIconSet
import com.idroid.stuido.mind_game.core.ui.primitives.iconset.rememberDefaultIconSet
import com.idroid.stuido.mind_game.core.ui.primitives.schemeFor

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
    fontSize: MindGameFontSize = defaultFontSize,
    fontFamily: MindGameFontFamily = defaultMindGameFontFamily,
    typography: MindGameTypography = defaultTypography,
    iconSet: IconSet = rememberDefaultIconSet(),
    content: @Composable () -> Unit,
) {
    val dark: Boolean = when (mode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM, null -> isSystemInDarkTheme()
    }
    // 窄屏（<360dp）且调用方未自定义字号/排版时，自动切换紧凑字号档。
    val useCompactTypo = fontSize == defaultFontSize && typography === defaultTypography &&
        LocalConfiguration.current.screenWidthDp < COMPACT_TYPO_WIDTH_DP
    val effectiveTypography = remember(useCompactTypo, fontFamily, typography) {
        if (useCompactTypo) MindGameTypography.create(fontSize = compactFontSize, fontFamily = fontFamily)
        else typography
    }
    CompositionLocalProvider(
        LocalFontSize provides if (useCompactTypo) compactFontSize else fontSize,
        LocalMindGameFontFamily provides fontFamily,
        LocalTypography provides effectiveTypography,
        LocalIconSet provides iconSet
    ) {
        MaterialTheme(
            colorScheme = schemeFor(scheme, dark),
            content = content,
        )
    }
}

object Theme {
    val typography: MindGameTypography
        @Composable
        get() = LocalTypography.current

    val icons: IconSet
        @Composable
        get() = LocalIconSet.current
}
