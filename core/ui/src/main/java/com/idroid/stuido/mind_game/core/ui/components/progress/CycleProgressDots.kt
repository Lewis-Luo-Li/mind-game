package com.idroid.stuido.mind_game.core.ui.components.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.core.ui.util.isHidden

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CycleProgressDots(
    modifier: Modifier = Modifier,
    uiContent: CycleProgressDotsUiContent,
    uiConfig: CycleProgressDotsConfig,
    uiStyle: CycleProgressDotsStyle,
) {
    if (uiConfig.visibility.isHidden()) return

    val states = cycleDotStates(uiContent.total, uiContent.completed, uiConfig.highlightCurrent)
    if (states.isEmpty()) return

    FlowRow(
        modifier = modifier.clearAndSetSemantics {
            uiContent.contentDescription?.let { contentDescription = it }
        },
        horizontalArrangement = Arrangement.spacedBy(uiStyle.spacing),
        verticalArrangement = Arrangement.spacedBy(uiStyle.spacing),
    ) {
        states.forEach { state ->
            val color = when (state) {
                DotState.COMPLETED -> uiStyle.completedColor
                DotState.CURRENT -> uiStyle.currentColor
                DotState.PENDING -> uiStyle.pendingColor
            }
            Box(
                modifier = Modifier
                    .size(uiStyle.dotSize)
                    .background(color, CircleShape),
            )
        }
    }
}
