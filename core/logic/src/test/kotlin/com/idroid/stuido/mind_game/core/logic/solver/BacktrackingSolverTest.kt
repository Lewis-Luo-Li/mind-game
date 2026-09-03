package com.idroid.stuido.mind_game.core.logic.solver

import com.idroid.stuido.mind_game.core.logic.model.SudokuBoardConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 求解器（BacktrackingSolver）单元测试。
 */
class BacktrackingSolverTest {

    private val solver = BacktrackingSolver()

    private fun config(size: Int, boxWidth: Int, boxHeight: Int) = SudokuBoardConfig(
        size = size,
        boxWidth = boxWidth,
        boxHeight = boxHeight,
        solution = IntArray(size * size),
        puzzle = IntArray(size * size),
        holes = 0,
    )

    private fun boardOf(size: Int, vararg values: Int): IntArray {
        require(values.size == size * size) { "数组长度必须为 size*size" }
        return intArrayOf(*values)
    }

    private fun assertValidSolution(board: IntArray, size: Int, boxWidth: Int, boxHeight: Int) {
        assert(board.all { it != 0 }) { "board 不应包含空位(0)" }

        for (r in 0 until size) {
            assert(board.sliceArray(r * size until r * size + size).distinct().size == size) { "第 $r 行取值重复" }
        }
        for (c in 0 until size) {
            val col = IntArray(size) { board[it * size + c] }
            assert(col.distinct().size == size) { "第 $c 列取值重复" }
        }
        for (boxR in 0 until size / boxHeight) {
            for (boxC in 0 until size / boxWidth) {
                val seen = mutableSetOf<Int>()
                for (r in boxR * boxHeight until (boxR + 1) * boxHeight) {
                    for (c in boxC * boxWidth until (boxC + 1) * boxWidth) {
                        seen += board[r * size + c]
                    }
                }
                assert(seen.size == size) { "宫 ($boxR,$boxC) 取值重复" }
            }
        }
    }

    private fun validConfigs(): List<Triple<Int, Int, Int>> = listOf(
        Triple(4, 2, 2),
        Triple(6, 3, 2),
        Triple(9, 3, 3),
    )

    @Test
    fun `空白盘可以求解出完整合法终盘`() {
        for ((size, w, h) in validConfigs()) {
            val empty = IntArray(size * size)
            val result = solver.solve(empty, config(size, w, h))
            assertNotNull("size=$size 应能解出终盘", result)
            assertValidSolution(result!!, size, w, h)
        }
    }

    @Test
    fun `solve 不修改传入的棋盘数组`() {
        val size = 9
        val given = IntArray(size * size)
        val snapshot = given.copyOf()
        solver.solve(given, config(size, 3, 3))
        assertArrayEquals("solve 不应修改入参", snapshot, given)
    }

    @Test
    fun `已知唯一解盘 countSolutions 返回 1`() {
        val board = boardOf(
            4,
            1, 2, 3, 4,
            3, 4, 1, 2,
            2, 1, 0, 3,
            4, 3, 2, 1,
        )
        assertEquals(1, solver.countSolutions(board, config(4, 2, 2), limit = 2))
    }

    @Test
    fun `空 4x4 盘产生多解 countSolutions 返回大于 1`() {
        val board = IntArray(16)
        val count = solver.countSolutions(board, config(4, 2, 2), limit = 2)
        assert(count >= 2) { "空 4x4 盘应为多解, 实际 count=$count" }
    }

    @Test
    fun `无解盘 countSolutions 返回 0`() {
        val board = boardOf(
            4,
            1, 1, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
        )
        val solved = solver.solve(board, config(4, 2, 2))
        assertNull("矛盾盘应无解", solved)
        assertEquals(0, solver.countSolutions(board, config(4, 2, 2), limit = 2))
    }

    @Test
    fun `countSolutions 遵守 limit 剪枝 - 多解盘 limit=1 返回 1`() {
        val board = IntArray(16)
        assertEquals(1, solver.countSolutions(board, config(4, 2, 2), limit = 1))
        assertEquals(2, solver.countSolutions(board, config(4, 2, 2), limit = 2))
    }

    @Test
    fun `countSolutions 不修改传入的棋盘数组`() {
        val size = 9
        val board = IntArray(size * size)
        val snapshot = board.copyOf()
        solver.countSolutions(board, config(size, 3, 3), limit = 2)
        assertArrayEquals("countSolutions 不应修改入参", snapshot, board)
    }

    @Test
    fun `完整终盘 countSolutions 返回 1`() {
        for ((size, w, h) in validConfigs()) {
            val empty = IntArray(size * size)
            val solved = solver.solve(empty, config(size, w, h))!!
            assertEquals("size=$size 完整终盘应为唯一解", 1, solver.countSolutions(solved, config(size, w, h), limit = 2))
        }
    }

    @Test
    fun `六格与九宫规格空白盘求解合法`() {
        val s6 = solver.solve(IntArray(36), config(6, 3, 2))
        assertNotNull(s6)
        assertValidSolution(s6!!, 6, 3, 2)

        val s9 = solver.solve(IntArray(81), config(9, 3, 3))
        assertNotNull(s9)
        assertValidSolution(s9!!, 9, 3, 3)
    }
}
