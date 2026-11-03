package com.idroid.stuido.mind_game.core.ui.components.modecard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.AnnotatedString
import com.idroid.stuido.mind_game.core.ui.components.button.Button
import com.idroid.stuido.mind_game.core.ui.components.button.ButtonConfig
import com.idroid.stuido.mind_game.core.ui.components.button.ButtonStyle
import com.idroid.stuido.mind_game.core.ui.components.button.ButtonUiContent
import com.idroid.stuido.mind_game.core.ui.components.text.BaseText
import com.idroid.stuido.mind_game.core.ui.theme.MindgameTheme
import com.idroid.stuido.mind_game.core.ui.theme.Theme
import com.idroid.stuido.mind_game.core.ui.util.VisibilityConfig
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ModeCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var actionTaps = 0

    @Test
    fun rendersTitleSubtitleContentAndAction() {
        setCard()
        composeTestRule.onNodeWithText("Daily Challenge", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("9×9 · Hard", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Ends in 03:12:45").assertIsDisplayed()
        composeTestRule.onNodeWithText("Play").assertIsDisplayed()
    }

    @Test
    fun actionButtonClickInvokesCallback() {
        setCard()
        composeTestRule.onNodeWithText("Play").performClick()
        assertEquals(1, actionTaps)
    }

    @Test
    fun hiddenCardDoesNotRenderTitleOrAction() {
        setCard(visibility = VisibilityConfig.HIDDEN)
        composeTestRule.onNodeWithText("Daily Challenge", useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onAllNodes(hasClickAction()).assertCountEquals(0)
        assertEquals(0, actionTaps)
    }

    @Test
    fun cardHasNoClickActionWhenActionSlotIsPlainText() {
        composeTestRule.setContent {
                MindgameTheme {
                val uiContent = ModeCardUiContent(
                    icon = Theme.icons.daily,
                    title = "Daily Challenge",
                    subtitle = "9×9 · Hard",
                )
                ModeCard(
                    uiContent = uiContent,
                    uiConfig = ModeCardConfig(),
                    uiStyle = cardStyle(),
                    action = { BaseText(text = AnnotatedString("Not a button")) },
                )
            }
        }
        composeTestRule.onAllNodes(hasClickAction()).assertCountEquals(0)
    }

    private fun setCard(
        visibility: VisibilityConfig = VisibilityConfig.VISIBLE,
    ) {
        actionTaps = 0
        composeTestRule.setContent {
            MindgameTheme {
                val uiContent = ModeCardUiContent(
                    icon = Theme.icons.daily,
                    title = "Daily Challenge",
                    subtitle = "9×9 · Hard",
                )
                val buttonStyle = testButtonStyle()
                ModeCard(
                    uiContent = uiContent,
                    uiConfig = ModeCardConfig(visibility = visibility),
                    uiStyle = cardStyle(),
                    content = {
                        BaseText(text = AnnotatedString("Ends in 03:12:45"))
                    },
                    action = {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            uiContent = ButtonUiContent(text = "Play", onClick = { actionTaps++ }),
                            uiConfig = ButtonConfig(),
                            uiStyle = buttonStyle,
                        )
                    },
                )
            }
        }
    }

}

@Composable
private fun cardStyle() = ModeCardStyle(
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface,
    secondaryContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    iconTint = MaterialTheme.colorScheme.primary,
)

@Composable
private fun testButtonStyle() = ButtonStyle(
    filledContainerColor = MaterialTheme.colorScheme.primary,
    filledContentColor = MaterialTheme.colorScheme.onPrimary,
    outlinedContentColor = MaterialTheme.colorScheme.onSurface,
    outlinedBorderColor = MaterialTheme.colorScheme.outline,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
)
