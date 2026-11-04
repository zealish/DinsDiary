package com.dinsdiary.domain.repository

import com.dinsdiary.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun observeAll(): Flow<List<Reminder>>

    suspend fun save(reminder: Reminder)

    suspend fun delete(id: String)

    suspend fun deleteAll()
}
