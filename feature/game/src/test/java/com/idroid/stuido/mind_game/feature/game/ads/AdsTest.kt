package com.idroid.stuido.mind_game.feature.game.ads

import com.idroid.stuido.mind_game.core.logic.config.InterstitialRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock

class InterstitialFrequencyGateTest {

    private val rule = InterstitialRule(
        enabled = true,
        minGamesBetween = 3,
        minSecondsBetween = 180,
    )

    @Test
    fun `show is blocked until min games completed`() {
        val gate = InterstitialFrequencyGate()
        repeat(2) { gate.recordGameFinished() }
        assertFalse(gate.shouldShow(rule, adsEnabled = true))
        gate.recordGameFinished()
        assertTrue(gate.shouldShow(rule, adsEnabled = true))
    }

    @Test
    fun `ads master switch off never shows`() {
        val gate = InterstitialFrequencyGate()
        repeat(10) { gate.recordGameFinished() }
        assertFalse(gate.shouldShow(rule, adsEnabled = false))
    }

    @Test
    fun `interstitial disabled in config never shows`() {
        val gate = InterstitialFrequencyGate()
        repeat(10) { gate.recordGameFinished() }
        assertFalse(
            gate.shouldShow(InterstitialRule(enabled = false, minGamesBetween = 0, minSecondsBetween = 0), true),
        )
    }

    @Test
    fun `first show needs no prior timestamp but still needs the game count`() {
        val gate = InterstitialFrequencyGate()
        assertFalse(gate.shouldShow(rule, adsEnabled = true))
        repeat(3) { gate.recordGameFinished() }
        // lastShownAtMs == null → 只检查局数。
        assertTrue(gate.shouldShow(rule, adsEnabled = true))
    }

    @Test
    fun `after showing, both game and time caps apply again`() {
        var now = 1_000_000L
        val gate = InterstitialFrequencyGate(clockMs = { now })
        repeat(3) { gate.recordGameFinished() }
        assertTrue(gate.shouldShow(rule, adsEnabled = true))
        gate.recordShown()

        assertEquals(0, gate.gamesCompletedSinceShow)
        assertEquals(now, gate.lastShownAtMs)

        // 局数未满：不播
        repeat(2) { gate.recordGameFinished() }
        assertFalse(gate.shouldShow(rule, adsEnabled = true))
        // 局数满但时间未满：不播
        gate.recordGameFinished()
        now += 179_000L
        assertFalse(gate.shouldShow(rule, adsEnabled = true))
        // 时间也满：可播
        now += 2_000L
        assertTrue(gate.shouldShow(rule, adsEnabled = true))
    }

    @Test
    fun `games played while waiting still count toward the next window`() {
        var now = 0L
        val gate = InterstitialFrequencyGate(clockMs = { now })
        repeat(3) { gate.recordGameFinished() }
        gate.recordShown()
        now += 400_000L
        repeat(3) { gate.recordGameFinished() }
        assertTrue(gate.shouldShow(rule, adsEnabled = true))
    }
}

class FakeRewardedAdControllerTest {

    private val context = mock<android.content.Context>()

    @Test
    fun `available fake grants the reward immediately`() {
        val controller = FakeRewardedAdController(available = true)
        var reward = 0
        var unavailable = 0
        controller.show(context, RewardKind.EXTRA_HINT, { reward++ }, { unavailable++ })
        assertEquals(1, reward)
        assertEquals(0, unavailable)
        assertEquals(1, controller.showCount)
    }

    @Test
    fun `unavailable fake triggers the unavailable path (snackbar at call site)`() {
        val controller = FakeRewardedAdController(available = false)
        assertFalse(controller.isAvailable(RewardKind.EXTRA_LIFE))
        var reward = 0
        var unavailable = 0
        controller.show(context, RewardKind.EXTRA_LIFE, { reward++ }, { unavailable++ })
        assertEquals(0, reward)
        assertEquals(1, unavailable)
    }
}
