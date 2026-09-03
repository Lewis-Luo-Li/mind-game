package com.idroid.stuido.mind_game.core.logic.config

/**
 * 数独难度枚举。
 *
 * 与实现计划 §3.2 的挖空数量映射表对应。
 * [EXPERT] 对 4x4 (MINI) 规格不开放（挖空超过 7 个极易导致多解）。
 */
enum class Difficulty {
    EASY,
    MEDIUM,
    HARD,
    EXPERT,
}
