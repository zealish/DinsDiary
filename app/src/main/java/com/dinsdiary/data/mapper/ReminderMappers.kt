package com.dinsdiary.data.mapper

import com.dinsdiary.data.local.entity.ReminderEntity
import com.dinsdiary.domain.model.Reminder
import com.dinsdiary.domain.model.ReminderType

fun ReminderEntity.toDomain() = Reminder(
    id = id,
    type = ReminderType.valueOf(type),
    enabled = enabled,
    hour = notificationHour,
    minute = notificationMinute,
    daysBefore = daysBefore,
    customMessage = customMessage,
)

fun Reminder.toEntity() = ReminderEntity(
    id = id,
    type = type.name,
    enabled = enabled,
    notificationHour = hour,
    notificationMinute = minute,
    daysBefore = daysBefore,
    customMessage = customMessage,
)
