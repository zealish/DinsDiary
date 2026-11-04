package com.dinsdiary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dinsdiary.data.local.dao.DailyHealthLogDao
import com.dinsdiary.data.local.dao.DailyMenstrualRecordDao
import com.dinsdiary.data.local.dao.DailySymptomDao
import com.dinsdiary.data.local.dao.FertilityRecordDao
import com.dinsdiary.data.local.dao.MenstrualPeriodDao
import com.dinsdiary.data.local.dao.PredictionSnapshotDao
import com.dinsdiary.data.local.dao.ReminderDao
import com.dinsdiary.data.local.dao.SymptomDao
import com.dinsdiary.data.local.dao.UserProfileDao
import com.dinsdiary.data.local.entity.DailyHealthLogEntity
import com.dinsdiary.data.local.entity.DailyMenstrualRecordEntity
import com.dinsdiary.data.local.entity.DailySymptomEntity
import com.dinsdiary.data.local.entity.FertilityRecordEntity
import com.dinsdiary.data.local.entity.MenstrualPeriodEntity
import com.dinsdiary.data.local.entity.PredictionSnapshotEntity
import com.dinsdiary.data.local.entity.ReminderEntity
import com.dinsdiary.data.local.entity.SymptomEntity
import com.dinsdiary.data.local.entity.UserProfileEntity

/**
 * Offline-first source of truth (PRD §5, ARCHITECTURE.md §7).
 * Versioned migrations required — never destructive in production.
 */
@Database(
    entities = [
        UserProfileEntity::class,
        MenstrualPeriodEntity::class,
        DailyMenstrualRecordEntity::class,
        DailyHealthLogEntity::class,
        SymptomEntity::class,
        DailySymptomEntity::class,
        FertilityRecordEntity::class,
        ReminderEntity::class,
        PredictionSnapshotEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class DinsDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun menstrualPeriodDao(): MenstrualPeriodDao
    abstract fun dailyMenstrualRecordDao(): DailyMenstrualRecordDao
    abstract fun dailyHealthLogDao(): DailyHealthLogDao
    abstract fun dailySymptomDao(): DailySymptomDao
    abstract fun symptomDao(): SymptomDao
    abstract fun fertilityRecordDao(): FertilityRecordDao
    abstract fun reminderDao(): ReminderDao
    abstract fun predictionSnapshotDao(): PredictionSnapshotDao
}
