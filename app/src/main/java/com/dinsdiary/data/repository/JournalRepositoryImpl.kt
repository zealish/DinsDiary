package com.dinsdiary.data.repository

import com.dinsdiary.data.local.dao.DailyHealthLogDao
import com.dinsdiary.data.local.dao.DailySymptomDao
import com.dinsdiary.data.local.dao.SymptomDao
import com.dinsdiary.data.mapper.newId
import com.dinsdiary.data.mapper.toDomain
import com.dinsdiary.data.mapper.toEntity
import com.dinsdiary.data.mapper.toIso
import com.dinsdiary.data.mapper.toJournalEntry
import com.dinsdiary.data.mapper.toLogEntity
import com.dinsdiary.data.mapper.toSymptomEntities
import com.dinsdiary.domain.model.DEFAULT_SYMPTOMS
import com.dinsdiary.domain.model.JournalEntry
import com.dinsdiary.domain.model.Symptom
import com.dinsdiary.domain.repository.JournalRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class JournalRepositoryImpl @Inject constructor(
    private val logDao: DailyHealthLogDao,
    private val symptomDao: DailySymptomDao,
    private val catalogDao: SymptomDao,
    private val clock: Clock,
) : JournalRepository {

    override fun observeDay(date: LocalDate): Flow<JournalEntry?> =
        logDao.observeByDate(date.toIso()).flatMapLatest { log ->
            if (log == null) {
                flowOf(null)
            } else {
                symptomDao.observeForLog(log.id).map { symptoms -> toJournalEntry(log, symptoms) }
            }
        }

    override fun observeRange(from: LocalDate, to: LocalDate): Flow<List<JournalEntry>> =
        logDao.observeRange(from.toIso(), to.toIso()).flatMapLatest { logs ->
            if (logs.isEmpty()) {
                flowOf(emptyList())
            } else {
                kotlinx.coroutines.flow.combine(
                    logs.map { log ->
                        symptomDao.observeForLog(log.id).map { toJournalEntry(log, it) }
                    },
                ) { it.toList() }
            }
        }

    override suspend fun save(entry: JournalEntry) {
        ensureSeeded()
        val existing = logDao.getByDate(entry.date.toIso())
        val logId = existing?.id ?: newId()
        logDao.upsert(entry.toLogEntity(logId, Instant.now(clock)))
        symptomDao.deleteForLog(logId)
        entry.toSymptomEntities(logId).forEach { symptomDao.upsert(it) }
    }

    override fun observeSymptoms(): Flow<List<Symptom>> =
        catalogDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun ensureSeeded() {
        if (catalogDao.count() == 0) {
            catalogDao.insertDefaults(DEFAULT_SYMPTOMS.map { it.toEntity() })
        }
    }
}
