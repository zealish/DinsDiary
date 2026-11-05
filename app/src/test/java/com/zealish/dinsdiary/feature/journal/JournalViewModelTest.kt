package com.zealish.dinsdiary.feature.journal

import com.zealish.dinsdiary.domain.model.DEFAULT_SYMPTOMS
import com.zealish.dinsdiary.domain.model.JournalEntry
import com.zealish.dinsdiary.domain.model.LoggedSymptom
import com.zealish.dinsdiary.domain.model.Symptom
import com.zealish.dinsdiary.domain.repository.JournalRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
/** Journal defaults and mappings (PRD §6.6, mockup journal screen). */
@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModelTest {

    private val today = LocalDate.parse("2026-11-04")
    private lateinit var fake: FakeJournalRepository
    private lateinit var viewModel: JournalViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fake = FakeJournalRepository()
        viewModel = JournalViewModel(
            fake,
            Clock.fixed(Instant.parse("2026-11-04T00:00:00Z"), ZoneId.of("UTC")),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun severityDefaultsToModerate() {
        assertEquals(3, viewModel.severity)
        assertEquals("moderate", viewModel.severityKey())
    }

    @Test
    fun severityFallsBackToModerateWhenEntryHasNone() {
        viewModel.prefillIfNeeded(today, entry(symptoms = listOf(LoggedSymptom("sys-cramps", null, null))))
        assertEquals("moderate", viewModel.severityKey())
    }

    @Test
    fun lateArrivingEntryPrefillsSameDate() {
        viewModel.prefillIfNeeded(today, null)
        assertNull(viewModel.mood)
        viewModel.prefillIfNeeded(today, entry(mood = "Calm"))
        assertEquals("Calm", viewModel.mood)
    }

    @Test
    fun noneChipClearsSelectedSymptoms() {
        viewModel.toggleSymptom("sys-cramps")
        viewModel.toggleSymptom("sys-fatigue")
        viewModel.clearSymptoms()
        assertTrue(viewModel.selectedSymptoms.isEmpty())
    }

    @Test
    fun saveRequiresMoodOrSymptom() = runTest {
        assertFalse(viewModel.save())
        assertEquals("need_input", viewModel.infoCode)
    }

    @Test
    fun saveMapsSleepBucketAndDefaultSeverity() = runTest {
        viewModel.toggleMood("Calm")
        viewModel.toggleSymptom("sys-cramps")
        viewModel.selectSleepBucket("h78")
        assertTrue(viewModel.save())

        val saved = fake.saved.single()
        assertEquals(today, saved.date)
        assertEquals("Calm", saved.mood)
        assertEquals(450, saved.sleepMinutes)
        assertEquals(listOf(LoggedSymptom("sys-cramps", 3, null)), saved.symptoms)
    }

    @Test
    fun prefillMapsStoredMinutesToNearestBucket() {
        viewModel.prefillIfNeeded(today, entry(sleepMinutes = 300))
        assertEquals("under6", viewModel.sleepBucket)

        viewModel.prefillIfNeeded(today.plusDays(1), entry(sleepMinutes = 480))
        assertEquals("h78", viewModel.sleepBucket)

        viewModel.prefillIfNeeded(today.plusDays(2), entry(sleepMinutes = 600))
        assertEquals("over8", viewModel.sleepBucket)
    }

    @Test
    fun prefillRestoresMoodEnergyAndNotes() {
        viewModel.prefillIfNeeded(
            today,
            entry(
                mood = "Happy",
                energyLevel = 3,
                notes = "good day",
                symptoms = listOf(LoggedSymptom("sys-fatigue", 5, null)),
            ),
        )
        assertEquals("Happy", viewModel.mood)
        assertEquals(setOf("sys-fatigue"), viewModel.selectedSymptoms)
        assertEquals("normal", viewModel.energyLabel)
        assertEquals("good day", viewModel.notes)
        assertEquals("severe", viewModel.severityKey())
    }

    @Test
    fun isCurrentDayMatchesFixedClock() {
        assertTrue(viewModel.isCurrentDay(today))
        assertFalse(viewModel.isCurrentDay(today.minusDays(1)))
    }

    private fun entry(
        mood: String? = null,
        energyLevel: Int? = null,
        sleepMinutes: Int? = null,
        notes: String? = null,
        symptoms: List<LoggedSymptom> = emptyList(),
    ) = JournalEntry(
        date = today,
        mood = mood,
        energyLevel = energyLevel,
        sleepMinutes = sleepMinutes,
        appetite = null,
        notes = notes,
        symptoms = symptoms,
    )

    private class FakeJournalRepository(var loaded: JournalEntry? = null) : JournalRepository {
        val saved = mutableListOf<JournalEntry>()
        val days = MutableStateFlow<Map<LocalDate, JournalEntry?>>(emptyMap())

        override fun observeRange(from: LocalDate, to: LocalDate): Flow<List<JournalEntry>> =
            flowOf(saved.filter { it.date in from..to })

        override fun observeDay(date: LocalDate): Flow<JournalEntry?> = flowOf(saved.firstOrNull { it.date == date })

        override suspend fun save(entry: JournalEntry) {
            saved.removeAll { it.date == entry.date }
            saved.add(entry)
        }

        override fun observeSymptoms(): Flow<List<Symptom>> = flowOf(DEFAULT_SYMPTOMS)

        override suspend fun ensureSeeded() = Unit
    }
}
