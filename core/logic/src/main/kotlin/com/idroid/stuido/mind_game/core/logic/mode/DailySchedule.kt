package com.idroid.stuido.mind_game.core.logic.mode

import com.idroid.stuido.mind_game.core.logic.config.DEFAULT_DAILY_RESET_TIME
import com.idroid.stuido.mind_game.core.logic.config.DEFAULT_DAILY_TIMEZONE
import com.idroid.stuido.mind_game.core.logic.config.DailySection
import com.idroid.stuido.mind_game.core.logic.config.parseResetTime
import com.idroid.stuido.mind_game.core.logic.config.parseZone
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * 每日挑战的换题时刻表：每天在 [zone] 的本地 [resetTime] 换题。
 *
 * [puzzleDay] = 当前窗口「开启那天」的 [zone] 日历日期：本地时间早于 [resetTime] 时仍属前一天。
 * 必须比较**本地钟面时间**，不能用 `now - 17h`：后者在时间线上做减法，
 * 夏令时切换日会把换题点漂到 18:00 / 16:00。与设备自身时区无关，全球玩家同一时刻换题。
 */
class DailySchedule(
    val zone: ZoneId,
    val resetTime: LocalTime,
) {

    fun puzzleDay(now: Instant): LocalDate {
        val local = now.atZone(zone)
        val date = local.toLocalDate()
        return if (local.toLocalTime() < resetTime) date.minusDays(1) else date
    }

    /** [day] 这一期开始的时刻（该日 [zone] 本地 [resetTime]）。 */
    fun windowStart(day: LocalDate): Instant = day.atTime(resetTime).atZone(zone).toInstant()

    /** 下一次换题的时刻，严格晚于 [now]。 */
    fun nextResetAt(now: Instant): Instant = windowStart(puzzleDay(now).plusDays(1))

    /** [day] 这一期的谜题种子；与天梯种子不同命名空间（天梯为 64 位散列值）。 */
    fun seedFor(day: LocalDate, seedSalt: Long): Long = day.toEpochDay() + seedSalt

    companion object {
        fun from(daily: DailySection): DailySchedule = DailySchedule(
            zone = parseZone(daily.timezone) ?: ZoneId.of(DEFAULT_DAILY_TIMEZONE),
            resetTime = parseResetTime(daily.resetTime) ?: parseResetTime(DEFAULT_DAILY_RESET_TIME)!!,
        )
    }
}
