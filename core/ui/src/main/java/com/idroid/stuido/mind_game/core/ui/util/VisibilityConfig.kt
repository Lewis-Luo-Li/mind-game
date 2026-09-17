package com.idroid.stuido.mind_game.core.ui.util

enum class VisibilityConfig {
    VISIBLE,
    HIDDEN,
}

fun VisibilityConfig.isVisible(): Boolean = this == VisibilityConfig.VISIBLE
fun VisibilityConfig.isHidden(): Boolean = this == VisibilityConfig.HIDDEN
