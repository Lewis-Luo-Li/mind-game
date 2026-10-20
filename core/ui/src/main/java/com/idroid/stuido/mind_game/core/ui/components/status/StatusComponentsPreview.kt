package com.idroid.stuido.mind_game.core.ui.components.status

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.components.button.IconButtonStyle
import com.idroid.stuido.mind_game.core.ui.components.button.IconButtonUiContent
import com.idroid.stuido.mind_game.core.ui.components.topbar.TopBar
import com.idroid.stuido.mind_game.core.ui.components.topbar.TopBarUiConfig
import com.idroid.stuido.mind_game.core.ui.components.topbar.TopBarUiContent
import com.idroid.stuido.mind_game.core.ui.components.topbar.TopBarUiStyle
import com.idroid.stuido.mind_game.core.ui.theme.MindgameTheme
import com.idroid.stuido.mind_game.core.ui.theme.Theme

@Composable
private fun previewChipStyle() = StatusChipStyle(
    iconTintColor = MaterialTheme.colorScheme.onSurfaceVariant,
    textColor = MaterialTheme.colorScheme.onSurface,
    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
    iconSize = 18.dp,
    textStyle = MaterialTheme.typography.labelLarge,
    secondaryTextStyle = MaterialTheme.typography.labelSmall,
)

@Composable
private fun previewRowStyle() = StatusInfoRowStyle(
    zoneColor = MaterialTheme.colorScheme.surface,
    chipStyle = previewChipStyle(),
)

/** 单个状态项：可点的计时器。 */
@Preview(showBackground = true, name = "StatusChip · timer", widthDp = 200)
@Composable
fun StatusChipPreviewTimer() {
    MindgameTheme {
        StatusChip(
            uiContent = StatusChipUiContent(
                icon = Theme.icons.timer,
                contentDescription = "Timer",
                text = "04:18",
                onClick = {},
            ),
            uiConfig = StatusChipConfig(),
            uiStyle = previewChipStyle(),
        )
    }
}

/** 禁用态状态项：图标与文本置灰。 */
@Preview(showBackground = true, name = "StatusChip · disabled", widthDp = 200)
@Composable
fun StatusChipPreviewDisabled() {
    MindgameTheme {
        StatusChip(
            uiContent = StatusChipUiContent(
                icon = Theme.icons.error,
                contentDescription = "Errors",
                text = "Errors: 3/3",
                onClick = {},
            ),
            uiConfig = StatusChipConfig(enabled = false),
            uiStyle = previewChipStyle(),
        )
    }
}

/** 完整状态行：计时（可点）· 错误 · 提示，三项均匀分布。 */
@Preview(showBackground = true, name = "StatusInfoRow · 3 items", widthDp = 400)
@Composable
fun StatusInfoRowPreview() {
    MindgameTheme {
        StatusInfoRow(
            modifier = Modifier.fillMaxWidth(),
            uiContent = StatusInfoRowUiContent(
                items = listOf(
                    StatusChipUiContent(
                        icon = Theme.icons.timer,
                        contentDescription = "Timer",
                        text = "04:18",
                        onClick = {},
                    ),
                    StatusChipUiContent(
                        icon = Theme.icons.error,
                        contentDescription = "Errors",
                        text = "Errors: 1/3",
                    ),
                    StatusChipUiContent(
                        icon = Theme.icons.hint,
                        contentDescription = "Hints",
                        text = "Hints: 3/3",
                    ),
                ),
            ),
            uiConfig = StatusInfoRowConfig(),
            uiStyle = previewRowStyle(),
        )
    }
}

/** 与共享 TopBar 组合后的 mockup 顶部：导航条 + 状态行两个分区。 */
@Preview(showBackground = true, name = "TopBar + StatusInfoRow", widthDp = 400)
@Composable
fun TopBarStatusRowPreview() {
    MindgameTheme {
        val iconStyle = IconButtonStyle(
            surfaceColor = MaterialTheme.colorScheme.surface,
            iconTintColor = MaterialTheme.colorScheme.onSurface,
            surfaceSize = 48.dp,
            iconSize = 24.dp,
        )
        Column {
            TopBar(
                uiContent = TopBarUiContent(
                    topBarTitle = "Classic Sudoku · Expert",
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
                ),
                uiConfig = TopBarUiConfig(),
                uiStyle = TopBarUiStyle(
                    topbarBgColor = MaterialTheme.colorScheme.surface,
                    titleTextColor = MaterialTheme.colorScheme.onSurface,
                    backButtonStyle = iconStyle,
                    settingsButtonStyle = iconStyle,
                ),
            )
            StatusInfoRow(
                uiContent = StatusInfoRowUiContent(
                    items = listOf(
                        StatusChipUiContent(
                            icon = Theme.icons.timer,
                            contentDescription = "Timer",
                            text = "04:18",
                            onClick = {},
                        ),
                        StatusChipUiContent(
                            icon = Theme.icons.error,
                            contentDescription = "Errors",
                            text = "Errors: 1/3",
                        ),
                        StatusChipUiContent(
                            icon = Theme.icons.hint,
                            contentDescription = "Hints",
                            text = "Hints: 3/3",
                        ),
                    ),
                ),
                uiConfig = StatusInfoRowConfig(),
                uiStyle = previewRowStyle(),
            )
        }
    }
}
