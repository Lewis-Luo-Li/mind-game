package com.idroid.stuido.mind_game.core.logic.config

/**
 * 难度配置数据类。
 *
 * 描述"规格 + 难度 → 挖空数量"的映射，以及生成器所需的全局参数，
 * 与实现计划 §6 的远程配置结构对应。
 *
 * @property holes 每个 (规格, 难度) 对应的挖空数量；未被包含的组合视为不可用
 *                 （例如 4x4 的 EXPERT）。
 * @property maxRetry 生成器完整终盘回退重试的最大次数。
 * @property dailySeed 每日挑战用的固定种子。
 */
data class DifficultyConfig(
    val holes: Map<BoardSpec, Map<Difficulty, Int>>,
    val maxRetry: Int,
    val dailySeed: Long,
) {

    /**
     * 返回某规格 + 难度对应的挖空数量；若该组合不可用则返回 null。
     */
    fun holeCount(spec: BoardSpec, difficulty: Difficulty): Int? =
        holes[spec]?.get(difficulty)

    /**
     * 该组合是否可用（存在有效的挖空数量）。
     */
    fun isAvailable(spec: BoardSpec, difficulty: Difficulty): Boolean =
        holeCount(spec, difficulty) != null

    /**
     * 某规格下所有可用的难度集合（按下表定义的顺序）。
     */
    fun availableDifficulties(spec: BoardSpec): Set<Difficulty> =
        holes[spec]?.keys ?: emptySet()

    /**
     * 该规格是否至少支持一个可用难度。
     */
    fun hasAnyDifficulty(spec: BoardSpec): Boolean =
        !availableDifficulties(spec).isEmpty()

    companion object {

        /**
         * 实现计划 §3.2 / §6 的默认挖空数量映射。
         *
         * > 注意：4x4 挖空超过 7 个极易多解，因此 EXPERT 对其关闭。
         */
        fun default(): DifficultyConfig = DifficultyConfig(
            holes = mapOf(
                BoardSpec.MINI to mapOf(
                    Difficulty.EASY to 3,
                    Difficulty.MEDIUM to 5,
                    Difficulty.HARD to 6,
                    // EXPERT 对 4x4 不开放
                ),
                BoardSpec.MEDIUM to mapOf(
                    Difficulty.EASY to 10,
                    Difficulty.MEDIUM to 14,
                    Difficulty.HARD to 18,
                    Difficulty.EXPERT to 20,
                ),
                BoardSpec.CLASSIC to mapOf(
                    Difficulty.EASY to 36,
                    Difficulty.MEDIUM to 42,
                    Difficulty.HARD to 48,
                    Difficulty.EXPERT to 55,
                ),
            ),
            maxRetry = 50,
            dailySeed = 20_260_817L,
        )
    }
}
