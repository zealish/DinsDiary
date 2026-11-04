package com.dinsdiary.domain.usecase

import com.dinsdiary.domain.cycle.PeriodRecord
import com.dinsdiary.domain.cycle.PeriodStatus
import com.dinsdiary.domain.model.JournalEntry
import com.dinsdiary.domain.model.LoggedSymptom
import com.dinsdiary.domain.model.UserProfile
import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GetInsightsUseCaseTest {

    private val useCase = GetInsightsUseCase()
    private val prefs = UserProfile(null, null, 28, 5, 14)

    private fun record(id: String, start: String, end: String) = PeriodRecord(
        id = id,
        start = LocalDate.parse(start),
        end = LocalDate.parse(end),
        status = PeriodStatus.COMPLETED,
    )

    private fun entry(date: String, mood: String?, symptoms: List<String> = emptyList()) = JournalEntry(
        date = LocalDate.parse(date),
        mood = mood,
        energyLevel = null,
        sleepMinutes = null,
        appetite = null,
        notes = null,
        symptoms = symptoms.map { LoggedSymptom(it, 2, null) },
    )

    @Test
    fun summarizesObservedHistory() {
        val data = useCase.build(
            records = listOf(
                record("1", "2026-07-01", "2026-07-05"),
                record("2", "2026-07-29", "2026-08-02"),
                record("3", "2026-08-26", "2026-08-30"),
                record("4", "2026-09-23", "2026-09-27"),
            ),
            entries = listOf(
                entry("2026-09-24", "Calm", listOf("sys-cramps")),
                entry("2026-09-25", "Calm", listOf("sys-cramps", "sys-fatigue")),
                entry("2026-09-26", "Low"),
            ),
            prefs = prefs,
        )

        assertEquals(3, data.cycleStats?.count)
        assertEquals(28.0, data.cycleStats?.average)
        assertEquals(5.0, data.averagePeriodDays)
        assertEquals(4, data.periodsCount)
        assertEquals(2, data.moodCounts["Calm"])
        assertEquals(listOf(28, 28, 28), data.cycleIntervals)
        assertEquals(listOf("Jul", "Aug", "Sep"), data.cycleIntervalLabels)
    }

    @Test
    fun indonesianLocale_monthLabels() {
        val data = useCase.build(
            records = listOf(
                record("1", "2026-07-01", "2026-07-05"),
                record("2", "2026-07-29", "2026-08-02"),
                record("3", "2026-08-26", "2026-08-30"),
                record("4", "2026-09-23", "2026-09-27"),
            ),
            entries = emptyList(),
            prefs = prefs,
            locale = Locale("id"),
        )

        assertEquals(listOf("Jul", "Agu", "Sep"), data.cycleIntervalLabels)
    }

    @Test
    fun emptyHistory_flagsInsufficientData() {
        val data = useCase.build(emptyList(), emptyList(), prefs)

        assertEquals(null, data.cycleStats)
        assertEquals(null, data.averagePeriodDays)
        assertFalse(data.hasEnoughHistory)
        assertTrue(data.moodCounts.isEmpty())
    }
}
