package com.idroid.stuido.mind_game.core.logic

import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.core.logic.config.DifficultyConfig
import com.idroid.stuido.mind_game.core.logic.generator.SudokuGenerator
import com.idroid.stuido.mind_game.core.logic.solver.BacktrackingSolver
import com.idroid.stuido.mind_game.core.logic.validator.PuzzleValidationResult
import com.idroid.stuido.mind_game.core.logic.validator.PuzzleValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 综合可靠性测试（对应实现计划 §7）。
 *
 * 覆盖：
 *  - 终盘生成冒泡测试（连续生成大量终盘，断言 100% 满足行列宫规则）
 *  - 唯一解硬性测试（对每个规格、每个难度随机抽取多个谜题，断言恒为唯一解）
 *  - 多解/无解检测
 *  - 种子一致性（固定种子跨调用生成完全一致）
 *  - 每日种子混合（同一 seed 下不同规格生成不同谜题）
 */
class PuzzleReliabilityTest {

    private val validator = BacktrackingSolver()
    private val puzzleValidator = PuzzleValidator()
    private val difficultyConfig = DifficultyConfig.default()

    // ---------- 工具方法 ----------

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

    private fun emptyConfig(size: Int, boxWidth: Int, boxHeight: Int, board: IntArray) = run {
        // 针对 countSolutions 的临时配置
        com.idroid.stuido.mind_game.core.logic.model.SudokuBoardConfig(
            size = size,
            boxWidth = boxWidth,
            boxHeight = boxHeight,
            solution = board,
            puzzle = board,
            holes = 0,
        )
    }

    // ---------- 1. 终盘生成冒泡测试 ----------

    @Test
    fun `终盘生成冒泡测试 - 连续生成一万个终盘 100pct 满足规则`() {
        val total = 10_000
        val specs = listOf(
            BoardSpec.MINI,
            BoardSpec.MEDIUM,
            BoardSpec.CLASSIC,
        )
        var generated = 0

        // 三种规格循环生成，累计 1 万个终盘
        while (generated < total) {
            for (spec in specs) {
                val solver = BacktrackingSolver { _, s -> (1..s).shuffled() }
                val empty = IntArray(spec.size * spec.size)
                val board = solver.solve(
                    empty,
                    emptyConfig(spec.size, spec.boxWidth, spec.boxHeight, empty),
                ) ?: throw AssertionError("size=${spec.size} 终盘生成失败")
                assertValidSolution(board, spec.size, spec.boxWidth, spec.boxHeight)
                generated++
                if (generated >= total) break
            }
        }

        assertEquals(total, generated)
    }

    // ---------- 2. 唯一解硬性测试 ----------

    @Test
    fun `唯一解硬性测试 - 每个规格每个难度抽样 100 个谜题均唯一解`() {
        val generator = SudokuGenerator(maxRetry = 10)
        val perDifficulty = 100

        for (spec in BoardSpec.entries) {
            for (difficulty in difficultyConfig.availableDifficulties(spec)) {
                val holes = difficultyConfig.holeCount(spec, difficulty)!!
                repeat(perDifficulty) {
                    val p = generator.generate(spec.size, spec.boxWidth, spec.boxHeight, holes)
                    val count = validator.countSolutions(p.puzzle, emptyConfig(spec.size, spec.boxWidth, spec.boxHeight, p.puzzle), limit = 2)
                    assertEquals("$spec/$difficulty 应为唯一解", 1, count)
                }
            }
        }
    }

    @Test
    fun `难度全覆盖 - 所有可用难度组合均可生成唯一解谜题`() {
        val generator = SudokuGenerator(maxRetry = 10)
        for (spec in BoardSpec.entries) {
            for (difficulty in difficultyConfig.availableDifficulties(spec)) {
                val holes = difficultyConfig.holeCount(spec, difficulty)!!
                val p = generator.generate(spec.size, spec.boxWidth, spec.boxHeight, holes)
                assertEquals(holes, p.holes)
                assertEquals(
                    "$spec/$difficulty 应通过校验器",
                    PuzzleValidationResult.VALID,
                    puzzleValidator.validate(p),
                )
            }
        }
    }

    // ---------- 3. 多解 / 无解检测 ----------

    @Test
    fun `多解检测 - 空盘被判定为多解`() {
        val board = IntArray(16)
        assertEquals(
            PuzzleValidationResult.MULTIPLE_SOLUTIONS,
            puzzleValidator.validate(board, emptyConfig(4, 2, 2, board)),
        )
    }

    @Test
    fun `无解检测 - 矛盾盘被判定为无解`() {
        val board = intArrayOf(
            1, 1, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
        )
        assertEquals(
            PuzzleValidationResult.NO_SOLUTION,
            puzzleValidator.validate(board, emptyConfig(4, 2, 2, board)),
        )
    }

    // ---------- 4. 种子一致性 ----------

    @Test
    fun `种子一致性 - 固定种子跨调用生成完全一致谜题`() {
        val seed = 12345L
        val generatorA = SudokuGenerator()
        val generatorB = SudokuGenerator()

        for (spec in BoardSpec.entries) {
            val holes = difficultyConfig.holeCount(spec, Difficulty.MEDIUM)!!
            val a = generatorA.generate(spec.size, spec.boxWidth, spec.boxHeight, holes, seed = seed)
            val b = generatorB.generate(spec.size, spec.boxWidth, spec.boxHeight, holes, seed = seed)
            assertArrayEquals("$spec puzzle 数组应一致", a.puzzle, b.puzzle)
            assertArrayEquals("$spec solution 数组应一致", a.solution, b.solution)
        }
    }

    @Test
    fun `每日种子混合 - 同一 seed 下不同规格谜题不同`() {
        val seed = 20_260_817L
        val generator = SudokuGenerator()

        val four = generator.generate(4, 2, 2, 5, seed = seed).puzzle
        val six = generator.generate(6, 3, 2, 14, seed = seed).puzzle
        val nine = generator.generate(9, 3, 3, 42, seed = seed).puzzle

        assertTrue("4x4 谜题非空", four.isNotEmpty())
        assertTrue("6x6 谜题非空", six.isNotEmpty())
        assertTrue("9x9 谜题非空", nine.isNotEmpty())

        // 三种规格尺寸不同，数组必然不同（逐一比较）
        assertTrue("4x4 与 6x6 不应相同", !four.contentEquals(six))
        assertTrue("6x6 与 9x9 不应相同", !six.contentEquals(nine))
        assertTrue("4x4 与 9x9 不应相同", !four.contentEquals(nine))
    }
}
