package com.idroid.stuido.mind_game.sudoku

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.idroid.stuido.mind_game.core.logic.config.BoardSpec
import com.idroid.stuido.mind_game.core.logic.config.Difficulty
import com.idroid.stuido.mind_game.feature.game.GameMode
import com.idroid.stuido.mind_game.feature.game.ui.GameScreenRoute
import com.idroid.stuido.mind_game.main.model.GameId
import com.idroid.stuido.mind_game.main.model.gameCatalog
import com.idroid.stuido.mind_game.mainpage.MainScreen

/**
 * 页内导航目标（非对局态）。
 *
 * 用 `sealed interface` 而非 `enum`，是为了携带数据、并让结构天然映射到未来的
 * Nav 路由（每个子类型 ≈ 一条 route）。迁移到 navigation-compose 时替换代价很低。
 */
private sealed interface Dest {
    data object Games : Dest
    data object Settings : Dest
    data class GameDetail(val game: GameId) : Dest
}

/** 把 [Dest] 转为可 `rememberSaveable` 保存的稳定字符串。 */
private fun Dest.encode(): String = when (this) {
    Dest.Games -> "games"
    Dest.Settings -> "settings"
    is Dest.GameDetail -> "game:${game.name}"
}

private fun decodeDest(value: String): Dest = when {
    value == "settings" -> Dest.Settings
    value.startsWith("game:") ->
        Dest.GameDetail(GameId.valueOf(value.removePrefix("game:")))
    else -> Dest.Games
}

/**
 * 应用内导航宿主。极轻量，纯 Compose 状态机在「主菜单 / 游戏详情 / 设置 / 游玩屏」间切换，
 * 暂不引入 Nav 库。
 *
 * 采用 **返回栈列表**（`List<Dest>`）而非单个当前目标：
 *  - `navigate(to)` ≈ 压栈，`pop()` ≈ 出栈 —— 与 `NavController` 语义一一对应；
 *  - 配合 [BackHandler] 响应系统返回键，行为与 Nav 的默认返回一致，
 *    未来迁移到 navigation-compose 时行为不漂移。
 *
 * @param themePrefs 由 [MindgameRoot] 注入；设置页读写它即可即时换肤。
 */
@Composable
fun MindGameApp(themePrefs: ThemePrefs) {
    var playRequest by remember { mutableStateOf<PlayRequest?>(null) }
    var backStack by rememberSaveable(
        stateSaver = listSaver(
            save = { stack -> stack.map { it.encode() } },
            restore = { encoded -> encoded.map(::decodeDest) },
        ),
    ) { mutableStateOf(listOf<Dest>(Dest.Games)) }

    fun navigate(to: Dest) { backStack = backStack + to }
    fun pop() { backStack = backStack.dropLast(1) }

    // 对局屏：作为覆盖式“页面”，由 playRequest 驱动；退出即回到下方导航栈。
    // 它自带 Scaffold（顶栏/数字键盘），需要满铺到系统栏，故不套 inset 容器。
    val req = playRequest
    if (req != null) {
        GameScreenRoute(
            onExit = { playRequest = null },
            mode = req.mode,
            spec = req.spec,
            difficulty = req.difficulty,
            seed = req.seed,
            sessionKey = "sudoku-game-${req.id}",
        )
        return
    }

    // 非对局态：系统返回键 = 出栈（栈底不响应，交还系统）。
    BackHandler(enabled = backStack.size > 1) { pop() }

    // 唯一的「边到边」处理点：用 safeDrawing 一次性为所有普通页面避开
    // 状态栏 / 导航栏 / 刘海，因此各页面无需各自 Scaffold。
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        when (val dest = backStack.last()) {
            Dest.Games -> MainScreen(
                games = gameCatalog,
                onGameClick = { entry -> navigate(Dest.GameDetail(entry.id)) },
            )
            Dest.Settings -> SettingsScreen(
                themePrefs = themePrefs,
                onBack = ::pop,
            )

            is Dest.GameDetail -> GameHost(
                game = dest.game,
                onBack = ::pop,
                onOpenSettings = { navigate(Dest.Settings) },
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
        }
    }
}

/**
 * 单个游戏的宿主：Sudoku 复用既有 [HomeScreen] 作为开局入口；
 * Crossword / Zooduku 尚无玩法，显示占位屏（带返回）。
 */
@Composable
private fun GameHost(
    game: GameId,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onStart: (GameMode, BoardSpec, Difficulty, Long?) -> Unit,
) {
    when (game) {
        GameId.SUDOKU -> HomeScreen(
            onOpenSettings = onOpenSettings,
            onStart = onStart,
        )

        GameId.CROSSWORD, GameId.ZOODUKU -> ComingSoonScreen(
            title = game.name,
            onBack = onBack,
        )
    }
}

private var sessionCounter = 0L

private fun nextSessionId(): Long = ++sessionCounter
