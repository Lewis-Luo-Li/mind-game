package com.idroid.stuido.mind_game.core.logic.validator

import com.idroid.stuido.mind_game.core.logic.model.SudokuBoardConfig
import com.idroid.stuido.mind_game.core.logic.solver.BacktrackingSolver
import com.idroid.stuido.mind_game.core.logic.solver.SolverStrategy

/**
 * 谜题校验器（Puzzle Validator）。
 *
 * 对生成出的谜题执行"唯一解校验"，是"三位一体"策略中的 **Validate** 一环。
 * 通过调用 [SolverStrategy.countSolutions] 并在找到第 2 个解时立即剪枝终止，
 * 高效判定谜题是否合法唯一解。
 *
 * 典型用法（经 [SudokuGenerator] 已自行内置校验，此处供上层 UI / DAO 复用）：
 * ```kotlin
 * val validator = PuzzleValidator()
 * when (validator.validate(board, config)) {
 *     PuzzleValidationResult.VALID -> // 通过
 *     PuzzleValidationResult.MULTIPLE_SOLUTIONS -> // 丢弃重试
 *     PuzzleValidationResult.NO_SOLUTION -> // 丢弃重试
 * }
 * ```
 *
 * @param solver 求解策略，默认使用通用回溯求解器。
 */
class PuzzleValidator(
    private val solver: SolverStrategy = BacktrackingSolver(),
) {

    /**
     * 校验 [board] 的解唯一性，返回 [PuzzleValidationResult]。
     */
    fun validate(board: IntArray, config: SudokuBoardConfig): PuzzleValidationResult {
        return when (solver.countSolutions(board, config, limit = 2)) {
            0 -> PuzzleValidationResult.NO_SOLUTION
            1 -> PuzzleValidationResult.VALID
            else -> PuzzleValidationResult.MULTIPLE_SOLUTIONS
        }
    }

    /**
     * 是否为合法唯一解谜题。
     */
    fun isValid(board: IntArray, config: SudokuBoardConfig): Boolean =
        validate(board, config) == PuzzleValidationResult.VALID

    /**
     * 校验 [config] 中自带的 puzzle（使用其自身规格参数）的解唯一性。
     */
    fun validate(config: SudokuBoardConfig): PuzzleValidationResult =
        validate(config.puzzle, config)

    /**
     * [config] 自带 puzzle 是否为合法唯一解。
     */
    fun isValid(config: SudokuBoardConfig): Boolean =
        isValid(config.puzzle, config)
}
