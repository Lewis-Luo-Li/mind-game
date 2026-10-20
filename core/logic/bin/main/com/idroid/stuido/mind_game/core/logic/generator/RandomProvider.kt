package com.idroid.stuido.mind_game.core.logic.generator

import kotlin.random.Random

/**
 * 随机数提供器（Random Provider）。
 *
 * 封装 [Random]，对外暴露数独生成所需的随机能力，并支持：
 *  - **自由模式**：使用系统随机种子，产生不可预测的随机终盘。
 *  - **每日挑战模式**：通过构造参数注入固定 [seed]，保证跨设备生成一致的谜题。
 *
 * == 种子一致性说明 ==
 * 为保证同一天内 4x4 / 6x6 / 9x9 三种规格题目互不相同且全球一致，
 * 调用方可参照实现计划中的种子混合策略：
 * ```kotlin
 * // size 对应 4 / 6 / 9
 * val mixedSeed = seed * 10 + size
 * val provider = RandomProvider(seed = mixedSeed)
 * ```
 *
 * @param seed 随机种子；若不传（或传 null）则自动使用系统随机种子。
 */
class RandomProvider(seed: Long? = null) {

    private val random: Random = if (seed != null) Random(seed) else Random.Default

    /**
     * 返回 [until] 之间的伪随机非负整数，即 `0 until until`。
     */
    fun nextInt(until: Int): Int = random.nextInt(until)

    /**
     * 返回 [from]（含）到 [until]（不含）之间的伪随机整数。
     */
    fun nextInt(from: Int, until: Int): Int = random.nextInt(from, until)

    /**
     * 对给定 [list] 进行原地 Fisher-Yates 洗牌并返回同一实例。
     * 该洗牌过程是随机的（依赖本 provider 的随机源）。
     */
    fun <T> shuffle(list: MutableList<T>): MutableList<T> {
        for (i in list.size - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val tmp = list[i]
            list[i] = list[j]
            list[j] = tmp
        }
        return list
    }
}
