package com.idroid.stuido.mind_game.feature.game.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/** formatTimer 的边界与格式：MM:SS（<1h）/ H:MM:SS（≥1h）/ 负值钳制。 */
class TimeFormatTest {

    @Test
    fun `zero renders as 00-00`() {
        assertEquals("00:00", formatTimer(0))
    }

    @Test
    fun `negative clamps to zero`() {
        assertEquals("00:00", formatTimer(-5))
    }

    @Test
    fun `single digit seconds pad to two`() {
        assertEquals("00:07", formatTimer(7))
    }

    @Test
    fun `over a minute renders MM-SS`() {
        assertEquals("01:05", formatTimer(65))
    }

    @Test
    fun `just under an hour stays MM-SS`() {
        assertEquals("59:59", formatTimer(3599))
    }

    @Test
    fun `one hour switches to H-MM-SS`() {
        assertEquals("1:00:00", formatTimer(3600))
    }

    @Test
    fun `hours keep minutes and seconds padded`() {
        assertEquals("1:01:01", formatTimer(3661))
    }

    @Test
    fun `large hours are not padded`() {
        assertEquals("12:34:56", formatTimer(12 * 3600 + 34 * 60 + 56))
    }
}
