package com.zealish.dinsdiary.data.repository

import com.zealish.dinsdiary.data.local.dao.DailyMenstrualRecordDao
import com.zealish.dinsdiary.data.local.dao.MenstrualPeriodDao
import com.zealish.dinsdiary.data.mapper.newId
import com.zealish.dinsdiary.data.mapper.toDomain
import com.zealish.dinsdiary.data.mapper.toEntity
import com.zealish.dinsdiary.data.mapper.toIso
import com.zealish.dinsdiary.domain.model.DailyRecord
import com.zealish.dinsdiary.domain.repository.DailyRecordRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DailyRecordRepositoryImpl @Inject constructor(
    private val dao: DailyMenstrualRecordDao,
    private val periodDao: MenstrualPeriodDao,
) : DailyRecordRepository {

    override fun observeRange(from: LocalDate, to: LocalDate): Flow<List<DailyRecord>> =
        dao.observeRange(from.toIso(), to.toIso()).map { rows -> rows.map { it.toDomain() } }

    override suspend fun save(record: DailyRecord) {
        val existing = dao.getByDate(record.date.toIso())
        // Attach to the containing period when one exists; never rewrite it.
        val periodId = record.periodId
            ?: periodDao.containingPeriod(record.date.toIso())?.id
        dao.upsert(record.copy(periodId = periodId).toEntity(existing?.id ?: newId()))
    }

    override suspend fun deleteByDate(date: LocalDate) {
        dao.getByDate(date.toIso())?.let { dao.deleteById(it.id) }
    }
}
