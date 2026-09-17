package com.idroid.stuido.mind_game.core.ui.primitives

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.idroid.stuido.mind_game.core.ui.R

/**
 * Mind Game Font
 */
data class MindGameFontFamily(
    val primary: FontFamily = primaryFontFamily,
    val secondary: FontFamily = secondaryFontFamily
)

internal val primaryFontFamily = FontFamily(
    Font(R.font.kleeone_regular, FontWeight.Normal)
)

internal val secondaryFontFamily = FontFamily(
    Font(R.font.kleeone_semibold, FontWeight.SemiBold)
)

val defaultMindGameFontFamily = MindGameFontFamily()

internal val LocalMindGameFontFamily = staticCompositionLocalOf { defaultMindGameFontFamily }