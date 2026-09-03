package com.idroid.stuido.mind_game.feature.game.mvi

import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.feature.game.GameMode
import com.idroid.stuido.mind_game.feature.game.GameStatus

/**
 * 输入模式：玩家在当前选中的格子上进行"输入数值"还是"做铅笔笔记"。
 */
enum class InputMode {
    /** 输入实际数值（覆盖笔记）。 */
    NUMBER,

    /** 铅笔/笔记模式：在一格内同时保留多个候选数字。 */
    NOTE,
}

/**
 * 游戏 UI State：一局游戏的**不可变快照**，作为 Compose 渲染与 Reducer 变换的唯一数据源。
 *
 * @property config 对局配置（模式 / 规格 / 难度 / 种子 / 允许错误次数）。
 * @property board  当前盘面进展（值 + 给定标记 + 每格笔记 + 冲突高亮）。
 * @property selection 当前选中信息与输入模式。
 * @property progress 进度、状态、计时与忙碌标记。
 */
data class GameUiState(
    val config: GameConfig = GameConfig(),
    val board: BoardCells = BoardCells.EMPTY,
    val selection: Selection = Selection(),
    val progress: GameProgress = GameProgress(),
)

/**
 * 对局静态配置（开局后一般在整局内不变）。
 */
data class GameConfig(
    val mode: GameMode = GameMode.FREE,
    val spec: BoardSpec = BoardSpec.CLASSIC,
    val difficulty: Difficulty = Difficulty.EASY,
    val seed: Long? = null,
    val maxMistakes: Int = 3,
) {
    val size: Int get() = spec.size
}

/**
 * 当前盘面（不可变视图）。
 *
 * @property values 每个格子的当前值；0 表示空格。索引规则 `row * size + col`。
 * @property givens 开局"给定数字"的格子索引集合（玩家不可改动，也不能计入错误）。
 * @property solution 完整终盘，用于提示(FillHint)、判错与胜利判定。
 * @property notes 每格的铅笔笔记：cell 索引 -> 候选数字集合。仅空格有意义。
 */
data class BoardCells(
    val size: Int,
    val values: List<Int>,
    val givens: Set<Int>,
    val solution: List<Int>,
    val notes: Map<Int, Set<Int>>,
) {
    val totalCells: Int get() = size * size

    companion object {
        val EMPTY = BoardCells(
            size = 0,
            values = emptyList(),
            givens = emptySet(),
            solution = emptyList(),
            notes = emptyMap(),
        )
    }
}

/**
 * 选中态与输入模式。
 *
 * @property index 当前选中的格子；null 表示无选中。
 * @property conflicts 与当前选中/输入同值且在同行列宫的可疑格（红色高亮）。
 * @property inputMode 当前输入模式（数值 or 笔记）。
 */
data class Selection(
    val index: Int? = null,
    val conflicts: Set<Int> = emptySet(),
    val inputMode: InputMode = InputMode.NUMBER,
)

/**
 * 对局进度与状态。
 */
data class GameProgress(
    val status: GameStatus = GameStatus.IDLE,
    val mistakes: Int = 0,
    val timerSeconds: Int = 0,
    val isBusy: Boolean = false,
) {
    val isRunning: Boolean get() = status == GameStatus.RUNNING
    val isFinished: Boolean get() = status == GameStatus.FINISHED
}
