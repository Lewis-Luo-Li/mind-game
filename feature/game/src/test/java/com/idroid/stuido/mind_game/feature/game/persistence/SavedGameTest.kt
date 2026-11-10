package com.idroid.stuido.mind_game.feature.game.persistence

import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.feature.game.GameMode
import com.idroid.stuido.mind_game.feature.game.GameStatus
import com.idroid.stuido.mind_game.feature.game.mvi.BoardCells
import com.idroid.stuido.mind_game.feature.game.mvi.GameConfig
import com.idroid.stuido.mind_game.feature.game.mvi.GameProgress
import com.idroid.stuido.mind_game.feature.game.mvi.GameUiState
import com.idroid.stuido.mind_game.feature.game.mvi.InputMode
import com.idroid.stuido.mind_game.feature.game.mvi.Selection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 存档格式 [SavedGame] 的往返与失效判定。
 *
 * 关键回归：**任何不可信的存档都必须返回 null**——宁可让玩家重开一局，
 * 也不要把错位/越界的数字铺到棋盘上。
 */
class SavedGameTest {

    private val solution = listOf(
        1, 2, 3, 4,
        3, 4, 1, 2,
        2, 1, 4, 3,
        4, 3, 2, 1,
    )

    /** 4x4：0..11 为给定，12..15 为空格，其中 12 已做笔记 {2,4}，15 已填 3。 */
    private fun inProgress(): GameUiState {
        val givens = (0..11).toSet()
        val values = solution.mapIndexed { i, v ->
            when {
                i in givens -> v
                i == 12 -> 0
                i == 15 -> solution[15]
                else -> 0
            }
        }
        return GameUiState(
            config = GameConfig(
                mode = GameMode.FREE,
                spec = BoardSpec.MINI,
                difficulty = Difficulty.EASY,
                seed = 42L,
                maxMistakes = 3,
                maxHints = 2,
            ),
            board = BoardCells(
                size = 4,
                values = values,
                givens = givens,
                solution = solution,
                notes = mapOf(12 to setOf(2, 4), 13 to emptySet()),
            ),
            selection = Selection(index = 12, inputMode = InputMode.NOTE),
            progress = GameProgress(
                status = GameStatus.RUNNING,
                mistakes = 1,
                timerSeconds = 187,
                hintsUsed = 1,
                bonusHints = 2,
                bonusLives = 1,
                hintRewardsUsed = 1,
                lifeRewardsUsed = 1,
                gameInstance = 3,
            ),
        )
    }

    // ---------------- 往返 ----------------

    @Test
    fun `存档往返后盘面 进度 选中 配置全部一致`() {
        val original = inProgress()
        val restored = original.toSavedGame(requestId = 7L, puzzleDay = null)
            .let(::encodeDecode)
            .toRestore()

        assertNotNull(restored)
        requireNotNull(restored)
        // 空笔记条目会被归一化掉，所以逐字段比对，而不是整块 BoardCells 相等。
        assertEquals(original.board.values, restored.board.values)
        assertEquals(original.board.givens, restored.board.givens)
        assertEquals(original.board.solution, restored.board.solution)
        assertEquals(
            original.board.notes.filterValues { it.isNotEmpty() },
            restored.board.notes,
        )
        assertEquals(original.selection, restored.selection)
        assertEquals(original.config, restored.config)
        // isBusy 恒为 false、outcome 为空；其余进度字段原样保留。
        assertEquals(original.progress.copy(isBusy = false), restored.progress)
        // 撤销历史不落盘。
        assertTrue(restored.history.isEmpty())
    }

    @Test
    fun `requestId 与 puzzleDay 一并保留`() {
        val saved = inProgress().toSavedGame(requestId = 99L, puzzleDay = "2026-11-09")
            .let(::encodeDecode)
        assertEquals(99L, saved.requestId)
        assertEquals("2026-11-09", saved.puzzleDay)
    }

    @Test
    fun `暂停中的局也能恢复`() {
        val paused = inProgress().let {
            it.copy(progress = it.progress.copy(status = GameStatus.PAUSED))
        }
        assertEquals(GameStatus.PAUSED, paused.toSavedGame(1L).let(::encodeDecode).toRestore()?.progress?.status)
    }

    @Test
    fun `空笔记不落盘`() {
        val saved = inProgress().toSavedGame(1L)
        assertFalse("空集合不该写入存档", saved.notes.containsKey(13))
        assertEquals(listOf(2, 4), saved.notes[12])
    }

    // ---------------- 失效判定 ----------------

    @Test
    fun `版本不符的存档被判废`() {
        val saved = inProgress().toSavedGame(1L).copy(version = SAVE_FORMAT_VERSION + 1)
        assertNull(saved.toRestore())
    }

