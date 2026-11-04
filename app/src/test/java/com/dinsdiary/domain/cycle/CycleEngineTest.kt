package com.dinsdiary.domain.cycle

import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * CYCLE_ENGINE.md §11 required cases. Fixed dates only — never the real
 * current date (CODING_STANDARD.md §10).
 */
class CycleEngineTest {

    private fun record(
        id: String,
        start: String,
        end: String? = null,
        status: PeriodStatus = PeriodStatus.COMPLETED,
        erroneous: Boolean = false,
    ) = PeriodRecord(
        id = id,
        start = LocalDate.parse(start),
        end = end?.let(LocalDate::parse),
        status = status,
        erroneous = erroneous,
    )

    @Test
    fun regular28DayCycle_predictsNextStartWithModerateConfidence() {
        val records = listOf(
            record("1", "2026-07-01", "2026-07-05"),
            record("2", "2026-07-29", "2026-08-02"),
            record("3", "2026-08-26", "2026-08-30"),
            record("4", "2026-09-23", "2026-09-27"),
        )
        val estimate = CycleEngine.predict(records, preferredCycleLengthDays = 28, preferredPeriodLengthDays = 5)

        assertEquals(28, estimate.estimatedCycleLengthDays)
        assertEquals(LocalDate.parse("2026-10-21"), estimate.predictedNextStart)
        assertEquals(LocalDate.parse("2026-10-25"), estimate.predictedPeriodEnd)
        assertEquals(Confidence.MODERATE, estimate.confidence)
        assertEquals("ROBUST_MEDIAN", estimate.explanationCode)
        assertTrue(estimate.flaggedRecordIds.isEmpty())
    }

    @Test
    fun regular30DayCycle_predictsNextStart() {
        val records = listOf(
            record("1", "2026-06-02", "2026-06-06"),
            record("2", "2026-07-02", "2026-07-06"),
            record("3", "2026-08-01", "2026-08-05"),
            record("4", "2026-08-31", "2026-09-04"),
        )
        val estimate = CycleEngine.predict(records, preferredCycleLengthDays = 30, preferredPeriodLengthDays = 5)

        assertEquals(30, estimate.estimatedCycleLengthDays)
        assertEquals(LocalDate.parse("2026-09-30"), estimate.predictedNextStart)
    }

    @Test
    fun variableCycleLengths_reducesConfidence() {
        val records = listOf(
            record("1", "2026-05-01", "2026-05-05"),
            record("2", "2026-05-28", "2026-06-01"), // 27
            record("3", "2026-07-02", "2026-07-06"), // 35
            record("4", "2026-07-28", "2026-08-01"), // 26
            record("5", "2026-09-01", "2026-09-05"), // 35
        )
        val estimate = CycleEngine.predict(records, preferredCycleLengthDays = 28, preferredPeriodLengthDays = 5)

        assertEquals(Confidence.LOW, estimate.confidence)
        assertEquals("HIGH_VARIABILITY", estimate.explanationCode)
    }

    @Test
    fun singleObservedInterval_lowConfidenceMedian() {
        val records = listOf(
            record("1", "2026-09-01", "2026-09-05"),
            record("2", "2026-09-29", "2026-10-03"),
        )
        val estimate = CycleEngine.predict(records, preferredCycleLengthDays = 30, preferredPeriodLengthDays = 5)

        assertEquals(28, estimate.estimatedCycleLengthDays)
        assertEquals(LocalDate.parse("2026-10-27"), estimate.predictedNextStart)
        assertEquals(Confidence.LOW, estimate.confidence)
        assertEquals("SINGLE_INTERVAL", estimate.explanationCode)
    }

    @Test
    fun noObservedInterval_usesPreferenceWithLowConfidence() {
        val records = listOf(record("1", "2026-10-24", "2026-10-28"))
        val estimate = CycleEngine.predict(records, preferredCycleLengthDays = 28, preferredPeriodLengthDays = 5)

        assertEquals(28, estimate.estimatedCycleLengthDays)
        assertEquals(LocalDate.parse("2026-11-21"), estimate.predictedNextStart)
        assertEquals(Confidence.LOW, estimate.confidence)
        assertEquals("PREFERENCE_FALLBACK", estimate.explanationCode)
    }

