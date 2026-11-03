package com.idroid.stuido.mind_game.core.ui.components.modecard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.util.VisibilityConfig

@Immutable
data class ModeCardUiContent(
    val icon: ImageVector,
    val title: String,
    val subtitle: String? = null,
    val iconContentDescription: String? = null,
)

@Immutable
data class ModeCardConfig(
    val visibility: VisibilityConfig = VisibilityConfig.VISIBLE,
)

@Immutable
data class ModeCardStyle(
    val shape: Shape = RoundedCornerShape(16.dp),
    val containerColor: Color,
    val contentColor: Color,
    val secondaryContentColor: Color,
    val iconTint: Color,
    val titleStyle: TextStyle = TextStyle.Default,
    val subtitleStyle: TextStyle = TextStyle.Default,
    val iconSize: Dp = 32.dp,
    val headerSpacing: Dp = 16.dp,
    val sectionSpacing: Dp = 12.dp,
    val contentPadding: PaddingValues = PaddingValues(20.dp),
    val border: BorderStroke? = null,
)
