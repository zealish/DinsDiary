package com.zealish.dinsdiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Optional user fertility observations (PRD §6.7). Inputs only —
 * calculated estimates live in [PredictionSnapshotEntity].
 */
@Entity(tableName = "fertility_record")
data class FertilityRecordEntity(
    @PrimaryKey val id: String,
    /** ISO date YYYY-MM-DD. */
    val date: String,
    val cervicalMucus: String?,
    val basalTemperatureCelsius: Double?,
    /** NEGATIVE, POSITIVE, NOT_TESTED. */
    val ovulationTestResult: String,
    val notes: String?,
)
