package com.idroid.stuido.mind_game.core.ui.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.components.text.BaseText
import com.idroid.stuido.mind_game.core.ui.components.text.TextConfig
import com.idroid.stuido.mind_game.core.ui.util.isHidden

/**
 * 圆形图标按钮 + 底部文字标签：用于辅助工具行（Undo / Notes / Erase / Hint）。
 * - [LabeledCircleButtonUiContent.badge] 非空时在圆右上叠加徽标（"On" / 计数）；
 * - [LabeledCircleButtonConfig.enabled] 为 false 时图标与标签置灰且点击无效；
 * - [LabeledCircleButtonConfig.active] 为 true 时以主题描边/填充高亮圆形。
 */
@Composable
fun LabeledCircleButton(
    modifier: Modifier = Modifier,
    uiContent: LabeledCircleButtonUiContent,
    uiConfig: LabeledCircleButtonConfig,
    uiStyle: LabeledCircleButtonStyle,
) {
    if (uiConfig.visibility.isHidden()) return

    val enabled = uiConfig.enabled
    val surfaceColor = when {
        !enabled -> uiStyle.disabledSurfaceColor
        uiConfig.active -> uiStyle.activeSurfaceColor
        else -> uiStyle.surfaceColor
    }
    val contentColor = if (enabled) uiStyle.iconTintColor else uiStyle.disabledContentColor

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            Surface(
                modifier = Modifier.size(uiStyle.surfaceSize),
                shape = uiStyle.shape,
                color = surfaceColor,
                border = if (uiConfig.active && enabled) {
                    BorderStroke(1.5.dp, uiStyle.activeBorderColor)
                } else {
                    null
                },
                enabled = enabled,
                onClick = uiContent.onClick,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = uiContent.icon,
                        contentDescription = uiContent.contentDescription,
                        tint = contentColor,
                        modifier = Modifier.size(uiStyle.iconSize)
                    )
                }
            }
            if (uiContent.badge != null) {
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd),
                    shape = CircleShape,
                    color = uiStyle.badgeBackgroundColor,
                ) {
                    BaseText(
                        text = AnnotatedString(uiContent.badge),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                        color = if (enabled) uiStyle.badgeContentColor else uiStyle.disabledContentColor,
                        style = uiStyle.badgeStyle,
                    )
                }
            }
        }
        BaseText(
            text = AnnotatedString(uiContent.label),
            color = if (enabled) uiStyle.labelColor else uiStyle.disabledContentColor,
            style = uiStyle.labelStyle,
            config = TextConfig(maxLine = 1),
        )
    }
}
