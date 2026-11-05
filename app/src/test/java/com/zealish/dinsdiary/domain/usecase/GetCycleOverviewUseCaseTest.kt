package com.zealish.dinsdiary.domain.usecase

import com.zealish.dinsdiary.domain.cycle.CyclePhase
import com.zealish.dinsdiary.domain.cycle.PeriodRecord
import com.zealish.dinsdiary.domain.cycle.PeriodStatus
import com.zealish.dinsdiary.domain.model.UserProfile
import java.time.Clock
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetCycleOverviewUseCaseTest {

    private val useCase = GetCycleOverviewUseCase(clock = Clock.systemUTC())
    private val prefs = UserProfile(
        displayName = "Dina",
        dateOfBirth = null,
        preferredCycleLengthDays = 28,
        preferredPeriodLengthDays = 5,
        lutealPhaseLengthDays = 14,
    )

    private fun record(id: String, start: String, end: String? = null) = PeriodRecord(
        id = id,
        start = LocalDate.parse(start),
        end = end?.let(LocalDate::parse),
        status = if (end == null) PeriodStatus.ACTIVE else PeriodStatus.COMPLETED,
    )

    @Test
    fun bleedingDay_reportsMenstruationAsFact() {
        val records = listOf(
            record("1", "2026-09-29", "2026-10-03"),
            record("2", "2026-10-27"), // ongoing
        )
        val overview = useCase.build(LocalDate.parse("2026-10-28"), records, prefs)

        assertTrue(overview.isBleedingToday)
        assertEquals(CyclePhase.MENSTRUATION, overview.phase)
        assertEquals(2, overview.cycleDay)
    }

    @Test
    fun follicularDay_reportsEstimatedPhase() {
        val records = listOf(record("1", "2026-10-24", "2026-10-28"))
        // Cycle day 7: after bleeding, before the estimated fertile window.
        val overview = useCase.build(LocalDate.parse("2026-10-30"), records, prefs)

        assertFalse(overview.isBleedingToday)
        assertEquals(CyclePhase.FOLLICULAR, overview.phase)
        assertEquals(7, overview.cycleDay)
        assertEquals(LocalDate.parse("2026-11-21"), overview.estimate.predictedNextStart)
    }

    @Test
    fun fertileWindowDay_reportsEstimatedWindow() {
        val records = listOf(record("1", "2026-10-24", "2026-10-28"))
        // Estimated ovulation Nov 7 -> window Nov 2..8.
        val overview = useCase.build(LocalDate.parse("2026-11-04"), records, prefs)

        assertEquals(CyclePhase.FERTILE_WINDOW, overview.phase)
    }

    @Test
    fun noHistory_reportsUnknownAndInsufficientData() {
        val overview = useCase.build(
            LocalDate.parse("2026-11-04"),
            emptyList(),
            prefs.copy(preferredCycleLengthDays = null),
        )

        assertEquals(CyclePhase.UNKNOWN, overview.phase)
        assertTrue(overview.insufficientData)
        assertNull(overview.cycleDay)
    }

    @Test
    fun completedPeriods_reportLastEndAndObservedAverage() {
        val records = listOf(
            record("1", "2026-09-01", "2026-09-05"), // 5 days
            record("2", "2026-09-29", "2026-10-02"), // 4 days
            record("3", "2026-10-27", "2026-10-31"), // 5 days
        )
        val overview = useCase.build(LocalDate.parse("2026-11-04"), records, prefs)

        assertEquals(LocalDate.parse("2026-10-31"), overview.lastPeriodEnd)
        assertEquals(5, overview.averagePeriodLengthDays) // (5+4+5)/3 rounded
        assertTrue(overview.periodAverageObserved)
    }

    @Test
    fun noCompletedPeriods_fallsBackToPreference() {
        val overview = useCase.build(
            LocalDate.parse("2026-11-04"),
            listOf(record("1", "2026-11-03")), // ongoing, no end yet
            prefs,
        )

        assertNull(overview.lastPeriodEnd)
        assertEquals(5, overview.averagePeriodLengthDays)
        assertFalse(overview.periodAverageObserved)
    }
}
