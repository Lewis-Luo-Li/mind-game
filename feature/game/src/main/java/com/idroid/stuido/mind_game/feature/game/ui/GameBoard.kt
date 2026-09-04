package com.idroid.stuido.mind_game.feature.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.idroid.stuido.mind_game.feature.game.mvi.BoardCells

/**
 * 自适应渲染 4x4 / 6x6 / 9x9 棋盘。网格线用 Canvas 叠加到均分格子上方，任意规格规整。
 */
@Composable
fun GameBoard(
    board: BoardCells,
    selectedIndex: Int?,
    checkFlags: Set<Int>,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val size = board.size
    if (size <= 0 || board.values.size != size * size) return

    val boxWidth = when (size) { 4 -> 2; 6 -> 3; else -> 3 }
    val boxHeight = when (size) { 4 -> 2; 6 -> 2; else -> 3 }

    val selectedValue = selectedIndex?.let { board.values.getOrNull(it) }
    val selectedPeers = selectedIndex?.let { BoardGeometry.peersOf(it, size, boxWidth, boxHeight) } ?: emptySet()
    val duplicates = remember(board.values) {
        BoardGeometry.duplicateCells(board.values, size, boxWidth, boxHeight)
    }

    Box(
        modifier = modifier
            .padding(10.dp)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            repeat(size) { r ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    repeat(size) { c ->
                        val i = r * size + c
                        val value = board.values[i]
                        val isGiven = i in board.givens
                        val isSelected = i == selectedIndex

                        val sameValueAsSelection = selectedValue != null &&
                            selectedIndex != i && value == selectedValue
                        val isSoft = (selectedIndex != null && selectedIndex != i && i in selectedPeers) ||
                            sameValueAsSelection
                        val inDuplicate = i in duplicates
                        val isCheckFlag = i in checkFlags
                        val conflict = (inDuplicate || isCheckFlag) && !isSelected && value != 0

                        CellView(
                            value = value,
                            notes = board.notes[i].orEmpty(),
                            isGiven = isGiven,
                            size = size,
                            visual = CellVisual(
                                isSelected = isSelected,
                                isConflict = conflict,
                                isSoftHighlight = isSoft && !inDuplicate && !isSelected,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            onClick = { onCellClick(i) },
                        )
                    }
                }
            }
        }
        GridLinesOverlay(size = size, boxWidth = boxWidth, boxHeight = boxHeight)
    }
}

@Composable
private fun GridLinesOverlay(size: Int, boxWidth: Int, boxHeight: Int) {
    val colorScheme = MaterialTheme.colorScheme
    val major = colorScheme.onSurface
    val minor = colorScheme.outlineVariant
    val density = LocalDensity.current
    val majorWidth = with(density) { 2.dp.toPx() }
    val minorWidth = with(density) { 0.75.dp.toPx() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = this.size.width
        val h = this.size.height
        for (i in 1 until size) {
            val x = w * i / size
            val isMajorV = i % boxWidth == 0
            drawLine(
                color = if (isMajorV) major else minor,
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = if (isMajorV) majorWidth else minorWidth,
            )
            val y = h * i / size
            val isMajorH = i % boxHeight == 0
            drawLine(
                color = if (isMajorH) major else minor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = if (isMajorH) majorWidth else minorWidth,
            )
        }
    }
}
