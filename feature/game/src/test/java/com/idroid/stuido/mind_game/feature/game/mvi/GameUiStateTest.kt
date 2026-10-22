package com.idroid.stuido.mind_game.feature.game.mvi

import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.core.logic.config.RewardRule
import com.idroid.stuido.mind_game.core.logic.config.RewardsSection
import com.idroid.stuido.mind_game.feature.game.GameMode
import com.idroid.stuido.mind_game.feature.game.GameOutcome
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
        assertEquals(3, cfg.maxHints)
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

    @Test
    fun `hintsLeft 和 livesLeft 根据配置与进度计算且不小于零`() {
        val state = GameUiState(
            config = GameConfig(maxMistakes = 3, maxHints = 2),
            progress = GameProgress(
                mistakes = 1,
                hintsUsed = 2,
                outcome = GameOutcome.REVEALED,
            ),
        )
        assertEquals(0, state.hintsLeft)
        assertEquals(2, state.livesLeft)

        val exhausted = state.copy(
            progress = state.progress.copy(mistakes = 5, hintsUsed = 4),
        )
        assertEquals(0, exhausted.hintsLeft)
        assertEquals(0, exhausted.livesLeft)
    }

    // ===== 激励广告奖励：可用性判定（enabled / max_per_game / allow_in_daily） =====

    private fun stateWithRewards(
        mode: GameMode = GameMode.FREE,
        extraHint: RewardRule = RewardRule(enabled = true, amount = 1, maxPerGame = 3),
        extraLife: RewardRule = RewardRule(enabled = true, amount = 1, maxPerGame = 2),
        hintRewardsUsed: Int = 0,
        lifeRewardsUsed: Int = 0,
    ) = GameUiState(
        config = GameConfig(
            mode = mode,
            rewards = RewardsSection(extraLife = extraLife, extraHint = extraHint),
        ),
        progress = GameProgress(
            status = GameStatus.RUNNING,
            hintRewardsUsed = hintRewardsUsed,
            lifeRewardsUsed = lifeRewardsUsed,
        ),
    )

    @Test
    fun `reward available when enabled and under per-game cap`() {
        val s = stateWithRewards()
        assertTrue(s.hintRewardAvailable)
        assertTrue(s.lifeRewardAvailable)
    }

    @Test
    fun `reward unavailable once per-game cap reached`() {
        val s = stateWithRewards(hintRewardsUsed = 3, lifeRewardsUsed = 2)
        assertFalse(s.hintRewardAvailable)
        assertFalse(s.lifeRewardAvailable)
    }

    @Test
    fun `reward unavailable when rule disabled`() {
        val s = stateWithRewards(
            extraHint = RewardRule(enabled = false, amount = 1, maxPerGame = 3),
            extraLife = RewardRule(enabled = false, amount = 1, maxPerGame = 2),
        )
        assertFalse(s.hintRewardAvailable)
        assertFalse(s.lifeRewardAvailable)
    }

    @Test
    fun `daily mode blocks rewards when allow_in_daily is false`() {
        val rule = RewardRule(enabled = true, amount = 1, maxPerGame = 3, allowInDaily = false)
        val daily = stateWithRewards(mode = GameMode.DAILY, extraHint = rule, extraLife = rule)
        assertFalse(daily.hintRewardAvailable)
        assertFalse(daily.lifeRewardAvailable)

        val free = stateWithRewards(mode = GameMode.FREE, extraHint = rule, extraLife = rule)
        assertTrue(free.hintRewardAvailable)
        assertTrue(free.lifeRewardAvailable)
    }

    @Test
    fun `bonus hints and lives raise the effective totals`() {
        val s = GameUiState(
            config = GameConfig(maxMistakes = 3, maxHints = 2),
            progress = GameProgress(bonusHints = 2, bonusLives = 1, hintsUsed = 3, mistakes = 3),
        )
        assertEquals(4, s.hintsTotal)
        assertEquals(4, s.livesTotal)
        assertEquals(1, s.hintsLeft)
        assertEquals(1, s.livesLeft)
    }
}
