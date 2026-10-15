package com.idroid.stuido.mind_game.core.logic.config

import kotlinx.serialization.Serializable

/** Lives and hints configured for one game. */
@Serializable
data class GameRules(
    val lives: Int = 3,
    val hints: Int = 3,
)
