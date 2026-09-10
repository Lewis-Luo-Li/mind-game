package com.idroid.stuido.mind_game.sudoku

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.idroid.stuido.mind_game.core.ui.theme.AppThemeMode
import com.idroid.stuido.mind_game.core.ui.theme.AppThemeScheme

/**
 * 「外观」设置的持久化小仓库（基于 SharedPreferences）。
 *
 * 存两件事：
 *  - scheme：强调色板（Lilac / Ocean / Forest / Sunset）
 *  - mode  ：亮度（System / Light / Dark）
 *
 * scheme / mode 用私有 Compose state 承载（存 SharedPreferences 的镜像），
 * 这样设置页直接调用 [setScheme] / [setMode] 后，凡是读取它们的 Compose 就重组换肤。
 */
class ThemePrefs(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var _scheme by mutableStateOf(loadScheme())

    private var _mode by mutableStateOf(loadMode())

    /** 当前选中色板。 */
    val scheme: AppThemeScheme get() = _scheme

    /** 当前亮度模式。 */
    val mode: AppThemeMode get() = _mode

    fun setScheme(value: AppThemeScheme) {
        prefs.edit().putString(KEY_SCHEME, value.name).apply()
        _scheme = value
    }

    fun setMode(value: AppThemeMode) {
        prefs.edit().putString(KEY_MODE, value.name).apply()
        _mode = value
    }

    private fun loadScheme(): AppThemeScheme =
        prefs.getString(KEY_SCHEME, null)
            ?.let { runCatching { AppThemeScheme.valueOf(it) }.getOrNull() }
            ?: AppThemeScheme.LILAC

    private fun loadMode(): AppThemeMode =
        prefs.getString(KEY_MODE, null)
            ?.let { runCatching { AppThemeMode.valueOf(it) }.getOrNull() }
            ?: AppThemeMode.SYSTEM

    private companion object {
        const val PREFS_NAME = "mind_game_theme"
        const val KEY_SCHEME = "theme_scheme"
        const val KEY_MODE = "theme_mode"
    }
}
