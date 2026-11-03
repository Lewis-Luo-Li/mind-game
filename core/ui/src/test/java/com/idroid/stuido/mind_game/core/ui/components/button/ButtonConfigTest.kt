package com.idroid.stuido.mind_game.core.ui.components.button

import com.idroid.stuido.mind_game.core.ui.util.VisibilityConfig
import com.idroid.stuido.mind_game.core.ui.util.isHidden
import com.idroid.stuido.mind_game.core.ui.util.isVisible
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ButtonConfig 默认值与变体枚举的单元测试：保证新增按钮在未显式配置时
 * 仍为「可见、可用、填充」形态。
 */
class ButtonConfigTest {

    @Test
    fun defaultsAreVisibleEnabledAndFilled() {
        val config = ButtonConfig()
        assertTrue(config.visibility.isVisible())
        assertFalse(config.visibility.isHidden())
        assertTrue(config.enabled)
        assertEquals(ButtonVariant.FILLED, config.variant)
    }

    @Test
    fun variantCanBeSwitchedToOutlined() {
        val config = ButtonConfig(variant = ButtonVariant.OUTLINED)
        assertEquals(ButtonVariant.OUTLINED, config.variant)
    }

    @Test
    fun uiContentIconIsOptional() {
        val withoutIcon = ButtonUiContent(text = "Retry", onClick = {})
        assertTrue(withoutIcon.leadIcon == null)
        assertEquals(null, withoutIcon.contentDescription)

        val withIcon = ButtonUiContent(
            text = "Retry",
            leadIcon = null,
            contentDescription = "retry icon",
            onClick = {},
        )
        assertEquals("retry icon", withIcon.contentDescription)
    }
}
