package com.idroid.stuido.mind_game.sudoku

import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.feature.game.GameMode

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
)
