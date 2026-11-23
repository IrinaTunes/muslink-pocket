package ru.muslink.pocket.domain

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormat {
    private val dayFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.forLanguageTag("ru"))
    private val monthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("ru"))

    fun formatDay(epochDay: Long): String =
        LocalDate.ofEpochDay(epochDay).format(dayFormatter)

    fun formatMonth(ym: YearMonth): String =
        ym.atDay(1).format(monthFormatter).replaceFirstChar { it.titlecase(Locale.forLanguageTag("ru")) }

    fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

    fun todayMillisUtc(): Long = LocalDate.now().toEpochDay() * 86_400_000L

    fun epochDayFromUtcMillis(millis: Long): Long = Math.floorDiv(millis, 86_400_000L)
}
