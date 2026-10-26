package com.idroid.stuido.mind_game.core.ui.components.keypad

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.components.text.BaseText
import com.idroid.stuido.mind_game.core.ui.util.isHidden

/**
 * 底部数字键区（对齐 docs/sudoku_ui.jpeg 的 Number Keyboard / thumb-easy zone）：
 * 一排等大的圆角方形数字键 1..digits，键下缘挂"剩余可填次数"徽标；
 * 某数字剩余 0 或全局禁用时置灰且点击无效。各键占等宽一格，9 键不裁剪。
 */
@Composable
fun NumberKeypad(
    modifier: Modifier = Modifier,
    uiContent: NumberKeypadUiContent,
    uiConfig: NumberKeypadConfig,
    uiStyle: NumberKeypadStyle,
) {
    if (uiConfig.visibility.isHidden() || uiContent.digits <= 0) return

    Surface(modifier = modifier.fillMaxWidth(), color = uiStyle.zoneColor) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
        ) {
            // 窄屏（分屏/小窗）每格不足 keySize 时按键宽缩键，避免相邻键重叠。
            val keySize = keySizeForWidth(maxWidth, uiContent.digits, uiStyle.keySize)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (digit in 1..uiContent.digits) {
                    val remaining = uiContent.remaining.getOrElse(digit - 1) { 0 }
                    KeyButton(
                        digit = digit,
                        remaining = remaining,
                        enabled = isDigitKeyEnabled(
                            enabled = uiConfig.enabled,
                            remaining = remaining,
                            digit = digit,
                            zeroCountEnabled = uiContent.zeroCountEnabled,
                        ),
                        onClick = { uiContent.onDigit(digit) },
                        uiStyle = uiStyle,
                        keySize = keySize,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * 单键可点判定：全局未禁用，且（剩余次数 > 0，或宿主声明该数字在 0 剩余时仍可点，
 * 如笔记模式下移除该格已有笔记）。
 */
internal fun isDigitKeyEnabled(
    enabled: Boolean,
    remaining: Int,
    digit: Int,
    zeroCountEnabled: Set<Int>,
): Boolean = enabled && (remaining > 0 || digit in zeroCountEnabled)

/**
 * 键边长：每格可用宽 = maxWidth/digits，取 min(格宽 - 4dp 间隔, keySize)，下限 16dp。
 */
internal fun keySizeForWidth(maxWidth: Dp, digits: Int, keySize: Dp): Dp {
    if (digits <= 0) return keySize
    return (maxWidth / digits - 4.dp).coerceAtMost(keySize).coerceAtLeast(16.dp)
}

/** 单个数字键：圆角方形 + 居中数字 + 下缘剩余次数徽标。 */
@Composable
private fun KeyButton(
    digit: Int,
    remaining: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    uiStyle: NumberKeypadStyle,
    keySize: Dp,
    modifier: Modifier = Modifier,
) {
    // 整格可点（而非仅 keySize 内）：pointerInput 直接吃触控，不产生额外语义节点
    // （否则每个数字在读屏里会被报成两个按钮），命中格内键与徽标外的任何位置都算。
    // onClick 必须经 rememberUpdatedState 转手：pointerInput 只在 enabled 变化时重启，
    // 直接捕获会把回调（及其捕获的 state 快照）冻结在协程启动那次重组，旧选择/Givens
    // 会让 keypressDigit 的守卫静默 early-return —— 表现为“点了没反应”。
    val currentOnClick by rememberUpdatedState(onClick)
    Box(
        modifier = modifier.pointerInput(enabled) {
            if (enabled) detectTapGestures { currentOnClick() }
        },
        contentAlignment = Alignment.Center,
    ) {
        Box {
            Surface(
                modifier = Modifier.size(keySize),
                shape = uiStyle.shape,
                color = if (enabled) uiStyle.surfaceColor else uiStyle.disabledSurfaceColor,
                enabled = enabled,
                onClick = onClick,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    BaseText(
                        text = AnnotatedString(digit.toString()),
                        color = if (enabled) uiStyle.digitColor else uiStyle.disabledContentColor,
                        style = uiStyle.digitStyle,
                    )
                }
            }
            // 剩余次数徽标：挂在键下缘正中（对齐 mockup），禁用态同样置灰。
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 7.dp),
                shape = RoundedCornerShape(4.dp),
                color = if (enabled) uiStyle.badgeBackgroundColor else uiStyle.disabledSurfaceColor,
            ) {
                BaseText(
                    text = AnnotatedString(remaining.toString()),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    color = if (enabled) uiStyle.badgeContentColor else uiStyle.disabledContentColor,
                    style = uiStyle.badgeStyle,
                )
            }
        }
    }
}
