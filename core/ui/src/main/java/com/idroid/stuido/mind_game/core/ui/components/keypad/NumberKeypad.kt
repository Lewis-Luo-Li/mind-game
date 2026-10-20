package com.idroid.stuido.mind_game.core.ui.components.keypad

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.util.isHidden

/**
 * 底部数字键区（对齐 docs/sudoku_ui.jpeg 的 Number Keyboard / thumb-easy zone）：
 * 一排等大的圆形数字键 1..digits，键下缘挂"剩余可填次数"徽标；
 * 某数字剩余 0 或全局禁用时置灰且点击无效。各键占等宽一格，9 键不裁剪。
 */
@Composable
fun NumberKeypad(
    modifier: Modifier = Modifier,
    uiContent: NumberKeypadUiContent,
    uiConfig: NumberKeypadConfig,
    uiStyle: NumberKeypadStyle,
) {
    if (uiConfig.visibility.isHidden()) return

    Surface(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (digit in 1..uiContent.digits) {
                val remaining = uiContent.remaining.getOrElse(digit - 1) { 0 }
                KeyButton(
                    digit = digit,
                    remaining = remaining,
                    enabled = uiConfig.enabled && remaining > 0,
                    onClick = { uiContent.onDigit(digit) },
                    uiStyle = uiStyle,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** 单个数字键：圆形 + 居中数字 + 下缘剩余次数徽标。 */
@Composable
private fun KeyButton(
    digit: Int,
    remaining: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    uiStyle: NumberKeypadStyle,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box {
            Surface(
                modifier = Modifier.size(uiStyle.keySize),
                shape = uiStyle.shape,
                color = if (enabled) uiStyle.surfaceColor else uiStyle.disabledSurfaceColor,
                shadowElevation = 2.dp,
                enabled = enabled,
                onClick = onClick,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = digit.toString(),
                        color = if (enabled) uiStyle.digitColor else uiStyle.disabledContentColor,
                        style = uiStyle.digitStyle,
                    )
                }
            }
            // 剩余次数徽标：挂在圆形下缘正中（对齐 mockup），禁用态同样置灰。
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 7.dp),
                shape = CircleShape,
                color = if (enabled) uiStyle.badgeBackgroundColor else uiStyle.disabledSurfaceColor,
                shadowElevation = 1.dp,
            ) {
                Text(
                    text = remaining.toString(),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    color = if (enabled) uiStyle.badgeContentColor else uiStyle.disabledContentColor,
                    style = uiStyle.badgeStyle,
                )
            }
        }
    }
}
