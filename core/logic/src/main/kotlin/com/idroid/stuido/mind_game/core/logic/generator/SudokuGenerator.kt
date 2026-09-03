package com.idroid.stuido.mind_game.core.logic.generator

import com.idroid.stuido.mind_game.core.logic.model.SudokuBoardConfig
import com.idroid.stuido.mind_game.core.logic.solver.BacktrackingSolver
import com.idroid.stuido.mind_game.core.logic.solver.SolverStrategy

/**
 * 数独谜题生成器（Sudoku Generator）。
 *
 * 严格遵循"三位一体"策略，通过纯 Kotlin 算法实时动态生成三类规格
 * （4x4 / 6x6 / 9x9）的合法唯一解谜题：
 *
 * 1. **生成终盘**：回溯 + Fisher-Yates 洗牌，遍历空位随机填入候选值并剪枝。
 * 2. **挖空（增量式，可靠性高）**：随机打乱所有格索引，对每个候选格**逐个**试探挖空，
 *    每次挖空后立即校验唯一解；仅当该格挖空后谜题仍具唯一解才真正保留该空位，
 *    否则撤销并尝试下一格。如此我们能在**一次完整终盘**上稳定挖出期望数量的空位，
 *    且保证结果必然唯一解，大幅降低对大量重试的依赖。
 * 3. **唯一解校验**：增量挖空过程本身即内置唯一解校验；若一轮增量挖空未能达到目标
 *    空位数量（极端情况下可挖安全格已耗尽），则回退到新终盘重来，最多 [maxRetry] 次。
 *
 * == 种子与每日挑战 ==
 * 传入 [seed] 时按实现计划做混合：`seed * 10 + size`，保证同日三种规格题目
 * 均不同且跨设备一致。
 *
 * @param randomProvider 默认随机源；自由模式下使用。
 * @param maxRetry 完整终盘回退重试的最大次数，默认 50。
 */
class SudokuGenerator(
    private val randomProvider: RandomProvider = RandomProvider(),
    private val maxRetry: Int = 50,
) {

    /**
     * 生成一个完整的、具有唯一解的谜题。
     *
     * @param size 网格尺寸：4 / 6 / 9。
     * @param boxWidth 宫格宽度。
     * @param boxHeight 宫格高度。
     * @param holes 期望挖空数量（0 表示满盘完整终盘）。
     * @param seed 可选种子；传入后按 `seed * 10 + size` 混合以实现每日一致性。
     * @throws IllegalStateException 若在 [maxRetry] 次内仍无法得到唯一解谜题。
     */
    fun generate(
        size: Int,
        boxWidth: Int,
        boxHeight: Int,
        holes: Int,
        seed: Long? = null,
    ): SudokuBoardConfig {
        require(size == boxWidth * boxHeight) { "size 必须等于 boxWidth * boxHeight" }

        // 种子混合：同一 seed 下三种规格互不相同
        val effectiveSeed = seed?.let { it * 10 + size }
        val provider = if (effectiveSeed != null) RandomProvider(effectiveSeed) else randomProvider

        // 生成期使用随机洗牌候选的回溯求解器，保证终盘随机
        val generatingSolver: SolverStrategy = BacktrackingSolver { _, s ->
            provider.shuffle((1..s).toMutableList())
        }

        // 唯一解校验使用通用求解器（候选顺序不影响计数结果）
        val validatingSolver: SolverStrategy = BacktrackingSolver()

        repeat(maxRetry) { attempt ->
            val solution = generateFullBoard(generatingSolver, size, boxWidth, boxHeight)
                ?: error("无法生成完整终盘 (attempt $attempt)")

            val puzzle = digHolesIncrementally(
                solution = solution,
                provider = provider,
                validatingSolver = validatingSolver,
                size = size,
                boxWidth = boxWidth,
                boxHeight = boxHeight,
                targetHoles = holes,
            )

            val actualHoles = puzzle.count { it == 0 }
            val config = SudokuBoardConfig(
                size = size,
                boxWidth = boxWidth,
                boxHeight = boxHeight,
                solution = solution,
                puzzle = puzzle,
                holes = actualHoles,
            )

            // 增量挖空保证唯一解；若实际空位数达目标则直接接受
            if (actualHoles == holes) {
                return config
            }
            // 未达目标 => 回退重试（换一张新终盘）
        }

        error("在 $maxRetry 次重试后仍无法生成目标空位数 ($size, holes=$holes)")
    }

    /**
     * 生成一个随机的完整终盘（棋盘所有格均非 0）。
     * 返回 null 表示给定规格下递归耗尽仍无解（理论上不应发生）。
     */
    private fun generateFullBoard(
        generatingSolver: SolverStrategy,
        size: Int,
        boxWidth: Int,
        boxHeight: Int,
    ): IntArray? {
        val emptyBoard = IntArray(size * size)
        val config = SudokuBoardConfig(
            size = size,
            boxWidth = boxWidth,
            boxHeight = boxHeight,
            solution = emptyBoard,
            puzzle = emptyBoard,
            holes = 0,
        )
        return generatingSolver.solve(emptyBoard, config)
    }

    /**
     * 增量式挖空：随机遍历所有格，对每个格逐个试挖并维持唯一解，
     * 直到挖出 [targetHoles] 个空位或无可安全挖空的格为止。
     *
     * 返回挖空后的谜题（0 代表空位）。结果必然具备唯一解。
     */
    private fun digHolesIncrementally(
        solution: IntArray,
        provider: RandomProvider,
        validatingSolver: SolverStrategy,
        size: Int,
        boxWidth: Int,
        boxHeight: Int,
        targetHoles: Int,
    ): IntArray {
        val puzzle = solution.copyOf()
        val target = targetHoles.coerceIn(0, size * size)
        if (target == 0) return puzzle

        val candidates = provider.shuffle((0 until size * size).toMutableList())
        var dug = 0

        for (cell in candidates) {
            if (dug >= target) break

            // 跳过已经是空的格（正常流程不会出现，但防御性处理）
            if (puzzle[cell] == 0) continue

            val saved = puzzle[cell]
            puzzle[cell] = 0

            // 挖空后仍需唯一解才保留
            val config = SudokuBoardConfig(
                size = size,
                boxWidth = boxWidth,
                boxHeight = boxHeight,
                solution = solution,
                puzzle = puzzle,
                holes = dug + 1,
            )
            if (validatingSolver.countSolutions(puzzle, config, limit = 2) == 1) {
                dug++
            } else {
                puzzle[cell] = saved // 撤销：挖空会破坏唯一解
            }
        }

        return puzzle
    }
}
