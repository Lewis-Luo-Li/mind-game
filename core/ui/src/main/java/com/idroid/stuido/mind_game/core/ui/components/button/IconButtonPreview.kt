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
fun IconButtonPreviewDarkMode() {
    MindgameTheme(
        mode = AppThemeMode.DARK,
    ) {
        val uiContentList = listOf<IconButtonUiContent>(
            IconButtonUiContent(
                icon = Theme.icons.back,
                contentDescription = "Back",
                onClick = {}
            ),
            IconButtonUiContent(
                icon = Theme.icons.settings,
                contentDescription = "Settings",
                onClick = {}
            )
        )
        val uiConfig = IconButtonConfig()
        val uiStyle = IconButtonStyle(
            shape = CircleShape,
            surfaceColor = MaterialTheme.colorScheme.surface,
            iconTintColor = MaterialTheme.colorScheme.onSurface,
            surfaceSize = 48.dp,
            iconSize = 24.dp
        )
        // 横向逐个渲染 IconButton，间距由 Arrangement 统一控制。
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            uiContentList.forEach { uiContent ->
                IconButton(
                    uiContent = uiContent,
                    uiConfig = uiConfig,
                    uiStyle = uiStyle,
                )
            }
        }
    }
}