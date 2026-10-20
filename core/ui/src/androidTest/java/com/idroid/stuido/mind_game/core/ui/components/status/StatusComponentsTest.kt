package com.idroid.stuido.mind_game.core.ui.components.status

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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * StatusChip / StatusInfoRow 的 Compose UI 测试：图标+文本渲染、点击回调、
 * 禁用态、以及整行 N 项的铺开。
 */
class StatusComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val stubIcon = ImageVector.Builder(
        name = "stub",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build()

    private fun chipStyle() = StatusChipStyle(
        iconTintColor = Color.Black,
        textColor = Color.Black,
        disabledContentColor = Color.Gray,
    )

    private fun setChip(
        text: String,
        enabled: Boolean = true,
        clickable: Boolean = true,
        onClick: (() -> Unit)? = null,
    ) {
        composeTestRule.setContent {
            StatusChip(
                uiContent = StatusChipUiContent(
                    icon = stubIcon,
                    contentDescription = "chip icon",
                    text = text,
                    onClick = onClick,
                ),
                uiConfig = StatusChipConfig(enabled = enabled, clickable = clickable),
                uiStyle = chipStyle(),
            )
        }
    }

    @Test
    fun chipRendersIconAndText() {
        setChip(text = "03:42", onClick = {})
        composeTestRule.onNodeWithText("03:42").assertExists()
        composeTestRule.onNodeWithContentDescription("chip icon").assertExists()
    }

    @Test
    fun chipRendersSecondaryText() {
        composeTestRule.setContent {
            StatusChip(
                uiContent = StatusChipUiContent(
                    icon = stubIcon,
                    contentDescription = "chip icon",
                    text = "Errors",
                    secondaryText = "1/3",
                ),
                uiConfig = StatusChipConfig(),
                uiStyle = chipStyle(),
            )
        }
        composeTestRule.onNodeWithText("Errors").assertExists()
        composeTestRule.onNodeWithText("1/3").assertExists()
    }

    @Test
    fun enabledChipClickFiresCallback() {
        var tapped = false
        setChip(text = "03:42", onClick = { tapped = true })
        composeTestRule.onNodeWithText("03:42").performClick()
        assertTrue(tapped)
    }

    @Test
    fun disabledChipDoesNotClick() {
        var tapped = false
        setChip(text = "03:42", enabled = false, onClick = { tapped = true })
        composeTestRule.onNode(hasClickAction()).assertIsNotEnabled()
        runCatching { composeTestRule.onNodeWithText("03:42").performClick() }
        assertFalse(tapped)
    }

    @Test
    fun chipWithoutOnClickHasNoClickAction() {
        setChip(text = "Errors: 1/3")
        composeTestRule.onNodeWithText("Errors: 1/3").assertExists()
        composeTestRule.onAllNodes(hasClickAction()).assertCountEquals(0)
    }

    @Test
    fun notClickableFlagSuppressesClick() {
        var tapped = false
        setChip(text = "03:42", clickable = false, onClick = { tapped = true })
        composeTestRule.onAllNodes(hasClickAction()).assertCountEquals(0)
        assertFalse(tapped)
    }

    @Test
    fun rowRendersAllItems() {
        composeTestRule.setContent {
            StatusInfoRow(
                uiContent = StatusInfoRowUiContent(
                    items = listOf(
                        StatusChipUiContent(icon = stubIcon, contentDescription = "timer", text = "04:18", onClick = {}),
                        StatusChipUiContent(icon = stubIcon, contentDescription = "errors", text = "Errors: 1/3"),
                        StatusChipUiContent(icon = stubIcon, contentDescription = "hints", text = "Hints: 3/3"),
                    ),
                ),
                uiConfig = StatusInfoRowConfig(),
                uiStyle = StatusInfoRowStyle(zoneColor = Color.White, chipStyle = chipStyle()),
            )
        }
        composeTestRule.onNodeWithText("04:18").assertExists()
        composeTestRule.onNodeWithText("Errors: 1/3").assertExists()
        composeTestRule.onNodeWithText("Hints: 3/3").assertExists()
        composeTestRule.onAllNodes(hasClickAction()).assertCountEquals(1) // 仅计时项可点
    }

    @Test
    fun rowGlobalDisableBlocksClicks() {
        var tapped = false
        composeTestRule.setContent {
            StatusInfoRow(
                uiContent = StatusInfoRowUiContent(
                    items = listOf(
                        StatusChipUiContent(icon = stubIcon, contentDescription = "timer", text = "04:18", onClick = { tapped = true }),
                    ),
                ),
                uiConfig = StatusInfoRowConfig(enabled = false),
                uiStyle = StatusInfoRowStyle(zoneColor = Color.White, chipStyle = chipStyle()),
            )
        }
        composeTestRule.onNode(hasClickAction()).assertIsNotEnabled()
        runCatching { composeTestRule.onNodeWithText("04:18").performClick() }
        assertFalse(tapped)
    }

    @Test
    fun emptyRowRendersNothing() {
        composeTestRule.setContent {
            StatusInfoRow(
                uiContent = StatusInfoRowUiContent(items = emptyList()),
                uiConfig = StatusInfoRowConfig(),
                uiStyle = StatusInfoRowStyle(zoneColor = Color.White, chipStyle = chipStyle()),
            )
        }
        composeTestRule.onAllNodes(hasClickAction()).assertCountEquals(0)
    }

    @Test
    fun enabledChipIsAssertedEnabled() {
        setChip(text = "03:42", onClick = {})
        composeTestRule.onNode(hasClickAction()).assertIsEnabled()
    }
}
