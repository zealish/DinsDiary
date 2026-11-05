package com.zealish.dinsdiary.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.zealish.dinsdiary.data.local.entity.MenstrualPeriodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenstrualPeriodDao {
    @Query("SELECT * FROM menstrual_period ORDER BY startDate ASC")
    fun observeAll(): Flow<List<MenstrualPeriodEntity>>


    @Query(
        "SELECT * FROM menstrual_period WHERE startDate <= :date " +
            "AND (endDate IS NULL OR endDate >= :date) " +
            "ORDER BY startDate DESC LIMIT 1",
    )
    suspend fun containingPeriod(date: String): MenstrualPeriodEntity?
    @Query("SELECT * FROM menstrual_period WHERE id = :id")
    suspend fun getById(id: String): MenstrualPeriodEntity?

    @Upsert
    suspend fun upsert(period: MenstrualPeriodEntity)

    @Query("DELETE FROM menstrual_period WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM menstrual_period")
    suspend fun deleteAll()
}
