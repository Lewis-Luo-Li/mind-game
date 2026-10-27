package com.idroid.stuido.mind_game.core.logic.mode

import com.idroid.stuido.mind_game.core.logic.config.DailySection
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class DailyScheduleTest {

    private val sydney = ZoneId.of("Australia/Sydney")
    private val schedule = DailySchedule(sydney, LocalTime.of(17, 0))

    private fun sydneyTime(value: String): Instant = LocalDateTime.parse(value).atZone(sydney).toInstant()

    @Test
    fun `before reset belongs to previous day, at reset switches`() {
        assertEquals(LocalDate.of(2026, 10, 24), schedule.puzzleDay(sydneyTime("2026-10-25T16:59:59")))
        assertEquals(LocalDate.of(2026, 10, 25), schedule.puzzleDay(sydneyTime("2026-10-25T17:00")))
        assertEquals(LocalDate.of(2026, 10, 25), schedule.puzzleDay(sydneyTime("2026-10-26T00:30")))
    }

    @Test
    fun `DST start day still rolls over at 17_00 local`() {
        // 2026-10-04 Sydney 进入夏令时（02:00 → 03:00）
        assertEquals(LocalDate.of(2026, 10, 3), schedule.puzzleDay(sydneyTime("2026-10-04T16:59")))
        assertEquals(LocalDate.of(2026, 10, 4), schedule.puzzleDay(sydneyTime("2026-10-04T17:00")))
        assertEquals(LocalDate.of(2026, 10, 4), schedule.puzzleDay(sydneyTime("2026-10-04T17:30")))
    }

    @Test
    fun `DST end day still rolls over at 17_00 local`() {
        // 2026-04-05 Sydney 结束夏令时（03:00 → 02:00）
        assertEquals(LocalDate.of(2026, 4, 4), schedule.puzzleDay(sydneyTime("2026-04-05T16:30")))
        assertEquals(LocalDate.of(2026, 4, 5), schedule.puzzleDay(sydneyTime("2026-04-05T17:00")))
    }

    @Test
    fun `reset instant is 06_00 UTC in AEDT and 07_00 UTC in AEST`() {
        assertEquals(Instant.parse("2026-10-25T06:00:00Z"), schedule.windowStart(LocalDate.of(2026, 10, 25)))
        assertEquals(Instant.parse("2026-06-15T07:00:00Z"), schedule.windowStart(LocalDate.of(2026, 6, 15)))
    }

    @Test
    fun `next reset is strictly after now`() {
        assertEquals(sydneyTime("2026-10-25T17:00"), schedule.nextResetAt(sydneyTime("2026-10-25T09:00")))
        assertEquals(sydneyTime("2026-10-26T17:00"), schedule.nextResetAt(sydneyTime("2026-10-25T17:00")))
        assertEquals(sydneyTime("2026-10-04T17:00"), schedule.nextResetAt(sydneyTime("2026-10-04T12:00")))
    }

    @Test
    fun `same instant gives same day regardless of device timezone`() {
        val instant = Instant.parse("2026-10-25T05:59:00Z") // Sydney 16:59
        assertEquals(LocalDate.of(2026, 10, 24), schedule.puzzleDay(instant))
        assertEquals(LocalDate.of(2026, 10, 25), schedule.puzzleDay(instant.plusSeconds(60)))
    }

    @Test
    fun `seed is epoch day plus salt and from() reads config`() {
        val day = LocalDate.of(2026, 10, 25)
        assertEquals(day.toEpochDay() + 7, schedule.seedFor(day, 7))

        val fromConfig = DailySchedule.from(DailySection(timezone = "Europe/London", resetTime = "09:30"))
        assertEquals(ZoneId.of("Europe/London"), fromConfig.zone)
        assertEquals(LocalTime.of(9, 30), fromConfig.resetTime)
        assertEquals(sydney, DailySchedule.from(DailySection()).zone)
    }
}
