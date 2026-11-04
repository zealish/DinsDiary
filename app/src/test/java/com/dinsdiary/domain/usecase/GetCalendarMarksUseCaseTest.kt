package com.dinsdiary.domain.usecase

import com.dinsdiary.domain.cycle.PeriodRecord
import com.dinsdiary.domain.cycle.PeriodStatus
import com.dinsdiary.domain.model.UserProfile
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetCalendarMarksUseCaseTest {

    private val useCase = GetCalendarMarksUseCase()
    private val prefs = UserProfile(
        displayName = null,
        dateOfBirth = null,
        preferredCycleLengthDays = 28,
        preferredPeriodLengthDays = 5,
        lutealPhaseLengthDays = 14,
    )

    private fun record(id: String, start: String, end: String? = null) = PeriodRecord(
        id = id,
        start = LocalDate.parse(start),
        end = end?.let(LocalDate::parse),
        status = PeriodStatus.COMPLETED,
    )

    @Test
    fun bleedingDates_expandRecordRanges() {
        val marks = useCase.build(
            listOf(record("1", "2026-10-24", "2026-10-28")),
            prefs,
        )

        assertEquals(
            setOf("24", "25", "26", "27", "28").map { LocalDate.parse("2026-10-$it") }.toSet(),
            marks.bleedingDates,
        )
    }

    @Test
    fun predictedAndFertileDates_deriveFromEstimate() {
        val marks = useCase.build(
            listOf(record("1", "2026-10-24", "2026-10-28")),
            prefs,
        )

        // Fallback estimate: next 2026-11-21 + 5 days; ovulation 11-07.
        assertEquals(LocalDate.parse("2026-11-07"), marks.ovulationDate)
        assertTrue(marks.predictedDates.contains(LocalDate.parse("2026-11-21")))
        assertTrue(marks.predictedDates.contains(LocalDate.parse("2026-11-25")))
        assertTrue(marks.fertileDates.contains(LocalDate.parse("2026-11-04")))
        // Sets never overlap.
        assertTrue(marks.predictedDates.intersect(marks.bleedingDates).isEmpty())
        assertTrue(marks.fertileDates.intersect(marks.bleedingDates).isEmpty())
        assertTrue(marks.fertileDates.intersect(marks.predictedDates).isEmpty())
    }

    @Test
    fun noData_emptyMarks() {
        val marks = useCase.build(emptyList(), prefs.copy(preferredCycleLengthDays = null))

        assertTrue(marks.bleedingDates.isEmpty())
        assertTrue(marks.predictedDates.isEmpty())
        assertTrue(marks.fertileDates.isEmpty())
    }
}
