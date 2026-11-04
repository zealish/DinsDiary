package com.dinsdiary.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.dinsdiary.data.local.entity.DailyHealthLogEntity
import com.dinsdiary.data.local.entity.DailySymptomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyHealthLogDao {
    @Query("SELECT * FROM daily_health_log WHERE date = :date")
    fun observeByDate(date: String): Flow<DailyHealthLogEntity?>
    @Query("SELECT * FROM daily_health_log WHERE date BETWEEN :fromDate AND :toDate ORDER BY date ASC")
    fun observeRange(fromDate: String, toDate: String): Flow<List<DailyHealthLogEntity>>

    @Query("SELECT * FROM daily_health_log WHERE date = :date")
    suspend fun getByDate(date: String): DailyHealthLogEntity?

    @Upsert
    suspend fun upsert(log: DailyHealthLogEntity)

    @Query("DELETE FROM daily_health_log WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM daily_health_log")
    suspend fun deleteAll()
}

@Dao
interface DailySymptomDao {
    @Query("SELECT * FROM daily_symptom WHERE healthLogId = :healthLogId")
    fun observeForLog(healthLogId: String): Flow<List<DailySymptomEntity>>

    @Upsert
    suspend fun upsert(symptom: DailySymptomEntity)

    @Query("DELETE FROM daily_symptom WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM daily_symptom")
    suspend fun deleteAll()

    @Transaction
    @Query("DELETE FROM daily_symptom WHERE healthLogId = :healthLogId")
    suspend fun deleteForLog(healthLogId: String)
}
