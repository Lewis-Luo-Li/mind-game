package com.idroid.stuido.mind_game.sudoku

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.R
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.core.logic.config.GameRemoteConfig
import com.idroid.stuido.mind_game.core.ui.components.button.Button
import com.idroid.stuido.mind_game.core.ui.components.button.ButtonConfig
import com.idroid.stuido.mind_game.core.ui.components.button.ButtonStyle
import com.idroid.stuido.mind_game.core.ui.components.button.ButtonUiContent
import com.idroid.stuido.mind_game.core.ui.components.button.ButtonVariant
import com.idroid.stuido.mind_game.core.ui.components.modecard.ModeCard
import com.idroid.stuido.mind_game.core.ui.components.modecard.ModeCardConfig
import com.idroid.stuido.mind_game.core.ui.components.modecard.ModeCardStyle
import com.idroid.stuido.mind_game.core.ui.components.modecard.ModeCardUiContent
import com.idroid.stuido.mind_game.core.ui.components.progress.CycleProgressDots
import com.idroid.stuido.mind_game.core.ui.components.progress.CycleProgressDotsConfig
import com.idroid.stuido.mind_game.core.ui.components.progress.CycleProgressDotsStyle
import com.idroid.stuido.mind_game.core.ui.components.progress.CycleProgressDotsUiContent
import com.idroid.stuido.mind_game.core.ui.components.text.BaseText
import com.idroid.stuido.mind_game.core.ui.theme.AppThemeMode
import com.idroid.stuido.mind_game.core.ui.theme.MindgameTheme
import com.idroid.stuido.mind_game.core.ui.theme.Theme
import kotlinx.coroutines.delay
import java.time.Clock
import java.time.Duration
import java.time.LocalDate

internal object HomeTestTags {
    const val DAILY = "home_daily_card"
    const val LADDER = "home_ladder_card"
}

@Composable
internal fun HomeRoute(
    gameConfig: GameRemoteConfig,
    progressStore: PlayerProgressStore,
    onPlayDaily: () -> Unit,
    onPlayLevel: (level: Int) -> Unit,
    onOpenSettings: () -> Unit,
    clock: Clock = Clock.systemUTC(),
) {
    val now by produceState(clock.instant(), clock) {
        while (true) {
            delay(1_000L - clock.millis() % 1_000L)
            value = clock.instant()
        }
    }
    val state = homeUiState(gameConfig, progressStore.ladderLevel, progressStore.dailySolvedDay, now)
    HomeScreen(
        state = state,
        onPlayDaily = onPlayDaily,
        onPlayLevel = { state.ladder?.let { onPlayLevel(it.level) } },
        onOpenSettings = onOpenSettings,
    )
}

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onPlayDaily: () -> Unit,
    onPlayLevel: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val cardStyle = homeCardStyle()
    val buttonStyle = homeButtonStyle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.home_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.home_subtitle), style = MaterialTheme.typography.bodyMedium)

        state.daily?.let { daily -> DailyCard(daily, cardStyle, buttonStyle, onPlayDaily) }
        state.ladder?.let { ladder -> LadderCard(ladder, cardStyle, buttonStyle, onPlayLevel) }

        OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_settings))
        }
    }
}

@Composable
private fun DailyCard(
    daily: DailyCardState,
    style: ModeCardStyle,
    buttonStyle: ButtonStyle,
    onPlay: () -> Unit,
) {
    val countdown = formatCountdown(daily.untilReset)
    val colors = MaterialTheme.colorScheme
    ModeCard(
        modifier = Modifier.testTag(HomeTestTags.DAILY),
        uiContent = ModeCardUiContent(
            icon = if (daily.solved) Theme.icons.done else Theme.icons.daily,
            title = stringResource(R.string.home_daily_title),
            subtitle = stringResource(
                R.string.home_daily_subtitle,
                daily.boardSize,
                daily.difficulty.label(),
            ),
        ),
        uiConfig = ModeCardConfig(),
        uiStyle = style.copy(iconTint = if (daily.solved) colors.tertiary else colors.primary),
        content = {
            BaseText(
                text = AnnotatedString(
                    stringResource(
                        if (daily.solved) R.string.home_daily_solved else R.string.home_daily_ends_in,
                        countdown,
                    ),
                ),
                color = style.secondaryContentColor,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        action = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                uiContent = ButtonUiContent(
                    text = stringResource(if (daily.solved) R.string.home_play_again else R.string.home_play),
                    onClick = onPlay,
                ),
                uiConfig = ButtonConfig(
                    variant = if (daily.solved) ButtonVariant.OUTLINED else ButtonVariant.FILLED,
                ),
                uiStyle = buttonStyle,
            )
        },
    )
}

