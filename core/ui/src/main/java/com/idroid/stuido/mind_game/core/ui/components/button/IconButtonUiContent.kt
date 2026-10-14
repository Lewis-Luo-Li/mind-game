package com.idroid.stuido.mind_game.core.ui.components.button

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.idroid.stuido.mind_game.core.ui.util.VisibilityConfig

@Immutable
data class IconButtonUiContent(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
)

data class IconButtonConfig(
    val visibility: VisibilityConfig = VisibilityConfig.VISIBLE,
)

data class IconButtonStyle(
    val shape: Shape = CircleShape,
    val surfaceColor: Color,
    val iconTintColor: Color,
    val surfaceSize: Dp,
    val iconSize: Dp
)