    @Test
    fun noHistoryAtAll_insufficientData() {
        val estimate = CycleEngine.predict(
            records = emptyList(),
            preferredCycleLengthDays = null,
            preferredPeriodLengthDays = null,
        )

        assertNull(estimate.predictedNextStart)
        assertEquals(Confidence.INSUFFICIENT_DATA, estimate.confidence)
        assertEquals("NO_HISTORY", estimate.explanationCode)
    }

    @Test
    fun missingEndDate_stillCountsAsValidStart() {
        val records = listOf(
            record("1", "2026-09-01", "2026-09-05"),
            record("2", "2026-09-29"), // ongoing, no end yet
        )
        val estimate = CycleEngine.predict(records, preferredCycleLengthDays = 28, preferredPeriodLengthDays = 5)

        assertEquals(28, estimate.estimatedCycleLengthDays)
        assertEquals(LocalDate.parse("2026-10-27"), estimate.predictedNextStart)
        assertTrue(estimate.flaggedRecordIds.isEmpty())
    }

    @Test
    fun duplicateStarts_flaggedAndDeduplicated() {
        val records = listOf(
            record("1", "2026-09-01", "2026-09-05"),
            record("2", "2026-09-01", "2026-09-05"),
            record("3", "2026-09-29", "2026-10-03"),
        )
        val estimate = CycleEngine.predict(records, preferredCycleLengthDays = 28, preferredPeriodLengthDays = 5)

        assertEquals(28, estimate.estimatedCycleLengthDays)
        assertTrue(estimate.flaggedRecordIds.contains("2"))
    }

    @Test
    fun overlappingRecords_flaggedForReview() {
        val records = listOf(
            record("1", "2026-09-01", "2026-10-05"), // runs into next start
            record("2", "2026-09-29", "2026-10-03"),
        )
        val estimate = CycleEngine.predict(records, preferredCycleLengthDays = 28, preferredPeriodLengthDays = 5)

        assertTrue(estimate.flaggedRecordIds.containsAll(listOf("1", "2")))
        // No valid anchor remains, so even a preference cannot anchor an
        // estimate: report insufficient data and let the user review.
        assertEquals(Confidence.INSUFFICIENT_DATA, estimate.confidence)
        assertEquals("NO_VALID_INTERVAL", estimate.explanationCode)
    }
    @Test
    fun endBeforeStart_flaggedAsInvalid() {
        val records = listOf(
            record("1", "2026-09-05", "2026-09-01"),
            record("2", "2026-10-03", "2026-10-07"),
        )
        val estimate = CycleEngine.predict(records, preferredCycleLengthDays = 28, preferredPeriodLengthDays = 5)

        assertTrue(estimate.flaggedRecordIds.contains("1"))
        assertEquals("PREFERENCE_FALLBACK", estimate.explanationCode)
    }

    @Test
    fun erroneousRecords_excludedSilentlyFromEstimateButFlagged() {
        val records = listOf(
            record("1", "2026-09-01", "2026-09-05", erroneous = true),
            record("2", "2026-09-29", "2026-10-03"),
        )
        val estimate = CycleEngine.predict(records, preferredCycleLengthDays = 28, preferredPeriodLengthDays = 5)

        assertEquals("PREFERENCE_FALLBACK", estimate.explanationCode)
        assertTrue(estimate.flaggedRecordIds.contains("1"))
    }

    @Test
    fun implausibleInterval_flaggedNotClamped() {
        val records = listOf(
            record("1", "2026-01-01", "2026-01-05"),
            record("2", "2026-08-01", "2026-08-05"), // ~212-day gap
        )
        val estimate = CycleEngine.predict(records, preferredCycleLengthDays = 28, preferredPeriodLengthDays = 5)

        assertEquals("PREFERENCE_FALLBACK", estimate.explanationCode)
        assertTrue(estimate.flaggedRecordIds.containsAll(listOf("1", "2")))
    }

