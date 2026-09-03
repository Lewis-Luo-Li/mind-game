package com.idroid.stuido.mind_game.core.logic.config

/**
 * 数独棋盘规格枚举。
 *
 * 对应实现计划 §2 的规格映射表：
 * | 规格 | 网格尺寸 | 宫格尺寸(宽x高) | 数字范围 | 总格数 |
 * |------|---------|---------------|---------|-------|
 * | Mini | 4x4     | 2x2           | 1~4     | 16    |
 * | Medium | 6x6   | 3x2           | 1~6     | 36    |
 * | Classic | 9x9  | 3x3           | 1~9     | 81    |
 */
enum class BoardSpec(
    val size: Int,
    val boxWidth: Int,
    val boxHeight: Int,
) {
    MINI(4, 2, 2),
    MEDIUM(6, 3, 2),
    CLASSIC(9, 3, 3);

    companion object {
        /**
         * 根据 [size] 查找对应规格；未知尺寸返回 null。
         */
        fun fromSize(size: Int): BoardSpec? = entries.firstOrNull { it.size == size }
    }
}
