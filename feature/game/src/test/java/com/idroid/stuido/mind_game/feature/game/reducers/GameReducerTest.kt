package com.idroid.stuido.mind_game.feature.game.reducers

import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.feature.game.GameMode
import com.idroid.stuido.mind_game.feature.game.GameStatus
import com.idroid.stuido.mind_game.feature.game.engine.GameSeed
import com.idroid.stuido.mind_game.feature.game.mvi.GameConfig
import com.idroid.stuido.mind_game.feature.game.mvi.GameUiState
import com.idroid.stuido.mind_game.feature.game.mvi.InputMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 2 —— GameReducer 纯函数单元测试。
 */
class GameReducerTest {

    private val solution = listOf(
        1, 2, 3, 4,
        3, 4, 1, 2,
        2, 1, 4, 3,
        4, 3, 2, 1,
    )

    private fun seed(): GameSeed {
        val givens = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
        return GameSeed(
            size = 4,
            values = solution.mapIndexed { i, v -> if (i in givens) v else 0 },
            solution = solution,
            givens = givens.toSet(),
        )
    }

    private fun startedState(): GameUiState {
        var s = GameUiState()
        s = GameReducer.setGameConfig(
            s,
            GameConfig(mode = GameMode.FREE, spec = BoardSpec.MINI, difficulty = Difficulty.EASY),
        )
        return GameReducer.startGame(s, seed())
    }

    // 配置与开局
    @Test
    fun `setGameConfig 记录对局配置`() {
        val state = GameReducer.setGameConfig(
            GameUiState(),
            GameConfig(
                mode = GameMode.DAILY,
                spec = BoardSpec.CLASSIC,
                difficulty = Difficulty.EXPERT,
                seed = 42L,
            ),
        )
        assertEquals(GameMode.DAILY, state.config.mode)
        assertEquals(BoardSpec.CLASSIC.size, state.config.size)
        assertEquals(Difficulty.EXPERT, state.config.difficulty)
        assertEquals(42L, state.config.seed)
    }

    @Test
    fun `startGame 铺盘并重置进度与选中`() {
        val s = startedState()
        assertEquals(4, s.board.size)
        assertEquals(seed().values, s.board.values)
        assertEquals(seed().givens, s.board.givens)
        assertEquals(GameStatus.RUNNING, s.progress.status)
        assertEquals(0, s.progress.mistakes)
        assertEquals(0, s.progress.timerSeconds)
        assertNull(s.selection.index)
        assertTrue(s.selection.conflicts.isEmpty())
    }

    @Test
    fun `startGame 之后可在允许格输入真实值`() {
        val after = GameReducer.inputNumber(startedState(), 12, 2)
        assertEquals(2, after.board.values[12])
        assertEquals(12, after.selection.index)
    }

    // 批量断言辅助
    private fun id(v: Int) = v

    // 选中与高亮
    @Test
    fun `selectCell 初次选中与再次点击取消`() {
        var s = startedState()
        s = GameReducer.selectCell(s, 3)
        assertEquals(3, s.selection.index)
        s = GameReducer.selectCell(s, 3)
        assertNull(s.selection.index)
    }

    @Test
    fun `selectCell 切换不同格子`() {
        var s = GameReducer.selectCell(startedState(), 0)
        s = GameReducer.selectCell(s, 1)
        assertEquals(1, s.selection.index)
    }

    @Test
    fun `selectCell 清除既有判错高亮`() {
        var s = GameReducer.setConflicts(startedState(), setOf(1, 2))
        s = GameReducer.selectCell(s, 5)
        assertTrue(s.selection.conflicts.isEmpty())
    }

    @Test
    fun `setConflicts 与 clearConflicts`() {
        var s = GameReducer.setConflicts(startedState(), setOf(0, 3))
        assertEquals(setOf(0, 3), s.selection.conflicts)
        s = GameReducer.clearConflicts(s)
        assertTrue(s.selection.conflicts.isEmpty())
    }

    // 输入 / 边界
    @Test
    fun `inputNumber 非法 index 不改动盘面`() {
        val s = startedState()
        assertEquals(s, GameReducer.inputNumber(s, 200, 1))
        assertEquals(s, GameReducer.inputNumber(s, -1, 1))
    }

    @Test
    fun `inputNumber 超出规格的值不落盘`() {
        val s = GameReducer.inputNumber(startedState(), 12, 5) // 4x4 max = 4
        assertEquals(0, s.board.values[12])
        assertNotEquals(5, s.board.values[12])
    }

    @Test
    fun `inputNumber 对给定格不可改动`() {
        val s = startedState()
        assertEquals(s.board.values[0], GameReducer.inputNumber(s, 0, 4).board.values[0])
    }

    @Test
    fun `inputNumber 输入真实值后清空同格笔记`() {
        var s = startedState()
        s = GameReducer.toggleNote(s, 12, 2)
        s = GameReducer.inputNumber(s, 12, 1)
        assertTrue(s.board.notes.getOrDefault(12, emptySet()).isEmpty())
    }