    @Test
    fun `已结束或未开局的存档不恢复`() {
        val finished = inProgress().let {
            it.copy(progress = it.progress.copy(status = GameStatus.FINISHED))
        }
        assertNull(finished.toSavedGame(1L).toRestore())
        assertNull(GameUiState().toSavedGame(1L).toRestore())
    }

    @Test
    fun `盘面尺寸与解数量不一致的存档被判废`() {
        val saved = inProgress().toSavedGame(1L)
        assertNull(saved.copy(values = saved.values.dropLast(1)).toRestore())
        assertNull(saved.copy(solution = emptyList()).toRestore())
    }

    @Test
    fun `越界的格值与候选数字被判废`() {
        val saved = inProgress().toSavedGame(1L)
        // 4x4 盘面出现 9 = 数据损坏
        assertNull(saved.copy(values = saved.values.toMutableList().also { it[12] = 9 }).toRestore())
        assertEquals(4, saved.size)
    }

    @Test
    fun `给定格与终盘不符的存档被判废`() {
        val saved = inProgress().toSavedGame(1L)
        val wrong = saved.values.toMutableList().also { it[0] = if (it[0] == 1) 2 else 1 }
        assertNull(saved.copy(values = wrong).toRestore())
    }

    @Test
    fun `已填满终盘的残档不恢复`() {
        // 终局落盘前进程被杀：盘面已经全对，没有可续的进度。
        val saved = inProgress().toSavedGame(1L).copy(values = solution)
        assertNull(saved.toRestore())
    }

    @Test
    fun `枚举值损坏时按不可恢复处理`() {
        val saved = inProgress().toSavedGame(1L)
        assertNull(saved.copy(mode = "NOT_A_MODE").toRestore())
        assertNull(saved.copy(status = "WHATEVER").toRestore())
        // 只有 inputMode 损坏时降级为 NUMBER，不整局丢弃。
        assertEquals(
            InputMode.NUMBER,
            saved.copy(inputMode = "???").toRestore()?.selection?.inputMode,
        )
    }

    @Test
    fun `笔记落在已填格或给定格时被丢弃`() {
        val saved = inProgress().toSavedGame(1L)
            .copy(notes = mapOf(0 to listOf(2), 15 to listOf(1), 12 to listOf(2, 99)))
        val restored = saved.toRestore()
        assertNotNull(restored)
        requireNotNull(restored)
        assertEquals(mapOf(12 to setOf(2)), restored.board.notes)
    }

    // ---------------- 编解码 ----------------

    @Test
    fun `解码垃圾内容返回 null 而不是抛异常`() {
        assertNull(GameSaveCodec.decode(null))
        assertNull(GameSaveCodec.decode(""))
        assertNull(GameSaveCodec.decode("not json"))
        // 字段残缺的 JSON 能解码（全部字段有默认值），但必须过不了盘面校验。
        assertNull(GameSaveCodec.decode("""{"size": 4}""")?.toRestore())
    }

    @Test
    fun `未知字段被忽略以兼容新版本存档`() {
        val json = GameSaveCodec.encode(inProgress().toSavedGame(1L))
        val withExtra = json.dropLast(1) + ""","future_field":123}"""
        assertNotNull(GameSaveCodec.decode(withExtra))
    }

    @Test
    fun `每秒计时变化不落盘 非计时变化立即落盘`() {
        val state = inProgress()

        // 首次一定落盘
        assertTrue(shouldPersist(null, state))

        // 只有计时 +1：不写
        val ticked = state.copy(progress = state.progress.copy(timerSeconds = state.progress.timerSeconds + 1))
        assertFalse(shouldPersist(state, ticked))

        // 累计到阈值：补写一次
        val tickedEnough = state.copy(
            progress = state.progress.copy(timerSeconds = state.progress.timerSeconds + TIMER_SAVE_INTERVAL_SECONDS),
        )
        assertTrue(shouldPersist(state, tickedEnough))

        // 落子 / 笔记 / 错误数 / 选中 变化：立即写
        assertTrue(shouldPersist(state, state.copy(selection = Selection(index = 13))))
        assertTrue(shouldPersist(state, state.copy(progress = state.progress.copy(mistakes = 2))))
        assertTrue(
            shouldPersist(
                state,
                state.copy(board = state.board.copy(notes = state.board.notes + (13 to setOf(1)))),
            ),
        )
    }

    @Test
    fun `结束或未开局的局不落盘`() {
        val state = inProgress()
        assertFalse(shouldPersist(null, state.copy(progress = state.progress.copy(status = GameStatus.FINISHED))))
        assertFalse(shouldPersist(null, GameUiState()))
        assertFalse(shouldPersist(null, state.copy(board = BoardCells.EMPTY)))
    }

    private fun encodeDecode(saved: SavedGame): SavedGame {
        val encoded = GameSaveCodec.encode(saved)
        return requireNotNull(GameSaveCodec.decode(encoded)) { "存档编码后应可解码" }
    }
}
