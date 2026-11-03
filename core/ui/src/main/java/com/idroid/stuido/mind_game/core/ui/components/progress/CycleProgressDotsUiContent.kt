package com.idroid.stuido.mind_game.core.ui.components.progress

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.util.VisibilityConfig

@Immutable
data class CycleProgressDotsUiContent(
    val total: Int,
    val completed: Int,
    val contentDescription: String? = null,
)

@Immutable
data class CycleProgressDotsConfig(
    val visibility: VisibilityConfig = VisibilityConfig.VISIBLE,
    val highlightCurrent: Boolean = true,
)

@Immutable
data class CycleProgressDotsStyle(
    val completedColor: Color,
    val currentColor: Color,
    val pendingColor: Color,
    val dotSize: Dp = 8.dp,
    val spacing: Dp = 4.dp,
)

enum class DotState {
    COMPLETED,
    CURRENT,
    PENDING,
}

fun cycleDotStates(total: Int, completed: Int, highlightCurrent: Boolean): List<DotState> {
    if (total <= 0) return emptyList()

    val completedCount = completed.coerceIn(0, total)
    return List(total) { index ->
        when {
            index < completedCount -> DotState.COMPLETED
            index == completedCount && highlightCurrent -> DotState.CURRENT
            else -> DotState.PENDING
        }
    }
}
