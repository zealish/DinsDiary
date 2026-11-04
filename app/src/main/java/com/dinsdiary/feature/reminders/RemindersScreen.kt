package com.dinsdiary.feature.reminders

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dinsdiary.R
import com.dinsdiary.domain.model.Reminder
import com.dinsdiary.domain.model.ReminderType
import kotlinx.coroutines.launch

/** Optional local reminders (PRD §6.9, mockup). Generic text by default. */
@Composable
fun RemindersScreen(
    modifier: Modifier = Modifier,
    viewModel: ReminderViewModel = hiltViewModel(),
) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        scope.launch { viewModel.confirmPendingEnable(granted) }
    }

    LazyColumn(
        modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(stringResource(R.string.rem_title), style = MaterialTheme.typography.headlineMedium)
            Text(
                stringResource(R.string.rem_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(reminders, key = { it.id }) { reminder ->
            ReminderRow(
                reminder = reminder,
                onToggle = { enabled ->
                    scope.launch {
                        val needsPermission = viewModel.setEnabled(reminder, enabled)
                        if (needsPermission && Build.VERSION.SDK_INT >= 33) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                },
                onEdit = { viewModel.edit(reminder) },
                onDelete = { scope.launch { viewModel.delete(reminder) } },
            )
        }
        if (reminders.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.rem_empty),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item { ReminderFormCard(viewModel) }
        viewModel.infoMessage?.let {
            item { Text(it, color = MaterialTheme.colorScheme.primary) }
        }
        item {
            Text(
                stringResource(R.string.rem_footnote),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ReminderRow(
    reminder: Reminder,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(typeLabel(reminder.type), style = MaterialTheme.typography.bodyLarge)
                val schedule = if (reminder.daysBefore == 1) {
                    stringResource(R.string.rem_schedule_one_day_before, reminder.hour, reminder.minute)
                } else {
                    stringResource(
                        R.string.rem_schedule_days_before,
                        reminder.hour,
                        reminder.minute,
                        reminder.daysBefore,
                    )
                }
                Text(
                    schedule,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = reminder.enabled, onCheckedChange = onToggle)
        }
        Row(modifier = Modifier.padding(horizontal = 8.dp)) {
            TextButton(onClick = onEdit) { Text(stringResource(R.string.rem_edit)) }
            TextButton(onClick = onDelete) { Text(stringResource(R.string.rem_delete)) }
        }
    }
}

@Composable
private fun typeLabel(type: ReminderType): String = when (type) {
    ReminderType.PERIOD -> stringResource(R.string.rem_type_period)
    ReminderType.JOURNAL -> stringResource(R.string.rem_type_journal)
    ReminderType.CUSTOM -> stringResource(R.string.rem_type_custom)
}

@Composable
private fun typeShortLabel(type: ReminderType): String = when (type) {
    ReminderType.PERIOD -> stringResource(R.string.rem_type_period_short)
    ReminderType.JOURNAL -> stringResource(R.string.rem_type_journal_short)
    ReminderType.CUSTOM -> stringResource(R.string.rem_type_custom_short)
}

@Composable
private fun ReminderFormCard(viewModel: ReminderViewModel) {
    val form = viewModel.form
    val scope = rememberCoroutineScope()
    if (form == null) {
        Button(onClick = viewModel::newReminder, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.rem_add))
        }
        return
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                if (form.id == null) stringResource(R.string.rem_form_title_new) else stringResource(R.string.rem_form_title_edit),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReminderType.entries.forEach { type ->
                    FilterChip(
                        selected = form.type == type,
                        onClick = { viewModel.updateForm(form.copy(type = type)) },
                        label = { Text(typeShortLabel(type)) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = form.hour,
                    onValueChange = { viewModel.updateForm(form.copy(hour = it.filter(Char::isDigit).take(2))) },
                    label = { Text(stringResource(R.string.rem_hour)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                TextField(
                    value = form.minute,
                    onValueChange = { viewModel.updateForm(form.copy(minute = it.filter(Char::isDigit).take(2))) },
                    label = { Text(stringResource(R.string.rem_minute)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                TextField(
                    value = form.daysBefore,
                    onValueChange = { viewModel.updateForm(form.copy(daysBefore = it.filter(Char::isDigit).take(2))) },
                    label = { Text(stringResource(R.string.rem_days_before)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }
            TextField(
                value = form.customMessage,
                onValueChange = { viewModel.updateForm(form.copy(customMessage = it)) },
                label = { Text(stringResource(R.string.rem_custom_message_label)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { scope.launch { viewModel.save() } },
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(R.string.rem_save)) }
                OutlinedButton(
                    onClick = viewModel::cancelForm,
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(R.string.rem_cancel)) }
            }
        }
    }
}
