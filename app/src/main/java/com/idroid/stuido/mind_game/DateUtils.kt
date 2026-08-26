package com.idroid.stuido.mind_game

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {

    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getCurrentDate(): String {
        return LocalDate.now().format(formatter)
    }
}
