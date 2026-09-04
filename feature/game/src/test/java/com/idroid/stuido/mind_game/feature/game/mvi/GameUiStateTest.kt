package com.idroid.stuido.mind_game.feature.game.mvi

import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.feature.game.GameMode
import com.idroid.stuido.mind_game.feature.game.GameStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameUiStateTest {

    @Test
    fun `默认配置为 9x9 EASY FREE`() {
        val cfg = GameConfig()
        assertEquals(GameMode.FREE, cfg.mode)
        assertEquals(BoardSpec.CLASSIC, cfg.spec)
        assertEquals(9, cfg.size)
        assertEquals(Difficulty.EASY, cfg.difficulty)
        assertEquals(3, cfg.maxMistakes)
        assertEquals(null, cfg.seed)
    }

    @Test
    fun `GameConfig size 由 spec 委派`() {
        assertEquals(4, GameConfig(spec = BoardSpec.MINI).size)
        assertEquals(6, GameConfig(spec = BoardSpec.MEDIUM).size)
        assertEquals(9, GameConfig(spec = BoardSpec.CLASSIC).size)
    }

    @Test
    fun `GameProgress 派生运行状态`() {
        assertFalse(GameProgress().isRunning)
        val running = GameProgress(status = GameStatus.RUNNING)
        assertTrue(running.isRunning)
        assertFalse(running.isFinished)

        val finished = GameProgress(status = GameStatus.FINISHED)
        assertTrue(finished.isFinished)
        assertFalse(finished.isRunning)

        assertEquals(GameStatus.IDLE, GameProgress().status)
    }

    @Test
    fun `Selection 默认无选中无冲突且为 NUMBER`() {
        val sel = Selection()
        assertEquals(null, sel.index)
        assertTrue(sel.conflicts.isEmpty())
        assertEquals(InputMode.NUMBER, sel.inputMode)
    }

    @Test
    fun `BoardCells EMPTY 是空盘占位`() {
        val empty = BoardCells.EMPTY
        assertEquals(0, empty.size)
        assertEquals(0, empty.totalCells)
        assertTrue(empty.values.isEmpty())
        assertTrue(empty.givens.isEmpty())
    }

    @Test
    fun `GameUiState 默认为 IDLE 的空快照`() {
        val s = GameUiState()
        assertEquals(GameStatus.IDLE, s.progress.status)
        assertEquals(BoardCells.EMPTY, s.board)
    }
}
