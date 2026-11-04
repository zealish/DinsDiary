package com.dinsdiary.domain.repository

import com.dinsdiary.domain.model.JournalEntry
import com.dinsdiary.domain.model.Symptom
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface JournalRepository {

    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<JournalEntry>>
    fun observeDay(date: LocalDate): Flow<JournalEntry?>

    suspend fun save(entry: JournalEntry)

    fun observeSymptoms(): Flow<List<Symptom>>

    /** Inserts the system-default catalog once (IGNORE on conflict). */
    suspend fun ensureSeeded()
}
