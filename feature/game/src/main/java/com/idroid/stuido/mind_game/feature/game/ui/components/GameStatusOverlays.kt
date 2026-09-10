package com.idroid.stuido.mind_game.feature.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.feature.game.R

/** 暂停覆盖层。 */
@Composable
fun PauseScrim(onResume: () -> Unit, onExit: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        CenteredCard {
            Text(stringResource(R.string.pause_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onResume) { Text(stringResource(R.string.action_resume)) }
            TextButton(onClick = onExit) { Text(stringResource(R.string.action_exit)) }
        }
    }
}

/**
 * 完成 / 失败收尾面板。
 *
 * @param victory true 表示解题成功；false 表示错误用尽失败。
 */
@Composable
fun FinishPanel(
    victory: Boolean,
    mistakes: Int,
    timerSeconds: Int,
    onRetry: () -> Unit,
    onExit: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center,
    ) {
        CenteredCard {
            Text(
                text = if (victory) stringResource(R.string.finish_victory)
                else stringResource(R.string.finish_failed),
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.finish_meta, timerSeconds, mistakes))
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text(stringResource(R.string.finish_play_again)) }
            TextButton(onClick = onExit) { Text(stringResource(R.string.action_exit)) }
        }
    }
}

/** 居中白色卡片容器。 */
@Composable
private fun CenteredCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()
    }
}
