package com.idroid.stuido.mind_game.core.logic.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameRemoteConfig(
    @SerialName("schema_version")
    val schemaVersion: Int = 1,
    @SerialName("config_version")
    val configVersion: String = "default",
    val sudoku: SudokuSection = SudokuSection(),
    val rewards: RewardsSection = RewardsSection(),
    val ads: AdsSection = AdsSection(),
    val features: FeatureFlags = FeatureFlags(),
) {
    fun toDifficultyConfig(): DifficultyConfig {
        val holes = sudoku.generator.holes.mapNotNull { (specKey, difficultyHoles) ->
            val spec = BoardSpec.entries.firstOrNull { it.name.lowercase() == specKey }
                ?: return@mapNotNull null
            val difficulties = difficultyHoles.mapNotNull { (difficultyKey, holeCount) ->
                Difficulty.entries
                    .firstOrNull { it.name.lowercase() == difficultyKey }
                    ?.let { it to holeCount }
            }.toMap()
            if (difficulties.isEmpty()) null else spec to difficulties
        }.toMap()

        return DifficultyConfig(
            holes = holes,
            maxRetry = sudoku.generator.maxRetry,
            dailySeed = DifficultyConfig.default().dailySeed,
        )
    }

    fun rulesFor(mode: String, spec: BoardSpec, difficulty: Difficulty): GameRules {
        val matchingOverrides = sudoku.rules.overrides
            .filter { override ->
                (override.mode == null || override.mode == mode) &&
                    (override.spec == null || override.spec == spec.name.lowercase()) &&
                    (override.difficulty == null || override.difficulty == difficulty.name.lowercase())
            }
            .sortedBy { override ->
                listOfNotNull(override.mode, override.spec, override.difficulty).size
            }

        return matchingOverrides.fold(sudoku.rules.default) { rules, override ->
            GameRules(
                lives = override.lives ?: rules.lives,
                hints = override.hints ?: rules.hints,
            )
        }
    }
}

@Serializable
data class SudokuSection(
    val generator: GeneratorSection = GeneratorSection(),
    val rules: RulesSection = RulesSection(),
    val daily: DailySection = DailySection(),
)

@Serializable
data class GeneratorSection(
    @SerialName("max_retry")
    val maxRetry: Int = 50,
    val holes: Map<String, Map<String, Int>> = defaultHoles(),
)

@Serializable
data class RulesSection(
    val default: GameRules = GameRules(),
    val overrides: List<RuleOverride> = emptyList(),
)

@Serializable
data class RuleOverride(
    val mode: String? = null,
    val spec: String? = null,
    val difficulty: String? = null,
    val lives: Int? = null,
    val hints: Int? = null,
)

@Serializable
data class DailySection(
    val enabled: Boolean = true,
    @SerialName("seed_salt")
    val seedSalt: Long = 0,
)

@Serializable
data class RewardsSection(
    @SerialName("extra_life")
    val extraLife: RewardRule = RewardRule(maxPerGame = 2),
    @SerialName("extra_hint")
    val extraHint: RewardRule = RewardRule(maxPerGame = 3),
)

@Serializable
data class RewardRule(
    val enabled: Boolean = true,
    val amount: Int = 1,
    @SerialName("max_per_game")
    val maxPerGame: Int,
    @SerialName("allow_in_daily")
    val allowInDaily: Boolean = true,
)

@Serializable
data class AdsSection(
    val enabled: Boolean = false,
    val interstitial: InterstitialRule = InterstitialRule(),
)

@Serializable
data class InterstitialRule(
    val enabled: Boolean = false,
    @SerialName("min_games_between")
    val minGamesBetween: Int = 3,
    @SerialName("min_seconds_between")
    val minSecondsBetween: Int = 180,
)

@Serializable
data class FeatureFlags(
    @SerialName("onboarding_tour")
    val onboardingTour: Boolean = true,
    @SerialName("notes_auto_fill")
    val notesAutoFill: Boolean = false,
    val crossword: Boolean = false,
    val zooduku: Boolean = false,
)

private fun defaultHoles(): Map<String, Map<String, Int>> = mapOf(
    "mini" to mapOf(
        "easy" to 3,
        "medium" to 5,
        "hard" to 6,
    ),
    "medium" to mapOf(
        "easy" to 10,
        "medium" to 14,
        "hard" to 18,
        "expert" to 20,
    ),
    "classic" to mapOf(
        "easy" to 36,
        "medium" to 42,
        "hard" to 48,
        "expert" to 55,
    ),
)
