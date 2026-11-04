package com.dinsdiary.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.dinsdiary.data.local.entity.FertilityRecordEntity
import com.dinsdiary.data.local.entity.PredictionSnapshotEntity
import com.dinsdiary.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FertilityRecordDao {
    @Query("SELECT * FROM fertility_record WHERE date = :date")
    fun observeByDate(date: String): Flow<FertilityRecordEntity?>


    @Upsert
    suspend fun upsert(record: FertilityRecordEntity)

    @Query("DELETE FROM fertility_record")
    suspend fun deleteAll()
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminder ORDER BY type ASC")
    fun observeAll(): Flow<List<ReminderEntity>>

    @Upsert
    suspend fun upsert(reminder: ReminderEntity)

    @Query("DELETE FROM reminder WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM reminder")
    suspend fun deleteAll()

    @Query("SELECT * FROM reminder WHERE id = :id")
    suspend fun getById(id: String): ReminderEntity?
}
@Dao
interface PredictionSnapshotDao {
    @Query("SELECT * FROM prediction_snapshot ORDER BY generatedAt DESC LIMIT 1")
    fun observeLatest(): Flow<PredictionSnapshotEntity?>

    @Upsert
    suspend fun upsert(snapshot: PredictionSnapshotEntity)

    @Query("DELETE FROM prediction_snapshot")
    suspend fun deleteAll()
}
