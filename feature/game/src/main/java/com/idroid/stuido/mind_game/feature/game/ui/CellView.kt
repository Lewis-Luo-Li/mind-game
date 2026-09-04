package com.idroid.stuido.mind_game.feature.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import kotlin.math.sqrt

/**
 * Single cell render state: computed by parent GameBoard, kept "dumb" rendering here.
 */
data class CellVisual(
    val isSelected: Boolean = false,
    val isConflict: Boolean = false,
    val isSoftHighlight: Boolean = false,
)

@Composable
fun CellView(
    value: Int,
    notes: Set<Int>,
    isGiven: Boolean,
    size: Int,
    visual: CellVisual,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme

    val bg = when {
        visual.isConflict -> colorScheme.errorContainer.copy(alpha = 0.55f)
        visual.isSelected -> colorScheme.primaryContainer
        visual.isSoftHighlight -> colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    val numberColor = when {
        visual.isConflict -> colorScheme.onErrorContainer
        isGiven -> colorScheme.onSurface
        value != 0 -> colorScheme.primary
        else -> colorScheme.onSurfaceVariant
    }
    val fontWeight = if (isGiven) FontWeight.Bold else FontWeight.SemiBold

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (value != 0) {
            Text(
                text = value.toString(),
                color = numberColor,
                fontWeight = fontWeight,
                fontSize = mainDigitFontSize(size),
                textAlign = TextAlign.Center,
            )
        } else if (notes.isNotEmpty()) {
            NotesGrid(notes = notes, maxDigit = size)
        }
    }
}

@Composable
private fun NotesGrid(notes: Set<Int>, maxDigit: Int) {
    val cols = ceil(sqrt(maxDigit.toDouble())).toInt().coerceIn(1, 3)
    val cap = maxDigit.coerceAtMost(9)
    val maxRows = ceil(cap.toDouble() / cols).toInt().coerceAtLeast(1)
    val noteColor = MaterialTheme.colorScheme.onSurfaceVariant
    val fontSize = noteFontSize(maxDigit)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(3.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        repeat(maxRows) { r ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                repeat(cols) { c ->
                    val digit = r * cols + c + 1
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (digit in notes) {
                            Text(
                                text = digit.toString(),
                                color = noteColor,
                                fontSize = fontSize,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun mainDigitFontSize(size: Int): TextUnit = when {
    size <= 4 -> 24.sp
    size <= 6 -> 20.sp
    else -> 16.sp
}

private fun noteFontSize(size: Int): TextUnit = when {
    size <= 4 -> 11.sp
    size <= 6 -> 10.sp
    else -> 8.sp
}
