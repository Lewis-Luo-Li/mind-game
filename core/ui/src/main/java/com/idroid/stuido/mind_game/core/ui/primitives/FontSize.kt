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

/** 窄屏紧凑字号：window width < [COMPACT_TYPO_WIDTH_DP] 时由 MindgameTheme 自动选用。 */
val compactFontSize = MindGameFontSize(
    body = 14.sp,
    label = 10.sp,
    title = 19.sp,
)

/** 启用紧凑字号的可用宽度阈值（dp）。 */
const val COMPACT_TYPO_WIDTH_DP = 360

internal val LocalFontSize = staticCompositionLocalOf { defaultFontSize }