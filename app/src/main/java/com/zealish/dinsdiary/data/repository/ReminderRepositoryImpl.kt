package com.zealish.dinsdiary.data.repository

import com.zealish.dinsdiary.data.local.dao.ReminderDao
import com.zealish.dinsdiary.data.mapper.toDomain
import com.zealish.dinsdiary.data.mapper.toEntity
import com.zealish.dinsdiary.domain.model.Reminder
import com.zealish.dinsdiary.domain.repository.ReminderRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepositoryImpl @Inject constructor(
    private val dao: ReminderDao,
) : ReminderRepository {

    override fun observeAll(): Flow<List<Reminder>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun save(reminder: Reminder) {
        dao.upsert(reminder.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }
}
