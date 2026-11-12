package com.idroid.stuido.mind_game.sudoku

import com.idroid.stuido.mind_game.feature.game.GameMode
import com.idroid.stuido.mind_game.feature.game.persistence.SavedGame
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * 存档的宿主层策略：每日挑战跨期即作废（见 [SavedGameStore.isStaleDaily]）。
 *
 * 纯函数测试，不触碰 SharedPreferences。
 */
class SavedGameStoreTest {

    private val today = LocalDate.of(2026, 11, 10)

    private fun daily(day: String?) = SavedGame(mode = GameMode.DAILY.name, puzzleDay = day)

    @Test
    fun `当天的每日挑战可恢复`() {
        assertFalse(SavedGameStore.isStaleDaily(daily("2026-11-10"), today))
    }

    @Test
    fun `跨期每日挑战判废`() {
        assertTrue(SavedGameStore.isStaleDaily(daily("2026-11-09"), today))
    }

    @Test
    fun `每日挑战缺日期视为脏数据`() {
        assertTrue(SavedGameStore.isStaleDaily(daily(null), today))
        assertTrue(SavedGameStore.isStaleDaily(daily("not-a-date"), today))
    }

    @Test
    fun `自由与天梯模式与日期无关 永远可恢复`() {
        assertFalse(SavedGameStore.isStaleDaily(SavedGame(mode = GameMode.FREE.name), today))
        assertFalse(
            SavedGameStore.isStaleDaily(
                SavedGame(mode = GameMode.LADDER.name, puzzleDay = "2020-01-01"),
                today,
            ),
        )
    }
}
