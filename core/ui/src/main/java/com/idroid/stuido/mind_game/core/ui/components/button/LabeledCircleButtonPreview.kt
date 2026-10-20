package com.idroid.stuido.mind_game.core.ui.components.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
fun LabeledCircleButtonPreviewDarkMode() {
    MindgameTheme(
        mode = AppThemeMode.DARK,
    ) {
        val colorScheme = MaterialTheme.colorScheme
        val uiStyle = LabeledCircleButtonStyle(
            shape = CircleShape,
            surfaceColor = colorScheme.surfaceVariant,
            activeSurfaceColor = colorScheme.primaryContainer,
            activeBorderColor = colorScheme.primary,
            disabledSurfaceColor = colorScheme.surfaceVariant.copy(alpha = 0.4f),
            iconTintColor = colorScheme.onSurfaceVariant,
            labelColor = colorScheme.onSurface,
            disabledContentColor = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            badgeBackgroundColor = colorScheme.primary,
            badgeContentColor = colorScheme.onPrimary,
            surfaceSize = 48.dp,
            iconSize = 24.dp,
            labelStyle = MaterialTheme.typography.labelMedium,
            badgeStyle = MaterialTheme.typography.labelSmall,
        )
        // 横向渲染四种典型状态：普通 / 激活("On") / 计数徽标 / 禁用。
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            LabeledCircleButton(
                uiContent = LabeledCircleButtonUiContent(
                    icon = Theme.icons.undo,
                    contentDescription = "Undo",
                    label = "Undo",
                    onClick = {},
                ),
                uiConfig = LabeledCircleButtonConfig(),
                uiStyle = uiStyle,
            )
            LabeledCircleButton(
                uiContent = LabeledCircleButtonUiContent(
                    icon = Theme.icons.notes,
                    contentDescription = "Notes",
                    label = "Notes",
                    badge = "On",
                    onClick = {},
                ),
                uiConfig = LabeledCircleButtonConfig(active = true),
                uiStyle = uiStyle,
            )
            LabeledCircleButton(
                uiContent = LabeledCircleButtonUiContent(
                    icon = Theme.icons.hint,
                    contentDescription = "Hint",
                    label = "Hint",
                    badge = "3",
                    onClick = {},
                ),
                uiConfig = LabeledCircleButtonConfig(),
                uiStyle = uiStyle,
            )
            LabeledCircleButton(
                uiContent = LabeledCircleButtonUiContent(
                    icon = Theme.icons.erase,
                    contentDescription = "Erase",
                    label = "Erase",
                    onClick = {},
                ),
                uiConfig = LabeledCircleButtonConfig(enabled = false),
                uiStyle = uiStyle,
            )
        }
    }
}
