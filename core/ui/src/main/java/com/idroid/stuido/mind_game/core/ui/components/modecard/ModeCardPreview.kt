package com.idroid.stuido.mind_game.core.ui.components.modecard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.components.button.Button
import com.idroid.stuido.mind_game.core.ui.components.button.ButtonConfig
import com.idroid.stuido.mind_game.core.ui.components.button.ButtonStyle
import com.idroid.stuido.mind_game.core.ui.components.button.ButtonUiContent
import com.idroid.stuido.mind_game.core.ui.components.button.ButtonVariant
import com.idroid.stuido.mind_game.core.ui.components.progress.CycleProgressDots
import com.idroid.stuido.mind_game.core.ui.components.progress.CycleProgressDotsConfig
import com.idroid.stuido.mind_game.core.ui.components.progress.CycleProgressDotsStyle
import com.idroid.stuido.mind_game.core.ui.components.progress.CycleProgressDotsUiContent
import com.idroid.stuido.mind_game.core.ui.components.text.BaseText
import com.idroid.stuido.mind_game.core.ui.theme.AppThemeMode
import com.idroid.stuido.mind_game.core.ui.theme.MindgameTheme
import com.idroid.stuido.mind_game.core.ui.theme.Theme

@Preview(name = "Mode cards · light", showBackground = true)
@Composable
private fun ModeCardPreviewLight() {
    MindgameTheme(mode = AppThemeMode.LIGHT) {
        ModeCardPreviewContent()
    }
}

@Preview(name = "Mode cards · dark", showBackground = true)
@Composable
private fun ModeCardPreviewDark() {
    MindgameTheme(mode = AppThemeMode.DARK) {
        ModeCardPreviewContent()
    }
}

@Composable
private fun ModeCardPreviewContent() {
    val colors = MaterialTheme.colorScheme
    val style = ModeCardStyle(
        containerColor = colors.surfaceContainerHigh,
        contentColor = colors.onSurface,
        secondaryContentColor = colors.onSurfaceVariant,
        iconTint = colors.primary,
        titleStyle = MaterialTheme.typography.titleLarge,
        subtitleStyle = MaterialTheme.typography.bodyMedium,
    )
    val buttonStyle = ButtonStyle(
        filledContainerColor = colors.primary,
        filledContentColor = colors.onPrimary,
        outlinedContainerColor = colors.surface,
        outlinedContentColor = colors.onSurface,
        outlinedBorderColor = colors.outline,
        disabledContainerColor = colors.surfaceVariant.copy(alpha = 0.4f),
        disabledContentColor = colors.onSurfaceVariant.copy(alpha = 0.4f),
        textStyle = MaterialTheme.typography.labelLarge,
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ModeCard(
            uiContent = ModeCardUiContent(
                icon = Theme.icons.daily,
                title = "Daily Challenge",
                subtitle = "9×9 · Hard · New puzzle at 5pm Sydney time",
            ),
            uiConfig = ModeCardConfig(),
            uiStyle = style,
            content = {
                BaseText(
                    text = AnnotatedString("Ends in 03:12:45"),
                    color = style.secondaryContentColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            action = {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    uiContent = ButtonUiContent(text = "Play", onClick = {}),
                    uiConfig = ButtonConfig(variant = ButtonVariant.FILLED),
                    uiStyle = buttonStyle,
                )
            },
        )
        ModeCard(
            uiContent = ModeCardUiContent(
                icon = Theme.icons.done,
                title = "Daily Challenge",
                subtitle = "9×9 · Hard · New puzzle at 5pm Sydney time",
            ),
            uiConfig = ModeCardConfig(),
            uiStyle = style.copy(iconTint = colors.tertiary),
            content = {
                BaseText(
                    text = AnnotatedString("Solved · next puzzle in 03:12:45"),
                    color = style.secondaryContentColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            action = {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    uiContent = ButtonUiContent(text = "Play again", onClick = {}),
                    uiConfig = ButtonConfig(variant = ButtonVariant.OUTLINED),
                    uiStyle = buttonStyle,
                )
            },
        )
        ModeCard(
            uiContent = ModeCardUiContent(
                icon = Theme.icons.ladder,
                title = "Level 23",
                subtitle = "Medium · Round 2",
            ),
            uiConfig = ModeCardConfig(),
            uiStyle = style,
            content = {
                CycleProgressDots(
                    uiContent = CycleProgressDotsUiContent(
                        total = 20,
                        completed = 2,
                        contentDescription = "2 of 20 levels cleared this round",
                    ),
                    uiConfig = CycleProgressDotsConfig(),
                    uiStyle = CycleProgressDotsStyle(
                        completedColor = colors.primary,
                        currentColor = colors.tertiary,
                        pendingColor = colors.outlineVariant,
                    ),
                )
                BaseText(
                    text = AnnotatedString("3 of 20 in this round"),
                    color = style.secondaryContentColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            action = {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    uiContent = ButtonUiContent(text = "Play", onClick = {}),
                    uiConfig = ButtonConfig(variant = ButtonVariant.FILLED),
                    uiStyle = buttonStyle,
                )
            },
        )
    }
}
