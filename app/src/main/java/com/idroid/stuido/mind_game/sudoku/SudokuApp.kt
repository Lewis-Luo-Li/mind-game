package com.idroid.stuido.mind_game.sudoku

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.idroid.stuido.mind_game.feature.game.ui.GameScreenRoute

/** 非对局时的页内导航目标。 */
private enum class MainDest { HOME, SETTINGS }

/**
 * 应用内导航宿主。极轻量，纯 Compose 状态机在「主菜单 / 设置 / 游玩屏」间切换，
 * 暂不引入 Nav 库。
 *
 * @param themePrefs 由 [MindgameRoot] 注入；设置页读写它即可即时换肤。
 */
@Composable
fun SudokuApp(themePrefs: ThemePrefs) {
    var playRequest by remember { mutableStateOf<PlayRequest?>(null) }
    var dest by rememberSaveable { mutableStateOf(MainDest.HOME) }

    val req = playRequest
    if (req != null) {
        GameScreenRoute(
            onExit = { playRequest = null }, // 返回主菜单
            mode = req.mode,
            spec = req.spec,
            difficulty = req.difficulty,
            seed = req.seed,
            sessionKey = "sudoku-game-${req.id}",
        )
        return
    }

    when (dest) {
        MainDest.HOME -> HomeScreen(
            onOpenSettings = { dest = MainDest.SETTINGS },
            onStart = { mode, spec, difficulty, seed ->
                playRequest = PlayRequest(
                    id = nextSessionId(),
                    mode = mode,
                    spec = spec,
                    difficulty = difficulty,
                    seed = seed,
                )
            },
        )

        MainDest.SETTINGS -> SettingsScreen(
            themePrefs = themePrefs,
            onBack = { dest = MainDest.HOME },
        )
    }
}

private var sessionCounter = 0L

private fun nextSessionId(): Long = ++sessionCounter
