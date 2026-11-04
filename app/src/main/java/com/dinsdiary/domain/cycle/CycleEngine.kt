package com.dinsdiary.domain.cycle

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Pure-Kotlin deterministic cycle prediction (PRD §6.5, §6.7, §7).
 *
 * No Android dependencies. Same input always yields same output.
 * Uses [LocalDate] only — never UTC timestamps (ARCHITECTURE.md §8).
 */
object CycleEngine {
    const val ALGORITHM_VERSION = "1.0"

    const val DEFAULT_LUTEAL_PHASE_DAYS = 14

    /** Intervals outside this range are flagged for review, never silently clamped. */
    const val MIN_PLAUSIBLE_INTERVAL_DAYS = 15
    const val MAX_PLAUSIBLE_INTERVAL_DAYS = 90

    /** Recent window used for the robust estimate. */
    const val RECENT_INTERVAL_COUNT = 6

    fun predict(
        records: List<PeriodRecord>,
        preferredCycleLengthDays: Int?,
        preferredPeriodLengthDays: Int?,
        lutealPhaseLengthDays: Int = DEFAULT_LUTEAL_PHASE_DAYS,
    ): CycleEstimate {
        val flagged = mutableSetOf<String>()

        // 1. Drop explicitly erroneous / under-review records.
        val candidates = records.filter {
            val keep = !it.erroneous && it.status != PeriodStatus.NEEDS_REVIEW
            if (!keep) flagged += it.id
            keep
        }

        // 2. Invalid sequences (end before start) are flagged, not rewritten.
        val dated = candidates.filter {
            val valid = it.end == null || !it.end.isBefore(it.start)
            if (!valid) flagged += it.id
            valid
        }

        // 3. Duplicate starts: keep first occurrence, flag the rest.
        val seenStarts = mutableSetOf<LocalDate>()
        val deduped = dated.sortedBy { it.start }.filter {
            if (!seenStarts.add(it.start)) {
                flagged += it.id
                false
            } else {
                true
            }
        }

        // 4. Overlapping records: a completed period reaching into the next
        // start flags both sides for user review; neither contributes intervals.
        val overlapping = mutableSetOf<String>()
        deduped.forEachIndexed { index, record ->
            val end = record.end ?: return@forEachIndexed
            val next = deduped.getOrNull(index + 1) ?: return@forEachIndexed
            if (!next.start.isAfter(end)) {
                overlapping += record.id
                overlapping += next.id
            }
        }
        flagged += overlapping
        val valid = deduped.filter { it.id !in overlapping }

        // 5. Intervals between consecutive valid starts (exclusive difference:
        // start day 1 -> next start day 29 == 28-day cycle).
        val intervals = valid.zipWithNext { a, b ->
            Triple(ChronoUnit.DAYS.between(a.start, b.start).toInt(), a.id to b.id, b.start)
        }
        val plausible = intervals.filter { (days, pair, _) ->
            val ok = days in MIN_PLAUSIBLE_INTERVAL_DAYS..MAX_PLAUSIBLE_INTERVAL_DAYS
            if (!ok) {
                flagged += pair.first
                flagged += pair.second
            }
            ok
        }

        val recent = plausible.takeLast(RECENT_INTERVAL_COUNT)
        val recentDays = recent.map { it.first }
        val latestStart = valid.maxByOrNull { it.start }?.start

        if (recent.isEmpty() || latestStart == null) {
            // No observed interval: provisional preference fallback, or nothing.
            if (preferredCycleLengthDays != null && latestStart != null) {
                return buildEstimate(
                    cycleLength = preferredCycleLengthDays,
                    latestStart = latestStart,
                    confidence = Confidence.LOW,
                    explanationCode = "PREFERENCE_FALLBACK",
                    validIntervals = emptyList(),
                    flagged = flagged,
                    preferredPeriodLengthDays = preferredPeriodLengthDays,
                    lutealPhaseLengthDays = lutealPhaseLengthDays,
                )
            }
            return CycleEstimate(
                estimatedCycleLengthDays = null,
                predictedNextStart = null,
                predictedPeriodEnd = null,
                estimatedOvulationDate = null,
                fertileWindowStart = null,
                fertileWindowEnd = null,
                confidence = Confidence.INSUFFICIENT_DATA,
                explanationCode = if (records.isEmpty()) "NO_HISTORY" else "NO_VALID_INTERVAL",
                validIntervals = emptyList(),
                flaggedRecordIds = flagged.toList(),
            )
        }

        val median = median(recentDays)
        val spread = recentDays.max() - recentDays.min()
        val confidence = when {
            recentDays.size <= 2 -> Confidence.LOW
            recentDays.size >= 4 && spread <= 4 -> Confidence.HIGHER
            spread <= 7 -> Confidence.MODERATE
            else -> Confidence.LOW
        }
        val explanationCode = when {
            recentDays.size == 1 -> "SINGLE_INTERVAL"
            confidence == Confidence.LOW -> "HIGH_VARIABILITY"
            else -> "ROBUST_MEDIAN"
        }

        return buildEstimate(
            cycleLength = median,
            latestStart = latestStart,
            confidence = confidence,
            explanationCode = explanationCode,
            validIntervals = recent.map { (days, _, endDate) -> ValidInterval(days, endDate) },
            flagged = flagged,
            preferredPeriodLengthDays = preferredPeriodLengthDays,
            lutealPhaseLengthDays = lutealPhaseLengthDays,
        )
    }

    /**
     * 1-based day within the current cycle, or null when [today] precedes
     * the latest recorded start. Pure date math — stable across timezones.
     */
    fun cycleDay(latestStart: LocalDate, today: LocalDate): Int? {
        if (today.isBefore(latestStart)) return null
        return ChronoUnit.DAYS.between(latestStart, today).toInt() + 1
    }

    private fun buildEstimate(
        cycleLength: Int,
        latestStart: LocalDate,
        confidence: Confidence,
        explanationCode: String,
        validIntervals: List<ValidInterval>,
        flagged: Set<String>,
        preferredPeriodLengthDays: Int?,
        lutealPhaseLengthDays: Int,
    ): CycleEstimate {
        val nextStart = latestStart.plusDays(cycleLength.toLong())
        val periodEnd = preferredPeriodLengthDays?.let { nextStart.plusDays(it.toLong() - 1) }
        val ovulation = nextStart.minusDays(lutealPhaseLengthDays.toLong())
        return CycleEstimate(
            estimatedCycleLengthDays = cycleLength,
            predictedNextStart = nextStart,
            predictedPeriodEnd = periodEnd,
            estimatedOvulationDate = ovulation,
            fertileWindowStart = ovulation.minusDays(5),
            fertileWindowEnd = ovulation.plusDays(1),
            confidence = confidence,
            explanationCode = explanationCode,
            validIntervals = validIntervals,
            flaggedRecordIds = flagged.toList(),
        )
    }

    private fun median(values: List<Int>): Int {
        val sorted = values.sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 1) {
            sorted[mid]
        } else {
            (sorted[mid - 1] + sorted[mid]) / 2
        }
    }
}
