package com.idroid.stuido.mind_game.sudoku

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.idroid.stuido.mind_game.core.ui.theme.MindgameTheme

/**
 * App 根：持有并应用用户的主题偏好，再把主题控制器传给内部 UI。
 *
 * [ThemePrefs] 的两个字段本身就是 Compose state；设置页一改它们，
 * 这里读取 [ThemePrefs.scheme]/[ThemePrefs.mode] 就会触发重组，
 * [MindgameTheme] 随即用新色板整棵重绘，实现「即时换肤」。
 */
@Composable
fun MindgameRoot(content: @Composable (themePrefs: ThemePrefs) -> Unit) {
    val context = LocalContext.current
    val themePrefs = remember { ThemePrefs(context) }

    MindgameTheme(
        scheme = themePrefs.scheme,
        mode = themePrefs.mode,
    ) {
        // 用主题背景色铺满整棵 UI，充当 window background。
        // 这样即使某些页面（如移除 Scaffold 后的主菜单）自身不画背景，
        // 也不会露出未着色的窗口底色，且换肤时背景随之更新。
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            content(themePrefs)
        }
    }
}
