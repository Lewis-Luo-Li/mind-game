package com.idroid.stuido.mind_game.core.ui.components.topbar

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.components.button.IconButtonConfig
import com.idroid.stuido.mind_game.core.ui.components.button.IconButtonStyle
import com.idroid.stuido.mind_game.core.ui.components.button.IconButtonUiContent
import com.idroid.stuido.mind_game.core.ui.components.text.TextConfig
import com.idroid.stuido.mind_game.core.ui.theme.AppThemeMode
import com.idroid.stuido.mind_game.core.ui.theme.MindgameTheme
import com.idroid.stuido.mind_game.core.ui.theme.Theme
import com.idroid.stuido.mind_game.core.ui.util.VisibilityConfig

/** 顶栏预览内容：返回 + 标题 + 设置，三个槽位齐备。 */
@Composable
private fun previewTopBarContent(): TopBarUiContent = TopBarUiContent(
    onBackClick = {},
    onSettingsClick = {},
    topBarTitle = "Mind Game",
    backButtonUiContent = IconButtonUiContent(
        icon = Theme.icons.back,
        contentDescription = "Back",
        onClick = {},
    ),
    settingsButtonUiContent = IconButtonUiContent(
        icon = Theme.icons.settings,
        contentDescription = "Settings",
        onClick = {},
    ),
)

/** 顶栏预览样式：颜色取自当前主题色板，图标按钮为统一圆形样式。 */
@Composable
private fun previewTopBarStyle(): TopBarUiStyle {
    val iconStyle = IconButtonStyle(
        shape = CircleShape,
        surfaceColor = MaterialTheme.colorScheme.surface,
        iconTintColor = MaterialTheme.colorScheme.onSurface,
        surfaceSize = 48.dp,
        iconSize = 24.dp,
    )
    return TopBarUiStyle(
        topbarBgColor = MaterialTheme.colorScheme.surface,
        titleTextColor = MaterialTheme.colorScheme.onSurface,
        backButtonStyle = iconStyle,
        settingsButtonStyle = iconStyle,
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun TopBarPreviewDarkMode() {
    MindgameTheme(mode = AppThemeMode.DARK) {
        TopBar(
            uiContent = previewTopBarContent(),
            uiConfig = TopBarUiConfig(),
            uiStyle = previewTopBarStyle(),
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun TopBarPreviewLightMode() {
    MindgameTheme(mode = AppThemeMode.LIGHT) {
        TopBar(
            uiContent = previewTopBarContent(),
            uiConfig = TopBarUiConfig(),
            uiStyle = previewTopBarStyle(),
        )
    }
}

/** 长标题 + 单行省略：验证标题在窄屏下的截断表现。 */
@Preview(showBackground = true, widthDp = 320)
@Composable
fun TopBarPreviewLongTitle() {
    MindgameTheme(mode = AppThemeMode.DARK) {
        TopBar(
            uiContent = previewTopBarContent().copy(
                topBarTitle = "9×9 · Classic · Very Long Difficulty Name",
            ),
            uiConfig = TopBarUiConfig(
                titleConfig = TextConfig(maxLine = 1, overflow = TextOverflow.Ellipsis),
            ),
            uiStyle = previewTopBarStyle(),
        )
    }
}

/** 仅标题：隐藏返回与设置按钮，验证槽位可见性开关。 */
@Preview(showBackground = true, widthDp = 360)
@Composable
fun TopBarPreviewTitleOnly() {
    MindgameTheme(mode = AppThemeMode.DARK) {
        TopBar(
            uiContent = previewTopBarContent(),
            uiConfig = TopBarUiConfig(
                backButtonConfig = IconButtonConfig(visibility = VisibilityConfig.HIDDEN),
                settingsButtonConfig = IconButtonConfig(visibility = VisibilityConfig.HIDDEN),
            ),
            uiStyle = previewTopBarStyle(),
        )
    }
}
