package com.idroid.stuido.mind_game.feature.game.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.feature.game.GameStatus
import com.idroid.stuido.mind_game.feature.game.R
import com.idroid.stuido.mind_game.feature.game.mvi.InputMode

/**
 * 底部第二节：快捷工具行（笔记 / 橡皮 / 提示 / 判错 / 求解 / 暂停）。
 * 一律以回调上抛，由宿主派发 MVI 事件。
 */
@Composable
fun GameToolBar(
    noteMode: Boolean,
    isPaused: Boolean,
    canAct: Boolean,
    onNoteMode: () -> Unit,
    onErase: () -> Unit,
    onHint: () -> Unit,
    onCheck: () -> Unit,
    onSolve: () -> Unit,
    onPause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val notesLabel = if (noteMode) stringResource(R.string.tool_notes_active) else stringResource(R.string.tool_notes)
    val pauseLabel = if (isPaused) stringResource(R.string.action_resume) else stringResource(R.string.action_pause)

    Surface(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            ToolButton(label = notesLabel, enabled = true) { onNoteMode() }
            ToolButton(label = stringResource(R.string.tool_erase), enabled = canAct) { onErase() }
            ToolButton(label = stringResource(R.string.tool_hint), enabled = canAct) { onHint() }
            ToolButton(label = stringResource(R.string.tool_check), enabled = canAct) { onCheck() }
            ToolButton(label = stringResource(R.string.tool_solve), enabled = canAct) { onSolve() }
            ToolButton(label = pauseLabel, enabled = true) { onPause() }
        }
    }
}

@Composable
private fun ToolButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.heightIn(min = 34.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
    }
}
