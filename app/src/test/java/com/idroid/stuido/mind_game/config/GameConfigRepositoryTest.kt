package com.idroid.stuido.mind_game.config

import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.core.logic.config.DifficultyConfig
import com.idroid.stuido.mind_game.core.logic.config.GameRemoteConfig
import com.idroid.stuido.mind_game.core.logic.config.GameRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File

class GameConfigRepositoryTest {

    @Test
    fun `bundled config preserves current defaults`() {
        val asset = File("src/main/assets/game_config.json").readText()
        val config = GameConfigRepository(loadJson = { asset }).config
        val difficultyConfig = config.toDifficultyConfig()
        val defaults = DifficultyConfig.default()

        assertEquals(defaults.holes, difficultyConfig.holes)
        assertEquals(defaults.maxRetry, difficultyConfig.maxRetry)
        assertEquals("2026-10-15.1", config.configVersion)

        for (spec in BoardSpec.entries) {
            for (difficulty in difficultyConfig.availableDifficulties(spec)) {
                assertEquals(GameRules(3, 3), config.rulesFor("free", spec, difficulty))
                assertEquals(GameRules(3, 3), config.rulesFor("daily", spec, difficulty))
            }
        }
    }

    @Test
    fun `invalid json falls back and reports one error`() {
        var errorCount = 0
        var errorMessage: String? = null
        val repository = GameConfigRepository(
            loadJson = { "not json" },
            onError = {
                errorCount++
                errorMessage = it
            },
        )

        assertEquals(GameRemoteConfig(), repository.config)
        assertEquals(GameRemoteConfig(), repository.config)
        assertEquals(1, errorCount)
        assertNotNull(errorMessage)
    }

    @Test
    fun `missing json falls back to defaults`() {
        val repository = GameConfigRepository(
            loadJson = { null },
            onError = {},
        )

        assertEquals(GameRemoteConfig(), repository.config)
    }

    @Test
    fun `loader exception falls back to defaults`() {
        val repository = GameConfigRepository(
            loadJson = { error("unavailable") },
            onError = {},
        )

        assertEquals(GameRemoteConfig(), repository.config)
    }
}
