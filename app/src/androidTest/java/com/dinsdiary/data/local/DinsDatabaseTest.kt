package com.dinsdiary.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.dinsdiary.data.local.entity.DailyHealthLogEntity
import com.dinsdiary.data.local.entity.DailySymptomEntity
import com.dinsdiary.data.local.entity.MenstrualPeriodEntity
import com.dinsdiary.data.local.entity.SymptomEntity
import com.dinsdiary.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * DAO + schema integration (DATABASE_SCHEMA.md §4, AGENTS.md §10).
 * Requires device/emulator: `./gradlew :app:connectedDebugAndroidTest`.
 */
class DinsDatabaseTest {

    private val db = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        DinsDatabase::class.java,
    ).allowMainThreadQueries().build()

    @After
    fun close() {
        db.close()
    }

    @Test
    fun period_crudRoundTrip() = runTest {
        val dao = db.menstrualPeriodDao()
        val period = MenstrualPeriodEntity(
            id = "p1", startDate = "2026-10-24", endDate = "2026-10-28",
            status = "COMPLETED", notes = null, createdAt = 0, updatedAt = 0,
        )
        dao.upsert(period)
        assertEquals(period, dao.getById("p1"))
        assertEquals(listOf(period), dao.observeAll().first())

        dao.deleteById("p1")
        assertNull(dao.getById("p1"))
    }

    @Test
    fun profile_singleRowUpsert() = runTest {
        val dao = db.userProfileDao()
        val profile = UserProfileEntity(
            id = "local-profile", displayName = "Dina", dateOfBirth = null,
            preferredCycleLength = 28, preferredPeriodLength = 5,
            lutealPhaseLength = 14, createdAt = 0, updatedAt = 0,
        )
        dao.upsert(profile)
        assertEquals(profile, dao.getProfile("local-profile"))
    }

    @Test
    fun deletingHealthLog_cascadesDailySymptoms() = runTest {
        db.symptomDao().insertDefaults(
            listOf(SymptomEntity("s1", "symptom.cramps", "MENSTRUAL", true)),
        )
        val log = DailyHealthLogEntity(
            id = "h1", date = "2026-11-04", mood = "Calm", energyLevel = 3,
            sleepDurationMinutes = 450, appetite = null, notes = null,
            createdAt = 0, updatedAt = 0,
        )
        db.dailyHealthLogDao().upsert(log)
        db.dailySymptomDao().upsert(DailySymptomEntity("ds1", "h1", "s1", 2, null))
        assertEquals(1, db.dailySymptomDao().observeForLog("h1").first().size)

        db.dailyHealthLogDao().deleteById("h1")
        assertEquals(0, db.dailySymptomDao().observeForLog("h1").first().size)
    }
}
