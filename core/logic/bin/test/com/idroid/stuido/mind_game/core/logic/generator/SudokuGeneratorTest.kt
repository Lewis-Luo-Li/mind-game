package com.idroid.stuido.mind_game.core.logic.generator

import com.idroid.stuido.mind_game.core.logic.model.SudokuBoardConfig
import com.idroid.stuido.mind_game.core.logic.solver.BacktrackingSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * 生成器（SudokuGenerator）单元测试。
 *
 * 覆盖（对应实现计划 §7）：
 *  - 全规格终盘生成合法性
 *  - 挖空数量准确性
 *  - 唯一解硬性校验（每个规格随机生成多个谜题均要求 countSolutions == 1）
 *  - 种子一致性（固定种子跨调用生成完全一致）
 *  - 每日种子混合（同一 seed 下不同规格生成不同谜题）
 *  - 极端小盘多解触发重试
 */
class SudokuGeneratorTest {

    private val validator = BacktrackingSolver()

    private fun configOf(p: SudokuBoardConfig) = SudokuBoardConfig(
        size = p.size,
        boxWidth = p.boxWidth,
        boxHeight = p.boxHeight,
        solution = p.solution,
        puzzle = p.puzzle,
        holes = p.holes,
    )

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

    private fun assertDigConsistent(p: SudokuBoardConfig) {
        val dugCount = p.puzzle.count { it == 0 }
        assertEquals("实际挖空数量应等于 holes", p.holes, dugCount)
        for (i in p.puzzle.indices) {
            if (p.puzzle[i] != 0) {
                assertEquals("非空格应与 solution 一致", p.solution[i], p.puzzle[i])
            }
        }
    }

    private data class Spec(val size: Int, val boxW: Int, val boxH: Int)

    private val specs = listOf(
        Spec(4, 2, 2),
        Spec(6, 3, 2),
        Spec(9, 3, 3),
    )

    private fun Spec.holesFor(difficulty: String): Int = when (difficulty) {
        "easy" -> when (size) {
            4 -> 3
            6 -> 10
            else -> 36
        }
        "medium" -> when (size) {
            4 -> 5
            6 -> 14
            else -> 42
        }
        "hard" -> when (size) {
            4 -> 6
            6 -> 18
            else -> 48
        }
        else -> throw IllegalArgumentException("unknown difficulty $difficulty")
    }

    @Test
    fun `三个规格均可生成唯一解谜题且终盘合法`() {
        val generator = SudokuGenerator(maxRetry = 8)
        for (spec in specs) {
            val holes = spec.holesFor("hard")
            val p = generator.generate(spec.size, spec.boxW, spec.boxH, holes)
            assertEquals("规格应为 size=${spec.size}", spec.size, p.size)
            assertValidSolution(p.solution, spec.size, spec.boxW, spec.boxH)
            assertDigConsistent(p)
            assertEquals(
                "size=${spec.size} 生成的谜题必须是唯一解",
                1,
                validator.countSolutions(p.puzzle, configOf(p), limit = 2),
            )
        }
    }

    @Test
    fun `连续生成多个谜题均为唯一解`() {
        val generator = SudokuGenerator(maxRetry = 8)
        for (spec in specs) {
            repeat(20) {
                val holes = spec.holesFor("medium")
                val p = generator.generate(spec.size, spec.boxW, spec.boxH, holes)
                assertEquals(1, validator.countSolutions(p.puzzle, configOf(p), limit = 2))
            }
        }
    }

    @Test
    fun `固定种子生成完全一致的谜题`() {
        val seed = 12345L
        val generatorA = SudokuGenerator(maxRetry = 8)
        val generatorB = SudokuGenerator(maxRetry = 8)

        for (spec in specs) {
            val holes = spec.holesFor("medium")
            val a = generatorA.generate(spec.size, spec.boxW, spec.boxH, holes, seed = seed)
            val b = generatorB.generate(spec.size, spec.boxW, spec.boxH, holes, seed = seed)
            assertArrayEquals("size=${spec.size}: puzzle 数组应完全一致", a.puzzle, b.puzzle)
            assertArrayEquals("size=${spec.size}: solution 数组应完全一致", a.solution, b.solution)
        }
    }

