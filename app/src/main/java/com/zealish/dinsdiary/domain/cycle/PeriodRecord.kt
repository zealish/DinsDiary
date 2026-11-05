package com.zealish.dinsdiary.domain.cycle

import java.time.LocalDate

/**
 * One user-recorded menstrual period. Actual history — never an estimate.
 * See PRD §6.3, §7 and CYCLE_ENGINE.md §3.
 */
data class PeriodRecord(
    val id: String,
    val start: LocalDate,
    val end: LocalDate?,
    val status: PeriodStatus,
    val erroneous: Boolean = false,
)

enum class PeriodStatus {
    ACTIVE,
    COMPLETED,
    NEEDS_REVIEW,
}