@Composable
private fun LadderCard(
    ladder: LadderCardState,
    style: ModeCardStyle,
    buttonStyle: ButtonStyle,
    onPlay: () -> Unit,
) {
    val cleared = ladder.positionInRound - 1
    ModeCard(
        modifier = Modifier.testTag(HomeTestTags.LADDER),
        uiContent = ModeCardUiContent(
            icon = Theme.icons.ladder,
            title = stringResource(R.string.home_level_title, ladder.level),
            subtitle = stringResource(R.string.home_level_subtitle, ladder.difficulty.label(), ladder.round),
        ),
        uiConfig = ModeCardConfig(),
        uiStyle = style,
        content = {
            CycleProgressDots(
                uiContent = CycleProgressDotsUiContent(
                    total = ladder.roundSize,
                    completed = cleared,
                    contentDescription = stringResource(
                        R.string.home_level_progress_desc,
                        cleared,
                        ladder.roundSize,
                    ),
                ),
                uiConfig = CycleProgressDotsConfig(),
                uiStyle = CycleProgressDotsStyle(
                    completedColor = MaterialTheme.colorScheme.primary,
                    currentColor = MaterialTheme.colorScheme.tertiary,
                    pendingColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )
            BaseText(
                text = AnnotatedString(
                    stringResource(R.string.home_level_progress, ladder.positionInRound, ladder.roundSize),
                ),
                color = style.secondaryContentColor,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        action = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                uiContent = ButtonUiContent(
                    text = stringResource(R.string.home_play),
                    onClick = onPlay,
                ),
                uiConfig = ButtonConfig(variant = ButtonVariant.FILLED),
                uiStyle = buttonStyle,
            )
        },
    )
}

@Composable
private fun homeCardStyle() = ModeCardStyle(
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor = MaterialTheme.colorScheme.onSurface,
    secondaryContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    iconTint = MaterialTheme.colorScheme.primary,
    titleStyle = MaterialTheme.typography.titleLarge,
    subtitleStyle = MaterialTheme.typography.bodyMedium,
)

@Composable
private fun homeButtonStyle() = ButtonStyle(
    filledContainerColor = MaterialTheme.colorScheme.primary,
    filledContentColor = MaterialTheme.colorScheme.onPrimary,
    outlinedContainerColor = MaterialTheme.colorScheme.surface,
    outlinedContentColor = MaterialTheme.colorScheme.onSurface,
    outlinedBorderColor = MaterialTheme.colorScheme.outline,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
    textStyle = MaterialTheme.typography.labelLarge,
)

private val previewDailyOpen = DailyCardState(
    difficulty = Difficulty.HARD,
    boardSize = 9,
    puzzleDay = LocalDate.of(2026, 10, 25),
    solved = false,
    untilReset = Duration.ofSeconds(3 * 3600 + 12 * 60 + 45),
)

@Preview(showBackground = true, name = "Home · new player", widthDp = 360, heightDp = 720)
@Composable
private fun HomeScreenNewPlayerPreview() {
    MindgameTheme {
        HomeScreen(
            state = HomeUiState(
                daily = previewDailyOpen,
                ladder = LadderCardState(
                    level = 1,
                    difficulty = Difficulty.EASY,
                    round = 1,
                    positionInRound = 1,
                    roundSize = 20,
                ),
            ),
            onPlayDaily = {},
            onPlayLevel = {},
            onOpenSettings = {},
        )
    }
}

@Preview(showBackground = true, name = "Home · daily solved, round 2 (dark)", widthDp = 360, heightDp = 720)
@Composable
private fun HomeScreenSolvedPreview() {
    MindgameTheme(mode = AppThemeMode.DARK) {
        HomeScreen(
            state = HomeUiState(
                daily = previewDailyOpen.copy(solved = true),
                ladder = LadderCardState(
                    level = 23,
                    difficulty = Difficulty.MEDIUM,
                    round = 2,
                    positionInRound = 3,
                    roundSize = 20,
                ),
            ),
            onPlayDaily = {},
            onPlayLevel = {},
            onOpenSettings = {},
        )
    }
}

@Preview(showBackground = true, name = "Home · small screen", widthDp = 320, heightDp = 568, fontScale = 1.3f)
@Composable
private fun HomeScreenSmallPreview() {
    MindgameTheme {
        HomeScreen(
            state = HomeUiState(
                daily = previewDailyOpen,
                ladder = LadderCardState(
                    level = 16,
                    difficulty = Difficulty.EXPERT,
                    round = 1,
                    positionInRound = 16,
                    roundSize = 20,
                ),
            ),
            onPlayDaily = {},
            onPlayLevel = {},
            onOpenSettings = {},
        )
    }
}
