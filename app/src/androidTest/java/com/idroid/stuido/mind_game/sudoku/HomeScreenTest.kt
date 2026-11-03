package com.idroid.stuido.mind_game.sudoku

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.core.ui.theme.MindgameTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.Duration
import java.time.LocalDate

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val daily = DailyCardState(
        difficulty = Difficulty.HARD,
        puzzleDay = LocalDate.of(2026, 10, 25),
        solved = false,
        untilReset = Duration.ofSeconds(3 * 3600 + 12 * 60 + 45),
    )
    private val ladder = LadderCardState(
        level = 23,
        difficulty = Difficulty.MEDIUM,
        round = 2,
        positionInRound = 3,
        roundSize = 20,
    )

    private var dailyTaps = 0
    private var levelTaps = 0

    private fun setHome(state: HomeUiState = HomeUiState(daily, ladder)) {
        composeTestRule.setContent {
            MindgameTheme {
                HomeScreen(
                    state = state,
                    onPlayDaily = { dailyTaps++ },
                    onPlayLevel = { levelTaps++ },
                    onOpenSettings = {},
                )
            }
        }
    }

    @Test
    fun showsDailyAndLevelCardsWithoutFreeModeOrBoardSizeControls() {
        setHome()
        composeTestRule.onNodeWithText("Daily Challenge").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ends in 03:12:45").assertIsDisplayed()
        composeTestRule.onNodeWithText("Level 23").assertIsDisplayed()
        composeTestRule.onNodeWithText("Medium · Round 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 of 20 in this round").assertIsDisplayed()
        composeTestRule.onNodeWithText("Free").assertDoesNotExist()
        composeTestRule.onNodeWithText("4×4").assertDoesNotExist()
        composeTestRule.onAllNodesWithText("Play")[0].assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Play")[1].performScrollTo().assertIsDisplayed()
    }

    @Test
    fun dailyAndLevelPlayButtonsStartTheirCallbacks() {
        setHome()
        composeTestRule.onAllNodesWithText("Play")[0].performClick()
        composeTestRule.onAllNodesWithText("Play")[1].performScrollTo().performClick()
        assertEquals(1, dailyTaps)
        assertEquals(1, levelTaps)
    }

    @Test
    fun solvedDailyOffersPlayAgainAndNextCountdown() {
        setHome(HomeUiState(daily.copy(solved = true), ladder))
        composeTestRule.onNodeWithText("Solved · next puzzle in 03:12:45").assertIsDisplayed()
        composeTestRule.onNodeWithText("Play again").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Play")[0].assertIsDisplayed()
    }

    @Test
    fun disabledDailyHidesItsCard() {
        setHome(HomeUiState(daily = null, ladder = ladder))
        composeTestRule.onNodeWithTag(HomeTestTags.DAILY).assertDoesNotExist()
        composeTestRule.onNodeWithTag(HomeTestTags.LADDER).assertIsDisplayed()
    }
}
