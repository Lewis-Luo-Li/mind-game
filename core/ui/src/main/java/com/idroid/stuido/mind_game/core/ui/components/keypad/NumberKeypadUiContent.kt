package com.idroid.stuido.mind_game.core.ui.components.keypad

import androidx.compose.foundation.shape.RoundedCornerShape
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
    /**
     * 剩余次数为 0 时仍可点的数字：宿主用于"笔记模式下移除该格已有的候选数字"
     * 这类不消耗配额的回退操作；仅影响置灰/可点，不绕过全局 enabled。
     */
    val zeroCountEnabled: Set<Int> = emptySet(),
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
    /** 键形：mockup 为圆角方形键。 */
    val shape: Shape = RoundedCornerShape(10.dp),
    /** 键区底色（整排键背后的色带，对齐 mockup 底部色带）。 */
    val zoneColor: Color,
    val surfaceColor: Color,
    val disabledSurfaceColor: Color,
    val digitColor: Color,
    val disabledContentColor: Color,
    val badgeBackgroundColor: Color,
    val badgeContentColor: Color,
    /** 单键边长；默认 36dp 使 9 键在 ~360dp 屏宽内不裁剪、键区高约 64dp。 */
    val keySize: Dp = 36.dp,
    val digitStyle: TextStyle = TextStyle.Default,
    val badgeStyle: TextStyle = TextStyle.Default,
)
