package com.idroid.stuido.mind_game.feature.game.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 格内候选数字排布 [noteGridFor] 单元测试。
 *
 * 重点回归：每个 1..规格 的数字都必须落在 *唯一* 的 (行, 列) 槽位上，
 * 且槽位不重叠——否则 UI 侧会出现候选数字互相覆盖 / 部分数字不显示。
 */
class NoteGridTest {

    @Test
    fun `4x4 排成 2x2`() {
        val grid = noteGridFor(4)
        assertEquals(2, grid.cols)
        assertEquals(2, grid.rows)
    }

    @Test
    fun `6x6 排成 3x2`() {
        val grid = noteGridFor(6)
        assertEquals(3, grid.cols)
        assertEquals(2, grid.rows)
    }

    @Test
    fun `9x9 排成 3x3`() {
        val grid = noteGridFor(9)
        assertEquals(3, grid.cols)
        assertEquals(3, grid.rows)
    }

    @Test
    fun `1x1 与非法规格退化为单槽`() {
        assertEquals(NoteGrid(cols = 1, rows = 1), noteGridFor(1))
        // 规格 0/负数不应抛异常，也不应产生 0 行 0 列而让整块笔记消失。
        assertEquals(NoteGrid(cols = 1, rows = 1), noteGridFor(0))
    }

    @Test
    fun `9x9 每个数字的行列都落在 3x3 内且不会越界`() {
        val grid = noteGridFor(9)
        for (digit in 1..9) {
            assertTrue("digit=$digit row 越界", grid.rowOf(digit) in 0 until grid.rows)
            assertTrue("digit=$digit col 越界", grid.colOf(digit) in 0 until grid.cols)
        }
    }

    @Test
    fun `所有规格下数字与槽位一一对应`() {
        for (size in listOf(4, 6, 9)) {
            val grid = noteGridFor(size)
            val slots = (1..size).map { grid.rowOf(it) to grid.colOf(it) }
            assertEquals("size=$size 槽位必须唯一", size, slots.toSet().size)
            assertTrue("size=$size 槽位数不足以容纳所有数字", size <= grid.capacity)
        }
    }

    @Test
    fun `数字按从左到右从上到下顺序排列`() {
        val grid = noteGridFor(9)
        assertEquals(0 to 0, grid.rowOf(1) to grid.colOf(1)) // 1 左上
        assertEquals(1 to 0, grid.rowOf(4) to grid.colOf(4)) // 4 换行首列
        assertEquals(2 to 2, grid.rowOf(9) to grid.colOf(9)) // 9 右下
    }
}