    @Test
    fun `同一 seed 下不同规格生成不同谜题`() {
        val seed = 12345L
        val generator = SudokuGenerator(maxRetry = 8)
        val four = generator.generate(4, 2, 2, 3, seed = seed).puzzle
        val six = generator.generate(6, 3, 2, 10, seed = seed).puzzle
        val nine = generator.generate(9, 3, 3, 36, seed = seed).puzzle
        assertNotEquals("4x4 与 6x6 不应相同", four.toList(), six.toList())
        assertNotEquals("6x6 与 9x9 不应相同", six.toList(), nine.toList())
    }

    @Test
    fun `不同种子生成不同谜题 - 随机性验证`() {
        val generator = SudokuGenerator()
        val seedA = 111L
        val seedB = 222L
        for (spec in specs) {
            val holes = spec.holesFor("easy")
            val a = generator.generate(spec.size, spec.boxW, spec.boxH, holes, seed = seedA)
            val b = generator.generate(spec.size, spec.boxW, spec.boxH, holes, seed = seedB)
            assertTrue("不同种子下 size=${spec.size} 谜题应不同", !a.puzzle.contentEquals(b.puzzle))
        }
    }

    @Test
    fun `挖空数量在可接受范围内`() {
        val generator = SudokuGenerator(maxRetry = 8)
        for (spec in specs) {
            for (diff in listOf("easy", "medium", "hard")) {
                val holes = spec.holesFor(diff)
                if (spec.size == 4 && diff == "hard") {
                    continue
                }
                val p = generator.generate(spec.size, spec.boxW, spec.boxH, holes)
                assertEquals(holes, p.holes)
                assertEquals(holes, p.puzzle.count { it == 0 })
                assertEquals(1, validator.countSolutions(p.puzzle, configOf(p), limit = 2))
            }
        }
    }

    @Test
    fun `4x4 挖空过多会触发重试直至失败`() {
        val generator = SudokuGenerator(maxRetry = 3)
        var failed = false
        try {
            generator.generate(4, 2, 2, 15)
        } catch (e: IllegalStateException) {
            failed = true
        }
        assert(failed) { "极端挖空应重试耗尽并抛异常" }
    }

    @Test
    fun `生成零挖空盘返回完整终盘`() {
        val generator = SudokuGenerator()
        val p = generator.generate(9, 3, 3, 0)
        assertEquals(0, p.puzzle.count { it == 0 })
        assertValidSolution(p.puzzle, 9, 3, 3)
    }

    @Test
    fun `增量挖空策略在低重试下也能稳定生成高难度谜题`() {
        // 由于采用"逐格试探+唯一解保持"的增量挖空，低重试即可稳定挖出目标空位。
        // 若旧策略(random dig + 丢弃重试) 在 hard 空位数下通常需要远超 5 次重试。
        val generator = SudokuGenerator(maxRetry = 5)
        for (spec in specs) {
            repeat(10) {
                val holes = spec.holesFor("hard")
                val p = generator.generate(spec.size, spec.boxW, spec.boxH, holes)
                assertEquals("size=${spec.size} 应达到目标空位数", holes, p.holes)
                assertEquals("实际空位应与 holes 一致", holes, p.puzzle.count { it == 0 })
                assertEquals("必须为唯一解", 1, validator.countSolutions(p.puzzle, configOf(p), limit = 2))
                assertDigConsistent(p)
            }
        }
    }

    @Test
    fun `增量挖空策略一次终盘即可挖满可达成空位`() {
        // 验证生成器在极小重试下仍能完成中等难度挖空，证明增量策略不依赖重试。
        val generator = SudokuGenerator(maxRetry = 1)
        for (spec in specs) {
            repeat(5) {
                val holes = spec.holesFor("medium")
                val p = generator.generate(spec.size, spec.boxW, spec.boxH, holes)
                assertEquals(holes, p.holes)
                assertEquals(1, validator.countSolutions(p.puzzle, configOf(p), limit = 2))
            }
        }
    }
}
