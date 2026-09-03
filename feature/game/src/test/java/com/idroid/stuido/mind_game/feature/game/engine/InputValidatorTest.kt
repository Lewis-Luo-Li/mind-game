package com.idroid.stuido.mind_game.feature.game.engine

import com.idroid.stuido.mind_game.feature.game.mvi.BoardCells
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputValidatorTest {

    private val validator = InputValidator()

    private val solution = listOf(
        1, 2, 3, 4,
        3, 4, 1, 2,
        2, 1, 4, 3,
        4, 3, 2, 1,
    )

    // 给定若干初始数字，其余为空（值 0）
    private fun board(vararg placed: Pair<Int, Int>) = BoardCells(
        size = 4,
        values = List(16) { i -> placed.firstOrNull { it.first == i }?.second ?: 0 },
        givens = placed.map { it.first }.toSet(),
        solution = solution,
        notes = emptyMap(),
    )

    @Test
    fun `conflictsWith 检测同行`() {
        // 在 (0,0)=1, (0,1)=2 时，尝试在 (0,2) 放 1 应同行冲突
        val b = board(0 to 1, 1 to 2)
        val conflicts = validator.conflictsWith(b, index = 2, value = 1, size = 4, boxWidth = 2, boxHeight = 2)
        assertTrue("应检测选中格(0,2)与(0,0)同行冲突", conflicts.contains(0))
    }

    @Test
    fun `conflictsWith 检测同列`() {
        // 在 (0,0)=1 时，尝试在 (2,0) 放 1 应同列冲突
        val b = board(0 to 1)
        val conflicts = validator.conflictsWith(b, index = 8, value = 1, size = 4, boxWidth = 2, boxHeight = 2)
        assertTrue(conflicts.contains(0))
    }

    @Test
    fun `conflictsWith 检测同宫`() {
        // 2x2 宫：单元格(0,1) 与其同宫有 (1,0)。在 (0,1) 放 (1,0) 已有的值 3 会同宫冲突
        val b = board(4 to 3) // (1,0)=3
        val conflicts = validator.conflictsWith(b, index = 1, value = 3, size = 4, boxWidth = 2, boxHeight = 2)
        assertTrue("应检测同宫(左上 2x2)冲突", conflicts.contains(4))
    }

    @Test
    fun `conflictsWith 无冲突时返回空`() {
        val b = board(1 to 2, 4 to 3)
        val conflicts = validator.conflictsWith(b, index = 0, value = 1, size = 4, boxWidth = 2, boxHeight = 2)
        assertTrue(conflicts.isEmpty())
    }

    @Test
    fun `isLegalPlacement 与 hasConflict 一致`() {
        val b = board(0 to 1)
        assertFalse(validator.isLegalPlacement(b, 1, 1, 4, 2, 2)) // 与(0,0)=1 同行
        assertTrue(validator.isLegalPlacement(b, 2, 2, 4, 2, 2))
    }

    @Test
    fun `isComplete 仅当全盘正确填满为 true`() {
        val incomplete = board() // 全空
        assertFalse(validator.isComplete(incomplete))
        val partial = board(0 to 1, 1 to 2, 2 to 3, 3 to 4)
        assertFalse(validator.isComplete(partial))
        // 完整且正确
        val full = BoardCells(size = 4, values = solution, givens = (0 until 16).toSet(), solution = solution, notes = emptyMap())
        assertTrue(validator.isComplete(full))
        // 完整但有一个填错
        val wrongFull = BoardCells(size = 4, values = solution.mapIndexed { i, v -> if (i == 0) 2 else v }, givens = (0 until 16).toSet(), solution = solution, notes = emptyMap())
        assertFalse(validator.isComplete(wrongFull))
    }
}
