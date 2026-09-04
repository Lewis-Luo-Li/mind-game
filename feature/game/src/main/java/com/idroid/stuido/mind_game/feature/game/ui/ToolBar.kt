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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.feature.game.GameStatus
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
    Surface(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            ToolButton(label = if (noteMode) "笔记✓" else "笔记", enabled = canAct || true) { onNoteMode() }
            ToolButton(label = "橡皮", enabled = canAct) { onErase() }
            ToolButton(label = "提示", enabled = canAct) { onHint() }
            ToolButton(label = "判错", enabled = canAct) { onCheck() }
            ToolButton(label = "求解", enabled = canAct) { onSolve() }
            ToolButton(label = if (isPaused) "继续" else "暂停", enabled = true) { onPause() }
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
