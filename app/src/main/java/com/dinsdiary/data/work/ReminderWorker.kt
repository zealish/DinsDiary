package com.dinsdiary.data.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.OneTimeWorkRequestBuilder
import com.dinsdiary.R
import com.dinsdiary.data.local.dao.ReminderDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Clock
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

const val REMINDER_CHANNEL_ID = "dins_reminders"
const val KEY_REMINDER_ID = "reminder_id"

/**
 * Fires one reminder, then reschedules itself for the next day.
 * Notification text is privacy-safe by default (PRD §6.9).
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val reminderDao: ReminderDao,
    private val clock: Clock,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getString(KEY_REMINDER_ID) ?: return Result.failure()
        val reminder = reminderDao.getById(id)
        if (reminder != null && reminder.enabled) {
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.notify(
                id.hashCode(),
                NotificationCompat.Builder(applicationContext, REMINDER_CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setContentTitle(applicationContext.getString(R.string.rem_notification_title))
                    .setContentText(reminder.customMessage ?: applicationContext.getString(R.string.rem_default_message))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build(),
            )
            scheduleNext(applicationContext, reminder.notificationHour, reminder.notificationMinute, id)
        }
        return Result.success()
    }

    companion object {

        fun scheduleNext(context: Context, hour: Int, minute: Int, reminderId: String) {
            val delayMinutes = delayMinutesUntilNext(LocalDateTime.now(), hour, minute)
            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .addTag(tagFor(reminderId))
                .setInputData(
                    androidx.work.Data.Builder().putString(KEY_REMINDER_ID, reminderId).build(),
                )
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork("reminder-$reminderId", androidx.work.ExistingWorkPolicy.REPLACE, request)
        }

        fun cancel(context: Context, reminderId: String) {
            WorkManager.getInstance(context).cancelAllWorkByTag(tagFor(reminderId))
        }

        fun tagFor(reminderId: String) = "dins-reminder-$reminderId"

        /** Pure next-fire delay: same-day when strictly in the future, else tomorrow. */
        fun delayMinutesUntilNext(now: LocalDateTime, hour: Int, minute: Int): Long {
            var next = now.toLocalDate().atTime(hour, minute)
            if (!next.isAfter(now)) next = next.plusDays(1)
            return ChronoUnit.MINUTES.between(now, next).coerceAtLeast(1)
        }

        fun createChannel(context: Context) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    REMINDER_CHANNEL_ID,
                    context.getString(R.string.rem_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = context.getString(R.string.rem_channel_description) },
            )
        }
    }
}
