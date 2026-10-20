package com.idroid.stuido.mind_game.core.logic.solver

import com.idroid.stuido.mind_game.core.logic.model.SudokuBoardConfig

/**
 * 通用回溯求解器。
 *
 * 基于经典回溯算法实现 [SolverStrategy]，通过 [SudokuBoardConfig] 中的
 * size / boxWidth / boxHeight 统一适配 4x4、6x6、9x9 三种规格。
 *
 * 核心特性：
 *  - **纯函数式输出**：solve / countSolutions 均不会修改传入的原始 [board] 数组。
 *  - **提前终止剪枝**：countSolutions 一旦统计到 [limit] 个解即终止递归，避免无谓开销。
 *  - **候选排序可定制**：通过构造参数 [candidateProvider] 可注入自定义候选序列
 *    （例如结合 Fisher-Yates 洗牌用于生成随机终盘，或固定顺序用于确定性测试）。
 *
 * @property candidateProvider 每格候选值的迭代序列提供器，默认升序 1..size。
 */
class BacktrackingSolver(
    private val candidateProvider: (cell: Int, size: Int) -> Iterable<Int> =
        { _, size -> 1..size },
) : SolverStrategy {

    /**
     * 求解棋盘，返回完整终盘（原数组的拷贝）。
     * 无解时返回 null。
     */
    override fun solve(board: IntArray, config: SudokuBoardConfig): IntArray? {
        val copy = board.copyOf()
        return if (backtrackFill(copy, index = 0, config)) {
            copy
        } else {
            null
        }
    }

    /**
     * 统计解的数量，最多统计到 [limit] 个即提前终止。
     */
    override fun countSolutions(
        board: IntArray,
        config: SudokuBoardConfig,
        limit: Int,
    ): Int {
        val copy = board.copyOf()
        return countSolutionsRecursive(
            board = copy,
            config = config,
            index = 0,
            limit = limit,
        ).coerceAtMost(limit)
    }

    /**
     * 递归回溯填数。返回是否找到至少一个解。
     */
    private fun backtrackFill(
        board: IntArray,
        index: Int,
        config: SudokuBoardConfig,
    ): Boolean {
        if (index >= config.size * config.size) return true // 全部填满即得到一个解

        if (board[index] != 0) {
            // 当前格已有预设值，直接跳到下一格
            return backtrackFill(board, index + 1, config)
        }

        val row = index / config.size
        val col = index % config.size

        for (candidate in candidateProvider(index, config.size)) {
            if (isPlacementValid(board, row, col, candidate, config)) {
                board[index] = candidate
                if (backtrackFill(board, index + 1, config)) {
                    return true
                }
                board[index] = 0 // 回溯：撤销填入
            }
        }
        return false
    }

    /**
     * 递归统计从 [index] 起其余空位能产生的解的数量。
     * 一旦累计到 [limit] 即终止并返回，实现剪枝优化。
     */
    private fun countSolutionsRecursive(
        board: IntArray,
        config: SudokuBoardConfig,
        index: Int,
        limit: Int,
    ): Int {
        // 已填满全部空格：找到一个完整解
        if (index >= config.size * config.size) return 1

        // 当前格已有预设值，跳过
        if (board[index] != 0) {
            return countSolutionsRecursive(board, config, index + 1, limit)
        }

        val row = index / config.size
        val col = index % config.size
        var total = 0

        for (candidate in candidateProvider(index, config.size)) {
            if (isPlacementValid(board, row, col, candidate, config)) {
                board[index] = candidate
                total += countSolutionsRecursive(board, config, index + 1, limit)
                board[index] = 0 // 回溯：撤销填入

                // 剪枝优化：达到 limit 立即终止，避免无谓递归
                if (total >= limit) return total
            }
        }
        return total
    }

    /**
     * 校验在某位置填入 [value] 是否合法（不违反行 / 列 / 宫约束）。
     */
    private fun isPlacementValid(
        board: IntArray,
        row: Int,
        col: Int,
        value: Int,
        config: SudokuBoardConfig,
    ): Boolean {
        val size = config.size

        // 行约束
        for (c in 0 until size) {
            if (board[row * size + c] == value) return false
        }

        // 列约束
        for (r in 0 until size) {
            if (board[r * size + col] == value) return false
        }

        // 宫（box）约束
        val boxStartRow = (row / config.boxHeight) * config.boxHeight
        val boxStartCol = (col / config.boxWidth) * config.boxWidth
        for (r in boxStartRow until boxStartRow + config.boxHeight) {
            for (c in boxStartCol until boxStartCol + config.boxWidth) {
                if (board[r * size + c] == value) return false
            }
        }

        return true
    }
}
