package com.zealish.dinsdiary.domain.repository

import com.zealish.dinsdiary.domain.model.DailyRecord
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface DailyRecordRepository {
    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<DailyRecord>>

    suspend fun save(record: DailyRecord)

    suspend fun deleteByDate(date: LocalDate)
}
