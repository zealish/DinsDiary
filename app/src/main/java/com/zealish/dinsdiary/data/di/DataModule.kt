package com.zealish.dinsdiary.data.di

import android.content.Context
import androidx.room.Room
import com.zealish.dinsdiary.data.local.DinsDatabase
import com.zealish.dinsdiary.data.local.dao.DailyHealthLogDao
import com.zealish.dinsdiary.data.local.dao.DailyMenstrualRecordDao
import com.zealish.dinsdiary.data.local.dao.DailySymptomDao
import com.zealish.dinsdiary.data.local.dao.FertilityRecordDao
import com.zealish.dinsdiary.data.local.dao.MenstrualPeriodDao
import com.zealish.dinsdiary.data.local.dao.PredictionSnapshotDao
import com.zealish.dinsdiary.data.local.dao.ReminderDao
import com.zealish.dinsdiary.data.local.dao.SymptomDao
import com.zealish.dinsdiary.data.local.dao.UserProfileDao
import com.zealish.dinsdiary.data.repository.MenstrualPeriodRepositoryImpl
import com.zealish.dinsdiary.data.repository.UserProfileRepositoryImpl
import com.zealish.dinsdiary.domain.repository.MenstrualPeriodRepository
import com.zealish.dinsdiary.data.repository.DailyRecordRepositoryImpl
import com.zealish.dinsdiary.domain.repository.DailyRecordRepository
import com.zealish.dinsdiary.data.repository.JournalRepositoryImpl
import com.zealish.dinsdiary.domain.repository.JournalRepository
import com.zealish.dinsdiary.data.repository.ReminderRepositoryImpl
import com.zealish.dinsdiary.domain.repository.ReminderRepository
import com.zealish.dinsdiary.domain.repository.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DinsDatabase =
        Room.databaseBuilder(context, DinsDatabase::class.java, "dinsdiary.db")
            // No destructive migration: schema changes need versioned
            // migrations (DATABASE_SCHEMA.md §5).
            .build()

    @Provides
    fun provideUserProfileDao(db: DinsDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    fun provideMenstrualPeriodDao(db: DinsDatabase): MenstrualPeriodDao = db.menstrualPeriodDao()

    @Provides
    fun provideDailyMenstrualRecordDao(db: DinsDatabase): DailyMenstrualRecordDao =
        db.dailyMenstrualRecordDao()

    @Provides
    fun provideDailyHealthLogDao(db: DinsDatabase): DailyHealthLogDao = db.dailyHealthLogDao()

    @Provides
    fun provideDailySymptomDao(db: DinsDatabase): DailySymptomDao = db.dailySymptomDao()

    @Provides
    fun provideSymptomDao(db: DinsDatabase): SymptomDao = db.symptomDao()

    @Provides
    fun provideFertilityRecordDao(db: DinsDatabase): FertilityRecordDao = db.fertilityRecordDao()

    @Provides
    fun provideReminderDao(db: DinsDatabase): ReminderDao = db.reminderDao()

    @Provides
    fun providePredictionSnapshotDao(db: DinsDatabase): PredictionSnapshotDao =
        db.predictionSnapshotDao()

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindMenstrualPeriodRepository(
        impl: MenstrualPeriodRepositoryImpl,
    ): MenstrualPeriodRepository

    @Binds
    abstract fun bindUserProfileRepository(
        impl: UserProfileRepositoryImpl,
    ): UserProfileRepository

    @Binds
    abstract fun bindDailyRecordRepository(
        impl: DailyRecordRepositoryImpl,
    ): DailyRecordRepository

    @Binds
    abstract fun bindJournalRepository(
        impl: JournalRepositoryImpl,
    ): JournalRepository

    @Binds
    abstract fun bindReminderRepository(
        impl: ReminderRepositoryImpl,
    ): ReminderRepository
}
