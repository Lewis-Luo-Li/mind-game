package com.idroid.stuido.mind_game.feature.game.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 3 —— 纯 UDF 几何/高亮工具 [BoardGeometry] 单元测试（4x4 与 9x9）。
 */
class BoardGeometryTest {

    // -------------- peersOf --------------

    @Test
    fun `4x4 peersOf 返回同行同列同宫且不含自身`() {
        val peers = BoardGeometry.peersOf(0, size = 4, boxWidth = 2, boxHeight = 2)
        assertEquals(setOf(1, 2, 3, 4, 5, 8, 12), peers)
        assertFalse("自身不在 peers", 0 in peers)
    }

    @Test
    fun `9x9 中央格 peersOf 精确`() {
        val peers = BoardGeometry.peersOf(40, size = 9, boxWidth = 3, boxHeight = 3)
        val row = (36..44).toSet() - 40
        val col = ((4 until 81) step 9).toSet() - 40
        val box = setOf(30, 31, 32, 39, 40, 41, 48, 49, 50) - 40
        assertEquals(row + col + box, peers)
        assertFalse(40 in peers)
    }

    @Test
    fun `越界 cell 返回空 peers`() {
        assertTrue(BoardGeometry.peersOf(999, 4, 2, 2).isEmpty())
    }

    // -------------- duplicateCells --------------

    private fun grid(vararg filled: Pair<Int, Int>): List<Int> {
        val a = MutableList(16) { 0 }
        filled.forEach { (idx, v) -> a[idx] = v }
        return a
    }

    @Test
    fun `同行重复的两格均被标出`() {
        val repeated = BoardGeometry.duplicateCells(grid(0 to 1, 1 to 1), 4, 2, 2)
        assertTrue(repeated.contains(0) && repeated.contains(1))
    }

    @Test
    fun `同列重复的两格均被标出`() {
        val repeated = BoardGeometry.duplicateCells(grid(0 to 1, 4 to 1), 4, 2, 2)
        assertTrue(repeated.contains(0) && repeated.contains(4))
    }

    @Test
    fun `同宫重复的两格均被标出`() {
        val repeated = BoardGeometry.duplicateCells(grid(0 to 1, 5 to 1), 4, 2, 2)
        assertTrue(repeated.contains(0) && repeated.contains(5))
    }

    @Test
    fun `无重复的合法面返回空`() {
        val solved = listOf(1, 2, 3, 4, 3, 4, 1, 2, 2, 1, 4, 3, 4, 3, 2, 1)
        assertTrue(BoardGeometry.duplicateCells(solved, 4, 2, 2).isEmpty())
    }

    @Test
    fun `空格(值为0)不参与冲突判定`() {
        assertTrue(BoardGeometry.duplicateCells(grid(2 to 1), 4, 2, 2).isEmpty())
    }

    @Test
    fun `远离的同值(不同行不同列不同宫)不冲突`() {
        // 格0(row0,col0,左上宫) 与 格10(row2,col2,右下宫) 值相同互不影响
        val repeated = BoardGeometry.duplicateCells(grid(0 to 1, 10 to 1), 4, 2, 2)
        assertTrue(repeated.isEmpty())
    }
}
