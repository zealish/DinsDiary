package com.zealish.dinsdiary.domain.usecase

import com.zealish.dinsdiary.data.local.dao.DailyHealthLogDao
import com.zealish.dinsdiary.data.local.dao.DailyMenstrualRecordDao
import com.zealish.dinsdiary.data.local.dao.DailySymptomDao
import com.zealish.dinsdiary.data.local.dao.FertilityRecordDao
import com.zealish.dinsdiary.data.local.dao.MenstrualPeriodDao
import com.zealish.dinsdiary.data.local.dao.PredictionSnapshotDao
import com.zealish.dinsdiary.data.local.dao.ReminderDao
import com.zealish.dinsdiary.data.local.dao.UserProfileDao
import com.zealish.dinsdiary.data.prefs.AppPrefs
import javax.inject.Inject

/**
 * Confirmed full wipe (PRD §6.10). UI must confirm before invoking;
 * this layer only executes. Symptom catalog is kept (not user data).
 */
class ClearUserDataUseCase @Inject constructor(
    private val periodDao: MenstrualPeriodDao,
    private val dailyRecordDao: DailyMenstrualRecordDao,
    private val logDao: DailyHealthLogDao,
    private val dailySymptomDao: DailySymptomDao,
    private val fertilityDao: FertilityRecordDao,
    private val reminderDao: ReminderDao,
    private val snapshotDao: PredictionSnapshotDao,
    private val profileDao: UserProfileDao,
    private val prefs: AppPrefs,
) {
    suspend fun clear() {
        dailySymptomDao.deleteAll()
        logDao.deleteAll()
        dailyRecordDao.deleteAll()
        periodDao.deleteAll()
        fertilityDao.deleteAll()
        reminderDao.deleteAll()
        snapshotDao.deleteAll()
        profileDao.deleteAll()
        prefs.resetSetup()
    }
}
