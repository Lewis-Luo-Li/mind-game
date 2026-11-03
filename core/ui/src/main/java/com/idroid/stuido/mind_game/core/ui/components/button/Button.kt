package com.idroid.stuido.mind_game.core.ui.components.button
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import com.idroid.stuido.mind_game.core.ui.components.text.BaseText
import com.idroid.stuido.mind_game.core.ui.components.text.TextConfig
import com.idroid.stuido.mind_game.core.ui.util.isHidden

/**
 * 通用按钮：前导图标 + 文本，通过 [ButtonConfig.variant] 渲染为
 * 填充（[ButtonVariant.FILLED]）或描边（[ButtonVariant.OUTLINED]）形态。
 * - [ButtonConfig.enabled] 为 false 时置灰且点击无效；
 * - [ButtonUiContent.leadIcon] 为 null 时退化为纯文本按钮。
 */
@Composable
fun Button(
    modifier: Modifier = Modifier,
    uiContent: ButtonUiContent,
    uiConfig: ButtonConfig,
    uiStyle: ButtonStyle,
) {
    if (uiConfig.visibility.isHidden()) return

    val enabled = uiConfig.enabled
    val isFilled = uiConfig.variant == ButtonVariant.FILLED
    val containerColor = when {
        !enabled -> uiStyle.disabledContainerColor
        isFilled -> uiStyle.filledContainerColor
        else -> uiStyle.outlinedContainerColor
    }
    val contentColor = when {
        !enabled -> uiStyle.disabledContentColor
        isFilled -> uiStyle.filledContentColor
        else -> uiStyle.outlinedContentColor
    }

    Surface(
        modifier = modifier.defaultMinSize(minHeight = uiStyle.minHeight),
        shape = uiStyle.shape,
        color = containerColor,
        border = if (isFilled) {
            null
        } else {
            BorderStroke(
                width = uiStyle.borderWidth,
                color = if (enabled) uiStyle.outlinedBorderColor else uiStyle.disabledContentColor,
            )
        },
        enabled = enabled,
        onClick = uiContent.onClick,
    ) {
        Row(
            modifier = Modifier.padding(uiStyle.contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val leadIcon = uiContent.leadIcon
            if (leadIcon != null) {
                Icon(
                    imageVector = leadIcon,
                    contentDescription = uiContent.contentDescription,
                    tint = contentColor,
                    modifier = Modifier.size(uiStyle.iconSize),
                )
                Spacer(Modifier.width(uiStyle.iconTextSpacing))
            }
            // 单行省略：大字号/窄屏下按钮文本不外溢。
            BaseText(
                text = AnnotatedString(uiContent.text),
                color = contentColor,
                style = uiStyle.textStyle,
                config = TextConfig(maxLine = 1, overflow = TextOverflow.Ellipsis),
            )
        }
    }
}
