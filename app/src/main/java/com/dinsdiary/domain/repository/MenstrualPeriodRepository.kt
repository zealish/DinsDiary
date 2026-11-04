package com.dinsdiary.domain.repository

import com.dinsdiary.domain.cycle.PeriodRecord
import kotlinx.coroutines.flow.Flow

/**
 * Persisted period history. Destructive calls require explicit UI
 * confirmation (PRD §6.3, §6.10); this layer only executes.
 */
interface MenstrualPeriodRepository {
    fun observePeriods(): Flow<List<PeriodRecord>>

    /** @throws InvalidPeriodException when end precedes start. */
    suspend fun savePeriod(record: PeriodRecord, notes: String?)

    suspend fun deletePeriod(id: String)

    suspend fun deleteAllPeriods()
}

class InvalidPeriodException(message: String) : IllegalArgumentException(message)
