package com.idroid.stuido.mind_game.core.ui.components.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.components.text.BaseText
import com.idroid.stuido.mind_game.core.ui.theme.AppThemeMode
import com.idroid.stuido.mind_game.core.ui.theme.MindgameTheme

@Preview(name = "Progress dots · light", showBackground = true)
@Composable
private fun CycleProgressDotsPreviewLight() {
    MindgameTheme(mode = AppThemeMode.LIGHT) {
        CycleProgressDotsPreviewContent()
    }
}

@Preview(name = "Progress dots · dark", showBackground = true)
@Composable
private fun CycleProgressDotsPreviewDark() {
    MindgameTheme(mode = AppThemeMode.DARK) {
        CycleProgressDotsPreviewContent()
    }
}

@Composable
private fun CycleProgressDotsPreviewContent() {
    val colors = MaterialTheme.colorScheme
    val style = CycleProgressDotsStyle(
        completedColor = colors.primary,
        currentColor = colors.tertiary,
        pendingColor = colors.outlineVariant,
    )
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BaseText(text = AnnotatedString("0/20"))
        CycleProgressDots(
            uiContent = CycleProgressDotsUiContent(0, 0, "0 of 20 levels cleared"),
            uiConfig = CycleProgressDotsConfig(),
            uiStyle = style,
        )
        BaseText(text = AnnotatedString("7/20"))
        CycleProgressDots(
            uiContent = CycleProgressDotsUiContent(20, 7, "7 of 20 levels cleared"),
            uiConfig = CycleProgressDotsConfig(),
            uiStyle = style,
        )
        BaseText(text = AnnotatedString("20/20"))
        CycleProgressDots(
            uiContent = CycleProgressDotsUiContent(20, 20, "20 of 20 levels cleared"),
            uiConfig = CycleProgressDotsConfig(),
            uiStyle = style,
        )
        BaseText(text = AnnotatedString("7/20 · no current highlight"))
        CycleProgressDots(
            uiContent = CycleProgressDotsUiContent(20, 7, "7 of 20 levels cleared"),
            uiConfig = CycleProgressDotsConfig(highlightCurrent = false),
            uiStyle = style,
        )
    }
}
