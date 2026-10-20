package com.idroid.stuido.mind_game.core.logic.solver

import com.idroid.stuido.mind_game.core.logic.model.SudokuBoardConfig

/**
 * 求解器策略接口（Strategy Pattern）。
 *
 * 通过该抽象定义统一的求解契约，允许在运行时替换不同的求解算法实现
 * （例如回溯、DLX / Dancing Links 等），而生成器与校验器无需感知具体实现。
 *
 * 所有方法均基于 [SudokuBoardConfig] 中携带的环境参数（size / boxWidth / boxHeight）
 * 运行，因此棋盘数组统一使用扁平的一维 [IntArray] 表示，
 * 索引规则为 `board[row * size + col]`。
 */
interface SolverStrategy {

    /**
     * 求解棋盘并返回其中一个合法解。
     *
     * @param board 待解的棋盘（0 表示空位），求解过程中不会修改原数组。
     * @param config 携带规格参数 (size / boxWidth / boxHeight) 的配置。
     * @return 完整终盘（[IntArray] 的拷贝）；若无解则返回 null。
     */
    fun solve(board: IntArray, config: SudokuBoardConfig): IntArray?

    /**
     * 统计解的数量，最多统计到 [limit] 个即提前终止递归（剪枝优化）。
     * 默认 [limit] = 2，专用于唯一解校验。
     *
     * @param board 待校验的棋盘。
     * @param config 携带规格参数的配置。
     * @param limit 解数量统计上限。
     * @return 实际解的数量（<= [limit]）。
     *         - 返回 1  => 唯一解，合法谜题；
     *         - 返回 >=2 => 多解谜题，应丢弃重试；
     *         - 返回 0  => 无解谜题，应丢弃重试。
     */
    fun countSolutions(board: IntArray, config: SudokuBoardConfig, limit: Int = 2): Int
}
