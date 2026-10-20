package com.idroid.stuido.mind_game.core.logic.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 难度配置（DifficultyConfig）单元测试。
 */
class DifficultyConfigTest {

    private val config = DifficultyConfig.default()

    // ---------- BoardSpec ----------

    @Test
    fun `fromSize 正确解析三种规格`() {
        assertEquals(BoardSpec.MINI, BoardSpec.fromSize(4))
        assertEquals(BoardSpec.MEDIUM, BoardSpec.fromSize(6))
        assertEquals(BoardSpec.CLASSIC, BoardSpec.fromSize(9))
        assertNull("未知尺寸应返回 null", BoardSpec.fromSize(8))
    }

    @Test
    fun `BoardSpec 携带正确的规格参数`() {
        assertEquals(Triple(4, 2, 2), Triple(BoardSpec.MINI.size, BoardSpec.MINI.boxWidth, BoardSpec.MINI.boxHeight))
        assertEquals(Triple(6, 3, 2), Triple(BoardSpec.MEDIUM.size, BoardSpec.MEDIUM.boxWidth, BoardSpec.MEDIUM.boxHeight))
        assertEquals(Triple(9, 3, 3), Triple(BoardSpec.CLASSIC.size, BoardSpec.CLASSIC.boxWidth, BoardSpec.CLASSIC.boxHeight))
        // 校验 size == boxWidth * boxHeight 一致性
        for (spec in BoardSpec.entries) {
            assertTrue("size 应等于 boxWidth*boxHeight", spec.size == spec.boxWidth * spec.boxHeight)
        }
    }

    // ---------- 挖空数量映射 ----------

    @Test
    fun `默认映射与实现计划一致`() {
        // 4x4
        assertEquals(3, config.holeCount(BoardSpec.MINI, Difficulty.EASY))
        assertEquals(5, config.holeCount(BoardSpec.MINI, Difficulty.MEDIUM))
        assertEquals(6, config.holeCount(BoardSpec.MINI, Difficulty.HARD))
        // 6x6
        assertEquals(10, config.holeCount(BoardSpec.MEDIUM, Difficulty.EASY))
        assertEquals(14, config.holeCount(BoardSpec.MEDIUM, Difficulty.MEDIUM))
        assertEquals(18, config.holeCount(BoardSpec.MEDIUM, Difficulty.HARD))
        assertEquals(20, config.holeCount(BoardSpec.MEDIUM, Difficulty.EXPERT))
        // 9x9
        assertEquals(36, config.holeCount(BoardSpec.CLASSIC, Difficulty.EASY))
        assertEquals(42, config.holeCount(BoardSpec.CLASSIC, Difficulty.MEDIUM))
        assertEquals(48, config.holeCount(BoardSpec.CLASSIC, Difficulty.HARD))
        assertEquals(55, config.holeCount(BoardSpec.CLASSIC, Difficulty.EXPERT))
    }

    @Test
    fun `4x4 的 EXPERT 难度不可用`() {
        assertNull("4x4 应不支持 EXPERT", config.holeCount(BoardSpec.MINI, Difficulty.EXPERT))
        assertFalse(config.isAvailable(BoardSpec.MINI, Difficulty.EXPERT))
    }

    @Test
    fun `isAvailable 正确反映可用性`() {
        assertTrue(config.isAvailable(BoardSpec.MINI, Difficulty.HARD))
        assertTrue(config.isAvailable(BoardSpec.MEDIUM, Difficulty.EXPERT))
        assertTrue(config.isAvailable(BoardSpec.CLASSIC, Difficulty.HARD))
    }

    @Test
    fun `availableDifficulties 返回正确难度集合`() {
        assertEquals(setOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD), config.availableDifficulties(BoardSpec.MINI))
        assertEquals(
            setOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD, Difficulty.EXPERT),
            config.availableDifficulties(BoardSpec.MEDIUM),
        )
        assertEquals(
            setOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD, Difficulty.EXPERT),
            config.availableDifficulties(BoardSpec.CLASSIC),
        )
    }

    @Test
    fun `dailySeed 与 maxRetry 采用默认值`() {
        assertEquals(20_260_817L, config.dailySeed)
        assertEquals(50, config.maxRetry)
        assertTrue(config.hasAnyDifficulty(BoardSpec.MINI))
    }

    @Test
    fun `可自定义配置覆盖默认映射`() {
        val custom = DifficultyConfig(
            holes = mapOf(BoardSpec.CLASSIC to mapOf(Difficulty.EASY to 40)),
            maxRetry = 20,
            dailySeed = 1L,
        )
        assertEquals(40, custom.holeCount(BoardSpec.CLASSIC, Difficulty.EASY))
        assertEquals(20, custom.maxRetry)
        // CLASSIC 未配置 HARD，应不可用
        assertNull(custom.holeCount(BoardSpec.CLASSIC, Difficulty.HARD))
        assertFalse(custom.isAvailable(BoardSpec.CLASSIC, Difficulty.HARD))
    }
}
