package com.zealish.dinsdiary.domain.model

/** Local reminder config (PRD §6.9). Message text stays generic by default. */
data class Reminder(
    val id: String,
    val type: ReminderType,
    val enabled: Boolean,
    val hour: Int,
    val minute: Int,
    val daysBefore: Int,
    val customMessage: String?,
)

enum class ReminderType {
    PERIOD,
    JOURNAL,
    CUSTOM,
}
