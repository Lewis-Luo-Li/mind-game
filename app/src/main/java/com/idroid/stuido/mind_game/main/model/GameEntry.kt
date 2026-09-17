package com.idroid.stuido.mind_game.main.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.idroid.stuido.mind_game.R

/**
 * 一个可玩游戏的静态描述，用于主菜单列表。
 */
@Immutable
data class GameEntry(
    val id: GameId,
    @param:DrawableRes val iconRes: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
)

enum class GameId { SUDOKU, CROSSWORD, ZOODUKU }

val gameCatalog: List<GameEntry> = listOf(
    GameEntry(
        GameId.SUDOKU,
        R.drawable.ic_deepseek_g_sudoku,
        R.string.game_sudoku_title,
        R.string.game_sudoku_desc
    ),
)