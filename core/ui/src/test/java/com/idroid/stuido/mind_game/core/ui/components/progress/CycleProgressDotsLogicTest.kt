package com.idroid.stuido.mind_game.core.ui.components.progress

import org.junit.Assert.assertEquals
import org.junit.Test

class CycleProgressDotsLogicTest {

    @Test
    fun `non-positive total has no dots`() {
        assertEquals(emptyList<DotState>(), cycleDotStates(0, 0, true))
        assertEquals(emptyList<DotState>(), cycleDotStates(-3, 0, true))
    }

    @Test
    fun `negative completed count highlights first dot`() {
        assertEquals(
            listOf(DotState.CURRENT, DotState.PENDING, DotState.PENDING),
            cycleDotStates(3, -3, true),
        )
    }

    @Test
    fun `completed levels current level and remaining levels are distinguished`() {
        val states = cycleDotStates(20, 7, true)
        assertEquals(7, states.count { it == DotState.COMPLETED })
        assertEquals(DotState.CURRENT, states[7])
        assertEquals(12, states.count { it == DotState.PENDING })
    }

    @Test
    fun `completed count above total marks every dot completed`() {
        assertEquals(
            List(20) { DotState.COMPLETED },
            cycleDotStates(20, 25, true),
        )
    }

    @Test
    fun `current dot is not highlighted when disabled`() {
        assertEquals(
            listOf(DotState.COMPLETED, DotState.PENDING, DotState.PENDING),
            cycleDotStates(3, 1, false),
        )
    }
}
