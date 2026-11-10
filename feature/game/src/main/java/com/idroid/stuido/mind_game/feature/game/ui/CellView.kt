package com.idroid.stuido.mind_game.feature.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    digitFontSize: TextUnit = mainDigitFontSize(size),
    noteFontSize: TextUnit = noteFontSize(size),
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
                fontSize = digitFontSize,
                textAlign = TextAlign.Center,
            )
        } else if (notes.isNotEmpty()) {
            NotesGrid(notes = notes, maxDigit = size, noteFontSize = noteFontSize)
        }
    }
}

/**
 * 格内候选数字的排布模板：[cols] 列 × [rows] 行，数字 d 固定落在
 * 第 `(d-1)/cols` 行、第 `(d-1)%cols` 列（即 1 左上、最大值右下）。
 * 位置固定是关键：候选位随数字移动会让人无法扫视比较。
 */
internal data class NoteGrid(val cols: Int, val rows: Int) {

    /** 该数字落在第几行（0-based）。 */
    fun rowOf(digit: Int): Int = (digit - 1) / cols

    /** 该数字落在第几列（0-based）。 */
    fun colOf(digit: Int): Int = (digit - 1) % cols

    /** 该格子（= 该行该列组成的格）能容纳的槽位数上限。 */
    val capacity: Int get() = cols * rows
}

/** 候选数字最多排到 9x9 的 9 个，列数取规格平方根向上取整，封顶 3 列。 */
internal const val MAX_NOTE_DIGITS = 9

/** 列数上限：9x9 时正好 3x3。 */
internal const val MAX_NOTE_COLS = 3

/** 按盘面规格算出候选数字的排布：4x4→2x2，6x6→3x2，9x9→3x3。 */
internal fun noteGridFor(maxDigit: Int): NoteGrid {
    val cols = ceil(sqrt(maxDigit.toDouble())).toInt().coerceIn(1, MAX_NOTE_COLS)
    val cap = maxDigit.coerceAtMost(MAX_NOTE_DIGITS)
    val rows = ceil(cap.toDouble() / cols).toInt().coerceAtLeast(1)
    return NoteGrid(cols = cols, rows = rows)
}

@Composable
private fun NotesGrid(notes: Set<Int>, maxDigit: Int, noteFontSize: TextUnit = noteFontSize(maxDigit)) {
    val noteColor = MaterialTheme.colorScheme.onSurfaceVariant
    val grid = remember(maxDigit) { noteGridFor(maxDigit) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
    ) {
        repeat(grid.rows) { r ->
            // 行必须按权重分高、宽占满；否则 Row 内首个 fillMaxSize 子项会独吞整行宽度，
            // 其余候选列被压成 0 宽而完全不显示。
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(grid.cols) { c ->
                    val digit = r * grid.cols + c + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (digit in notes) {
                            Text(
                                text = digit.toString(),
                                color = noteColor,
                                fontSize = noteFontSize,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun mainDigitFontSize(size: Int): TextUnit = when {
    size <= 4 -> 24.sp
    size <= 6 -> 20.sp
    else -> 16.sp
}

internal fun noteFontSize(size: Int): TextUnit = when {
    size <= 4 -> 11.sp
    size <= 6 -> 10.sp
    else -> 8.sp
}
