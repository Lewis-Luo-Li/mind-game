package com.idroid.stuido.mind_game.core.ui.primitives

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class MindGameTypography(
    val bodyStyleNormal: TextStyle,
    val bodyStyleSemiBold: TextStyle,
    val titleStyleNormal: TextStyle,
    val titleStyleSemiBold: TextStyle,
    val labelStyle: TextStyle
) {
    companion object {

        fun create(
            fontSize: MindGameFontSize = defaultFontSize,
            fontFamily: MindGameFontFamily = defaultMindGameFontFamily
        ) : MindGameTypography {
            return MindGameTypography(
                bodyStyleNormal = TextStyle(
                    fontFamily = fontFamily.primary,
                    fontWeight = FontWeight.Normal,
                    fontSize = fontSize.body,
                    lineHeight = 24.sp,
                    letterSpacing = 0.5.sp,
                ),
                bodyStyleSemiBold = TextStyle(
                    fontFamily = fontFamily.secondary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSize.body,
                    lineHeight = 24.sp,
                    letterSpacing = 0.5.sp,
                ),
                titleStyleNormal = TextStyle(
                    fontFamily = fontFamily.primary,
                    fontWeight = FontWeight.Normal,
                    fontSize = fontSize.title,
                    lineHeight = 28.sp,
                    letterSpacing = 0.sp
                ),
                titleStyleSemiBold = TextStyle(
                    fontFamily = fontFamily.secondary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSize.title,
                    lineHeight = 28.sp,
                    letterSpacing = 0.sp
                ),
                labelStyle = TextStyle(
                    fontFamily = fontFamily.primary,
                    fontWeight = FontWeight.Normal,
                    fontSize = fontSize.label,
                    lineHeight = 16.sp,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

val defaultTypography = MindGameTypography.create()

internal val LocalTypography = staticCompositionLocalOf { defaultTypography }