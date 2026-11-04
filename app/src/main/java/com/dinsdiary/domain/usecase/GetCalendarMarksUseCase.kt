package com.dinsdiary.domain.usecase

import com.dinsdiary.domain.cycle.CalendarMarks
import com.dinsdiary.domain.cycle.CycleEngine
import com.dinsdiary.domain.cycle.PeriodRecord
import com.dinsdiary.domain.model.UserProfile
import java.time.LocalDate
import javax.inject.Inject

/**
 * Derives calendar markers from history + preferences (PRD §6.4).
 * Pure function for deterministic tests.
 */
class GetCalendarMarksUseCase @Inject constructor() {

    fun build(records: List<PeriodRecord>, prefs: UserProfile): CalendarMarks {
        val bleeding = records.flatMap { record ->
            val end = record.end ?: record.start
            generateSequence(record.start) { it.plusDays(1) }
                .takeWhile { !it.isAfter(end) }
                .toList()
        }.toSet()

        val estimate = CycleEngine.predict(
            records = records,
            preferredCycleLengthDays = prefs.preferredCycleLengthDays,
            preferredPeriodLengthDays = prefs.preferredPeriodLengthDays,
            lutealPhaseLengthDays = prefs.lutealPhaseLengthDays,
        )
        val predicted = if (estimate.predictedNextStart != null && estimate.predictedPeriodEnd != null) {
            generateSequence(estimate.predictedNextStart) { it.plusDays(1) }
                .takeWhile { !it.isAfter(estimate.predictedPeriodEnd) }
                .toSet()
        } else {
            emptySet()
        }
        val fertile = if (estimate.fertileWindowStart != null && estimate.fertileWindowEnd != null) {
            generateSequence(estimate.fertileWindowStart) { it.plusDays(1) }
                .takeWhile { !it.isAfter(estimate.fertileWindowEnd) }
                .toSet()
        } else {
            emptySet()
        }
        return CalendarMarks(
            bleedingDates = bleeding,
            predictedDates = predicted - bleeding,
            fertileDates = fertile - bleeding - predicted,
            ovulationDate = estimate.estimatedOvulationDate,
        )
    }
}
