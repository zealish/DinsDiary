package com.zealish.dinsdiary.feature.calendar

import java.time.DayOfWeek
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the reported "missing Saturday" bug: every week row must hold
 * exactly 7 cells and every Saturday of the month must be rendered.
 */
class CalendarGridTest {

    private val months = listOf(
        YearMonth.of(2026, 11), // starts Sunday, no leading blanks
        YearMonth.of(2026, 8), // starts Saturday, six leading blanks
        YearMonth.of(2026, 6), // starts Monday, one leading blank
        YearMonth.of(2026, 2), // 28 days, starts Sunday
        YearMonth.of(2024, 2), // leap February, starts Thursday
    )

    @Test
    fun everyWeekHasExactlySevenCells() {
        for (month in months) {
            val weeks = monthCells(month)
            assertTrue("$month renders at least one week", weeks.isNotEmpty())
            weeks.forEach { week -> assertEquals("$month week $week", 7, week.size) }
        }
    }

    @Test
    fun allSaturdaysAreRenderedExactlyOnce() {
        for (month in months) {
            val expected = (1..month.lengthOfMonth())
                .map { month.atDay(it) }
                .filter { it.dayOfWeek == DayOfWeek.SATURDAY }
            assertTrue("$month has Saturdays", expected.isNotEmpty())
            val rendered = monthCells(month).flatten().filterNotNull()
            assertEquals("$month renders every date once", month.lengthOfMonth(), rendered.size)
            expected.forEach { saturday ->
                assertEquals("$month Saturday $saturday", 1, rendered.count { it == saturday })
            }
        }
    }

    @Test
    fun leadingBlanksMatchSundayFirstOrder() {
        // November 2026 starts on Sunday: first cell is the 1st, no blanks.
        val november = monthCells(YearMonth.of(2026, 11))
        assertEquals(YearMonth.of(2026, 11).atDay(1), november.first().first())
        // August 2026 starts on Saturday: six blanks then the 1st.
        val august = monthCells(YearMonth.of(2026, 8))
        assertEquals(List(6) { null } + listOf(YearMonth.of(2026, 8).atDay(1)), august.first())
    }
}
