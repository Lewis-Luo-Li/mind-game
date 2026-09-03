package com.idroid.stuido.mind_game.feature.game.engine

import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.feature.game.GameMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * GameFactory：验证对 core:logic 的封装（生成 / 解包 / 种子一致性 / 非法参数）。
 */
class GameFactoryTest {

    private val factory = GameFactory()

    // 简单验证终盘素材基本形状
    private fun assertValidSeed(seed: GameSeed, size: Int) {
        assertEquals(size, seed.size)
        assertEquals(size * size, seed.values.size)
        assertEquals(size * size, seed.solution.size)
        // 所有值落在 [0, size]（0 = 空格）
        assertTrue("棋盘值应在 0..size", seed.values.all { it in 0..size })
        assertTrue("终盘不应含 0", seed.solution.all { it in 1..size })
    }

    @Test
    fun `自由模式 MINI_EASY 生成合法初始素材`() {
        val seed = factory.newGame(GameMode.FREE, BoardSpec.MINI, Difficulty.EASY)
        assertValidSeed(seed, 4)
        val holes = seed.values.count { it == 0 }
        assertEquals("4x4 easy 应有 3 个空格", 3, holes)
        assertEquals("givens 数 = 16 - 空格", 16 - holes, seed.givens.size)
    }

    @Test
    fun `自由模式各规格生成合法初始素材`() {
        for (spec in listOf(BoardSpec.MINI, BoardSpec.MEDIUM, BoardSpec.CLASSIC)) {
            val seed = factory.newGame(GameMode.FREE, spec, Difficulty.MEDIUM)
            assertValidSeed(seed, spec.size)
        }
    }

    @Test
    fun `每日模式固定种子可复现同一题目`() {
        val seed = 12345L
        val a = factory.newGame(GameMode.DAILY, BoardSpec.MINI, Difficulty.HARD, seed = seed)
        val b = factory.newGame(GameMode.DAILY, BoardSpec.MINI, Difficulty.HARD, seed = seed)
        assertEquals("同种子 puzzle 值应完全一致", a.values, b.values)
        assertEquals("同种子终盘应完全一致", a.solution, b.solution)
    }

    @Test
    fun `每日模式必须提供 seed 否则抛异常`() {
        assertThrows(IllegalArgumentException::class.java) {
            factory.newGame(GameMode.DAILY, BoardSpec.MINI, Difficulty.EASY)
        }
    }

    @Test
    fun `不支持的难度组合抛异常 - 4x4 EXPERT`() {
        assertThrows(IllegalArgumentException::class.java) {
            factory.newGame(GameMode.FREE, BoardSpec.MINI, Difficulty.EXPERT)
        }
    }
}
