package com.zealish.dinsdiary.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.zealish.dinsdiary.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val ZONE = ZoneId.systemDefault()
private const val MILLIS_PER_DAY = 86_400_000L

private fun LocalDate.toPickerMillis(): Long = toEpochDay() * MILLIS_PER_DAY

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZONE).toLocalDate()

private fun parseIso(raw: String): LocalDate? =
    raw.ifBlank { null }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

/**
 * Read-only date field that opens a Material 3 calendar picker instead of
 * requiring manual YYYY-MM-DD typing. The string contract matches the
 * existing form/viewmodel convention (ISO-8601 or blank); viewmodel
 * validation stays as the safety net.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    allowClear: Boolean = true,
) {
    var open by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            placeholder = { Text("YYYY-MM-DD") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null)
            },
        )
        // Transparent tap target: the whole field opens the calendar.
        if (enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { open = true },
            )
        }
    }
    if (open) {
        DatePickerDialogHost(
            current = parseIso(value),
            onConfirm = {
                onValueChange(it.toString())
                open = false
            },
            onClear = if (allowClear && value.isNotBlank()) {
                { onValueChange(""); open = false }
            } else {
                null
            },
            onDismiss = { open = false },
        )
    }
}

/**
 * Standalone calendar dialog host for surfaces without a text field
 * (e.g. tappable date headers). Renders nothing while dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogHost(
    current: LocalDate?,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = (current ?: LocalDate.now(ZONE)).toPickerMillis(),
    )
    // Own Dialog window instead of DatePickerDialog: its internal layout
    // ignores outer padding and stretches edge-to-edge. Here the Surface
    // carries an explicit horizontal margin.
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                DatePicker(state = state)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    if (onClear != null) {
                        TextButton(onClick = onClear) { Text(stringResource(R.string.datepicker_clear)) }
                    } else {
                        TextButton(onClick = onDismiss) { Text(stringResource(R.string.datepicker_cancel)) }
                    }
                    TextButton(
                        onClick = {
                            state.selectedDateMillis?.toLocalDate()?.let(onConfirm)
                                ?: onDismiss()
                        },
                    ) {
                        Text(stringResource(R.string.datepicker_confirm))
                    }
                }
            }
        }
    }
}
