package com.idroid.stuido.mind_game.core.logic.validator

import com.idroid.stuido.mind_game.core.logic.generator.SudokuGenerator
import com.idroid.stuido.mind_game.core.logic.model.SudokuBoardConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 谜题校验器（PuzzleValidator）单元测试。
 */
class PuzzleValidatorTest {

    private val validator = PuzzleValidator()

    private fun configOf(size: Int, boxWidth: Int, boxHeight: Int, puzzle: IntArray): SudokuBoardConfig =
        SudokuBoardConfig(
            size = size,
            boxWidth = boxWidth,
            boxHeight = boxHeight,
            solution = IntArray(size * size),
            puzzle = puzzle,
            holes = 0,
        )

    // ---------- 判定结果 ----------

    @Test
    fun `空盘被判定为多解`() {
        val board = IntArray(16) // 4x4 全空
        assertEquals(
            PuzzleValidationResult.MULTIPLE_SOLUTIONS,
            validator.validate(board, configOf(4, 2, 2, board)),
        )
    }

    @Test
    fun `仅有单空格的唯一解盘被判定为合法`() {
        // 一个完整合法终盘仅空 1 格 => 唯一解
        val board = intArrayOf(
            1, 2, 3, 4,
            3, 4, 1, 2,
            2, 1, 0, 3,
            4, 3, 2, 1,
        )
        assertEquals(PuzzleValidationResult.VALID, validator.validate(board, configOf(4, 2, 2, board)))
        assertTrue(validator.isValid(board, configOf(4, 2, 2, board)))
    }

    @Test
    fun `矛盾盘被判定为无解`() {
        // 同一行出现两个 1 => 无解
        val board = intArrayOf(
            1, 1, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
        )
        assertEquals(PuzzleValidationResult.NO_SOLUTION, validator.validate(board, configOf(4, 2, 2, board)))
        assertFalse(validator.isValid(board, configOf(4, 2, 2, board)))
    }

    @Test
    fun `完整终盘被判定为合法唯一解`() {
        val solved = intArrayOf(
            1, 2, 3, 4,
            3, 4, 1, 2,
            2, 1, 4, 3,
            4, 3, 2, 1,
        )
        assertEquals(PuzzleValidationResult.VALID, validator.validate(solved, configOf(4, 2, 2, solved)))
    }

    // ---------- 集成：校验生成器产物 ----------

    @Test
    fun `生成器产物通过校验器`() {
        val generator = SudokuGenerator(maxRetry = 8)
        // 抽查三种规格的生成谜题均可通过唯一解校验
        val specs = listOf(
            Triple(4, 2, 2),
            Triple(6, 3, 2),
            Triple(9, 3, 3),
        )
        for ((size, w, h) in specs) {
            val holes = when (size) {
                4 -> 3
                6 -> 10
                else -> 36
            }
            val p = generator.generate(size, w, h, holes)
            assertEquals(
                "size=$size 生成谜题应通过唯一解校验",
                PuzzleValidationResult.VALID,
                validator.validate(p),
            )
            assertTrue(validator.isValid(p))
        }
    }
}
