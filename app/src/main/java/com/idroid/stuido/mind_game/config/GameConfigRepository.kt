package com.idroid.stuido.mind_game.config

import android.content.Context
import android.util.Log
import com.idroid.stuido.mind_game.core.logic.config.GameConfigParser
import com.idroid.stuido.mind_game.core.logic.config.GameRemoteConfig

class GameConfigRepository(
    private val loadJson: () -> String?,
    private val onError: (String) -> Unit = { Log.w(TAG, it) },
) {
    val config: GameRemoteConfig by lazy { loadConfig() }

    private fun loadConfig(): GameRemoteConfig {
        val json = try {
            loadJson()
        } catch (exception: Exception) {
            return fallback(exception.message ?: "Failed to load game config.")
        } ?: return fallback("Game config is missing.")

        return GameConfigParser.parse(json).fold(
            onSuccess = { it },
            onFailure = { exception ->
                fallback(exception.message ?: "Failed to parse game config.")
            },
        )
    }

    private fun fallback(reason: String): GameRemoteConfig {
        onError(reason)
        return GameRemoteConfig()
    }

    companion object {
        private const val TAG = "GameConfigRepository"

        fun fromAssets(context: Context): GameConfigRepository = GameConfigRepository(
            loadJson = {
                context.assets.open("game_config.json").bufferedReader().use { it.readText() }
            },
        )
    }
}
