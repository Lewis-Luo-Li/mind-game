package com.idroid.stuido.mind_game.core.ui.components.status

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.util.VisibilityConfig

@Immutable
data class StatusInfoRowUiContent(
    /** 行内各状态项，按顺序从左到右铺开。 */
    val items: List<StatusChipUiContent>,
)

@Immutable
data class StatusInfoRowConfig(
    val visibility: VisibilityConfig = VisibilityConfig.VISIBLE,
    /** 全局禁用：整行状态项置灰，且可点项不再响应点击。 */
    val enabled: Boolean = true,
)

@Immutable
data class StatusInfoRowStyle(
    /** 状态条底色（对齐 mockup 顶部信息条色带）。 */
    val zoneColor: Color,
    /** 状态条高度：mockup 为 44dp。 */
    val height: Dp = 44.dp,
    val horizontalPadding: Dp = 12.dp,
    /** 行内所有状态项共用的样式。 */
    val chipStyle: StatusChipStyle,
)
