package com.dinsdiary.domain.cycle

import java.time.LocalDate

/**
 * Calendar event sets for one viewed period (PRD §6.4).
 * Recorded bleeding and estimates are separate sets by construction.
 */
data class CalendarMarks(
    val bleedingDates: Set<LocalDate>,
    val predictedDates: Set<LocalDate>,
    val fertileDates: Set<LocalDate>,
    val ovulationDate: LocalDate?,
)
