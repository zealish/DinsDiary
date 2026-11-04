package com.dinsdiary.domain.usecase

import com.dinsdiary.domain.cycle.PeriodRecord
import com.dinsdiary.domain.cycle.PeriodStatus
import com.dinsdiary.domain.model.DailyRecord
import com.dinsdiary.domain.model.FlowIntensity
import com.dinsdiary.domain.model.JournalEntry
import com.dinsdiary.domain.model.LoggedSymptom
import com.dinsdiary.domain.model.Reminder
import com.dinsdiary.domain.model.ReminderType
import com.dinsdiary.domain.model.Symptom
import com.dinsdiary.domain.model.UserProfile
import com.dinsdiary.domain.repository.DailyRecordRepository
import com.dinsdiary.domain.repository.JournalRepository
import com.dinsdiary.domain.repository.MenstrualPeriodRepository
import com.dinsdiary.domain.repository.ReminderRepository
import com.dinsdiary.domain.repository.UserProfileRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

/** Export contract: schema version plus every record section (PRD §6.10). */
class BuildExportJsonUseCaseTest {

    @Test
    fun export_containsSchemaVersionAndAllSections() = runTest {
        val useCase = useCaseWith(
            profile = UserProfile("Ayu", null, 28, 5, 14),
            periods = listOf(
                PeriodRecord("p1", LocalDate.parse("2026-10-01"), LocalDate.parse("2026-10-05"), PeriodStatus.COMPLETED),
            ),
            records = listOf(
                DailyRecord(LocalDate.parse("2026-10-01"), "p1", FlowIntensity.MEDIUM, false, null),
            ),
            entries = listOf(
                JournalEntry(
                    LocalDate.parse("2026-10-02"), "Calm", 4, 480, null, "ok",
                    listOf(LoggedSymptom("sys-cramps", 2, null)),
                ),
            ),
            reminders = listOf(
                Reminder("r1", ReminderType.JOURNAL, true, 20, 0, 0, null),
            ),
        )

        val root = kotlinx.serialization.json.Json.parseToJsonElement(useCase()).jsonObject

        assertEquals(1, root["schemaVersion"]!!.jsonPrimitive.int)
        assertEquals("Ayu", root["profile"]!!.jsonObject["displayName"]!!.jsonPrimitive.content)
        assertEquals(1, root["periods"]!!.jsonArray.size)
        assertEquals(
            "2026-10-01",
            root["dailyRecords"]!!.jsonArray[0].jsonObject["date"]!!.jsonPrimitive.content,
        )
        assertEquals(1, root["journal"]!!.jsonArray[0].jsonObject["symptoms"]!!.jsonArray.size)
        assertEquals(
            "JOURNAL",
            root["reminders"]!!.jsonArray[0].jsonObject["type"]!!.jsonPrimitive.content,
        )
    }

    @Test
    fun export_withEmptyHistory_stillValid() = runTest {
        val useCase = useCaseWith(
            profile = UserProfile(null, null, null, null, 14),
            periods = emptyList(),
            records = emptyList(),
            entries = emptyList(),
            reminders = emptyList(),
        )

        val root = kotlinx.serialization.json.Json.parseToJsonElement(useCase()).jsonObject

        assertEquals(1, root["schemaVersion"]!!.jsonPrimitive.int)
        assertEquals(14, root["profile"]!!.jsonObject["lutealPhaseLengthDays"]!!.jsonPrimitive.int)
        assertEquals(0, root["periods"]!!.jsonArray.size)
        assertEquals(0, root["dailyRecords"]!!.jsonArray.size)
        assertEquals(0, root["journal"]!!.jsonArray.size)
        assertEquals(0, root["reminders"]!!.jsonArray.size)
    }

    private fun useCaseWith(
        profile: UserProfile,
        periods: List<PeriodRecord>,
        records: List<DailyRecord>,
        entries: List<JournalEntry>,
        reminders: List<Reminder>,
    ) = BuildExportJsonUseCase(
        object : UserProfileRepository {
            override fun observeProfile(): Flow<UserProfile> = flowOf(profile)
            override suspend fun saveProfile(profile: UserProfile) = Unit
        },
        object : MenstrualPeriodRepository {
            override fun observePeriods() = flowOf(periods)
            override suspend fun savePeriod(record: PeriodRecord, notes: String?) = Unit
            override suspend fun deletePeriod(id: String) = Unit
            override suspend fun deleteAllPeriods() = Unit
        },
        object : DailyRecordRepository {
            override fun observeRange(from: LocalDate, to: LocalDate) = flowOf(records)
            override suspend fun save(record: DailyRecord) = Unit
            override suspend fun deleteByDate(date: LocalDate) = Unit
        },
        object : JournalRepository {
            override fun observeRange(from: LocalDate, to: LocalDate) = flowOf(entries)
            override fun observeDay(date: LocalDate) = flowOf(entries.firstOrNull { it.date == date })
            override suspend fun save(entry: JournalEntry) = Unit
            override fun observeSymptoms(): Flow<List<Symptom>> = flowOf(emptyList())
            override suspend fun ensureSeeded() = Unit
        },
        object : ReminderRepository {
            override fun observeAll() = flowOf(reminders)
            override suspend fun save(reminder: Reminder) = Unit
            override suspend fun delete(id: String) = Unit
            override suspend fun deleteAll() = Unit
        },
    )
}
