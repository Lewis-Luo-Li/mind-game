package com.idroid.stuido.mind_game.core.logic.validator

/**
 * 谜题校验结果。
 *
 * 对应实现计划 §4.2 的判定规则：
 *  - [VALID]              解数 == 1，合法唯一解谜题，通过。
 *  - [MULTIPLE_SOLUTIONS] 解数 >= 2，多解谜题，应丢弃重试。
 *  - [NO_SOLUTION]        解数 == 0，无解谜题，应丢弃重试。
 */
enum class PuzzleValidationResult {
    VALID,
    MULTIPLE_SOLUTIONS,
    NO_SOLUTION,
}
