package com.idroid.stuido.mind_game.core.ui.components.button

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.util.VisibilityConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Button 的 Compose UI 测试：图标+文本渲染、填充/描边两种形态、
 * 点击回调、禁用态以及隐藏态。
 */
class ButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val stubIcon = ImageVector.Builder(
        name = "stub",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build()

    private fun buttonStyle() = ButtonStyle(
        filledContainerColor = Color.Blue,
        filledContentColor = Color.White,
        outlinedContentColor = Color.Blue,
        outlinedBorderColor = Color.Blue,
        disabledContainerColor = Color.Gray,
        disabledContentColor = Color.DarkGray,
    )

    private fun setButton(
        text: String = "Continue",
        variant: ButtonVariant = ButtonVariant.FILLED,
        enabled: Boolean = true,
        visibility: VisibilityConfig = VisibilityConfig.VISIBLE,
        withIcon: Boolean = true,
        onClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            Button(
                uiContent = ButtonUiContent(
                    text = text,
                    leadIcon = if (withIcon) stubIcon else null,
                    contentDescription = if (withIcon) "lead icon" else null,
                    onClick = onClick,
                ),
                uiConfig = ButtonConfig(
                    visibility = visibility,
                    enabled = enabled,
                    variant = variant,
                ),
                uiStyle = buttonStyle(),
            )
        }
    }

    @Test
    fun filledButtonRendersIconAndText() {
        setButton(variant = ButtonVariant.FILLED)
        composeTestRule.onNodeWithText("Continue").assertExists()
        composeTestRule.onNodeWithContentDescription("lead icon").assertExists()
    }

    @Test
    fun outlinedButtonRendersIconAndText() {
        setButton(variant = ButtonVariant.OUTLINED)
        composeTestRule.onNodeWithText("Continue").assertExists()
        composeTestRule.onNodeWithContentDescription("lead icon").assertExists()
    }

    @Test
    fun buttonWithoutIconStillRendersText() {
        setButton(withIcon = false)
        composeTestRule.onNodeWithText("Continue").assertExists()
        composeTestRule.onNodeWithContentDescription("lead icon").assertDoesNotExist()
    }

    @Test
    fun filledButtonClickFiresCallback() {
        var tapped = false
        setButton(variant = ButtonVariant.FILLED, onClick = { tapped = true })
        composeTestRule.onNodeWithText("Continue").performClick()
        assertTrue(tapped)
    }

    @Test
    fun outlinedButtonClickFiresCallback() {
        var tapped = false
        setButton(variant = ButtonVariant.OUTLINED, onClick = { tapped = true })
        composeTestRule.onNodeWithText("Continue").performClick()
        assertTrue(tapped)
    }

    @Test
    fun enabledButtonIsAssertedEnabled() {
        setButton()
        composeTestRule.onNode(hasClickAction()).assertIsEnabled()
    }

    @Test
    fun disabledButtonDoesNotClick() {
        var tapped = false
        setButton(enabled = false, onClick = { tapped = true })
        composeTestRule.onNode(hasClickAction()).assertIsNotEnabled()
        runCatching { composeTestRule.onNodeWithText("Continue").performClick() }
        assertFalse(tapped)
    }

    @Test
    fun hiddenButtonRendersNothing() {
        setButton(visibility = VisibilityConfig.HIDDEN)
        composeTestRule.onAllNodes(hasClickAction()).assertCountEquals(0)
        composeTestRule.onNodeWithText("Continue").assertDoesNotExist()
    }
}
