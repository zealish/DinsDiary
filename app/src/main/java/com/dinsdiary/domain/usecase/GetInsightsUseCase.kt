package com.dinsdiary.domain.usecase

import com.dinsdiary.domain.cycle.CycleEngine
import com.dinsdiary.domain.cycle.PeriodRecord
import com.dinsdiary.domain.model.JournalEntry
import com.dinsdiary.domain.model.UserProfile
import javax.inject.Inject
import kotlin.math.roundToInt

data class CycleLengthStats(
    val count: Int,
    val average: Double,
    val median: Int,
    val min: Int,
    val max: Int,
)

/**
 * Observed-history summaries for Insights (PRD §6.8).
 * Observed statistics only — no diagnostic conclusions.
 */
data class InsightsData(
    val cycleStats: CycleLengthStats?,
    val averagePeriodDays: Double?,
    val periodsCount: Int,
    val moodCounts: Map<String, Int>,
    val symptomCounts: Map<String, Int>,
    val hasEnoughHistory: Boolean,
    val cycleIntervals: List<Int> = emptyList(),
    /** Month short name ("Oct") of the period ending each interval, newest last. */
    val cycleIntervalLabels: List<String> = emptyList(),
)
class GetInsightsUseCase @Inject constructor() {

    fun build(
        records: List<PeriodRecord>,
        entries: List<JournalEntry>,
        prefs: UserProfile,
        locale: java.util.Locale = java.util.Locale.ENGLISH,
    ): InsightsData {
        val estimate = CycleEngine.predict(
            records = records,
            preferredCycleLengthDays = prefs.preferredCycleLengthDays,
            preferredPeriodLengthDays = prefs.preferredPeriodLengthDays,
            lutealPhaseLengthDays = prefs.lutealPhaseLengthDays,
        )
        val intervals = estimate.validIntervals
        val days = intervals.map { it.days }
        val stats = days.takeIf { it.isNotEmpty() }?.sorted()?.let { sorted ->
            CycleLengthStats(
                count = sorted.size,
                average = (sorted.average() * 10).roundToInt() / 10.0,
                median = sorted.let { it[it.size / 2] },
                min = sorted.first(),
                max = sorted.last(),
            )
        }
        val durations = records.mapNotNull { r ->
            r.end?.let { java.time.temporal.ChronoUnit.DAYS.between(r.start, it).toInt() + 1 }
        }
        return InsightsData(
            cycleStats = stats,
            averagePeriodDays = durations.takeIf { it.isNotEmpty() }?.average()?.let {
                (it * 10).roundToInt() / 10.0
            },
            periodsCount = records.size,
            moodCounts = entries.mapNotNull { it.mood }.groupingBy { it }.eachCount(),
            symptomCounts = entries.flatMap { it.symptoms }.groupingBy { it.symptomId }.eachCount(),
            hasEnoughHistory = intervals.size >= 2,
            cycleIntervals = days,
            cycleIntervalLabels = intervals.map {
                it.endDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM", locale))
            },
        )
    }
}
