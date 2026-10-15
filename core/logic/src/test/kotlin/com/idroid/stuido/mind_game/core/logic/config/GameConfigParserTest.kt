package com.idroid.stuido.mind_game.core.logic.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameConfigParserTest {

    @Test
    fun `empty config uses today's defaults`() {
        val config = parse("{}")
        val defaults = DifficultyConfig.default()

        assertEquals(defaults.holes, config.toDifficultyConfig().holes)
        assertEquals(defaults.maxRetry, config.toDifficultyConfig().maxRetry)
        assertEquals(GameRules(3, 3), config.rulesFor("free", BoardSpec.CLASSIC, Difficulty.EASY))
    }

    @Test
    fun `unknown top level and nested keys are ignored`() {
        val result = GameConfigParser.parse(
            """
            {
              "unknown": true,
              "sudoku": {
                "unknown": "ignored",
                "generator": {
                  "unknown": 1,
                  "holes": { "mini": { "easy": 3, "unknown": 9 } }
                }
              }
            }
            """.trimIndent(),
        )

        assertTrue(result.isSuccess)
        assertEquals(
            mapOf(BoardSpec.MINI to mapOf(Difficulty.EASY to 3)),
            result.getOrThrow().toDifficultyConfig().holes,
        )
    }

    @Test
    fun `malformed or wrongly typed json fails`() {
        assertTrue(GameConfigParser.parse("""{"schema_version":""").isFailure)
        assertTrue(GameConfigParser.parse("""{"schema_version":"1"}""").isFailure)
    }

    @Test
    fun `unsupported schema versions fail`() {
        assertTrue(GameConfigParser.parse("""{"schema_version":2}""").isFailure)
        assertTrue(GameConfigParser.parse("""{"schema_version":0}""").isFailure)
    }

    @Test
    fun `invalid hole entries are dropped`() {
        val config = parse(
            """
            {
              "sudoku": {
                "generator": {
                  "holes": {
                    "mini": { "easy": 16, "medium": 5 },
                    "classic": { "hard": 0, "easy": 30 },
                    "huge": { "easy": 3 }
                  }
                }
              }
            }
            """.trimIndent(),
        )
        val difficultyConfig = config.toDifficultyConfig()

        assertEquals(
            mapOf(
                BoardSpec.MINI to mapOf(Difficulty.MEDIUM to 5),
                BoardSpec.CLASSIC to mapOf(Difficulty.EASY to 30),
            ),
            difficultyConfig.holes,
        )
        assertFalse(difficultyConfig.isAvailable(BoardSpec.MINI, Difficulty.EASY))
    }

    @Test
    fun `all invalid holes use the default map`() {
        val config = parse(
            """{"sudoku":{"generator":{"holes":{"mini":{"easy":16},"huge":{"easy":3}}}}}""",
        )

        assertEquals(DifficultyConfig.default().holes, config.toDifficultyConfig().holes)
    }

    @Test
    fun `invalid max retry uses default`() {
        val config = parse("""{"sudoku":{"generator":{"max_retry":0}}}""")

        assertEquals(50, config.toDifficultyConfig().maxRetry)
    }

    @Test
    fun `invalid default rules use the default rules`() {
        val config = parse("""{"sudoku":{"rules":{"default":{"lives":0,"hints":8}}}}""")

        assertEquals(GameRules(3, 3), config.sudoku.rules.default)
    }

    @Test
    fun `override with unknown key is dropped`() {
        val config = parse(
            """{"sudoku":{"rules":{"overrides":[{"spec":"huge","lives":4}]}}}""",
        )

        assertTrue(config.sudoku.rules.overrides.isEmpty())
    }

    @Test
    fun `rules use specificity and later entries win ties`() {
        val config = parse(
            """
            {
              "sudoku": {
                "rules": {
                  "default": { "lives": 3, "hints": 3 },
                  "overrides": [
                    { "spec": "classic", "hints": 5 },
                    { "spec": "classic", "difficulty": "expert", "hints": 7 },
                    { "mode": "daily", "lives": 4 },
                    { "mode": "daily", "lives": 6 }
                  ]
                }
              }
            }
            """.trimIndent(),
        )

        assertEquals(GameRules(3, 7), config.rulesFor("free", BoardSpec.CLASSIC, Difficulty.EXPERT))
        assertEquals(GameRules(3, 5), config.rulesFor("free", BoardSpec.CLASSIC, Difficulty.EASY))
        assertEquals(GameRules(6, 7), config.rulesFor("daily", BoardSpec.CLASSIC, Difficulty.EXPERT))
        assertEquals(GameRules(6, 3), config.rulesFor("daily", BoardSpec.MINI, Difficulty.EASY))
    }

    @Test
    fun `invalid reward rule uses its default`() {
        val config = parse(
            """{"rewards":{"extra_life":{"enabled":false,"amount":0,"max_per_game":8}}}""",
        )

        assertEquals(RewardRule(maxPerGame = 2), config.rewards.extraLife)
    }

    @Test
    fun `negative interstitial timings use defaults`() {
        val config = parse(
            """{"ads":{"interstitial":{"enabled":true,"min_games_between":-1}}}""",
        )

        assertEquals(InterstitialRule(), config.ads.interstitial)
    }

    private fun parse(json: String): GameRemoteConfig =
        GameConfigParser.parse(json).getOrThrow()
}
