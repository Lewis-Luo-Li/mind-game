package com.idroid.stuido.mind_game.core.ui.components.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import com.idroid.stuido.mind_game.core.ui.components.text.BaseText
import com.idroid.stuido.mind_game.core.ui.components.text.TextConfig
import com.idroid.stuido.mind_game.core.ui.util.isHidden

/**
 * 单个状态信息项（对齐 docs/sudoku_ui.jpeg 的 Game Status Information 条目）：
 * 图标 + 主文本（如 "⏱ 04:18"、"⚠ Errors: 1/3"），可选尾随次要文本。
 *
 * 仅展示：不提供 onClick 或 [StatusChipConfig.clickable] 为 false 时为纯文本排，
 * 不携带点击语义；可点项（如计时器暂停/继续）通过 Surface 获得 enabled/click 语义。
 */
@Composable
fun StatusChip(
    modifier: Modifier = Modifier,
    uiContent: StatusChipUiContent,
    uiConfig: StatusChipConfig,
    uiStyle: StatusChipStyle,
) {
    if (uiConfig.visibility.isHidden()) return

    val iconTint = if (uiConfig.enabled) uiStyle.iconTintColor else uiStyle.disabledContentColor
    val textColor = if (uiConfig.enabled) uiStyle.textColor else uiStyle.disabledContentColor

    val content: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = uiContent.icon,
                contentDescription = uiContent.contentDescription,
                tint = iconTint,
                modifier = Modifier.size(uiStyle.iconSize),
            )
            Spacer(Modifier.width(uiStyle.spacing))
            // 文本限宽并单行省略：大字号/窄屏下项内内容不外溢。
            BaseText(
                text = AnnotatedString(uiContent.text),
                color = textColor,
                style = uiStyle.textStyle,
                config = TextConfig(maxLine = 1, overflow = TextOverflow.Ellipsis),
                modifier = Modifier.weight(1f, fill = false),
            )
            if (uiContent.secondaryText != null) {
                Spacer(Modifier.width(uiStyle.spacing))
                BaseText(
                    text = AnnotatedString(uiContent.secondaryText),
                    color = textColor,
                    style = uiStyle.secondaryTextStyle,
                    config = TextConfig(maxLine = 1, overflow = TextOverflow.Ellipsis),
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
        }
    }

    val onClick = uiContent.onClick
    if (onClick != null && uiConfig.clickable) {
        Surface(
            modifier = modifier,
            color = Color.Transparent,
            enabled = uiConfig.enabled,
            onClick = onClick,
        ) { content() }
    } else {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) { content() }
    }
}
