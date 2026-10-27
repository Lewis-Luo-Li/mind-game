package com.idroid.stuido.mind_game.sudoku

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.idroid.stuido.mind_game.core.logic.mode.LadderProgress
import com.idroid.stuido.mind_game.feature.game.GameMode
import com.idroid.stuido.mind_game.feature.game.GameOutcome
import java.time.LocalDate

/**
 * 玩家本地进度（SharedPreferences，模式同 [ThemePrefs]）：
 *  - 天梯已解锁的最高关；
 *  - 最近一次解出的每日挑战期。
 * 只有 [GameOutcome.SOLVED] 会写入；失败 / 揭示答案 / 重玩已通关卡不推进。
 */
class PlayerProgressStore(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var _ladderLevel by mutableStateOf(prefs.getInt(KEY_LADDER_LEVEL, 1).coerceAtLeast(1))

    private var _dailySolvedDay by mutableStateOf(
        prefs.getString(KEY_DAILY_SOLVED_DAY, null)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
    )

    /** 天梯当前可玩的最高关（从 1 开始）。 */
    val ladderLevel: Int get() = _ladderLevel

    /** 最近一次解出的每日挑战期；null = 从未解出。 */
    val dailySolvedDay: LocalDate? get() = _dailySolvedDay

    fun record(request: PlayRequest, outcome: GameOutcome) {
        if (outcome != GameOutcome.SOLVED) return
        when (request.mode) {
            GameMode.LADDER -> request.level?.let { level ->
                val next = LadderProgress.afterGame(_ladderLevel, level, solved = true)
                if (next != _ladderLevel) {
                    prefs.edit().putInt(KEY_LADDER_LEVEL, next).apply()
                    _ladderLevel = next
                }
            }
            GameMode.DAILY -> request.puzzleDay?.let { day ->
                if (_dailySolvedDay == null || day > _dailySolvedDay) {
                    prefs.edit().putString(KEY_DAILY_SOLVED_DAY, day.toString()).apply()
                    _dailySolvedDay = day
                }
            }
            GameMode.FREE -> Unit
        }
    }

    private companion object {
        const val PREFS_NAME = "mind_game_progress"
        const val KEY_LADDER_LEVEL = "ladder_level"
        const val KEY_DAILY_SOLVED_DAY = "daily_solved_day"
    }
}
