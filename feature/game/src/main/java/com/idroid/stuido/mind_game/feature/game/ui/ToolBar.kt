package com.idroid.stuido.mind_game.feature.game.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.components.button.LabeledCircleButton
import com.idroid.stuido.mind_game.core.ui.components.button.LabeledCircleButtonConfig
import com.idroid.stuido.mind_game.core.ui.components.button.LabeledCircleButtonStyle
import com.idroid.stuido.mind_game.core.ui.components.button.LabeledCircleButtonUiContent
import com.idroid.stuido.mind_game.core.ui.theme.Theme
import com.idroid.stuido.mind_game.feature.game.R

/**
 * 底部第二节：快捷工具行（Undo / Notes / Erase / Hint），对齐 docs/sudoku_ui.jpeg。
 * 圆形图标钮 + 文字标签，均匀排开；Check 已移除、Pause 改为点击计时器（D4）。
 * Solve 仅在 debug（showDebugSolve）显示。一律以回调上抛，由宿主派发 MVI 事件。
 */
@Composable
fun GameToolBar(
    canUndo: Boolean,
    noteMode: Boolean,
    canErase: Boolean,
    hintsLeft: Int,
    showDebugSolve: Boolean,
    onUndo: () -> Unit,
    onNoteMode: () -> Unit,
    onErase: () -> Unit,
    onHint: () -> Unit,
    onSolve: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val buttonStyle = LabeledCircleButtonStyle(
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

    Surface(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabeledCircleButton(
                uiContent = LabeledCircleButtonUiContent(
                    icon = Theme.icons.undo,
                    contentDescription = stringResource(R.string.tool_undo),
                    label = stringResource(R.string.tool_undo),
                    onClick = onUndo,
                ),
                uiConfig = LabeledCircleButtonConfig(enabled = canUndo),
                uiStyle = buttonStyle,
            )
            LabeledCircleButton(
                uiContent = LabeledCircleButtonUiContent(
                    icon = Theme.icons.notes,
                    contentDescription = stringResource(R.string.tool_notes),
                    label = stringResource(R.string.tool_notes),
                    badge = if (noteMode) stringResource(R.string.tool_badge_on) else null,
                    onClick = onNoteMode,
                ),
                uiConfig = LabeledCircleButtonConfig(active = noteMode),
                uiStyle = buttonStyle,
            )
            LabeledCircleButton(
                uiContent = LabeledCircleButtonUiContent(
                    icon = Theme.icons.erase,
                    contentDescription = stringResource(R.string.tool_erase),
                    label = stringResource(R.string.tool_erase),
                    onClick = onErase,
                ),
                uiConfig = LabeledCircleButtonConfig(enabled = canErase),
                uiStyle = buttonStyle,
            )
            LabeledCircleButton(
                uiContent = LabeledCircleButtonUiContent(
                    icon = Theme.icons.hint,
                    contentDescription = stringResource(R.string.tool_hint),
                    label = stringResource(R.string.tool_hint),
                    badge = hintsLeft.toString(),
                    onClick = onHint,
                ),
                uiConfig = LabeledCircleButtonConfig(enabled = hintsLeft > 0),
                uiStyle = buttonStyle,
            )
            if (showDebugSolve) {
                TextButton(onClick = onSolve) {
                    Text(stringResource(R.string.tool_solve))
                }
            }
        }
    }
}
