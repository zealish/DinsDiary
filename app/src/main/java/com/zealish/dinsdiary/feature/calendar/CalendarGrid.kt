package com.zealish.dinsdiary.feature.calendar

import java.time.LocalDate
import java.time.YearMonth

/**
 * Pure month-grid layout backing [MonthGrid] (PRD §6.4).
 *
 * Sunday-first weeks of exactly 7 cells; `null` marks blank padding
 * before the 1st and after the last day. Every date of [month] —
 * including Saturdays — appears exactly once.
 */
fun monthCells(month: YearMonth): List<List<LocalDate?>> {
    val first = month.atDay(1)
    // DayOfWeek: Monday == 1 .. Sunday == 7, so Sunday maps to zero leading blanks.
    val leading = first.dayOfWeek.value % 7
    val cells: List<LocalDate?> = List(leading) { null } +
        (1..month.lengthOfMonth()).map { month.atDay(it) }
    return cells.chunked(7).map { week ->
        if (week.size < 7) week + List(7 - week.size) { null } else week
    }
}
