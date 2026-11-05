package com.zealish.dinsdiary.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One symptom observation inside a [DailyHealthLogEntity].
 */
@Entity(
    tableName = "daily_symptom",
    indices = [
        Index("healthLogId"),
        Index(value = ["healthLogId", "symptomId"], unique = true),
    ],
    foreignKeys = [
        ForeignKey(
            entity = DailyHealthLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["healthLogId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SymptomEntity::class,
            parentColumns = ["id"],
            childColumns = ["symptomId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
)
data class DailySymptomEntity(
    @PrimaryKey val id: String,
    val healthLogId: String,
    val symptomId: String,
    /** Documented scale 1..5. */
    val severity: Int?,
    val notes: String?,
)
