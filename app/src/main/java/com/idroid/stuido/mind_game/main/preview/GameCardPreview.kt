package com.idroid.stuido.mind_game.main.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.idroid.stuido.mind_game.R
import com.idroid.stuido.mind_game.core.ui.theme.AppThemeMode
import com.idroid.stuido.mind_game.core.ui.theme.MindgameTheme
import com.idroid.stuido.mind_game.main.model.GameEntry
import com.idroid.stuido.mind_game.main.model.GameId
import com.idroid.stuido.mind_game.main.page.GameCard

@Preview
@Composable
fun GameCardPreview() {
    MindgameTheme(
        mode = AppThemeMode.DARK,
    ) {
        val entry = GameEntry(
            id = GameId.SUDOKU,
            iconRes = R.drawable.ic_deepseek_g_sudoku,
            titleRes = R.string.game_sudoku_title,
            descriptionRes = R.string.game_sudoku_desc
        )
        GameCard(
            entry = entry,
            onClick = {}
        )
    }
}

@Preview
@Composable
fun GameCardLightPreview() {
    MindgameTheme(
        mode = AppThemeMode.LIGHT,
    ) {
        val entry = GameEntry(
            id = GameId.SUDOKU,
            iconRes = R.drawable.ic_deepseek_g_sudoku,
            titleRes = R.string.game_sudoku_title,
            descriptionRes = R.string.game_sudoku_desc
        )
        GameCard(
            entry = entry,
            onClick = {}
        )
    }
}