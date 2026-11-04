package com.dinsdiary.feature.reminders

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinsdiary.data.mapper.newId
import com.dinsdiary.data.work.ReminderWorker
import com.dinsdiary.domain.model.Reminder
import com.dinsdiary.domain.model.ReminderType
import com.dinsdiary.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

data class ReminderForm(
    val id: String? = null,
    val type: ReminderType = ReminderType.JOURNAL,
    val hour: String = "20",
    val minute: String = "00",
    val daysBefore: String = "1",
    val customMessage: String = "",
)

/** Optional local reminders (PRD §6.9). Text stays generic unless customized. */
@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val repository: ReminderRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val reminders: StateFlow<List<Reminder>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var form by mutableStateOf<ReminderForm?>(null)
        private set
    var infoMessage by mutableStateOf<String?>(null)
        private set
    var pendingEnable by mutableStateOf<Reminder?>(null)
        private set

    fun newReminder() {
        form = ReminderForm()
        infoMessage = null
    }

    fun edit(reminder: Reminder) {
        form = ReminderForm(
            id = reminder.id,
            type = reminder.type,
            hour = reminder.hour.toString(),
            minute = reminder.minute.toString().padStart(2, '0'),
            daysBefore = reminder.daysBefore.toString(),
            customMessage = reminder.customMessage.orEmpty(),
        )
        infoMessage = null
    }

    fun cancelForm() {
        form = null
    }

    fun updateForm(form: ReminderForm) {
        this.form = form
        infoMessage = null
    }

    suspend fun save(): Boolean {
        val form = form ?: return false
        val hour = form.hour.toIntOrNull()
        val minute = form.minute.toIntOrNull()
        if (hour == null || hour !in 0..23 || minute == null || minute !in 0..59) {
            infoMessage = "Time must be a valid hour (0–23) and minute (0–59)."
            return false
        }
        val daysBefore = form.daysBefore.toIntOrNull()
        if (daysBefore == null || daysBefore !in 0..14) {
            infoMessage = "Days before must be 0–14."
            return false
        }
        val id = form.id ?: newId()
        val current = reminders.value.firstOrNull { it.id == id }
        val reminder = Reminder(
            id = id,
            type = form.type,
            enabled = current?.enabled ?: true,
            hour = hour,
            minute = minute,
            daysBefore = daysBefore,
            customMessage = form.customMessage.ifBlank { null },
        )
        repository.save(reminder)
        applySchedule(reminder)
        this.form = null
        infoMessage = "Reminder saved."
        return true
    }

    /** Toggle from UI. Returns true when a notification permission check is needed. */
    suspend fun setEnabled(reminder: Reminder, enabled: Boolean): Boolean {
        if (enabled && !hasNotificationPermission()) {
            pendingEnable = reminder
            return true
        }
        persistEnabled(reminder, enabled)
        return false
    }

    suspend fun confirmPendingEnable(granted: Boolean) {
        pendingEnable?.let {
            persistEnabled(it, granted)
            if (!granted) infoMessage = "Reminder kept off: notification permission denied."
        }
        pendingEnable = null
    }

    private suspend fun persistEnabled(reminder: Reminder, enabled: Boolean) {
        val updated = reminder.copy(enabled = enabled)
        repository.save(updated)
        applySchedule(updated)
    }

    suspend fun delete(reminder: Reminder) {
        repository.delete(reminder.id)
        ReminderWorker.cancel(context, reminder.id)
        infoMessage = "Reminder deleted."
    }

    private fun applySchedule(reminder: Reminder) {
        if (reminder.enabled) {
            ReminderWorker.scheduleNext(context, reminder.hour, reminder.minute, reminder.id)
        } else {
            ReminderWorker.cancel(context, reminder.id)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        if (android.os.Build.VERSION.SDK_INT < 33) return true
        return context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
