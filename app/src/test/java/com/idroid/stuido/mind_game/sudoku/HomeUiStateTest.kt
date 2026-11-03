package com.idroid.stuido.mind_game.sudoku

import com.idroid.stuido.mind_game.config.GameConfigRepository
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

class HomeUiStateTest {

    private val config = GameConfigRepository(
        loadJson = { File("src/main/assets/game_config.json").readText() },
    ).config

    private val beforeReset = Instant.parse("2026-10-25T05:00:00Z")
    private val atReset = Instant.parse("2026-10-25T06:00:00Z")

    @Test
    fun `daily card counts down to the next 5pm sydney reset`() {
        val daily = homeUiState(config, 1, null, beforeReset).daily!!
        assertEquals(LocalDate.of(2026, 10, 24), daily.puzzleDay)
        assertEquals(Difficulty.HARD, daily.difficulty)
        assertEquals(Duration.ofHours(1), daily.untilReset)
        assertFalse(daily.solved)
    }

    @Test
    fun `solved daily flips back to open at the reset`() {
        val solvedDay = LocalDate.of(2026, 10, 24)
        assertTrue(homeUiState(config, 1, solvedDay, beforeReset).daily!!.solved)
        val after = homeUiState(config, 1, solvedDay, atReset).daily!!
        assertFalse(after.solved)
        assertEquals(LocalDate.of(2026, 10, 25), after.puzzleDay)
        assertEquals(Duration.ofHours(24), after.untilReset)
    }

    @Test
    fun `ladder card shows level round and position`() {
        val first = homeUiState(config, 1, null, atReset).ladder!!
        assertEquals(LadderCardState(1, Difficulty.EASY, 1, 1, 20), first)

        val round2 = homeUiState(config, 23, null, atReset).ladder!!
        assertEquals(23, round2.level)
        assertEquals(2, round2.round)
        assertEquals(3, round2.positionInRound)
        assertEquals(20, round2.roundSize)
    }

    @Test
    fun `invalid stored level is clamped to level 1`() {
        assertEquals(1, homeUiState(config, 0, null, atReset).ladder!!.level)
    }

    @Test
    fun `disabled modes have no card`() {
        val off = config.copy(
            sudoku = config.sudoku.copy(
                daily = config.sudoku.daily.copy(enabled = false),
                ladder = config.sudoku.ladder.copy(enabled = false),
            ),
        )
        val state = homeUiState(off, 5, null, atReset)
        assertNull(state.daily)
        assertNull(state.ladder)
    }

    @Test
    fun `countdown formats as hh mm ss and never goes negative`() {
        assertEquals("03:12:45", formatCountdown(Duration.ofSeconds(3 * 3600 + 12 * 60 + 45)))
        assertEquals("24:00:00", formatCountdown(Duration.ofHours(24)))
        assertEquals("00:00:00", formatCountdown(Duration.ofSeconds(-5)))
    }
}
