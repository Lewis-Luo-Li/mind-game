package com.idroid.stuido.mind_game.core.ui.primitives

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

data class MindGameFontSize(
    val body: TextUnit = 16.sp,
    val label: TextUnit = 11.sp,
    val title: TextUnit = 22.sp
)

val defaultFontSize = MindGameFontSize()

internal val LocalFontSize = staticCompositionLocalOf { defaultFontSize }