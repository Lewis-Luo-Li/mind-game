package com.idroid.stuido.mind_game.core.logic.generator

/**
 * SplitMix64 伪随机数生成器（Steele/Lea/Flood 2014）。
 *
 * 不使用 `kotlin.random.Random(seed)`：Kotlin 文档声明其带种子算法在未来版本可能变化，
 * 而每日挑战 / 天梯关卡要求「同一种子 → 同一谜题」跨设备、跨 App 版本稳定。
 * 本实现完全自持，输出序列永久固定。
 */
internal class SplitMix64(seed: Long) {

    private var state: Long = seed

    fun nextLong(): Long {
        state += GOLDEN_GAMMA
        return finalize(state)
    }

    /** 返回 `0 until bound` 的均匀整数（31 位拒绝采样，无取模偏差）。 */
    fun nextInt(bound: Int): Int {
        require(bound > 0) { "bound must be positive" }
        val b = bound.toLong()
        while (true) {
            val bits = nextLong() ushr 33
            val value = bits % b
            if (bits - value + (b - 1) < (1L shl 31)) return value.toInt()
        }
    }

    companion object {
        private const val GOLDEN_GAMMA = -0x61c8864680b583ebL // 0x9E3779B97F4A7C15

        /** 把任意 64 位值打散为分布均匀的 64 位值，用于派生互不相关的子种子。 */
        fun mix(value: Long): Long = finalize(value + GOLDEN_GAMMA)

        private fun finalize(value: Long): Long {
            var z = value
            z = (z xor (z ushr 30)) * -0x40a7b892e31b1a47L // 0xBF58476D1CE4E5B9
            z = (z xor (z ushr 27)) * -0x6b2fb644ecceee15L // 0x94D049BB133111EB
            return z xor (z ushr 31)
        }
    }
}
