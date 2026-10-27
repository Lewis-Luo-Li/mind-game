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
import com.idroid.stuido.mind_game.feature.game.GameOutcome
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
 * Game outcome summary and replay actions.
 */
@Composable
fun FinishPanel(
    outcome: GameOutcome,
    timerSeconds: Int,
    mistakes: Int,
    maxMistakes: Int,
    hintsUsed: Int,
    maxHints: Int,
    watchAdLabel: String? = null,
    onWatchAd: (() -> Unit)? = null,
    onRetrySamePuzzle: () -> Unit,
    onNewGame: () -> Unit,
    onExit: () -> Unit,
    /** 天梯解出后非 null：主按钮改为「下一关」。 */
    onNextLevel: (() -> Unit)? = null,
    /** 每日/天梯题目固定，「新游戏」无意义，传 false 隐藏。 */
    allowNewGame: Boolean = true,
    /** 结算附注（如每日挑战的下一期倒计时）。 */
    footnote: String? = null,
) {
    val title = when (outcome) {
        GameOutcome.SOLVED -> stringResource(R.string.finish_victory)
        GameOutcome.FAILED -> stringResource(R.string.finish_out_of_lives)
        GameOutcome.REVEALED -> stringResource(R.string.finish_revealed)
    }
    val subtitle = when (outcome) {
        GameOutcome.SOLVED -> null
        GameOutcome.FAILED -> stringResource(R.string.finish_out_of_lives_body, maxMistakes)
        GameOutcome.REVEALED -> stringResource(R.string.finish_revealed_body)
    }
    val minutes = timerSeconds / 60
    val seconds = timerSeconds % 60
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center,
    ) {
        CenteredCard {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            if (subtitle != null) {
                Spacer(Modifier.height(8.dp))
                Text(subtitle)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(
                    R.string.finish_meta,
                    minutes,
                    seconds,
                    mistakes,
                    maxMistakes,
                    hintsUsed,
                    maxHints,
                ),
            )
            if (footnote != null) {
                Spacer(Modifier.height(8.dp))
                Text(footnote, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(16.dp))
            // 命数用尽且奖励可领：「看广告 +N 命」主按钮，复活后继续本局。
            if (outcome == GameOutcome.FAILED && watchAdLabel != null && onWatchAd != null) {
                Button(onClick = onWatchAd) { Text(watchAdLabel) }
                Spacer(Modifier.height(4.dp))
            }
            if (outcome == GameOutcome.SOLVED) {
                when {
                    onNextLevel != null ->
                        Button(onClick = onNextLevel) { Text(stringResource(R.string.action_next_level)) }
                    allowNewGame ->
                        Button(onClick = onNewGame) { Text(stringResource(R.string.action_new_game)) }
                }
            } else {
                Button(onClick = onRetrySamePuzzle) { Text(stringResource(R.string.action_retry_same)) }
                if (allowNewGame) {
                    TextButton(onClick = onNewGame) { Text(stringResource(R.string.action_new_game)) }
                }
            }
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
