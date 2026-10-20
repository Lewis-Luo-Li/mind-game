package com.idroid.stuido.mind_game.core.ui.components.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.idroid.stuido.mind_game.core.ui.util.isHidden

/**
 * 对局状态信息行（对齐 docs/sudoku_ui.jpeg 的 44dp Game Status Information 条）：
 * 将 N 个 [StatusChip] 均匀分布在一整行上（计时 / 错误 / 提示等）。
 */
@Composable
fun StatusInfoRow(
    modifier: Modifier = Modifier,
    uiContent: StatusInfoRowUiContent,
    uiConfig: StatusInfoRowConfig,
    uiStyle: StatusInfoRowStyle,
) {
    if (uiConfig.visibility.isHidden() || uiContent.items.isEmpty()) return

    Surface(modifier = modifier.fillMaxWidth(), color = uiStyle.zoneColor) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(uiStyle.height)
                .padding(horizontal = uiStyle.horizontalPadding),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            uiContent.items.forEach { item ->
                // 每项等宽一格并居中：保证 N 项均匀铺满且不相互挤压出屏（大字号下
                // 项内文本由 StatusChip 单行省略承接）。
                StatusChip(
                    uiContent = item,
                    uiConfig = StatusChipConfig(enabled = uiConfig.enabled),
                    uiStyle = uiStyle.chipStyle,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
