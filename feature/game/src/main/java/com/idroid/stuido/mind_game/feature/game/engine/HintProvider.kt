package com.idroid.stuido.mind_game.feature.game.engine

/**
 * 提示提供者：基于完整终盘 [solution] 计算对局所需的提示。
 * 仅作纯函数用途，不持有可变状态。
 */
class HintProvider {

    /**
     * 返回指定 [index] 格在终盘中的正解数值。
     *
     * @param index    目标格子索引。
     * @param solution 完整终盘（flat `IntArray`/`List<Int>`）。
     * @return 该格正确的数值；若索引越界返回 null。
     */
    fun hintFor(index: Int, solution: List<Int>): Int? =
        if (index in solution.indices) solution[index] else null

    /**
     * 判断玩家在某格 [index] 填入的值 [value] 是否等于终盘值。
     *
     * @return true 表示正确；false 表示与终盘不一致（判错依据之一）。
     */
    fun isCorrect(index: Int, value: Int, solution: List<Int>): Boolean =
        index in solution.indices && solution[index] == value
}
