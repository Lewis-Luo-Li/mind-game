package com.idroid.stuido.mind_game.core.logic.config

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

object GameConfigParser {
    const val SUPPORTED_SCHEMA_VERSION = 1

    private val jsonFormat = Json {
        ignoreUnknownKeys = true
    }

    fun parse(json: String): Result<GameRemoteConfig> = runCatching {
        val element = jsonFormat.parseToJsonElement(json)
        element.validateConfigTypes()
        val config = jsonFormat.decodeFromJsonElement<GameRemoteConfig>(element)
        require(config.schemaVersion in 1..SUPPORTED_SCHEMA_VERSION) {
            "Unsupported schema version: ${config.schemaVersion}"
        }
        config.validated()
    }
}

internal fun GameRemoteConfig.validated(): GameRemoteConfig {
    val defaults = GameRemoteConfig()
    val validHoles = sudoku.generator.holes.mapNotNull { (specKey, difficultyHoles) ->
        val spec = BoardSpec.entries.firstOrNull { it.name.lowercase() == specKey }
            ?: return@mapNotNull null
        val validDifficulties = difficultyHoles.filter { (difficultyKey, holeCount) ->
            Difficulty.entries.any { it.name.lowercase() == difficultyKey } &&
                holeCount in 1 until spec.size * spec.size
        }
        if (validDifficulties.isEmpty()) null else specKey to validDifficulties
    }.toMap()

    val validDefaultRules = sudoku.rules.default.let { rules ->
        if (rules.lives in 1..9 && rules.hints in 0..9) rules else GameRules()
    }
    val validOverrides = sudoku.rules.overrides.filter { override ->
        (override.mode == null || override.mode in setOf("free", "daily")) &&
            (override.spec == null || BoardSpec.entries.any { it.name.lowercase() == override.spec }) &&
            (override.difficulty == null ||
                Difficulty.entries.any { it.name.lowercase() == override.difficulty }) &&
            (override.lives == null || override.lives in 1..9) &&
            (override.hints == null || override.hints in 0..9)
    }

    val validInterstitial = ads.interstitial.let { interstitial ->
        if (interstitial.minGamesBetween >= 0 && interstitial.minSecondsBetween >= 0) {
            interstitial
        } else {
            InterstitialRule()
        }
    }

    return copy(
        sudoku = sudoku.copy(
            generator = sudoku.generator.copy(
                maxRetry = sudoku.generator.maxRetry.takeIf { it in 1..1000 }
                    ?: defaults.sudoku.generator.maxRetry,
                holes = validHoles.ifEmpty { defaults.sudoku.generator.holes },
            ),
            rules = sudoku.rules.copy(
                default = validDefaultRules,
                overrides = validOverrides,
            ),
        ),
        rewards = rewards.copy(
            extraLife = rewards.extraLife.validatedOr(defaults.rewards.extraLife),
            extraHint = rewards.extraHint.validatedOr(defaults.rewards.extraHint),
        ),
        ads = ads.copy(interstitial = validInterstitial),
    )
}

private fun RewardRule.validatedOr(default: RewardRule): RewardRule =
        if (amount in 1..9 && maxPerGame in 0..9) this else default

