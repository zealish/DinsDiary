package com.zealish.dinsdiary.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One daily bleeding observation (PRD §6.3). One record per date is
 * enforced by unique index; duplicates are a repository-level review case.
 */
@Entity(
    tableName = "daily_menstrual_record",
    indices = [Index("periodId")],
    foreignKeys = [
        ForeignKey(
            entity = MenstrualPeriodEntity::class,
            parentColumns = ["id"],
            childColumns = ["periodId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class DailyMenstrualRecordEntity(
    @PrimaryKey val id: String,
    val periodId: String?,
    /** ISO date YYYY-MM-DD, unique per day. */
    val date: String,
    /** LIGHT, MEDIUM, HEAVY, VERY_HEAVY, or null. */
    val flowIntensity: String?,
    val spotting: Boolean,
    val notes: String?,
)
