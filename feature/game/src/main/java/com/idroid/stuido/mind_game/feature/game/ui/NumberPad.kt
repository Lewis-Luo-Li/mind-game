package com.idroid.stuido.mind_game.feature.game.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 底部数字键区 1..size。当前输入模式由宿主决定当命中时派发 InputNumber 还是 ToggleNote。
 * [onDigit] 统一回调数字本身，语义由调用方决定。
 */
@Composable
fun NumberPad(
    size: Int,
    selectedGiven: Boolean,
    onDigit: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val digits = 1..size
    Surface(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            digits.forEach { digit ->
                DigitButton(
                    digit = digit,
                    enabled = !selectedGiven,
                    onClick = { onDigit(digit) },
                )
            }
        }
    }
}

@Composable
private fun DigitButton(digit: Int, enabled: Boolean, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val bg = if (enabled) colorScheme.primaryContainer else colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val fg = if (enabled) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(46.dp)
            .let { m ->
                if (enabled) m.clickable { onClick() } else m
            },
        contentAlignment = Alignment.Center,
    ) {
        Surface(shape = CircleShape, color = bg) {
            Box(
                modifier = Modifier.size(46.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = digit.toString(),
                    color = if (enabled) fg else fg,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}
