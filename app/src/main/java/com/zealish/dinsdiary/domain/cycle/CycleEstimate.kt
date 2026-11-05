package com.zealish.dinsdiary.domain.cycle

import java.time.LocalDate

/** Estimate confidence. Wording is "estimate confidence", never medical accuracy. */
enum class Confidence {
    INSUFFICIENT_DATA,
    LOW,
    MODERATE,
    HIGHER,
}

/**
 * One observed cycle interval: length in days plus the start date of the
 * period that ended it (used for history month labels). Newest last.
 */
data class ValidInterval(
    val days: Int,
    val endDate: LocalDate,
)

/**
 * Deterministic prediction snapshot. Predicted values must never be
 * stored or presented as actual [PeriodRecord] entries (PRD §8).
 */
data class CycleEstimate(
    val estimatedCycleLengthDays: Int?,
    val predictedNextStart: LocalDate?,
    val predictedPeriodEnd: LocalDate?,
    val estimatedOvulationDate: LocalDate?,
    val fertileWindowStart: LocalDate?,
    val fertileWindowEnd: LocalDate?,
    val confidence: Confidence,
    val explanationCode: String,
    /** Intervals that contributed to the estimate, newest last. */
    val validIntervals: List<ValidInterval>,
    /** Record ids needing user review (duplicate/overlap/invalid/outlier). */
    val flaggedRecordIds: List<String>,
    val algorithmVersion: String = CycleEngine.ALGORITHM_VERSION,
)
