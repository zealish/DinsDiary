package com.zealish.dinsdiary.domain.usecase

import com.zealish.dinsdiary.domain.cycle.Confidence
import com.zealish.dinsdiary.domain.cycle.CycleEngine
import com.zealish.dinsdiary.domain.cycle.CycleOverview
import com.zealish.dinsdiary.domain.cycle.CyclePhase
import com.zealish.dinsdiary.domain.cycle.PeriodRecord
import com.zealish.dinsdiary.domain.cycle.PeriodStatus
import com.zealish.dinsdiary.domain.model.UserProfile
import java.time.Clock
import java.time.LocalDate
import kotlin.math.roundToInt
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Combines recorded history + preferences into dashboard state
 * (PRD §6.2). Owns domain logic; ViewModels only expose the result.
 */
class GetCycleOverviewUseCase @Inject constructor(
    private val clock: Clock,
) {
    fun observe(
        periods: Flow<List<PeriodRecord>>,
        profile: Flow<UserProfile>,
    ): Flow<CycleOverview> = combine(periods, profile) { records, prefs ->
        build(LocalDate.now(clock), records, prefs)
    }

    /** Pure function for deterministic tests. */
    fun build(today: LocalDate, records: List<PeriodRecord>, prefs: UserProfile): CycleOverview {
        val estimate = CycleEngine.predict(
            records = records,
            preferredCycleLengthDays = prefs.preferredCycleLengthDays,
            preferredPeriodLengthDays = prefs.preferredPeriodLengthDays,
            lutealPhaseLengthDays = prefs.lutealPhaseLengthDays,
        )
        val latestStart = records.maxByOrNull { it.start }?.start
        val cycleDay = latestStart?.let { CycleEngine.cycleDay(it, today) }

        val bleedingToday = records.any { r ->
            !today.isBefore(r.start) && (r.end == null || !today.isAfter(r.end))
        }
        val phase = when {
            bleedingToday -> CyclePhase.MENSTRUATION
            estimate.confidence == Confidence.INSUFFICIENT_DATA -> CyclePhase.UNKNOWN
            estimate.estimatedOvulationDate == today -> CyclePhase.OVULATION
            estimate.fertileWindowStart != null && estimate.fertileWindowEnd != null &&
                !today.isBefore(estimate.fertileWindowStart) &&
                !today.isAfter(estimate.fertileWindowEnd) -> CyclePhase.FERTILE_WINDOW
            estimate.estimatedOvulationDate != null && today.isBefore(estimate.estimatedOvulationDate) ->
                CyclePhase.FOLLICULAR
            else -> CyclePhase.LUTEAL
        }
        val observedDurations = records
            .filter {
                it.status == PeriodStatus.COMPLETED &&
                    it.end != null && !it.end.isBefore(it.start)
            }
            .sortedBy { it.start }
            .takeLast(6)
            .map { java.time.temporal.ChronoUnit.DAYS.between(it.start, it.end) + 1 }
        val averagePeriodLengthDays =
            if (observedDurations.isNotEmpty()) {
                observedDurations.average().roundToInt().coerceAtLeast(1)
            } else {
                prefs.preferredPeriodLengthDays
            }
        return CycleOverview(
            today = today,
            cycleDay = cycleDay,
            phase = phase,
            isBleedingToday = bleedingToday,
            estimate = estimate,
            lastPeriodEnd = records.mapNotNull { it.end }.maxOrNull(),
            averagePeriodLengthDays = averagePeriodLengthDays,
            periodAverageObserved = observedDurations.isNotEmpty(),
        )
    }
}
