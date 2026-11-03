package com.idroid.stuido.mind_game.core.ui.components.button

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

/**
 * 按钮展示的数据：前导图标 + 文本。
 * - [leadIcon] 为 null 时只渲染文本；
 * - [contentDescription] 为 null 时图标视为装饰性（文本已表达语义）。
 */
@Immutable
data class ButtonUiContent(
    val text: String,
    val leadIcon: ImageVector? = null,
    val contentDescription: String? = null,
    val onClick: () -> Unit,
)

/** 按钮的渲染形态：填充 / 描边。 */
enum class ButtonVariant {
    FILLED,
    OUTLINED,
}

@Immutable
data class ButtonConfig(
    val visibility: VisibilityConfig = VisibilityConfig.VISIBLE,
    val enabled: Boolean = true,
    val variant: ButtonVariant = ButtonVariant.FILLED,
)

/**
 * 按钮的样式参数；填充与描边两种形态各自持有配色，
 * 使调用方切换 [ButtonConfig.variant] 时无需重建 Style。
 */
@Immutable
data class ButtonStyle(
    val shape: Shape = RoundedCornerShape(12.dp),
    val filledContainerColor: Color,
    val filledContentColor: Color,
    val outlinedContainerColor: Color = Color.Transparent,
    val outlinedContentColor: Color,
    val outlinedBorderColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val borderWidth: Dp = 1.dp,
    val contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    val iconSize: Dp = 18.dp,
    val iconTextSpacing: Dp = 8.dp,
    val minHeight: Dp = 48.dp,
    val textStyle: TextStyle = TextStyle.Default,
)