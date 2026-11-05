package com.zealish.dinsdiary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zealish.dinsdiary.data.local.entity.SymptomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {
    @Query("SELECT * FROM symptom ORDER BY category ASC, nameKey ASC")
    fun observeAll(): Flow<List<SymptomEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaults(symptoms: List<SymptomEntity>)

    @Query("SELECT COUNT(*) FROM symptom")
    suspend fun count(): Int
}
