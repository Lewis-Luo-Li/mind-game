package com.idroid.stuido.mind_game.sudoku

import android.content.Context
import com.idroid.stuido.mind_game.feature.game.GameMode
import com.idroid.stuido.mind_game.feature.game.persistence.GameSaveCodec
import com.idroid.stuido.mind_game.feature.game.persistence.SavedGame
import com.idroid.stuido.mind_game.feature.game.persistence.toRestore
import java.time.LocalDate

/**
 * 未完成对局的本地存档（SharedPreferences + JSON，模式同 [PlayerProgressStore]）。
 *
 * 只存**一局**：玩家同时只会进行一个数独，新局覆盖旧档。
 * 存档格式与校验规则在 `:feature:game` 的 `SavedGame` 里，本类只负责读写与
 * 宿主层策略（每日挑战跨期作废）。
 */
class SavedGameStore(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * 读出可恢复的存档；不可恢复（版本不符 / 盘面矛盾 / 已结束 / 跨期每日）
     * 或被解出的局一律返回 null，并顺手删除脏数据，避免下次再判一遍。
     */
    fun loadResumable(currentDailyDay: LocalDate): SavedGame? {
        val saved = GameSaveCodec.decode(prefs.getString(KEY_SAVED_GAME, null))
            ?: return null.also { if (prefs.contains(KEY_SAVED_GAME)) clear() }

        if (isStaleDaily(saved, currentDailyDay) || saved.toRestore() == null) {
            clear()
            return null
        }
        return saved
    }

    fun save(saved: SavedGame) {
        prefs.edit().putString(KEY_SAVED_GAME, GameSaveCodec.encode(saved)).apply()
    }

    /** 终局 / 玩家放弃 / 存档无效时清空。 */
    fun clear() {
        prefs.edit().remove(KEY_SAVED_GAME).apply()
    }

    companion object {
        private const val PREFS_NAME = "mind_game_saved_state"
        private const val KEY_SAVED_GAME = "saved_game"

        /**
         * 每日挑战跨期即作废：题目已经换成新的一期，继续恢复旧盘会算错归属的挑战期。
         * 其他模式（自由 / 天梯）与日期无关，永远可恢复。
         */
        fun isStaleDaily(saved: SavedGame, currentDailyDay: LocalDate): Boolean {
            if (saved.mode != GameMode.DAILY.name) return false
            val savedDay = saved.puzzleDay?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?: return true
            return savedDay != currentDailyDay
        }
    }
}
