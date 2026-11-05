package com.zealish.dinsdiary.domain.model

import java.time.LocalDate

data class Symptom(
    val id: String,
    val nameKey: String,
    val category: String,
)

data class LoggedSymptom(
    val symptomId: String,
    val severity: Int?,
    val notes: String?,
)

/** One day of journaling (PRD §6.6). Symptoms stored separately by design. */
data class JournalEntry(
    val date: LocalDate,
    val mood: String?,
    val energyLevel: Int?,
    val sleepMinutes: Int?,
    val appetite: String?,
    val notes: String?,
    val symptoms: List<LoggedSymptom>,
)

/** System-default catalog seeded locally (mockup journal symptom set). */
val DEFAULT_SYMPTOMS = listOf(
    Symptom("sys-cramps", "Cramps", "MENSTRUAL"),
    Symptom("sys-headache", "Headache", "PHYSICAL"),
    Symptom("sys-bloating", "Bloating", "PHYSICAL"),
    Symptom("sys-acne", "Acne", "PHYSICAL"),
    Symptom("sys-breast-tenderness", "Breast tenderness", "PHYSICAL"),
    Symptom("sys-backache", "Backache", "PHYSICAL"),
    Symptom("sys-fatigue", "Fatigue", "PHYSICAL"),
)

/** Quick-select moods (PRD §6.6, mockup). Stored as plain strings. */
val MOOD_OPTIONS = listOf("Happy", "Calm", "Loved", "Okay", "Low", "Anxious")

/** Display emoji for quick-select moods (mockup journal/insight chips). */
val MOOD_EMOJI = mapOf(
    "Happy" to "😊",
    "Calm" to "😌",
    "Loved" to "🥰",
    "Okay" to "😐",
    "Low" to "😔",
    "Anxious" to "😣",
)
