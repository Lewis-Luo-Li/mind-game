package com.idroid.stuido.mind_game.sudoku

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.R

/**
 * 尚未实现的游戏占位屏。
 *
 * Crossword / Zooduku 目前无玩法，由 [GameHost] 路由到此，仅显示标题与"敬请期待"，
 * 并提供返回入口回到主菜单。后续替换为各自真正的开局/对局流程即可。
 *
 * @param title 游戏名称（用于标题展示）。
 * @param onBack 返回上一页（弹栈）。
 */
@Composable
fun ComingSoonScreen(
    title: String,
    onBack: () -> Unit,
) {
    // 系统栏内边距由上层（MindGameApp 的 safeDrawing 容器）统一处理。
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
            Text(title, style = MaterialTheme.typography.headlineMedium)
        }
        Text(
            stringResource(R.string.coming_soon_message),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
