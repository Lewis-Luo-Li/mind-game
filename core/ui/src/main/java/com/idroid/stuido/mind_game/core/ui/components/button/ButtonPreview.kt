package com.idroid.stuido.mind_game.core.ui.components.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.theme.AppThemeMode
import com.idroid.stuido.mind_game.core.ui.theme.MindgameTheme
import com.idroid.stuido.mind_game.core.ui.theme.Theme

@Preview
@Composable
fun ButtonPreviewDarkMode() {
    MindgameTheme(mode = AppThemeMode.DARK) {
        ButtonPreviewContent()
    }
}

@Preview
@Composable
fun ButtonPreviewLightMode() {
    MindgameTheme(mode = AppThemeMode.LIGHT) {
        ButtonPreviewContent()
    }
}

@Composable
private fun ButtonPreviewContent() {
    val colorScheme = MaterialTheme.colorScheme
    val uiStyle = ButtonStyle(
        filledContainerColor = colorScheme.primary,
        filledContentColor = colorScheme.onPrimary,
        outlinedContainerColor = colorScheme.surface,
        outlinedContentColor = colorScheme.onSurface,
        outlinedBorderColor = colorScheme.outline,
        disabledContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.4f),
        disabledContentColor = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        textStyle = MaterialTheme.typography.labelLarge,
    )
    val uiContent = ButtonUiContent(
        text = "Continue",
        leadIcon = Theme.icons.hint,
        contentDescription = "Hint",
        onClick = {},
    )
    // 四种典型状态：填充 / 描边 / 填充禁用 / 纯文本描边。
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            uiContent = uiContent,
            uiConfig = ButtonConfig(variant = ButtonVariant.FILLED),
            uiStyle = uiStyle,
        )
        Button(
            uiContent = uiContent,
            uiConfig = ButtonConfig(variant = ButtonVariant.OUTLINED),
            uiStyle = uiStyle,
        )
        Button(
            uiContent = uiContent,
            uiConfig = ButtonConfig(variant = ButtonVariant.FILLED, enabled = false),
            uiStyle = uiStyle,
        )
        Button(
            uiContent = uiContent.copy(leadIcon = null, contentDescription = null),
            uiConfig = ButtonConfig(variant = ButtonVariant.OUTLINED),
            uiStyle = uiStyle,
        )
    }
}
