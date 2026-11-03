package com.idroid.stuido.mind_game.core.ui.components.modecard

import com.idroid.stuido.mind_game.core.ui.util.VisibilityConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class ModeCardConfigTest {

    @Test
    fun `default visibility is visible`() {
        assertEquals(VisibilityConfig.VISIBLE, ModeCardConfig().visibility)
    }
}
