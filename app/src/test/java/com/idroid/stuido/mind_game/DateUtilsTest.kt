package com.idroid.stuido.mind_game

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateUtilsTest {

    @Test
    fun getCurrentDate_returnsTodayInYyyyMmDdFormat() {
        val actual = DateUtils.getCurrentDate()
        val expected = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        assertEquals("日期格式应为 yyyy-MM-dd", expected, actual)
    }

    @Test
    fun getCurrentDate_formatMatchesPattern() {
        val date = DateUtils.getCurrentDate()
        val pattern = Regex("""\d{4}-\d{2}-\d{2}""")
        assertTrue("返回值应匹配 yyyy-MM-dd 格式", pattern.matches(date))
    }

    @Test
    fun getCurrentDate_lengthIs10() {
        val date = DateUtils.getCurrentDate()
        assertEquals("yyyy-MM-dd 格式的长度应为 10", 10, date.length)
    }
}
