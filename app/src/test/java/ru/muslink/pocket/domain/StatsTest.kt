package ru.muslink.pocket.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class StatsTest {

    private val may = YearMonth.of(2026, 5)
    private val june = YearMonth.of(2026, 6)

    private fun day(y: Int, m: Int, d: Int): Long = LocalDate.of(y, m, d).toEpochDay()

    private val lessons = listOf(
        // май: 1500.00 оплачено
        LessonInfo(id = 1, studentId = 1, epochDay = day(2026, 5, 4), durationMin = 60, amountKopecks = 150_000, paid = true),
        // май: 1200.00 долг
        LessonInfo(id = 2, studentId = 1, epochDay = day(2026, 5, 11), durationMin = 45, amountKopecks = 120_000, paid = false),
        // май: 800.50 долг
        LessonInfo(id = 3, studentId = 2, epochDay = day(2026, 5, 18), durationMin = 30, amountKopecks = 80_050, paid = false),
        // июнь: 2000.00 оплачено
        LessonInfo(id = 4, studentId = 2, epochDay = day(2026, 6, 1), durationMin = 60, amountKopecks = 200_000, paid = true)
    )

    @Test
    fun incomeForMonth_countsAccruedOnlyInThatMonth() {
        assertEquals(350_050L, Stats.incomeForMonth(lessons, may))
        assertEquals(200_000L, Stats.incomeForMonth(lessons, june))
        assertEquals(0L, Stats.incomeForMonth(lessons, YearMonth.of(2026, 7)))
    }

    @Test
    fun paidAndDebtForMonth_areConsistent() {
        assertEquals(150_000L, Stats.paidForMonth(lessons, may))
        assertEquals(200_050L, Stats.debtForMonth(lessons, may))
        // начислено = оплачено + долг
        assertEquals(
            Stats.incomeForMonth(lessons, may),
            Stats.paidForMonth(lessons, may) + Stats.debtForMonth(lessons, may)
        )
    }

    @Test
    fun totalDebt_sumsOnlyUnpaidLessonsAcrossMonths() {
        assertEquals(200_050L, Stats.totalDebt(lessons))
        assertEquals(350_000L, Stats.totalPaid(lessons))
    }

    @Test
    fun monthlySummaries_sortedDescendingAndAggregated() {
        val summaries = Stats.monthlySummaries(lessons)
        assertEquals(2, summaries.size)
        assertEquals(june, summaries[0].ym)
        assertEquals(may, summaries[1].ym)
        assertEquals(350_050L, summaries[1].accruedKopecks)
        assertEquals(150_000L, summaries[1].paidKopecks)
        assertEquals(200_050L, summaries[1].debtKopecks)
        assertEquals(0L, summaries[0].debtKopecks)
    }

    @Test
    fun upcoming_skipsPastAndLimits() {
        val today = day(2026, 5, 11)
        val result = Stats.upcoming(lessons, today, limit = 5)
        assertEquals(listOf(2L, 3L, 4L), result.map { it.id })
        assertEquals(2, Stats.upcoming(lessons, today, limit = 2).size)
    }

    @Test
    fun activeStudents_countsCurrentAndFuture() {
        val today = day(2026, 5, 11)
        assertEquals(setOf(1L, 2L), Stats.activeStudentIds(lessons, today))
    }

    @Test
    fun lessonsOn_returnsOnlyThatDay() {
        assertEquals(1, Stats.lessonsOn(lessons, day(2026, 5, 4)).size)
        assertTrue(Stats.lessonsOn(lessons, day(2026, 5, 5)).isEmpty())
    }
}
