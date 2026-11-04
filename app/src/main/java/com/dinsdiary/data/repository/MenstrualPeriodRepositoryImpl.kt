package com.dinsdiary.data.repository

import com.dinsdiary.data.local.dao.MenstrualPeriodDao
import com.dinsdiary.data.mapper.toDomain
import com.dinsdiary.data.mapper.toEntity
import com.dinsdiary.domain.cycle.PeriodRecord
import com.dinsdiary.domain.repository.InvalidPeriodException
import com.dinsdiary.domain.repository.MenstrualPeriodRepository
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MenstrualPeriodRepositoryImpl @Inject constructor(
    private val dao: MenstrualPeriodDao,
    private val clock: Clock,
) : MenstrualPeriodRepository {

    override fun observePeriods(): Flow<List<PeriodRecord>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun savePeriod(record: PeriodRecord, notes: String?) {
        if (record.end != null && record.end.isBefore(record.start)) {
            // Generic message: never echo health data into logs (PRD §6.10).
            throw InvalidPeriodException("Period end date precedes start date")
        }
        dao.upsert(record.toEntity(notes, Instant.now(clock)))
    }

    override suspend fun deletePeriod(id: String) {
        dao.deleteById(id)
    }

    override suspend fun deleteAllPeriods() {
        dao.deleteAll()
    }
}