private fun JsonElement.validateConfigTypes() {
    val root = this as? JsonObject ?: invalidType("$", "object")
    root.validateInt("schema_version", "$")
    root.validateString("config_version", "$")
    root.validateObject("sudoku", "$") { sudoku ->
        sudoku.validateObject("generator", "$.sudoku") { generator ->
            generator.validateInt("max_retry", "$.sudoku.generator")
            generator.validateObject("holes", "$.sudoku.generator") { holes ->
                holes.forEach { (spec, value) ->
                    val difficulties = value as? JsonObject
                        ?: invalidType("$.sudoku.generator.holes.$spec", "object")
                    difficulties.forEach { (difficulty, holeCount) ->
                        holeCount.validateInt("$.sudoku.generator.holes.$spec.$difficulty")
                    }
                }
            }
        }
        sudoku.validateObject("rules", "$.sudoku") { rules ->
            rules.validateObject("default", "$.sudoku.rules") { default ->
                default.validateInt("lives", "$.sudoku.rules.default")
                default.validateInt("hints", "$.sudoku.rules.default")
            }
            rules.validateArray("overrides", "$.sudoku.rules") { element, path ->
                val override = element as? JsonObject ?: invalidType(path, "object")
                override.validateNullableString("mode", path)
                override.validateNullableString("spec", path)
                override.validateNullableString("difficulty", path)
                override.validateNullableInt("lives", path)
                override.validateNullableInt("hints", path)
            }
        }
        sudoku.validateObject("daily", "$.sudoku") { daily ->
            daily.validateBoolean("enabled", "$.sudoku.daily")
            daily.validateLong("seed_salt", "$.sudoku.daily")
        }
    }
    root.validateObject("rewards", "$") { rewards ->
        rewards.validateObject("extra_life", "$.rewards") { it.validateReward("$.rewards.extra_life") }
        rewards.validateObject("extra_hint", "$.rewards") { it.validateReward("$.rewards.extra_hint") }
    }
    root.validateObject("ads", "$") { ads ->
        ads.validateBoolean("enabled", "$.ads")
        ads.validateObject("interstitial", "$.ads") { interstitial ->
            interstitial.validateBoolean("enabled", "$.ads.interstitial")
            interstitial.validateInt("min_games_between", "$.ads.interstitial")
            interstitial.validateInt("min_seconds_between", "$.ads.interstitial")
        }
    }
    root.validateObject("features", "$") { features ->
        features.validateBoolean("onboarding_tour", "$.features")
        features.validateBoolean("notes_auto_fill", "$.features")
        features.validateBoolean("crossword", "$.features")
        features.validateBoolean("zooduku", "$.features")
    }
}

private fun JsonObject.validateReward(path: String) {
    validateBoolean("enabled", path)
    validateInt("amount", path)
    validateInt("max_per_game", path)
    validateBoolean("allow_in_daily", path)
}

private fun JsonObject.validateObject(
    name: String,
    path: String,
    validate: (JsonObject) -> Unit,
) {
    val value = this[name] ?: return
    val objectValue = value as? JsonObject ?: invalidType("$path.$name", "object")
    validate(objectValue)
}

private fun JsonObject.validateArray(
    name: String,
    path: String,
    validate: (JsonElement, String) -> Unit,
) {
    val value = this[name] ?: return
    val arrayValue = value as? JsonArray ?: invalidType("$path.$name", "array")
    arrayValue.forEachIndexed { index, element ->
        validate(element, "$path.$name[$index]")
    }
}

private fun JsonObject.validateString(name: String, path: String) {
    this[name]?.validateString("$path.$name")
}

private fun JsonObject.validateNullableString(name: String, path: String) {
    val value = this[name] ?: return
    if (value is JsonNull) return
    value.validateString("$path.$name")
}

private fun JsonElement.validateString(path: String) {
    val primitive = this as? JsonPrimitive ?: invalidType(path, "string")
    if (!primitive.isString) invalidType(path, "string")
}

private fun JsonObject.validateInt(name: String, path: String) {
    this[name]?.validateInt("$path.$name")
}

private fun JsonObject.validateNullableInt(name: String, path: String) {
    val value = this[name] ?: return
    if (value is JsonNull) return
    value.validateInt("$path.$name")
}

private fun JsonElement.validateInt(path: String) {
    val primitive = this as? JsonPrimitive ?: invalidType(path, "integer")
    if (primitive.isString || primitive.intOrNull == null) invalidType(path, "integer")
}

private fun JsonObject.validateLong(name: String, path: String) {
    val value = this[name] ?: return
    val primitive = value as? JsonPrimitive ?: invalidType("$path.$name", "integer")
    if (primitive.isString || primitive.longOrNull == null) invalidType("$path.$name", "integer")
}

private fun JsonObject.validateBoolean(name: String, path: String) {
    val value = this[name] ?: return
    val primitive = value as? JsonPrimitive ?: invalidType("$path.$name", "boolean")
    if (primitive.isString || primitive.booleanOrNull == null) invalidType("$path.$name", "boolean")
}

private fun invalidType(path: String, expected: String): Nothing =
    throw IllegalArgumentException("Expected $expected at $path")
