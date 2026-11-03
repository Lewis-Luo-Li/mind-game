package com.idroid.stuido.mind_game.sudoku

import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.core.logic.config.GameRemoteConfig
import com.idroid.stuido.mind_game.core.logic.mode.DailySchedule
import com.idroid.stuido.mind_game.core.logic.mode.LadderPlan
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.Locale

internal data class HomeUiState(
    val daily: DailyCardState?,
    val ladder: LadderCardState?,
)

internal data class DailyCardState(
    val difficulty: Difficulty,
    val boardSize: Int,
    val puzzleDay: LocalDate,
    val solved: Boolean,
    val untilReset: Duration,
)

internal data class LadderCardState(
    val level: Int,
    val difficulty: Difficulty,
    val round: Int,
    val positionInRound: Int,
    val roundSize: Int,
)

internal fun homeUiState(
    config: GameRemoteConfig,
    ladderLevel: Int,
    dailySolvedDay: LocalDate?,
    now: Instant,
): HomeUiState {
    val dailySection = config.sudoku.daily
    val daily = if (dailySection.enabled) {
        val schedule = DailySchedule.from(dailySection)
        val day = schedule.puzzleDay(now)
        DailyCardState(
            difficulty = dailySection.difficultyLevel(),
            boardSize = dailySection.boardSpec().size,
            puzzleDay = day,
            solved = dailySolvedDay == day,
            untilReset = Duration.between(now, schedule.nextResetAt(now)),
        )
    } else {
        null
    }

    val ladderSection = config.sudoku.ladder
    val ladder = if (ladderSection.enabled) {
        val plan = LadderPlan(ladderSection)
        val level = ladderLevel.coerceAtLeast(1)
        LadderCardState(
            level = level,
            difficulty = plan.difficultyFor(level),
            round = plan.roundOf(level),
            positionInRound = plan.positionInRound(level),
            roundSize = plan.roundSize,
        )
    } else {
        null
    }
    return HomeUiState(daily = daily, ladder = ladder)
}

internal fun formatCountdown(remaining: Duration): String {
    val total = remaining.seconds.coerceAtLeast(0)
    return String.format(Locale.ROOT, "%02d:%02d:%02d", total / 3600, total % 3600 / 60, total % 60)
}

internal fun Difficulty.label(): String = name.lowercase().replaceFirstChar { it.uppercase() }
