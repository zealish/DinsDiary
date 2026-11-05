package com.zealish.dinsdiary.feature.journal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zealish.dinsdiary.domain.model.JournalEntry
import com.zealish.dinsdiary.domain.model.LoggedSymptom
import com.zealish.dinsdiary.domain.model.MOOD_OPTIONS
import com.zealish.dinsdiary.domain.model.Symptom
import com.zealish.dinsdiary.domain.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/**
 * Daily check-in state (PRD §6.6). Quick chips, multi-symptom, optional notes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repository: JournalRepository,
    clock: Clock,
) : ViewModel() {

    private val today = LocalDate.now(clock)
    private val dateFlow = MutableStateFlow(today)
    val date: StateFlow<LocalDate> = dateFlow

    val entry: StateFlow<JournalEntry?> = dateFlow.flatMapLatest { repository.observeDay(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val catalog: StateFlow<List<Symptom>> = repository.observeSymptoms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var mood by mutableStateOf<String?>(null)
        private set
    var selectedSymptoms by mutableStateOf(setOf<String>())
        private set
    /** Mockup default severity (moderate active). Applies to every logged symptom. */
    var severity by mutableStateOf<Int?>(SEVERITY_LEVELS["moderate"])
        private set
    /** Stored energy key (low/normal/high); the screen resolves display text. */
    var energyLabel by mutableStateOf<String?>(null)
        private set
    /** Mockup sleep bucket key; mapped to minutes only on save. */
    var sleepBucket by mutableStateOf<String?>(null)
        private set
    var notes by mutableStateOf("")
        private set
    /** Info banner code (need_input/saved); the screen resolves display text. */
    var infoCode by mutableStateOf<String?>(null)
        private set
    private var prefilledFor: LocalDate? = null
    private var prefilledEntry: JournalEntry? = null

    init {
        viewModelScope.launch { repository.ensureSeeded() }
    }

    fun changeDate(date: LocalDate) {
        dateFlow.value = date
    }

    fun isCurrentDay(day: LocalDate): Boolean = day == today

    fun prefillIfNeeded(forDate: LocalDate, loaded: JournalEntry?) {
        if (prefilledFor == forDate && prefilledEntry == loaded) return
        prefilledFor = forDate
        prefilledEntry = loaded
        mood = loaded?.mood
        selectedSymptoms = loaded?.symptoms?.map { it.symptomId }?.toSet() ?: emptySet()
        severity = loaded?.symptoms?.firstOrNull()?.severity ?: SEVERITY_LEVELS["moderate"]
        energyLabel = loaded?.energyLevel?.let { ENERGY_LABELS[it] }
        sleepBucket = loaded?.sleepMinutes?.let(::bucketForMinutes)
        notes = loaded?.notes ?: ""
        infoCode = null
    }

    fun toggleMood(value: String) {
        mood = value.takeIf { mood != value }
    }

    fun toggleSymptom(id: String) {
        selectedSymptoms = if (id in selectedSymptoms) selectedSymptoms - id else selectedSymptoms + id
    }

    fun setSeverity(key: String) {
        severity = SEVERITY_LEVELS[key]
    }

    fun severityKey(): String? = SEVERITY_LEVELS.entries.firstOrNull { it.value == severity }?.key

    fun setEnergy(key: String) {
        energyLabel = key.takeIf { energyLabel != key }
    }

    fun selectSleepBucket(key: String) {
        sleepBucket = key.takeIf { sleepBucket != key }
    }

    fun updateNotes(value: String) {
        notes = value
    }

    /** Mockup "none" chip: explicitly records no symptoms for the day. */
    fun clearSymptoms() {
        selectedSymptoms = emptySet()
    }

    suspend fun save(): Boolean {
        if (mood == null && selectedSymptoms.isEmpty()) {
            infoCode = "need_input"
            return false
        }
        val minutes = sleepBucket?.let { SLEEP_BUCKETS[it] }
        val snapshot = JournalEntry(
            date = dateFlow.value,
            mood = mood,
            energyLevel = energyLabel?.let { ENERGY_LEVELS[it] },
            sleepMinutes = minutes,
            appetite = null,
            notes = notes.ifBlank { null },
            symptoms = selectedSymptoms.map { LoggedSymptom(it, severity, null) },
        )
        repository.save(snapshot)
        prefilledFor = snapshot.date
        prefilledEntry = snapshot
        infoCode = "saved"
        return true
    }

    companion object {
        /** Stored severity keys to levels; display text resolves in the screen. */
        val SEVERITY_LEVELS = mapOf("mild" to 1, "moderate" to 3, "severe" to 5)
        /** Mockup sleep select: bucket key to representative minutes. */
        val SLEEP_BUCKETS = mapOf("under6" to 300, "h78" to 450, "over8" to 540)
        val SLEEP_BUCKET_LABELS = SLEEP_BUCKETS.keys.toList()
        /** Stored energy keys to levels; display text resolves in the screen. */
        val ENERGY_LEVELS = mapOf("low" to 1, "normal" to 3, "high" to 5)
        val ENERGY_LABELS = ENERGY_LEVELS.entries.associate { (k, v) -> v to k }
        val MOODS = MOOD_OPTIONS
}
/** Nearest mockup sleep bucket key for a stored minute value (prefill only). */
private fun bucketForMinutes(minutes: Int): String = when {
    minutes < 360 -> "under6"
    minutes <= 480 -> "h78"
    else -> "over8"
}

}
