package com.idroid.stuido.mind_game.core.ui.components.progress

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.theme.MindgameTheme
import com.idroid.stuido.mind_game.core.ui.util.VisibilityConfig
import org.junit.Rule
import org.junit.Test

class CycleProgressDotsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun contentDescriptionIsExposedAsOneNode() {
        composeTestRule.setContent {
            MindgameTheme {
                CycleProgressDots(
                    uiContent = CycleProgressDotsUiContent(
                        total = 20,
                        completed = 7,
                        contentDescription = "7 of 20 levels cleared this round",
                    ),
                    uiConfig = CycleProgressDotsConfig(),
                    uiStyle = testStyle,
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("7 of 20 levels cleared this round").assertExists()
    }

    @Test
    fun hiddenDotsDoNotRender() {
        composeTestRule.setContent {
            MindgameTheme {
                CycleProgressDots(
                    uiContent = CycleProgressDotsUiContent(
                        total = 20,
                        completed = 7,
                        contentDescription = "7 of 20 levels cleared this round",
                    ),
                    uiConfig = CycleProgressDotsConfig(visibility = VisibilityConfig.HIDDEN),
                    uiStyle = testStyle,
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("7 of 20 levels cleared this round").assertDoesNotExist()
    }

    private companion object {
        val testStyle = CycleProgressDotsStyle(
            completedColor = Color.Blue,
            currentColor = Color.Green,
            pendingColor = Color.Gray,
            dotSize = 8.dp,
            spacing = 4.dp,
        )
    }
}
