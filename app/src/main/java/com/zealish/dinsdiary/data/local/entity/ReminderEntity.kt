package com.zealish.dinsdiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local reminder configuration. Notification text stays privacy-safe
 * by default (PRD §6.9, DATABASE_SCHEMA.md §2).
 */
@Entity(tableName = "reminder")
data class ReminderEntity(
    @PrimaryKey val id: String,
    /** PERIOD, OVULATION, FERTILE_WINDOW, JOURNAL, MEDICATION, CUSTOM. */
    val type: String,
    val enabled: Boolean,
    val notificationHour: Int,
    val notificationMinute: Int,
    val daysBefore: Int,
    val customMessage: String?,
)
