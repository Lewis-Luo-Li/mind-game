package com.idroid.stuido.mind_game.feature.game.engine

import com.idroid.stuido.mind_game.feature.game.mvi.BoardCells

/**
 * 输入校验器：纯函数地判定：
 *  - 同值冲突（同行 / 同列 / 同宫已被占）；
 *  - 是否填错终端值（用于增加次数）；
 *  - 是否已把全盘填满且正确（胜利）。
 *
 * 所有方法都用 flat 数组索引 `row * size + col`，完全不接触 `core:logic` 求解器，
 * 因此可在主线程安全地即时调用。
 */
class InputValidator {

    /**
     * 计算"填入 value 于 index 后会与哪些现有格子冲突"。给定格（givens）也算现有值。
     *
     * 返回的集合是**受影响的格子索引**（冲突源），可用于 UI 红色高亮。
     */
    fun conflictsWith(
        board: BoardCells,
        index: Int,
        value: Int,
        size: Int,
        boxWidth: Int,
        boxHeight: Int,
    ): Set<Int> {
        if (index !in 0 until board.values.size || value == 0) return emptySet()
        val row = index / size
        val col = index % size
        val boxLeftTopRow = (row / boxHeight) * boxHeight
        val boxLeftTopCol = (col / boxWidth) * boxWidth

        val conflicted = mutableSetOf<Int>()
        for (i in board.values.indices) {
            if (i == index) continue
            if (board.values[i] != value) continue
            val r = i / size
            val c = i % size
            val sameRow = r == row
            val sameCol = c == col
            val sameBox = (r >= boxLeftTopRow && r < boxLeftTopRow + boxHeight &&
                           c >= boxLeftTopCol && c < boxLeftTopCol + boxWidth)
            if (sameRow || sameCol || sameBox) conflicted.add(i)
        }
        return conflicted
    }

    /** 快捷形式：若内部已持有 [BoardCells] 的规格参数则更直白，否则传入 size。 */
    fun hasConflict(
        board: BoardCells,
        index: Int,
        value: Int,
        size: Int,
        boxWidth: Int,
        boxHeight: Int,
    ): Boolean = conflictsWith(board, index, value, size, boxWidth, boxHeight).isNotEmpty()

    /**
     * 判定将 [value] 填入 [index] 后，是否为"可填写的合法值"：
     * 与现有值不冲突（不考虑是否匹配终盘——错但对的方法是合法可写，但会触发判错）。
     */
    fun isLegalPlacement(
        board: BoardCells,
        index: Int,
        value: Int,
        size: Int,
        boxWidth: Int,
        boxHeight: Int,
    ): Boolean = !hasConflict(board, index, value, size, boxWidth, boxHeight)

    /**
     * 判定全盘是否已被正确填满（胜利条件）。
     */
    fun isComplete(board: BoardCells): Boolean =
        board.solution.isNotEmpty() &&
            board.values.indices.all { i -> board.values[i] != 0 && board.values[i] == board.solution[i] }
}
