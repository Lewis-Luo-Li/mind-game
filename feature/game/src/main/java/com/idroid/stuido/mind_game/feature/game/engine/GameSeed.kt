package com.idroid.stuido.mind_game.feature.game.engine

import com.idroid.stuido.mind_game.core.logic.model.SudokuBoardConfig

/**
 * 一局游戏的"初始素材"：由 [SudokuBoardConfig] 解包后转成 UI 层可用的纯净不可变视图。
 *
 * @property size         网格尺寸。
 * @property values       谜题初盘每个格子的值（0 为空）。索引规则 `row*size+col`。
 * @property solution     完整终盘（用于提示 / 判错 / 胜利判定）。
 * @property givens       给定数字的格子索引（初始非 0 位置）。
 */
data class GameSeed(
    val size: Int,
    val values: List<Int>,
    val solution: List<Int>,
    val givens: Set<Int>,
) {
    companion object {
        /** 从 [SudokuBoardConfig] 解包出 [GameSeed]。 */
        fun from(config: SudokuBoardConfig): GameSeed = GameSeed(
            size = config.size,
            values = config.puzzle.toList(),
            solution = config.solution.toList(),
            givens = config.puzzle
                .asSequence()
                .mapIndexedNotNull { i, v -> if (v != 0) i else null }
                .toSet(),
        )

        /** 空的占位种子，避免启动阶段空指针。 */
        fun empty(size: Int = 0): GameSeed = GameSeed(
            size = size,
            values = List(size * size) { 0 },
            solution = emptyList(),
            givens = emptySet(),
        )
    }
}
