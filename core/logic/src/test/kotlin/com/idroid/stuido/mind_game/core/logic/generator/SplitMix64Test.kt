package com.idroid.stuido.mind_game.core.logic.generator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SplitMix64Test {

    @Test
    fun `matches the reference SplitMix64 sequence`() {
        // 参考实现（Vigna, splitmix64.c）seed = 0 的前两个输出
        val random = SplitMix64(0L)
        assertEquals(-2152535657050944081L, random.nextLong()) // 0xE220A8397B1DCDAF
        assertEquals(7960286522194355700L, random.nextLong()) // 0x6E789E6AA1B965F4
    }

    @Test
    fun `nextInt stays in bounds and covers all values`() {
        val random = SplitMix64(42L)
        val seen = IntArray(9)
        repeat(9_000) { seen[random.nextInt(9)]++ }
        assertTrue(seen.all { it > 800 })
    }
}
