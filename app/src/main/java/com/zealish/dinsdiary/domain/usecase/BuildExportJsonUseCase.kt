package com.zealish.dinsdiary.domain.usecase

import com.zealish.dinsdiary.domain.repository.DailyRecordRepository
import com.zealish.dinsdiary.domain.repository.JournalRepository
import com.zealish.dinsdiary.domain.repository.MenstrualPeriodRepository
import com.zealish.dinsdiary.domain.repository.ReminderRepository
import com.zealish.dinsdiary.domain.repository.UserProfileRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/** Machine-readable backup of the user's own records, schema v1 (PRD §6.10). */
class BuildExportJsonUseCase @Inject constructor(
    private val profileRepository: UserProfileRepository,
    private val periodRepository: MenstrualPeriodRepository,
    private val dailyRepository: DailyRecordRepository,
    private val journalRepository: JournalRepository,
    private val reminderRepository: ReminderRepository,
) {
    suspend operator fun invoke(): String {
        val profile = profileRepository.observeProfile().first()
        val periods = periodRepository.observePeriods().first()
        val records = dailyRepository
            .observeRange(LocalDate.parse("2000-01-01"), LocalDate.parse("2100-01-01")).first()
        val entries = journalRepository
            .observeRange(LocalDate.parse("2000-01-01"), LocalDate.parse("2100-01-01")).first()
        val reminders = reminderRepository.observeAll().first()
        return buildJsonObject {
            put("schemaVersion", 1)
            put("exportedFor", "local-backup")
            putJsonObject("profile") {
                profile.displayName?.let { put("displayName", it) }
                profile.dateOfBirth?.let { put("dateOfBirth", it.toString()) }
                profile.preferredCycleLengthDays?.let { put("preferredCycleLengthDays", it) }
                profile.preferredPeriodLengthDays?.let { put("preferredPeriodLengthDays", it) }
                put("lutealPhaseLengthDays", profile.lutealPhaseLengthDays)
            }
            putJsonArray("periods") {
                periods.forEach { period ->
                    addJsonObject {
                        put("id", period.id)
                        put("start", period.start.toString())
                        period.end?.let { put("end", it.toString()) }
                        put("status", period.status.name)
                    }
                }
            }
            putJsonArray("dailyRecords") {
                records.forEach { record ->
                    addJsonObject {
                        put("date", record.date.toString())
                        record.periodId?.let { put("periodId", it) }
                        record.flow?.let { put("flow", it.name) }
                        put("spotting", record.spotting)
                        record.notes?.let { put("notes", it) }
                    }
                }
            }
            putJsonArray("journal") {
                entries.forEach { entry ->
                    addJsonObject {
                        put("date", entry.date.toString())
                        entry.mood?.let { put("mood", it) }
                        entry.energyLevel?.let { put("energyLevel", it) }
                        entry.sleepMinutes?.let { put("sleepMinutes", it) }
                        entry.notes?.let { put("notes", it) }
                        putJsonArray("symptoms") {
                            entry.symptoms.forEach { symptom ->
                                addJsonObject {
                                    put("symptomId", symptom.symptomId)
                                    symptom.severity?.let { put("severity", it) }
                                }
                            }
                        }
                    }
                }
            }
            putJsonArray("reminders") {
                reminders.forEach { reminder ->
                    addJsonObject {
                        put("id", reminder.id)
                        put("type", reminder.type.name)
                        put("enabled", reminder.enabled)
                        put("hour", reminder.hour)
                        put("minute", reminder.minute)
                        put("daysBefore", reminder.daysBefore)
                    }
                }
            }
        }.toString()
    }
}
