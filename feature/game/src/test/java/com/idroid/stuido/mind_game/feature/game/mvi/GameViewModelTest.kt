package com.idroid.stuido.mind_game.feature.game.mvi

import androidx.lifecycle.viewModelScope
import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.feature.game.GameMode
import com.idroid.stuido.mind_game.feature.game.GameOutcome
import com.idroid.stuido.mind_game.feature.game.GameStatus
import com.idroid.stuido.mind_game.feature.game.engine.GameFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val viewModels = mutableListOf<GameViewModel>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        viewModels.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }

    private fun newViewModel(): GameViewModel =
        GameViewModel(
            gameFactory = GameFactory(),
            generationDispatcher = testDispatcher,
        ).also(viewModels::add)

    private fun TestScope.collectEffects(vm: GameViewModel, effects: MutableList<GameEffect>) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.effect.toList(effects)
        }
    }

    private fun runGameTest(block: suspend TestScope.() -> Unit) = runTest(testDispatcher) {
        try {
            block()
        } finally {
            viewModels.forEach { it.viewModelScope.cancel() }
            viewModels.clear()
        }
    }

    private fun TestScope.startGame(
        vm: GameViewModel,
        maxMistakes: Int = 3,
        maxHints: Int = 2,
    ) {
        vm.dispatch(
            GameEvent.NewGame(
                GameMode.FREE,
                BoardSpec.MINI,
                Difficulty.EASY,
                seed = 42L,
                maxMistakes = maxMistakes,
                maxHints = maxHints,
            ),
        )
        runCurrent()
        assertEquals(GameStatus.RUNNING, vm.uiState.value.progress.status)
    }

    @Test
    fun `filling every empty cell solves the puzzle and emits completion`() = runGameTest {
        val vm = newViewModel()
        val effects = mutableListOf<GameEffect>()
        collectEffects(vm, effects)
        startGame(vm)

        val board = vm.uiState.value.board
        board.values.indices.filter { it !in board.givens }.forEach { index ->
            vm.dispatch(GameEvent.InputNumber(index, board.solution[index]))
        }

        assertEquals(GameStatus.FINISHED, vm.uiState.value.progress.status)
        assertEquals(GameOutcome.SOLVED, vm.uiState.value.progress.outcome)
        assertTrue(effects.any { it is GameEffect.PuzzleCompleted })
    }

    @Test
    fun `keypad digit resolves selection from the latest state at dispatch time`() = runGameTest {
        val vm = newViewModel()
        startGame(vm)
        val board = vm.uiState.value.board
        val first = board.values.indices.first { it !in board.givens }
        val second = board.values.indices.filter { it !in board.givens }[1]

        // 未选中时不做任何事（不依赖 UI 侧的 state 快照）
        vm.dispatch(GameEvent.KeypadDigit(1))
        assertEquals(0, vm.uiState.value.board.values[first])

        vm.dispatch(GameEvent.SelectCell(first))
        vm.dispatch(GameEvent.SelectCell(second))
        val digit = board.solution[second]
        vm.dispatch(GameEvent.KeypadDigit(digit))

        // 写入的是当前选中的 second（而不是最初选中的 first）
        assertEquals(digit, vm.uiState.value.board.values[second])
        assertEquals(0, vm.uiState.value.board.values[first])
    }

    @Test
    fun `keypad digit on a given cell is ignored`() = runGameTest {
        val vm = newViewModel()
        startGame(vm)
        val board = vm.uiState.value.board
        val given = board.givens.first()

        vm.dispatch(GameEvent.SelectCell(given))
        vm.dispatch(GameEvent.KeypadDigit(1))

        assertEquals(board.values[given], vm.uiState.value.board.values[given])
    }

    @Test
    fun `keypad digit in note mode toggles a pencil mark`() = runGameTest {
        val vm = newViewModel()
        startGame(vm)
        val board = vm.uiState.value.board
        val cell = board.values.indices.first { it !in board.givens }

        vm.dispatch(GameEvent.SelectCell(cell))
        vm.dispatch(GameEvent.ToggleNoteMode)
        vm.dispatch(GameEvent.KeypadDigit(3))

        assertEquals(0, vm.uiState.value.board.values[cell])
        assertTrue(3 in vm.uiState.value.board.notes[cell].orEmpty())
    }

    @Test
    fun `three wrong inputs fail the game and later input is ignored`() = runGameTest {
        val vm = newViewModel()
        val effects = mutableListOf<GameEffect>()
        collectEffects(vm, effects)
        startGame(vm)

        val board = vm.uiState.value.board
        board.values.indices.filter { it !in board.givens }.take(3).forEach { index ->
            val wrong = (1..board.size).first { it != board.solution[index] }
            vm.dispatch(GameEvent.InputNumber(index, wrong))
        }

        assertEquals(GameStatus.FINISHED, vm.uiState.value.progress.status)
        assertEquals(GameOutcome.FAILED, vm.uiState.value.progress.outcome)
        assertTrue(effects.any { it is GameEffect.GameOver })

        val valuesAfterFailure = vm.uiState.value.board.values
        val index = board.values.indices.first { it !in board.givens }
        vm.dispatch(GameEvent.InputNumber(index, board.solution[index]))
        assertEquals(valuesAfterFailure, vm.uiState.value.board.values)
    }

    @Test
    fun `retry same puzzle restores original board and resets progress`() = runGameTest {
        val vm = newViewModel()
        startGame(vm)
        val original = vm.uiState.value.board
        val firstEmpty = original.values.indices.first { it !in original.givens }
        vm.dispatch(GameEvent.RequestHint)

        val remaining = vm.uiState.value.board.values.indices
            .filter { it !in original.givens && it != firstEmpty }
        val wrong = (1..original.size).filter { it != original.solution[firstEmpty] }
        vm.dispatch(GameEvent.InputNumber(firstEmpty, wrong[0]))
        vm.dispatch(GameEvent.InputNumber(firstEmpty, wrong[1]))
        val secondEmpty = remaining.first()
        val wrongForSecond = (1..original.size).first { it != original.solution[secondEmpty] }
        vm.dispatch(GameEvent.InputNumber(secondEmpty, wrongForSecond))

        assertEquals(GameStatus.FINISHED, vm.uiState.value.progress.status)
        assertEquals(1, vm.uiState.value.progress.hintsUsed)
        vm.dispatch(GameEvent.RetrySamePuzzle)

        val retried = vm.uiState.value
        assertEquals(original.values, retried.board.values)
        assertEquals(original.givens, retried.board.givens)
        assertEquals(0, retried.progress.mistakes)
        assertEquals(0, retried.progress.hintsUsed)
        assertEquals(GameStatus.RUNNING, retried.progress.status)
        assertNull(retried.progress.outcome)
    }

    @Test
    fun `retry after failed new game restores the last successful config and puzzle`() = runGameTest {
        val vm = newViewModel()
        val effects = mutableListOf<GameEffect>()
        collectEffects(vm, effects)
        vm.dispatch(
            GameEvent.NewGame(
                GameMode.FREE,
                BoardSpec.CLASSIC,
                Difficulty.EASY,
                seed = 42L,
            ),
        )
        runCurrent()
        val originalValues = vm.uiState.value.board.values

        vm.dispatch(GameEvent.NewGame(GameMode.FREE, BoardSpec.MINI, Difficulty.EXPERT))
        runCurrent()

        assertTrue(effects.any { it is GameEffect.NewGameFailed })
        vm.dispatch(GameEvent.RetrySamePuzzle)

        val retried = vm.uiState.value
        assertEquals(BoardSpec.CLASSIC, retried.config.spec)
        assertEquals(Difficulty.EASY, retried.config.difficulty)
        assertEquals(9, retried.board.size)
        assertEquals(originalValues, retried.board.values)
    }

    @Test
    fun `hints select unresolved cells and stop at the configured budget`() = runGameTest {
        val vm = newViewModel()
        val effects = mutableListOf<GameEffect>()
        collectEffects(vm, effects)
        startGame(vm)

        val initial = vm.uiState.value.board
        val firstEmpty = initial.values.indices.first { it !in initial.givens }
        vm.dispatch(GameEvent.RequestHint)
        assertEquals(initial.solution[firstEmpty], vm.uiState.value.board.values[firstEmpty])
        assertEquals(1, vm.uiState.value.progress.hintsUsed)
        assertEquals(0, vm.uiState.value.progress.mistakes)
        assertTrue(effects.contains(GameEffect.HintFilled(firstEmpty, initial.solution[firstEmpty])))

        vm.dispatch(GameEvent.RequestHint)
        val secondHint = effects.filterIsInstance<GameEffect.HintFilled>().last()
        assertTrue(secondHint.index != firstEmpty)
        assertEquals(initial.solution[secondHint.index], vm.uiState.value.board.values[secondHint.index])
        assertEquals(2, vm.uiState.value.progress.hintsUsed)

        val exhausted = vm.uiState.value
        vm.dispatch(GameEvent.RequestHint)
        assertEquals(exhausted, vm.uiState.value)
        assertTrue(effects.contains(GameEffect.ShowMessage("No hints left")))
    }

    @Test
    fun `hint on a correctly filled selected cell targets the first unresolved cell`() =
        runGameTest {
            val vm = newViewModel()
            val effects = mutableListOf<GameEffect>()
            collectEffects(vm, effects)
            startGame(vm)

            val board = vm.uiState.value.board
            val selected = board.values.indices.first { it !in board.givens }
            vm.dispatch(GameEvent.InputNumber(selected, board.solution[selected]))
            val firstUnresolved = vm.uiState.value.board.values.indices.first {
                it !in board.givens && it != selected
            }
            vm.dispatch(GameEvent.RequestHint)

            assertEquals(board.solution[firstUnresolved], vm.uiState.value.board.values[firstUnresolved])
            assertTrue(effects.contains(GameEffect.HintFilled(firstUnresolved, board.solution[firstUnresolved])))
            assertEquals(1, vm.uiState.value.progress.hintsUsed)
        }

    @Test
    fun `request solve reveals the solution without completion effect`() = runGameTest {
        val vm = newViewModel()
        val effects = mutableListOf<GameEffect>()
        collectEffects(vm, effects)
        startGame(vm)

        vm.dispatch(GameEvent.RequestSolve)

        assertEquals(GameStatus.FINISHED, vm.uiState.value.progress.status)
        assertEquals(GameOutcome.REVEALED, vm.uiState.value.progress.outcome)
        assertEquals(vm.uiState.value.board.solution, vm.uiState.value.board.values)
        assertTrue(effects.any { it is GameEffect.SolutionRevealed })
        assertFalse(effects.any { it is GameEffect.PuzzleCompleted })
    }

    @Test
    fun `restart retains lives and hint budgets from the last request`() = runGameTest {
        val vm = newViewModel()
        startGame(vm, maxMistakes = 5, maxHints = 4)

        vm.dispatch(GameEvent.Restart)
        runCurrent()

        assertEquals(5, vm.uiState.value.config.maxMistakes)
        assertEquals(4, vm.uiState.value.config.maxHints)
    }

    @Test
    fun `gameplay events are ignored after finish`() = runGameTest {
        val vm = newViewModel()
        startGame(vm)
        vm.dispatch(GameEvent.RequestSolve)
        val finished = vm.uiState.value

        vm.dispatch(GameEvent.SelectCell(12))
        assertEquals(finished, vm.uiState.value)
        vm.dispatch(GameEvent.InputNumber(12, 1))
        assertEquals(finished, vm.uiState.value)
        vm.dispatch(GameEvent.ToggleNote(12, 1))
        assertEquals(finished, vm.uiState.value)
        vm.dispatch(GameEvent.Erase)
        assertEquals(finished, vm.uiState.value)
        vm.dispatch(GameEvent.RequestHint)
        assertEquals(finished, vm.uiState.value)
        vm.dispatch(GameEvent.RequestCheck)
        assertEquals(finished, vm.uiState.value)
        vm.dispatch(GameEvent.RequestSolve)
        assertEquals(finished, vm.uiState.value)
    }

    @Test
    fun `undo reverts the last input and is ignored after finish`() = runGameTest {
        val vm = newViewModel()
        startGame(vm)

        val board = vm.uiState.value.board
        val index = board.values.indices.first { it !in board.givens }
        vm.dispatch(GameEvent.InputNumber(index, board.solution[index]))
        assertTrue(vm.uiState.value.canUndo)

        vm.dispatch(GameEvent.Undo)
        assertEquals(0, vm.uiState.value.board.values[index])
        assertFalse(vm.uiState.value.canUndo)

        vm.dispatch(GameEvent.RequestSolve)
        val finished = vm.uiState.value
        vm.dispatch(GameEvent.Undo)
        assertEquals(finished, vm.uiState.value)
    }

    @Test
    fun `granted rewarded hints refill the hint budget and can fill a cell`() = runGameTest {
        val vm = newViewModel()
        val effects = mutableListOf<GameEffect>()
        collectEffects(vm, effects)
        startGame(vm, maxHints = 1)

        // 用光仅有的一次提示。
        vm.dispatch(GameEvent.RequestHint)
        assertEquals(0, vm.uiState.value.hintsLeft)

        // 激励广告发奖：+1 提示，随后立即消耗填格（路由层的调用顺序）。
        vm.dispatch(GameEvent.GrantRewardedHints(1))
        assertEquals(1, vm.uiState.value.hintsLeft)
        assertEquals(1, vm.uiState.value.progress.hintRewardsUsed)
        vm.dispatch(GameEvent.RequestHint)
        val lastHint = effects.filterIsInstance<GameEffect.HintFilled>().last()
        assertEquals(
            vm.uiState.value.board.solution[lastHint.index],
            vm.uiState.value.board.values[lastHint.index],
        )
        assertEquals(0, vm.uiState.value.hintsLeft)
    }

    @Test
    fun `granted rewarded life revives a failed game back to running`() = runGameTest {
        val vm = newViewModel()
        val effects = mutableListOf<GameEffect>()
        collectEffects(vm, effects)
        startGame(vm, maxMistakes = 1)

        val board = vm.uiState.value.board
        val index = board.values.indices.first { it !in board.givens }
        val wrong = (1..board.size).first { it != board.solution[index] }
        vm.dispatch(GameEvent.InputNumber(index, wrong))
        assertEquals(GameStatus.FINISHED, vm.uiState.value.progress.status)
        assertEquals(GameOutcome.FAILED, vm.uiState.value.progress.outcome)

        vm.dispatch(GameEvent.GrantRewardedLives(1))

        assertEquals(GameStatus.RUNNING, vm.uiState.value.progress.status)
        assertNull(vm.uiState.value.progress.outcome)
        assertEquals(1, vm.uiState.value.livesLeft)
        assertEquals(1, vm.uiState.value.progress.lifeRewardsUsed)

        // 复活后再错一次（另一格）：正常判负终局。
        val board2 = vm.uiState.value.board
        val other = board2.values.indices.first { it !in board2.givens && it != index }
        val wrong2 = (1..board2.size).first { it != board2.solution[other] }
        vm.dispatch(GameEvent.InputNumber(other, wrong2))
        assertEquals(GameStatus.FINISHED, vm.uiState.value.progress.status)
        assertEquals(GameOutcome.FAILED, vm.uiState.value.progress.outcome)
    }
}
