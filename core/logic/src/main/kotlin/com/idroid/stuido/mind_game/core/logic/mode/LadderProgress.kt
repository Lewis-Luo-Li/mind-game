package com.idroid.stuido.mind_game.core.logic.mode

/**
 * 天梯进度规则：[unlockedLevel] = 玩家当前可玩的最高关（从 1 开始）。
 * 只有「真正解出」当前最高关才解锁下一关；失败、揭示答案、重玩已通关的关卡都不推进。
 */
object LadderProgress {

    fun afterGame(unlockedLevel: Int, playedLevel: Int, solved: Boolean): Int =
        if (solved && playedLevel == unlockedLevel) unlockedLevel + 1 else unlockedLevel

    /** [level] 是否可以开始：不能跳过尚未解锁的关卡。 */
    fun canPlay(unlockedLevel: Int, level: Int): Boolean = level in 1..unlockedLevel
}