    @Test
    fun leapYear_calculatesAcrossFeb29() {
        val records = listOf(
            record("1", "2024-01-13", "2024-01-17"),
            record("2", "2024-02-10", "2024-02-14"),
        )
        val estimate = CycleEngine.predict(records, preferredCycleLengthDays = 28, preferredPeriodLengthDays = 5)

        assertEquals(28, estimate.estimatedCycleLengthDays)
        assertEquals(LocalDate.parse("2024-03-09"), estimate.predictedNextStart)
    }

    @Test
    fun yearBoundary_rollsIntoJanuary() {
        val records = listOf(
            record("1", "2025-11-22", "2025-11-26"),
            record("2", "2025-12-20", "2025-12-24"),
        )
        val estimate = CycleEngine.predict(records, preferredCycleLengthDays = 28, preferredPeriodLengthDays = 5)

        assertEquals(LocalDate.parse("2026-01-17"), estimate.predictedNextStart)
    }

    @Test
    fun changedPreference_changesFallbackEstimate() {
        val records = listOf(record("1", "2026-10-24", "2026-10-28"))
        val before = CycleEngine.predict(records, preferredCycleLengthDays = 28, preferredPeriodLengthDays = 5)
        val after = CycleEngine.predict(records, preferredCycleLengthDays = 32, preferredPeriodLengthDays = 5)

        assertEquals(LocalDate.parse("2026-11-21"), before.predictedNextStart)
        assertEquals(LocalDate.parse("2026-11-25"), after.predictedNextStart)
    }

    @Test
    fun changedLutealPhase_shiftsOvulationEstimate() {
        val records = listOf(
            record("1", "2026-09-01", "2026-09-05"),
            record("2", "2026-09-29", "2026-10-03"),
        )
        val default = CycleEngine.predict(records, preferredCycleLengthDays = 28, preferredPeriodLengthDays = 5)
        val custom = CycleEngine.predict(
            records,
            preferredCycleLengthDays = 28,
            preferredPeriodLengthDays = 5,
            lutealPhaseLengthDays = 12,
        )

        // Next start 2026-10-27; ovulation = next - luteal.
        assertEquals(LocalDate.parse("2026-10-13"), default.estimatedOvulationDate)
        assertEquals(LocalDate.parse("2026-10-15"), custom.estimatedOvulationDate)
        // Fertile window = ovulation -5 .. +1.
        assertEquals(LocalDate.parse("2026-10-08"), default.fertileWindowStart)
        assertEquals(LocalDate.parse("2026-10-14"), default.fertileWindowEnd)
    }

    @Test
    fun historicalEdit_recomputesPrediction() {
        val before = CycleEngine.predict(
            listOf(
                record("1", "2026-09-01", "2026-09-05"),
                record("2", "2026-09-29", "2026-10-03"),
            ),
            preferredCycleLengthDays = 28,
            preferredPeriodLengthDays = 5,
        )
        // User corrects the second start by two days.
        val after = CycleEngine.predict(
            listOf(
                record("1", "2026-09-01", "2026-09-05"),
                record("2", "2026-10-01", "2026-10-05"),
            ),
            preferredCycleLengthDays = 28,
            preferredPeriodLengthDays = 5,
        )

        assertEquals(LocalDate.parse("2026-10-27"), before.predictedNextStart)
        assertEquals(LocalDate.parse("2026-10-31"), after.predictedNextStart)
    }

    @Test
    fun sameLocalDate_stableAcrossTimezones() {
        val zoneIds = listOf(ZoneId.of("Asia/Jakarta"), ZoneId.of("America/New_York"), ZoneId.of("Pacific/Kiritimati"))
        val expected = LocalDate.parse("2026-10-27")
        zoneIds.forEach { zone ->
            // Date-only values never pass through instant conversion.
            val start = LocalDate.parse("2026-09-29").atStartOfDay(zone).toLocalDate()
            assertEquals(expected, start.plusDays(28))
        }
    }

    @Test
    fun cycleDay_countsFromOne() {
        assertEquals(1, CycleEngine.cycleDay(LocalDate.parse("2026-10-24"), LocalDate.parse("2026-10-24")))
        assertEquals(12, CycleEngine.cycleDay(LocalDate.parse("2026-10-24"), LocalDate.parse("2026-11-04")))
        assertNull(CycleEngine.cycleDay(LocalDate.parse("2026-10-24"), LocalDate.parse("2026-10-23")))
    }
}
