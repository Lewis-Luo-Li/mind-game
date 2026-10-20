package com.idroid.stuido.mind_game.core.ui.components.keypad

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.theme.MindgameTheme

@Composable
private fun previewStyle() = NumberKeypadStyle(
    zoneColor = MaterialTheme.colorScheme.secondaryContainer,
    surfaceColor = MaterialTheme.colorScheme.surface,
    disabledSurfaceColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
    digitColor = MaterialTheme.colorScheme.onSurface,
    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
    badgeBackgroundColor = MaterialTheme.colorScheme.primary,
    badgeContentColor = MaterialTheme.colorScheme.onPrimary,
    keySize = 36.dp,
    digitStyle = MaterialTheme.typography.titleMedium,
    badgeStyle = MaterialTheme.typography.labelSmall,
)

/** 9 键（CLASSIC）：剩余次数不一，数字 4 与 9 已放满而置灰（徽标 0）。 */
@Preview(showBackground = true, name = "Keypad · 9 digits", widthDp = 400)
@Composable
fun NumberKeypadPreviewNine() {
    MindgameTheme {
        NumberKeypad(
            modifier = Modifier.fillMaxWidth(),
            uiContent = NumberKeypadUiContent(
                digits = 9,
                remaining = listOf(2, 4, 1, 0, 5, 1, 3, 2, 0),
                onDigit = {},
            ),
            uiConfig = NumberKeypadConfig(),
            uiStyle = previewStyle(),
        )
    }
}

/** 4 键（MINI）：一行仅 4 个等宽键位。 */
@Preview(showBackground = true, name = "Keypad · 4 digits", widthDp = 400)
@Composable
fun NumberKeypadPreviewFour() {
    MindgameTheme {
        NumberKeypad(
            modifier = Modifier.fillMaxWidth(),
            uiContent = NumberKeypadUiContent(
                digits = 4,
                remaining = listOf(3, 1, 0, 2),
                onDigit = {},
            ),
            uiConfig = NumberKeypadConfig(),
            uiStyle = previewStyle(),
        )
    }
}

/** 全局禁用：整排置灰（如选中 given 格）。 */
@Preview(showBackground = true, name = "Keypad · disabled", widthDp = 400)
@Composable
fun NumberKeypadPreviewDisabled() {
    MindgameTheme {
        Column {
            NumberKeypad(
                modifier = Modifier.fillMaxWidth(),
                uiContent = NumberKeypadUiContent(
                    digits = 9,
                    remaining = listOf(2, 4, 1, 0, 5, 1, 3, 2, 0),
                    onDigit = {},
                ),
                uiConfig = NumberKeypadConfig(enabled = false),
                uiStyle = previewStyle(),
            )
        }
    }
}
