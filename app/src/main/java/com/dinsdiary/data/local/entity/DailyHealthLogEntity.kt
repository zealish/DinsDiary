package com.dinsdiary.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One journal container per date; symptoms stored separately
 * (DATABASE_SCHEMA.md §2, PRD §6.6).
 */
@Entity(
    tableName = "daily_health_log",
    indices = [Index(value = ["date"], unique = true)],
)
data class DailyHealthLogEntity(
    @PrimaryKey val id: String,
    /** ISO date YYYY-MM-DD, unique. */
    val date: String,
    val mood: String?,
    /** Bounded scale 1..5, null when unset. */
    val energyLevel: Int?,
    val sleepDurationMinutes: Int?,
    val appetite: String?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
