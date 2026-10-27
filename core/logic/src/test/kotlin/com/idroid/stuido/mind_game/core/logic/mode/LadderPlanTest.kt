package com.idroid.stuido.mind_game.core.logic.mode

import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.core.logic.config.Difficulty.EASY
import com.idroid.stuido.mind_game.core.logic.config.Difficulty.EXPERT
import com.idroid.stuido.mind_game.core.logic.config.Difficulty.HARD
import com.idroid.stuido.mind_game.core.logic.config.Difficulty.MEDIUM
import com.idroid.stuido.mind_game.core.logic.config.DifficultyConfig
import com.idroid.stuido.mind_game.core.logic.config.LadderSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LadderPlanTest {

    private val plan = LadderPlan(LadderSection())

    @Test
    fun `round 1 is ordered easy to expert with five levels each`() {
        assertEquals(BoardSpec.CLASSIC, plan.spec)
        assertEquals(20, plan.roundSize)
        val round1 = (1..20).map(plan::difficultyFor)
        assertEquals(
            List(5) { EASY } + List(5) { MEDIUM } + List(5) { HARD } + List(5) { EXPERT },
            round1,
        )
        assertEquals(1, plan.roundOf(20))
        assertEquals(2, plan.roundOf(21))
        assertEquals(1, plan.positionInRound(21))
        assertEquals(20, plan.positionInRound(40))
    }

    @Test
    fun `shuffled rounds keep five of each tier and respect the expert rules`() {
        for (round in 2..500) {
            val order = plan.roundOrder(round)
            Difficulty.entries.forEach { tier -> assertEquals(5, order.count { it == tier }) }
            assertNotEquals(EXPERT, order.first())
            assertNotEquals(EXPERT, order.last())
            var run = 0
            order.forEach {
                run = if (it == EXPERT) run + 1 else 0
                assertTrue("round $round has an expert run > 2: $order", run <= 2)
            }
        }
    }

    @Test
    fun `shuffles are deterministic and vary between rounds`() {
        assertEquals(plan.roundOrder(7), LadderPlan(LadderSection()).roundOrder(7))
        val distinctOrders = (2..50).map(plan::roundOrder).toSet()
        assertTrue(distinctOrders.size > 40)
        assertNotEquals(plan.roundOrder(2), plan.roundOrder(1))
    }

    @Test
    fun `one level per tier without shuffle cycles every four levels`() {
        val simple = LadderPlan(LadderSection(levelsPerTier = 1, shuffleFromRound = Int.MAX_VALUE))
        assertEquals(listOf(EASY, MEDIUM, HARD, EXPERT, EASY, MEDIUM), (1..6).map(simple::difficultyFor))
    }

    @Test
    fun `level seeds are stable, unique and never in the daily seed range`() {
        assertEquals(plan.seedFor(12), LadderPlan(LadderSection()).seedFor(12))
        val seeds = (1..100_000).map(plan::seedFor)
        assertEquals(seeds.size, seeds.toSet().size)
        // 每日种子 = epochDay + salt（数万量级）
        assertTrue(seeds.none { it in 0L..10_000_000L })
        assertNotEquals(plan.seedFor(12), LadderPlan(LadderSection(seedSalt = 1)).seedFor(12))
    }

    @Test
    fun `holes follow tier and optional per round step capped at hardest configured tier`() {
        val config = DifficultyConfig.default()
        assertEquals(36, plan.holesFor(1, config))
        assertEquals(55, plan.holesFor(16, config))

        val stepped = LadderPlan(LadderSection(holesStepPerRound = 2, shuffleFromRound = Int.MAX_VALUE))
        assertEquals(38, stepped.holesFor(21, config))
        assertEquals(55, stepped.holesFor(20 * 1000, config))
        val bigStep = LadderPlan(LadderSection(holesStepPerRound = 10))
        val round2 = (21..40).map { bigStep.difficultyFor(it) to bigStep.holesFor(it, config)!! }
        assertTrue(round2.all { (_, holes) -> holes <= 55 })
        assertTrue(round2.filter { (tier, _) -> tier == EXPERT }.all { (_, holes) -> holes == 55 })
    }

    @Test
    fun `tight but feasible configs always meet the expert rules`() {
        val tight = LadderPlan(LadderSection(cycle = listOf("easy", "expert"), levelsPerTier = 4, maxConsecutiveExpert = 2))
        val singles = LadderPlan(LadderSection(maxConsecutiveExpert = 1))
        for (round in 2..200) {
            for ((plan, maxRun) in listOf(tight to 2, singles to 1)) {
                val order = plan.roundOrder(round)
                assertNotEquals(EXPERT, order.first())
                assertNotEquals(EXPERT, order.last())
                assertTrue(order.windowed(maxRun + 1).none { w -> w.all { it == EXPERT } })
            }
        }
    }

    @Test
    fun `infeasible shuffle keeps the round ordered`() {
        val infeasible = LadderPlan(LadderSection(cycle = listOf("easy", "expert"), levelsPerTier = 1))
        assertEquals(listOf(EASY, EXPERT), infeasible.roundOrder(5))
        val allExpert = LadderPlan(LadderSection(cycle = listOf("expert")))
        assertEquals(List(5) { EXPERT }, allExpert.roundOrder(3))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `level zero is rejected`() {
        plan.difficultyFor(0)
    }
}
