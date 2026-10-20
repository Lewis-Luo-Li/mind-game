package com.idroid.stuido.mind_game.core.ui.components.status

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.util.VisibilityConfig

@Immutable
data class StatusChipUiContent(
    val icon: ImageVector,
    val contentDescription: String,
    val text: String,
    /** 主文本右侧的次要文本（如单位/状态标注）；null 表示不显示。 */
    val secondaryText: String? = null,
    /** 非 null 且 config.clickable 时该项可点（如计时器点击暂停/继续）。 */
    val onClick: (() -> Unit)? = null,
)

@Immutable
data class StatusChipConfig(
    val visibility: VisibilityConfig = VisibilityConfig.VISIBLE,
    val enabled: Boolean = true,
    /** 置 false 时忽略 onClick，仅作展示。 */
    val clickable: Boolean = true,
)

@Immutable
data class StatusChipStyle(
    val iconTintColor: Color,
    val textColor: Color,
    val disabledContentColor: Color,
    val iconSize: Dp = 18.dp,
    /** 图标与文本、文本与次要文本之间的间距。 */
    val spacing: Dp = 6.dp,
    val textStyle: TextStyle = TextStyle.Default,
    val secondaryTextStyle: TextStyle = TextStyle.Default,
)
