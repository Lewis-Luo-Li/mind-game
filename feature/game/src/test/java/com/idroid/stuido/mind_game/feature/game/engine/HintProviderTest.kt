package com.idroid.stuido.mind_game.feature.game.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HintProviderTest {

    private val provider = HintProvider()

    // 一个 4x4 完整终盘
    private val solution = listOf(
        1, 2, 3, 4,
        3, 4, 1, 2,
        2, 1, 4, 3,
        4, 3, 2, 1,
    )

    @Test
    fun `hintFor 返回该格正解`() {
        assertEquals(1, provider.hintFor(0, solution))
        assertEquals(4, provider.hintFor(3, solution))
        assertEquals(2, provider.hintFor(14, solution))
        assertEquals(1, provider.hintFor(15, solution))
    }

    @Test
    fun `hintFor 越界返回 null`() {
        assertNull(provider.hintFor(-1, solution))
        assertNull(provider.hintFor(100, solution))
    }

    @Test
    fun `hintFor 空终盘返回 null`() {
        assertNull(provider.hintFor(0, emptyList()))
    }

    @Test
    fun `isCorrect 正确判断`() {
        assertTrue(provider.isCorrect(0, 1, solution))
        assertFalse(provider.isCorrect(0, 2, solution))
        assertFalse(provider.isCorrect(5, 1, solution)) // solution[5] == 4
    }

    @Test
    fun `isCorrect 越界为 false`() {
        assertFalse(provider.isCorrect(999, 1, solution))
    }
}
