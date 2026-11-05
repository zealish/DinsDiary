package com.zealish.dinsdiary.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Actual user-recorded period. Estimates live in [PredictionSnapshotEntity],
 * never here (DATABASE_SCHEMA.md §1, §6).
 */
@Entity(
    tableName = "menstrual_period",
    indices = [Index("startDate"), Index("endDate")],
)
data class MenstrualPeriodEntity(
    @PrimaryKey val id: String,
    /** ISO date YYYY-MM-DD. */
    val startDate: String,
    /** ISO date, null while ongoing. */
    val endDate: String?,
    /** ACTIVE, COMPLETED, NEEDS_REVIEW. */
    val status: String,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
