package com.idroid.stuido.mind_game.core.ui.components.keypad

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.util.VisibilityConfig

@Immutable
data class NumberKeypadUiContent(
    /** 渲染数字键 1..digits（如 MINI=4、MEDIUM=6、CLASSIC=9）。 */
    val digits: Int,
    /** 各数字剩余可填次数：索引 0..digits-1 对应数字 1..digits；为 0 时该键置灰且不可点。 */
    val remaining: List<Int>,
    /** 命中数字键时回调数字本身；派发 InputNumber 还是 ToggleNote 由宿主决定。 */
    val onDigit: (Int) -> Unit,
)

@Immutable
data class NumberKeypadConfig(
    val visibility: VisibilityConfig = VisibilityConfig.VISIBLE,
    /** 全局禁用（如选中了不可改的 given 格）：整排键置灰且不响应点击。 */
    val enabled: Boolean = true,
)

@Immutable
data class NumberKeypadStyle(
    val shape: Shape = CircleShape,
    val surfaceColor: Color,
    val disabledSurfaceColor: Color,
    val digitColor: Color,
    val disabledContentColor: Color,
    val badgeBackgroundColor: Color,
    val badgeContentColor: Color,
    /** 单键圆形直径；默认 36dp 使 9 键在 ~360dp 屏宽内不裁剪、键区高约 64dp。 */
    val keySize: Dp = 36.dp,
    val digitStyle: TextStyle = TextStyle.Default,
    val badgeStyle: TextStyle = TextStyle.Default,
)
