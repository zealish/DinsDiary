package com.dinsdiary.data.mapper

import com.dinsdiary.data.local.entity.DailyHealthLogEntity
import com.dinsdiary.data.local.entity.DailySymptomEntity
import com.dinsdiary.data.local.entity.SymptomEntity
import com.dinsdiary.domain.model.JournalEntry
import com.dinsdiary.domain.model.LoggedSymptom
import com.dinsdiary.domain.model.Symptom
import java.time.Instant

fun SymptomEntity.toDomain() = Symptom(id, nameKey, category)

fun Symptom.toEntity() = SymptomEntity(id, nameKey, category, isSystemDefault = id.startsWith("sys-"))

fun JournalEntry.toLogEntity(id: String, now: Instant) = DailyHealthLogEntity(
    id = id,
    date = date.toIso(),
    mood = mood,
    energyLevel = energyLevel,
    sleepDurationMinutes = sleepMinutes,
    appetite = appetite,
    notes = notes,
    createdAt = now.toEpochMilli(),
    updatedAt = now.toEpochMilli(),
)

fun JournalEntry.toSymptomEntities(logId: String) = symptoms.map {
    DailySymptomEntity(
        id = "$logId:${it.symptomId}",
        healthLogId = logId,
        symptomId = it.symptomId,
        severity = it.severity,
        notes = it.notes,
    )
}

fun toJournalEntry(
    log: DailyHealthLogEntity,
    symptoms: List<DailySymptomEntity>,
) = JournalEntry(
    date = log.date.toLocalDate(),
    mood = log.mood,
    energyLevel = log.energyLevel,
    sleepMinutes = log.sleepDurationMinutes,
    appetite = log.appetite,
    notes = log.notes,
    symptoms = symptoms.map { LoggedSymptom(it.symptomId, it.severity, it.notes) },
)
