package com.dinsdiary.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.dinsdiary.data.local.entity.DailyMenstrualRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyMenstrualRecordDao {
    @Query("SELECT * FROM daily_menstrual_record WHERE date BETWEEN :fromDate AND :toDate ORDER BY date ASC")
    fun observeRange(fromDate: String, toDate: String): Flow<List<DailyMenstrualRecordEntity>>

    @Query("SELECT * FROM daily_menstrual_record WHERE date = :date")
    suspend fun getByDate(date: String): DailyMenstrualRecordEntity?

    @Upsert
    suspend fun upsert(record: DailyMenstrualRecordEntity)

    @Query("DELETE FROM daily_menstrual_record WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM daily_menstrual_record")
    suspend fun deleteAll()
}
