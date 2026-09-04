package com.idroid.stuido.mind_game.feature.game.ui

/**
 * 纯 UDF 派生的棋盘几何/高亮工具：输入扁平盘面与规格，产出用于渲染的可索引集合。
 * 无副作用，便于单测。
 */
object BoardGeometry {

    /**
     * 一个格子的"同线 peers"：与其同行、同列、同宫的其它格子集合（不含自身）。
     */
    fun peersOf(
        cell: Int,
        size: Int,
        boxWidth: Int,
        boxHeight: Int,
    ): Set<Int> {
        val total = size * size
        if (cell !in 0 until total) return emptySet()
        val row = cell / size
        val col = cell % size
        val boxTopRow = (row / boxHeight) * boxHeight
        val boxLeftCol = (col / boxWidth) * boxWidth

        val peers = mutableSetOf<Int>()
        for (i in 0 until total) {
            if (i == cell) continue
            val r = i / size
            val c = i % size
            val sameRow = r == row
            val sameCol = c == col
            val sameBox = r in boxTopRow until (boxTopRow + boxHeight) &&
                c in boxLeftCol until (boxLeftCol + boxWidth)
            if (sameRow || sameCol || sameBox) peers.add(i)
        }
        return peers
    }

    /**
     * 当前盘面上已填且存在"同值重复"(同行/列/宫)的全部格子。
     * 结果用于涂红冲突格。
     */
    fun duplicateCells(
        values: List<Int>,
        size: Int,
        boxWidth: Int,
        boxHeight: Int,
    ): Set<Int> {
        val total = size * size
        val repeated = mutableSetOf<Int>()
        for (i in 0 until total) {
            val v = values.getOrNull(i) ?: 0
            if (v == 0) continue
            // 与 peers 中同值即为重复（只会影响双方，若某 peer 也是同一重复则都被标记）
            val peers = peersOf(i, size, boxWidth, boxHeight)
            if (peers.any { p -> (values.getOrNull(p) ?: 0) == v }) repeated.add(i)
        }
        return repeated
    }
}
