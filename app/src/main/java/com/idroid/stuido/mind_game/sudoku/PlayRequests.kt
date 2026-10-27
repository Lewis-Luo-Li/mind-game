package com.idroid.stuido.mind_game.sudoku

import com.idroid.stuido.mind_game.core.logic.config.GameRemoteConfig
import com.idroid.stuido.mind_game.core.logic.mode.DailySchedule
import com.idroid.stuido.mind_game.core.logic.mode.LadderPlan
import com.idroid.stuido.mind_game.feature.game.GameMode
import java.time.Instant

/** 每日挑战开局请求：规格/难度/种子全部来自配置与 [now] 所在的悉尼期。 */
internal fun dailyPlayRequest(id: Long, config: GameRemoteConfig, now: Instant): PlayRequest {
    val daily = config.sudoku.daily
    val schedule = DailySchedule.from(daily)
    val day = schedule.puzzleDay(now)
    val spec = daily.boardSpec()
    val difficulty = daily.difficultyLevel()
    return PlayRequest(
        id = id,
        mode = GameMode.DAILY,
        spec = spec,
        difficulty = difficulty,
        seed = schedule.seedFor(day, daily.seedSalt),
        rules = config.rulesFor("daily", spec, difficulty),
        puzzleDay = day,
    )
}

/** 天梯第 [level] 关开局请求：难度与种子由 [LadderPlan] 确定，所有玩家一致。 */
internal fun ladderPlayRequest(id: Long, config: GameRemoteConfig, level: Int): PlayRequest {
    val plan = LadderPlan(config.sudoku.ladder)
    val difficulty = plan.difficultyFor(level)
    return PlayRequest(
        id = id,
        mode = GameMode.LADDER,
        spec = plan.spec,
        difficulty = difficulty,
        seed = plan.seedFor(level),
        rules = config.rulesFor("ladder", plan.spec, difficulty),
        level = level,
    )
}
