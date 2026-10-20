package com.idroid.stuido.mind_game.feature.game.reducers

import com.idroid.stuido.mind_game.feature.game.GameOutcome
import com.idroid.stuido.mind_game.feature.game.GameStatus
import com.idroid.stuido.mind_game.feature.game.engine.GameSeed
import com.idroid.stuido.mind_game.feature.game.mvi.BoardCells
import com.idroid.stuido.mind_game.feature.game.mvi.GameConfig
import com.idroid.stuido.mind_game.feature.game.mvi.GameProgress
import com.idroid.stuido.mind_game.feature.game.mvi.GameSnapshot
import com.idroid.stuido.mind_game.feature.game.mvi.GameUiState
import com.idroid.stuido.mind_game.feature.game.mvi.InputMode
import com.idroid.stuido.mind_game.feature.game.mvi.Selection

/**
 * 纯函数 Reducer：`(GameUiState, XxxInput) -> GameUiState`，不含副作用。
 */
object GameReducer {

    fun setGameConfig(state: GameUiState, config: GameConfig): GameUiState =
        state.copy(config = config)

    fun startGame(state: GameUiState, seed: GameSeed): GameUiState {
        val board = BoardCells(
            size = seed.size,
            values = seed.values,
            givens = seed.givens,
            solution = seed.solution,
            notes = emptyMap(),
        )
        return state.copy(
            board = board,
            selection = Selection(),
            progress = GameProgress(
                status = GameStatus.RUNNING,
                mistakes = 0,
                timerSeconds = 0,
                hintsUsed = 0,
                outcome = null,
            ),
            history = emptyList(),
        )
    }

    fun selectCell(state: GameUiState, index: Int): GameUiState {
        val current = state.selection.index
        val toggled = if (current == index) null else index
        return state.copy(
            selection = state.selection.copy(index = toggled, conflicts = emptySet()),
        )
    }

    fun setConflicts(state: GameUiState, conflicts: Set<Int>): GameUiState =
        state.copy(selection = state.selection.copy(conflicts = conflicts))

    fun clearConflicts(state: GameUiState): GameUiState =
        state.copy(selection = state.selection.copy(conflicts = emptySet()))

    fun inputNumber(state: GameUiState, index: Int, value: Int): GameUiState {
        val board = state.board
        if (!board.isMutable(index)) return state
        if (value != 0 && value !in 1..board.size) return state

        val values = board.values.toMutableList()
        values[index] = value
        val notes = when {
            value == 0 -> board.notes - index
            else -> board.notes + (index to emptySet())
        }
        val updated = state.copy(
            board = board.copy(values = values, notes = notes),
            selection = state.selection.copy(index = index, conflicts = emptySet()),
        )
        return updated.recordHistory(state)
    }

    fun useHint(state: GameUiState, index: Int, value: Int): GameUiState {
        if (!state.board.isMutable(index)) return state
        val updated = inputNumber(state, index, value)
        return updated.copy(
            selection = updated.selection.copy(index = index),
            progress = updated.progress.copy(hintsUsed = state.progress.hintsUsed + 1),
        )
    }

    fun toggleNote(state: GameUiState, index: Int, value: Int): GameUiState {
        val board = state.board
        if (!board.isMutable(index) || board.values[index] != 0) return state
        if (value !in 1..board.size) return state

        val existing = board.notes[index].orEmpty()
        val updated = if (value in existing) existing - value else existing + value
        val notes = board.notes + (index to updated)
        val next = state.copy(
            board = board.copy(notes = notes),
            selection = state.selection.copy(index = index, conflicts = emptySet()),
        )
        return next.recordHistory(state)
    }

    fun erase(state: GameUiState, index: Int): GameUiState = inputNumber(state, index, 0)

    /** 撤销：弹出最近一份快照，恢复盘面与选中；不回滚错误 / 计时 / 提示 / outcome。 */
    fun undo(state: GameUiState): GameUiState {
        val snapshot = state.history.lastOrNull() ?: return state
        return state.copy(
            board = snapshot.board,
            selection = snapshot.selection,
            history = state.history.dropLast(1),
        )
    }

    /** 单步/自动求解：全盘铺满终盘、清笔记，并将选中态归零。 */
    fun revealSolution(state: GameUiState): GameUiState {
        val board = state.board
        return state.copy(
            board = board.copy(values = board.solution.toList(), notes = emptyMap()),
            selection = Selection(),
        )
    }

    fun toggleNoteMode(state: GameUiState): GameUiState {
        val next = if (state.selection.inputMode == InputMode.NUMBER) InputMode.NOTE else InputMode.NUMBER
        return state.copy(selection = state.selection.copy(inputMode = next))
    }

    fun setNoteMode(state: GameUiState, mode: InputMode): GameUiState =
        state.copy(selection = state.selection.copy(inputMode = mode))

    fun togglePause(state: GameUiState): GameUiState {
        val status = state.progress.status
        val next = when (status) {
            GameStatus.RUNNING -> GameStatus.PAUSED
            GameStatus.PAUSED -> GameStatus.RUNNING
            else -> status
        }
        if (next == status) return state
        return state.copy(progress = state.progress.copy(status = next))
    }

    fun tick(state: GameUiState, elapsedSeconds: Int): GameUiState =
        state.copy(progress = state.progress.copy(timerSeconds = elapsedSeconds))

    fun incrementMistakes(state: GameUiState, by: Int = 1): GameUiState =
        state.copy(progress = state.progress.copy(mistakes = state.progress.mistakes + by))

    fun finish(state: GameUiState, outcome: GameOutcome): GameUiState =
        state.copy(progress = state.progress.copy(status = GameStatus.FINISHED, outcome = outcome))

    fun setBusy(state: GameUiState, busy: Boolean): GameUiState =
        state.copy(progress = state.progress.copy(isBusy = busy))
}

/** 撤销历史上限：超出时丢弃最旧快照。 */
private const val MAX_HISTORY = 100

/**
 * 仅当盘面或选中态相对 [previous] 真正变化时，才把变更前快照压入历史。
 */
private fun GameUiState.recordHistory(previous: GameUiState): GameUiState =
    if (board == previous.board && selection == previous.selection) {
        this
    } else {
        copy(
            history = (previous.history + GameSnapshot(previous.board, previous.selection))
                .takeLast(MAX_HISTORY),
        )
    }

private fun BoardCells.isMutable(index: Int): Boolean =
    index in values.indices && index !in givens
