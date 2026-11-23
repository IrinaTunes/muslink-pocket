package ru.muslink.pocket.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth

/**
 * Чистая модель занятия, не зависящая от Android/Room.
 * Все суммы — в копейках (Long), без плавающей точки.
 */
data class LessonInfo(
    val id: Long = 0L,
    val studentId: Long = 0L,
    val epochDay: Long = 0L,
    val durationMin: Int = 0,
    val amountKopecks: Long = 0L,
    val paid: Boolean = false
)

data class MonthSummary(
    val ym: YearMonth,
    val accruedKopecks: Long,
    val paidKopecks: Long
) {
    val debtKopecks: Long get() = accruedKopecks - paidKopecks
}

object Stats {

    fun monthOf(epochDay: Long): YearMonth = YearMonth.from(LocalDate.ofEpochDay(epochDay))

    /** Начислено за месяц (доход месяца). */
    fun incomeForMonth(lessons: List<LessonInfo>, ym: YearMonth): Long =
        lessons.asSequence()
            .filter { monthOf(it.epochDay) == ym }
            .sumOf { it.amountKopecks }

    /** Оплачено за месяц. */
    fun paidForMonth(lessons: List<LessonInfo>, ym: YearMonth): Long =
        lessons.asSequence()
            .filter { monthOf(it.epochDay) == ym && it.paid }
            .sumOf { it.amountKopecks }

    /** Долг за конкретный месяц. */
    fun debtForMonth(lessons: List<LessonInfo>, ym: YearMonth): Long =
        incomeForMonth(lessons, ym) - paidForMonth(lessons, ym)

    /** Общий долг по всем занятиям. */
    fun totalDebt(lessons: List<LessonInfo>): Long =
        lessons.asSequence().filter { !it.paid }.sumOf { it.amountKopecks }

    /** Общая сумма оплаченного. */
    fun totalPaid(lessons: List<LessonInfo>): Long =
        lessons.asSequence().filter { it.paid }.sumOf { it.amountKopecks }

    /** Сводка по месяцам, новые сверху. */
    fun monthlySummaries(lessons: List<LessonInfo>): List<MonthSummary> =
        lessons.groupBy { monthOf(it.epochDay) }
            .map { (ym, list) ->
                MonthSummary(
                    ym = ym,
                    accruedKopecks = list.sumOf { it.amountKopecks },
                    paidKopecks = list.filter { it.paid }.sumOf { it.amountKopecks }
                )
            }
            .sortedByDescending { it.ym }

    /** Ближайшие занятия (включая сегодня), по возрастанию даты. */
    fun upcoming(lessons: List<LessonInfo>, fromDay: Long, limit: Int = 5): List<LessonInfo> =
        lessons.asSequence()
            .filter { it.epochDay >= fromDay }
            .sortedBy { it.epochDay }
            .take(limit)
            .toList()

    /** Занятия на конкретный день. */
    fun lessonsOn(lessons: List<LessonInfo>, epochDay: Long): List<LessonInfo> =
        lessons.filter { it.epochDay == epochDay }

    /**
     * «Активные» ученики — те, у кого есть занятие в текущем месяце
     * или запланированное в будущем.
     */
    fun activeStudentIds(lessons: List<LessonInfo>, todayDay: Long): Set<Long> {
        val currentMonth = monthOf(todayDay)
        return lessons.asSequence()
            .filter { it.epochDay >= todayDay || monthOf(it.epochDay) == currentMonth }
            .map { it.studentId }
            .toSet()
    }
}

object MoneyFormat {

    /** 150050 -> "1 500,50 ₽" */
    fun formatKopecks(kopecks: Long): String {
        val negative = kopecks < 0
        val abs = if (negative) -kopecks else kopecks
        val rublesPart = abs / 100
        val kopecksPart = (abs % 100).toInt()

        val digits = rublesPart.toString()
        val grouped = StringBuilder()
        var count = 0
        for (i in digits.length - 1 downTo 0) {
            grouped.append(digits[i])
            count++
            if (count % 3 == 0 && i != 0) grouped.append('\u00A0')
        }
        val rubles = grouped.reverse().toString()

        return buildString {
            if (negative) append('-')
            append(rubles)
            append(',')
            append(kopecksPart.toString().padStart(2, '0'))
            append(" ₽")
        }
    }

    /** Сумма без знака валюты: 150050 -> "1 500,50" */
    fun formatAmount(kopecks: Long): String = formatKopecks(kopecks).removeSuffix(" ₽")
}

object RubleParser {

    /** "1 500,50" / "1500.5" / "1500" -> копейки; null при ошибке. */
    fun parseToKopecks(input: String): Long? {
        val cleaned = input.trim()
            .replace("\u00A0", "")
            .replace(" ", "")
            .replace(',', '.')
        if (cleaned.isEmpty()) return null
        val value = try {
            BigDecimal(cleaned)
        } catch (e: NumberFormatException) {
            return null
        }
        if (value.signum() < 0) return null
        return try {
            value.setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact()
        } catch (e: ArithmeticException) {
            null
        }
    }

    /** Копейки -> строка для поля ввода (без знака ₽). */
    fun toInputString(kopecks: Long): String =
        BigDecimal.valueOf(kopecks, 2).stripTrailingZeros().toPlainString()
}
