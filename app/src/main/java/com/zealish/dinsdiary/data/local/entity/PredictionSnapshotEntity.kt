package com.zealish.dinsdiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Versioned calculation snapshot for auditability (PRD §7,
 * DATABASE_SCHEMA.md §2). Never inserted into `menstrual_period`.
 */
@Entity(tableName = "prediction_snapshot")
data class PredictionSnapshotEntity(
    @PrimaryKey val id: String,
    val generatedAt: Long,
    val algorithmVersion: String,
    /** Serialized list of source record ids. */
    val sourceCycleIds: String,
    /** ISO date YYYY-MM-DD. */
    val predictedPeriodStart: String,
    /** ISO date, nullable. */
    val predictedPeriodEnd: String?,
    val estimatedOvulationDate: String?,
    val fertileWindowStart: String?,
    val fertileWindowEnd: String?,
    /** INSUFFICIENT, LOW, MODERATE, HIGHER. */
    val confidenceLevel: String,
    val explanationCode: String,
)
