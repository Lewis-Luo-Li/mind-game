package com.idroid.stuido.mind_game.sudoku

import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.core.logic.config.GameRules
import com.idroid.stuido.mind_game.feature.game.GameMode
import java.time.LocalDate

/**
 * 一次"开始游玩"的完整请求，由主菜单产出、交由 GameRoute 开局。
 * 足够轻量，不需要 Parcelable（compose 内部直接用 remember 持有即可）。
 */
data class PlayRequest(
    val id: Long,                 // 每次开始唯一，用来强制新建 GameViewModel（状态隔离）
    val mode: GameMode,
    val spec: BoardSpec,
    val difficulty: Difficulty,
    val seed: Long?,
    val rules: GameRules,
    /** 每日挑战所属期（开局时确定；跨过换题时刻再解出仍记入这一期）。 */
    val puzzleDay: LocalDate? = null,
    /** 天梯关卡号。 */
    val level: Int? = null,
    /** 覆盖该规格/难度的挖空数（天梯 holes_step_per_round 逐轮加难）。 */
    val holes: Int? = null,
)
