package com.dinsdiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single local profile. Preferences are user input, never observed truth
 * (PRD §6.1, DATABASE_SCHEMA.md §2).
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val displayName: String?,
    /** ISO date YYYY-MM-DD, nullable. */
    val dateOfBirth: String?,
    val preferredCycleLength: Int?,
    val preferredPeriodLength: Int?,
    val lutealPhaseLength: Int,
    val createdAt: Long,
    val updatedAt: Long,
)
