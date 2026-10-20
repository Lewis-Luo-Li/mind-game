package com.idroid.stuido.mind_game.core.ui.components.button

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.util.VisibilityConfig

@Immutable
data class LabeledCircleButtonUiContent(
    val icon: ImageVector,
    val contentDescription: String,
    val label: String,
    /** 圆形右上角的小徽标文本（如 "On" 或剩余次数）；null 表示不显示。 */
    val badge: String? = null,
    val onClick: () -> Unit
)

@Immutable
data class LabeledCircleButtonConfig(
    val visibility: VisibilityConfig = VisibilityConfig.VISIBLE,
    val enabled: Boolean = true,
    /** 激活态（如笔记模式开启）：以描边/填充高亮圆形。 */
    val active: Boolean = false,
)

@Immutable
data class LabeledCircleButtonStyle(
    val shape: Shape = CircleShape,
    val surfaceColor: Color,
    val activeSurfaceColor: Color,
    val activeBorderColor: Color,
    val disabledSurfaceColor: Color,
    val iconTintColor: Color,
    val labelColor: Color,
    val disabledContentColor: Color,
    val badgeBackgroundColor: Color,
    val badgeContentColor: Color,
    val surfaceSize: Dp = 48.dp,
    val iconSize: Dp = 24.dp,
    val labelStyle: TextStyle = TextStyle.Default,
    val badgeStyle: TextStyle = TextStyle.Default,
)
