package com.idroid.stuido.mind_game.core.ui.components.keypad

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * NumberKeypad 的 Compose UI 测试：键数渲染、0 剩余置灰、zeroCountEnabled 豁免、
 * 全局禁用与点击回调。
 */
class NumberKeypadTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun testStyle() = NumberKeypadStyle(
        zoneColor = Color(0xFFE8DEF8),
        surfaceColor = Color.White,
        disabledSurfaceColor = Color(0x14000000),
        digitColor = Color.Black,
        disabledContentColor = Color(0x66000000),
        badgeBackgroundColor = Color(0xFF6750A4),
        badgeContentColor = Color.White,
    )

    private fun setKeypad(
        digits: Int,
        remaining: List<Int>,
        zeroCountEnabled: Set<Int> = emptySet(),
        enabled: Boolean = true,
        onDigit: (Int) -> Unit = {},
    ) {
        composeTestRule.setContent {
            NumberKeypad(
                uiContent = NumberKeypadUiContent(
                    digits = digits,
                    remaining = remaining,
                    zeroCountEnabled = zeroCountEnabled,
                    onDigit = onDigit,
                ),
                uiConfig = NumberKeypadConfig(enabled = enabled),
                uiStyle = testStyle(),
            )
        }
    }

    private fun keys() = composeTestRule.onAllNodes(hasClickAction())

    @Test
    fun rendersNineKeysForClassic() {
        setKeypad(digits = 9, remaining = List(9) { 1 })
        keys().assertCountEquals(9)
        composeTestRule.onAllNodesWithText("9").assertCountEquals(1)
    }

    @Test
    fun rendersFourKeysForMini() {
        setKeypad(digits = 4, remaining = List(4) { 1 })
        keys().assertCountEquals(4)
    }

    @Test
    fun zeroRemainingKeyIsDisabled() {
        setKeypad(digits = 9, remaining = listOf(2, 4, 1, 0, 5, 1, 3, 2, 0))
        keys()[3].assertIsNotEnabled() // digit 4
        keys()[8].assertIsNotEnabled() // digit 9
        keys()[0].assertIsEnabled() // digit 1 still has 2
    }

    @Test
    fun enabledKeyClickDispatchesDigit() {
        var tapped: Int? = null
        setKeypad(digits = 9, remaining = List(9) { 1 }, onDigit = { tapped = it })
        keys()[6].performClick()
        assertEquals(7, tapped)
    }

    @Test
    fun disabledKeyClickIsIgnored() {
        var tapped: Int? = null
        setKeypad(digits = 9, remaining = listOf(0, 1, 1, 1, 1, 1, 1, 1, 1), onDigit = { tapped = it })
        keys()[0].assertIsNotEnabled()
        runCatching { keys()[0].performClick() }
        assertNull(tapped)
    }

    @Test
    fun zeroCountEnabledKeepsKeyClickable() {
        var tapped: Int? = null
        setKeypad(
            digits = 4,
            remaining = listOf(0, 1, 1, 1),
            zeroCountEnabled = setOf(1),
            onDigit = { tapped = it },
        )
        keys()[0].assertIsEnabled()
        keys()[0].performClick()
        assertEquals(1, tapped)
    }

    @Test
    fun globalDisableGreysWholeRow() {
        setKeypad(
            digits = 4,
            remaining = listOf(2, 1, 1, 0),
            zeroCountEnabled = setOf(4),
            enabled = false,
        )
        keys().assertCountEquals(4)
        keys()[0].assertIsNotEnabled()
        keys()[3].assertIsNotEnabled() // zeroCountEnabled must not bypass global disable
    }
}
