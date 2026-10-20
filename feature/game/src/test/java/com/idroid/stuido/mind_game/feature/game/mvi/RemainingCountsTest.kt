package com.idroid.stuido.mind_game.feature.game.mvi

import com.idroid.stuido.mind_game.feature.game.reducers.GameReducer
import org.junit.Assert.assertEquals
import org.junit.Test

/** 4×4 合法终盘：数字 1..4 各出现 4 次，其位置为 0、6、9、15。 */
private val SOLUTION_4 = listOf(
    1, 2, 3, 4,
    3, 4, 1, 2,
    2, 1, 4, 3,
    4, 3, 2, 1,
)

class RemainingCountsTest {

    private fun board(
        values: List<Int> = List(16) { 0 },
        givens: Set<Int> = emptySet(),
    ) = BoardCells(
        size = 4,
        values = values,
        givens = givens,
        solution = SOLUTION_4,
        notes = emptyMap(),
    )

    @Test
    fun `空盘每个数字剩余 size 次`() {
        assertEquals(listOf(4, 4, 4, 4), board().remainingCounts())
    }

    @Test
    fun `空棋盘 EMPTY 返回空列表`() {
        assertEquals(emptyList<Int>(), BoardCells.EMPTY.remainingCounts())
    }

    @Test
    fun `填对消耗该数字一次配额`() {
        val values = MutableList(16) { 0 }
        values[0] = 1 // solution[0] == 1
        assertEquals(listOf(3, 4, 4, 4), board(values).remainingCounts())
    }

    @Test
    fun `填错不消耗配额`() {
        val values = MutableList(16) { 0 }
        values[0] = 2 // solution[0] == 1，放错
        assertEquals(listOf(4, 4, 4, 4), board(values).remainingCounts())
    }

    @Test
    fun `givens 同样消耗配额`() {
        // 数字 1 的四个位置全部作为 given
        val givens = setOf(0, 6, 9, 15)
        val values = SOLUTION_4.mapIndexed { i, v -> if (i in givens) v else 0 }
        assertEquals(0, board(values, givens).remainingCounts()[0])
    }

    @Test
    fun `数字全部放对后剩余为 0`() {
        val values = MutableList(16) { 0 }
        listOf(0, 6, 9, 15).forEach { values[it] = 1 }
        assertEquals(0, board(values).remainingCounts()[0])
    }

    @Test
    fun `提示填入与手动输入同样消耗配额`() {
        var state = GameUiState(board = board())
        state = GameReducer.useHint(state, 0, 1)      // hint 填入正确的 1
        state = GameReducer.inputNumber(state, 1, 2)  // 手动填对 2
        assertEquals(listOf(3, 3, 4, 4), state.board.remainingCounts())
    }
}
