package com.idroid.stuido.mind_game.sudoku

import com.idroid.stuido.mind_game.config.GameConfigRepository
import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.core.logic.mode.LadderPlan
import com.idroid.stuido.mind_game.feature.game.GameMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.io.File
import java.time.Instant
import java.time.LocalDate

class PlayRequestsTest {

    private val config = GameConfigRepository(
        loadJson = { File("src/main/assets/game_config.json").readText() },
    ).config

    @Test
    fun `daily request uses the sydney puzzle day and hard classic`() {
        val beforeReset = dailyPlayRequest(1, config, Instant.parse("2026-10-25T05:59:00Z"))
        val afterReset = dailyPlayRequest(2, config, Instant.parse("2026-10-25T06:00:00Z"))

        assertEquals(GameMode.DAILY, beforeReset.mode)
        assertEquals(BoardSpec.CLASSIC, beforeReset.spec)
        assertEquals(Difficulty.HARD, beforeReset.difficulty)
        assertEquals(LocalDate.of(2026, 10, 24), beforeReset.puzzleDay)
        assertEquals(LocalDate.of(2026, 10, 25), afterReset.puzzleDay)
        assertEquals(LocalDate.of(2026, 10, 24).toEpochDay(), beforeReset.seed)
        assertNotEquals(beforeReset.seed, afterReset.seed)
    }

    @Test
    fun `ladder request follows the ladder plan`() {
        val plan = LadderPlan(config.sudoku.ladder)
        for (level in listOf(1, 6, 16, 21, 37)) {
            val request = ladderPlayRequest(level.toLong(), config, level)
            assertEquals(GameMode.LADDER, request.mode)
            assertEquals(level, request.level)
            assertEquals(plan.difficultyFor(level), request.difficulty)
            assertEquals(plan.seedFor(level), request.seed)
            assertEquals(BoardSpec.CLASSIC, request.spec)
        }
        assertEquals(Difficulty.EASY, ladderPlayRequest(1, config, 1).difficulty)
        assertEquals(Difficulty.EXPERT, ladderPlayRequest(1, config, 20).difficulty)
    }

    @Test
    fun `ladder holes step applies to later rounds`() {
        val stepped = config.copy(
            sudoku = config.sudoku.copy(ladder = config.sudoku.ladder.copy(holesStepPerRound = 2)),
        )
        val base = stepped.toDifficultyConfig()
        val round1 = ladderPlayRequest(1, stepped, 1)
        val round2 = ladderPlayRequest(2, stepped, 21)

        assertEquals(base.holeCount(round1.spec, round1.difficulty), round1.holes)
        assertEquals(base.holeCount(round2.spec, round2.difficulty)!! + 2, round2.holes)
        assertEquals(
            round2.holes,
            round2.difficultyConfigFor(base).holeCount(round2.spec, round2.difficulty),
        )
    }

    @Test
    fun `requests without holes keep the base difficulty config`() {
        val base = config.toDifficultyConfig()
        val daily = dailyPlayRequest(1, config, Instant.parse("2026-10-25T06:00:00Z"))
        assertEquals(base, daily.difficultyConfigFor(base))
    }
}
