package com.zealish.dinsdiary.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.zealish.dinsdiary.R

/**
 * Display labels for stored language-neutral codes (PRD §6.12).
 * Moods, system symptoms, energy levels, and sleep buckets are stored as
 * stable identifiers; only the display text is translated. Unknown or
 * user-entered values fall back to the stored text verbatim.
 * Key resources live in `strings_journal.xml` (owned by the journal slice);
 * this file only references them.
 */

/** Quick-select mood key (stored plain string) to display label. */
@Composable
fun moodLabel(mood: String): String = when (mood) {
    "Happy" -> stringResource(R.string.mood_happy)
    "Calm" -> stringResource(R.string.mood_calm)
    "Loved" -> stringResource(R.string.mood_loved)
    "Okay" -> stringResource(R.string.mood_okay)
    "Low" -> stringResource(R.string.mood_low)
    "Anxious" -> stringResource(R.string.mood_anxious)
    else -> mood
}

/** System symptom id (`sys-*`) to display label; custom symptoms keep their stored name. */
@Composable
fun systemSymptomLabel(id: String, fallback: String): String = when (id) {
    "sys-cramps" -> stringResource(R.string.symptom_cramps)
    "sys-headache" -> stringResource(R.string.symptom_headache)
    "sys-bloating" -> stringResource(R.string.symptom_bloating)
    "sys-acne" -> stringResource(R.string.symptom_acne)
    "sys-breast-tenderness" -> stringResource(R.string.symptom_breast_tenderness)
    "sys-backache" -> stringResource(R.string.symptom_backache)
    "sys-fatigue" -> stringResource(R.string.symptom_fatigue)
    else -> fallback
}
