package com.zealish.dinsdiary.data.mapper

import com.zealish.dinsdiary.data.local.entity.ReminderEntity
import com.zealish.dinsdiary.domain.model.Reminder
import com.zealish.dinsdiary.domain.model.ReminderType

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
