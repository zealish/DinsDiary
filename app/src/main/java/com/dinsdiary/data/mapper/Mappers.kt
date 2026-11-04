package com.dinsdiary.data.mapper

import com.dinsdiary.data.local.entity.MenstrualPeriodEntity
import com.dinsdiary.data.local.entity.UserProfileEntity
import com.dinsdiary.domain.cycle.PeriodRecord
import com.dinsdiary.domain.cycle.PeriodStatus
import com.dinsdiary.domain.model.UserProfile
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/** ISO-8601 date-only strings; never timezone-converted (ARCHITECTURE.md §8). */
fun LocalDate.toIso(): String = toString()
fun String.toLocalDate(): LocalDate = LocalDate.parse(this)

fun MenstrualPeriodEntity.toDomain() = PeriodRecord(
    id = id,
    start = startDate.toLocalDate(),
    end = endDate?.toLocalDate(),
    status = PeriodStatus.valueOf(status),
    erroneous = false,
)

fun PeriodRecord.toEntity(notes: String?, now: Instant) = MenstrualPeriodEntity(
    id = id,
    startDate = start.toIso(),
    endDate = end?.toIso(),
    status = status.name,
    notes = notes,
    createdAt = now.toEpochMilli(),
    updatedAt = now.toEpochMilli(),
)

fun UserProfileEntity.toDomain() = UserProfile(
    displayName = displayName,
    dateOfBirth = dateOfBirth?.toLocalDate(),
    preferredCycleLengthDays = preferredCycleLength,
    preferredPeriodLengthDays = preferredPeriodLength,
    lutealPhaseLengthDays = lutealPhaseLength,
)

fun UserProfile.toEntity(now: Instant) = UserProfileEntity(
    id = SINGLE_PROFILE_ID,
    displayName = displayName,
    dateOfBirth = dateOfBirth?.toIso(),
    preferredCycleLength = preferredCycleLengthDays,
    preferredPeriodLength = preferredPeriodLengthDays,
    lutealPhaseLength = lutealPhaseLengthDays,
    createdAt = now.toEpochMilli(),
    updatedAt = now.toEpochMilli(),
)

fun newId(): String = UUID.randomUUID().toString()

const val SINGLE_PROFILE_ID = "local-profile"
const val DEFAULT_LUTEAL_PHASE_DAYS = 14
