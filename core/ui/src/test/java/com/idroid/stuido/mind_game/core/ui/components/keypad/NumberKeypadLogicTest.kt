package com.idroid.stuido.mind_game.core.ui.components.keypad

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 数字键的纯逻辑：可点判定（剩余次数 / 全局禁用 / zeroCountEnabled 豁免）
 * 与窄屏缩键边长计算。
 */
class NumberKeypadLogicTest {

    // ---- isDigitKeyEnabled ----

    @Test
    fun `key enabled when remaining above zero`() {
        assertTrue(isDigitKeyEnabled(enabled = true, remaining = 3, digit = 1, zeroCountEnabled = emptySet()))
    }

    @Test
    fun `key disabled when remaining is zero`() {
        assertFalse(isDigitKeyEnabled(enabled = true, remaining = 0, digit = 4, zeroCountEnabled = emptySet()))
    }

    @Test
    fun `zero-count key enabled when whitelisted via zeroCountEnabled`() {
        assertTrue(
            isDigitKeyEnabled(enabled = true, remaining = 0, digit = 4, zeroCountEnabled = setOf(4)),
        )
    }

    @Test
    fun `zeroCountEnabled does not leak to other digits`() {
        assertFalse(
            isDigitKeyEnabled(enabled = true, remaining = 0, digit = 9, zeroCountEnabled = setOf(4)),
        )
    }

    @Test
    fun `global disable wins over remaining and whitelist`() {
        assertFalse(isDigitKeyEnabled(enabled = false, remaining = 3, digit = 1, zeroCountEnabled = emptySet()))
        assertFalse(isDigitKeyEnabled(enabled = false, remaining = 0, digit = 4, zeroCountEnabled = setOf(4)))
    }

    // ---- keySizeForWidth ----

    @Test
    fun `wide screen keeps configured key size`() {
        // 9 keys on 400dp row: slot 44.4dp > 36dp cap
        assertEquals(36.dp, keySizeForWidth(maxWidth = 400.dp, digits = 9, keySize = 36.dp))
    }

    @Test
    fun `narrow screen shrinks key to slot width minus gap`() {
        // 9 keys on 320dp: slot ~35.6dp → key 31.6dp < 36dp, prevents overlap
        val size = keySizeForWidth(maxWidth = 320.dp, digits = 9, keySize = 36.dp)
        assertEquals(320.dp / 9 - 4.dp, size)
        assertTrue(size < 36.dp)
    }

    @Test
    fun `shrunk key never reaches slot width (keeps a gap)`() {
        val size = keySizeForWidth(maxWidth = 300.dp, digits = 9, keySize = 36.dp)
        assertTrue(size <= 300.dp / 9)
    }

    @Test
    fun `key size floors at 16dp on absurdly narrow rows`() {
        assertEquals(16.dp, keySizeForWidth(maxWidth = 100.dp, digits = 9, keySize = 36.dp))
    }

    @Test
    fun `non-positive digit count returns configured size`() {
        assertEquals(36.dp, keySizeForWidth(maxWidth = 100.dp, digits = 0, keySize = 36.dp))
    }
}