    @Test
    fun `inputNumber value=0 等价清空`() {
        var s = GameReducer.inputNumber(startedState(), 12, 3)
        s = GameReducer.inputNumber(s, 12, 0)
        assertEquals(0, s.board.values[12])
    }

    // 笔记
    @Test
    fun `toggleNote 在空白非给定格添加并可再切移除`() {
        var s = startedState()
        s = GameReducer.toggleNote(s, 12, 2)
        assertEquals(setOf(2), s.board.notes[12])
        s = GameReducer.toggleNote(s, 12, 2)
        assertTrue(s.board.notes.getOrDefault(12, emptySet()).isEmpty())
    }

    @Test
    fun `toggleNote 拒绝对给定格或已填数字格`() {
        val s = startedState()
        assertTrue(GameReducer.toggleNote(s, 0, 2).board.notes.getOrDefault(0, emptySet()).isEmpty())
        val filled = GameReducer.inputNumber(s, 12, 2)
        assertTrue(GameReducer.toggleNote(filled, 12, 1).board.notes.getOrDefault(12, emptySet()).isEmpty())
    }

    @Test
    fun `toggleNote 越界候选值被忽略`() {
        val s = GameReducer.toggleNote(startedState(), 12, 99)
        assertTrue(s.board.notes.getOrDefault(12, emptySet()).isEmpty())
    }

    // 擦除
    @Test
    fun `erase 清空内容`() {
        var s = GameReducer.inputNumber(startedState(), 12, 2)
        assertEquals(0, GameReducer.erase(s, 12).board.values[12])
    }

    @Test
    fun `erase 对给定格无效`() {
        val s = startedState()
        assertEquals(s.board.values[1], GameReducer.erase(s, 1).board.values[1])
    }

    // 模式
    @Test
    fun `toggleNoteMode 轮换`() {
        var s = startedState()
        assertEquals(InputMode.NUMBER, s.selection.inputMode)
        s = GameReducer.toggleNoteMode(s)
        assertEquals(InputMode.NOTE, s.selection.inputMode)
        s = GameReducer.toggleNoteMode(s)
        assertEquals(InputMode.NUMBER, s.selection.inputMode)
    }

    @Test
    fun `setNoteMode 显式设定到 NOTE`() {
        val s = GameReducer.setNoteMode(startedState(), InputMode.NOTE)
        assertEquals(InputMode.NOTE, s.selection.inputMode)
    }

    // 暂停 / 计时 / 错误
    @Test
    fun `togglePause 在 RUNNING 与 PAUSED 之间切换`() {
        var s = startedState()
        s = GameReducer.togglePause(s)
        assertEquals(GameStatus.PAUSED, s.progress.status)
        s = GameReducer.togglePause(s)
        assertEquals(GameStatus.RUNNING, s.progress.status)
    }

    @Test
    fun `tick 写入已经时间`() {
        assertEquals(137, GameReducer.tick(startedState(), 137).progress.timerSeconds)
    }

    @Test
    fun `incrementMistakes 累加错误计数`() {
        var s = GameReducer.incrementMistakes(startedState(), 2)
        s = GameReducer.incrementMistakes(s)
        assertEquals(3, s.progress.mistakes)
    }

    // 终态
    @Test
    fun `finish 置为 FINISHED 并驱动辅助字段`() {
        val s = GameReducer.finish(GameReducer.tick(startedState(), 9))
        assertEquals(GameStatus.FINISHED, s.progress.status)
        assertFalse(s.progress.isRunning)
        assertTrue(s.progress.isFinished)
    }

    @Test
    fun `finished 态再次 togglePause 不会改变`() {
        val finished = GameReducer.finish(startedState())
        assertEquals(GameStatus.FINISHED, GameReducer.togglePause(finished).progress.status)
    }

    @Test
    fun `fail 代表错误用尽的失败结束`() {
        val s = GameReducer.fail(GameReducer.incrementMistakes(startedState(), 3))
        assertEquals(GameStatus.FINISHED, s.progress.status)
        assertEquals(3, s.progress.mistakes)
    }

    @Test
    fun `revealSolution 铺满终盘并清空笔记与选中与高亮`() {
        var s = startedState()
        s = GameReducer.toggleNote(s, 12, 2)
        s = GameReducer.setConflicts(s, setOf(0))
        val solved = GameReducer.revealSolution(s)
        assertEquals(solution, solved.board.values)
        assertTrue(solved.board.notes.isEmpty())
        assertTrue(solved.selection.conflicts.isEmpty())
        assertNull(solved.selection.index)
    }

    @Test
    fun `revealSolution 保持给定集合`() {
        val s = startedState()
        assertEquals(s.board.givens, GameReducer.revealSolution(s).board.givens)
    }

    @Suppress("unused")
    private fun _idUnused(v: Int) = v
}
