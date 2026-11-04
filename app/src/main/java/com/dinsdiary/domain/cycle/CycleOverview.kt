package com.dinsdiary.domain.cycle

import java.time.LocalDate

/**
 * Dashboard-ready cycle state (PRD §6.2, CYCLE_ENGINE.md §8).
 * Phase names are estimates unless [isBleedingToday] (recorded fact).
 */
data class CycleOverview(
    val today: LocalDate,
    val cycleDay: Int?,
    /** Language-neutral phase code; display text resolves in UI resources. */
    val phase: CyclePhase,
    val isBleedingToday: Boolean,
    val estimate: CycleEstimate,
    /** Latest observed period end (recorded fact), null when never recorded. */
    val lastPeriodEnd: LocalDate?,
    /** Recent observed average, else the configured preference as fallback. */
    val averagePeriodLengthDays: Int?,
    /** True when [averagePeriodLengthDays] comes from observed records. */
    val periodAverageObserved: Boolean,
) {
    val hasEstimate: Boolean get() = estimate.predictedNextStart != null
    val insufficientData: Boolean
        get() = estimate.confidence == Confidence.INSUFFICIENT_DATA
}

/** Language-neutral cycle phase (PRD §6.12); UI maps each to a localized label. */
enum class CyclePhase {
    MENSTRUATION,
    UNKNOWN,
    OVULATION,
    FERTILE_WINDOW,
    FOLLICULAR,
    LUTEAL,
}
