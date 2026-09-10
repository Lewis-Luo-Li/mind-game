package com.idroid.stuido.mind_game.feature.game.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.feature.game.R

/** 顶部信息条：规格/难度描述、计时、错误计数，以及退出动作。 */
@Composable
fun GameTopBar(
    title: String,
    timerSeconds: Int,
    mistakes: Int,
    maxMistakes: Int,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val minutes = timerSeconds / 60
    val seconds = timerSeconds % 60
    Surface(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onExit) { Text(stringResource(R.string.action_exit)) }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.game_topbar_time, minutes, seconds), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.game_topbar_mistakes, mistakes, maxMistakes), style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.width(4.dp))
        }
    }
}
