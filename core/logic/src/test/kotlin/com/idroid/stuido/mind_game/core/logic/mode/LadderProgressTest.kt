package com.idroid.stuido.mind_game.core.logic.mode

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LadderProgressTest {

    @Test
    fun `solving the current level unlocks the next`() {
        assertEquals(2, LadderProgress.afterGame(unlockedLevel = 1, playedLevel = 1, solved = true))
    }

    @Test
    fun `failing or revealing does not advance`() {
        assertEquals(5, LadderProgress.afterGame(unlockedLevel = 5, playedLevel = 5, solved = false))
    }

    @Test
    fun `replaying an earlier level does not advance`() {
        assertEquals(5, LadderProgress.afterGame(unlockedLevel = 5, playedLevel = 3, solved = true))
    }

    @Test
    fun `only unlocked levels can be played`() {
        assertTrue(LadderProgress.canPlay(unlockedLevel = 5, level = 5))
        assertTrue(LadderProgress.canPlay(unlockedLevel = 5, level = 1))
        assertFalse(LadderProgress.canPlay(unlockedLevel = 5, level = 6))
        assertFalse(LadderProgress.canPlay(unlockedLevel = 5, level = 0))
    }
}
