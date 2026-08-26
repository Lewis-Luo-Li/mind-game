package com.idroid.stuido.mind_game.core.logic.model

/**
 * 通用数独棋盘配置数据类，通过参数差异化控制所有规格（4x4 / 6x6 / 9x9）。
 *
 * @property size 网格尺寸：4、6 或 9
 * @property boxWidth 宫格宽度：2、3 或 3
 * @property boxHeight 宫格高度：2、2 或 3
 * @property solution 完整终盘（size * size 大小）
 * @property puzzle 挖空后的谜题（0 代表空格）
 * @property holes 实际挖空数量
 */
data class SudokuBoardConfig(
    val size: Int,
    val boxWidth: Int,
    val boxHeight: Int,
    val solution: IntArray,
    val puzzle: IntArray,
    val holes: Int,
)
