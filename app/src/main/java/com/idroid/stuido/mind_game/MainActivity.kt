package com.idroid.stuido.mind_game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.idroid.stuido.mind_game.sudoku.MindgameRoot
import com.idroid.stuido.mind_game.sudoku.SudokuApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 由 MindgameThemeApp 凭 ThemePrefs 应用用户选择的外观/色板。
            MindgameRoot { themePrefs ->
                SudokuApp(themePrefs = themePrefs)
            }
        }
    }
}
