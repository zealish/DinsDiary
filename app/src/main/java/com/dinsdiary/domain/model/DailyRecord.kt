package com.dinsdiary.domain.model

import java.time.LocalDate

enum class FlowIntensity {
    LIGHT,
    MEDIUM,
    HEAVY,
    VERY_HEAVY,
}

/** One daily bleeding observation (PRD §6.3). */
data class DailyRecord(
    val date: LocalDate,
    val periodId: String?,
    val flow: FlowIntensity?,
    val spotting: Boolean,
    val notes: String?,
)
