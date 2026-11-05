@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.zealish.dinsdiary.feature.period

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zealish.dinsdiary.R
import com.zealish.dinsdiary.domain.cycle.PeriodRecord
import com.zealish.dinsdiary.domain.model.FlowIntensity
import com.zealish.dinsdiary.ui.components.DatePickerField
import com.zealish.dinsdiary.ui.components.DinsGhostButton
import com.zealish.dinsdiary.ui.components.DinsPrimaryButton
import com.zealish.dinsdiary.ui.theme.DinsCardShape
import com.zealish.dinsdiary.ui.theme.DinsSerif
import com.zealish.dinsdiary.ui.theme.currentHomePalette
import kotlinx.coroutines.launch

/**
 * Period tracking (PRD §6.3, mockup `period` screen). Recorded facts only —
 * predictions stay in the calendar and are never written here.
 */
@Composable
fun PeriodScreen(
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PeriodViewModel = hiltViewModel(),
) {
    val periods by viewModel.periods.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        ScreenHeader(editing = viewModel.form.editingId != null)

        PeriodFormCard(viewModel) { scope.launch { viewModel.savePeriod() } }
        DailyFlowCard(viewModel) { scope.launch { viewModel.saveDailyFlow() } }

        val error = viewModel.errorMessage
        val info = viewModel.infoMessage
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
        if (info != null) {
            Text(info, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
        }

        ActualNote()

        DinsGhostButton(
            text = stringResource(R.string.per_view_calendar),
            onClick = onOpenCalendar,
            modifier = Modifier.fillMaxWidth(),
        )


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.per_recorded),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = stringResource(R.string.per_view_calendar),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onOpenCalendar),
            )
        }
        if (periods.isEmpty()) {
            Text(
                stringResource(R.string.per_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            periods.forEach { record ->
                PeriodRow(
                    record = record,
                    editing = viewModel.form.editingId == record.id,
                    onEdit = { viewModel.edit(record) },
                    onDelete = { viewModel.askDelete(record) },
                )
            }
        }
    }

    viewModel.deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text(stringResource(R.string.per_delete_title)) },
            text = {
                Text(
                    stringResource(R.string.per_delete_text, target.start.toString()),
                )
            },
            confirmButton = {
                DinsGhostButton(
                    text = stringResource(R.string.per_delete),
                    onClick = { scope.launch { viewModel.confirmDelete() } },
                )
            },
            dismissButton = {
                DinsGhostButton(
                    text = stringResource(R.string.per_keep),
                    onClick = viewModel::cancelDelete,
                )
            },
        )
    }
}

@Composable
private fun ScreenHeader(editing: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            stringResource(R.string.per_eyebrow),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.6.sp,
            color = currentHomePalette.eyebrow,
        )
        Text(
            if (editing) {
                stringResource(R.string.per_edit)
            } else {
                stringResource(R.string.per_title)
            },
            fontFamily = DinsSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 25.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            stringResource(R.string.per_subtitle),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PeriodFormCard(viewModel: PeriodViewModel, onSave: () -> Unit) {
    val palette = currentHomePalette
    val form = viewModel.form
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DinsCardShape)
            .background(Brush.linearGradient(listOf(palette.softTop, palette.softBottom)))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                if (form.editingId == null) {
                    stringResource(R.string.per_new)
                } else {
                    stringResource(R.string.per_edit)
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            FieldBlock(label = stringResource(R.string.per_start)) {
                DatePickerField(
                    value = form.start,
                    onValueChange = { viewModel.updateForm(form.copy(start = it)) },
                    label = stringResource(R.string.per_start),
                    modifier = Modifier.fillMaxWidth(),
                    allowClear = false,
                )
            }
            FieldBlock(label = stringResource(R.string.per_end)) {
                DatePickerField(
                    value = form.end,
                    onValueChange = { viewModel.updateForm(form.copy(end = it)) },
                    label = stringResource(R.string.per_end),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !form.ongoing,
                )
            }
            FieldBlock(label = stringResource(R.string.per_status)) {
                StatusChoices(
                    ongoing = form.ongoing,
                    onChange = { viewModel.updateForm(form.copy(ongoing = it)) },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DinsPrimaryButton(
                    text = if (form.editingId == null) {
                        stringResource(R.string.per_save_record)
                    } else {
                        stringResource(R.string.per_update)
                    },
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                )
                if (form.editingId != null) {
                    DinsGhostButton(
                        text = stringResource(R.string.per_cancel),
                        onClick = viewModel::resetForm,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyFlowCard(viewModel: PeriodViewModel, onSave: () -> Unit) {
    val palette = currentHomePalette
    val form = viewModel.flowForm
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DinsCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, palette.cardBorder),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(R.string.per_flow_title),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            FieldBlock(label = stringResource(R.string.per_date)) {
                DatePickerField(
                    value = form.date,
                    onValueChange = { viewModel.updateFlowForm(form.copy(date = it)) },
                    label = stringResource(R.string.per_date),
                    modifier = Modifier.fillMaxWidth(),
                    allowClear = false,
                )
            }
            FieldBlock(label = stringResource(R.string.per_flow_intensity)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    FlowIntensity.entries.forEach { intensity ->
                        ChoiceChip(
                            label = flowLabel(intensity),
                            selected = form.flow == intensity,
                            onClick = {
                                viewModel.updateFlowForm(
                                    form.copy(flow = intensity.takeIf { form.flow != intensity }),
                                )
                            },
                        )
                    }
                }
            }
            FieldBlock(label = stringResource(R.string.per_spotting_q)) {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    ChoiceChip(
                        label = stringResource(R.string.per_yes),
                        selected = form.spotting,
                        onClick = { viewModel.updateFlowForm(form.copy(spotting = true)) },
                    )
                    ChoiceChip(
                        label = stringResource(R.string.per_no),
                        selected = !form.spotting,
                        onClick = { viewModel.updateFlowForm(form.copy(spotting = false)) },
                    )
                }
            }
            FieldBlock(label = stringResource(R.string.per_notes)) {
                OutlinedTextField(
                    value = form.notes,
                    onValueChange = { viewModel.updateFlowForm(form.copy(notes = it)) },
                    label = { Text(stringResource(R.string.per_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
            }
            DinsPrimaryButton(
                text = stringResource(R.string.per_save_flow),
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun flowLabel(intensity: FlowIntensity): String = when (intensity) {
    FlowIntensity.LIGHT -> stringResource(R.string.per_flow_light)
    FlowIntensity.MEDIUM -> stringResource(R.string.per_flow_medium)
    FlowIntensity.HEAVY -> stringResource(R.string.per_flow_heavy)
    FlowIntensity.VERY_HEAVY -> stringResource(R.string.per_flow_very_heavy)
}

@Composable
private fun StatusChoices(ongoing: Boolean, onChange: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        ChoiceChip(
            label = stringResource(R.string.per_completed),
            selected = !ongoing,
            onClick = { onChange(false) },
        )
        ChoiceChip(
            label = stringResource(R.string.per_ongoing),
            selected = ongoing,
            onClick = { onChange(true) },
        )
    }
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val palette = currentHomePalette
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) palette.pillBg else palette.choiceBg)
            .border(
                1.dp,
                if (selected) palette.secondaryBorder else palette.choiceBorder,
                RoundedCornerShape(50),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Normal,
            color = if (selected) palette.pillText else palette.choiceText,
        )
    }
}

@Composable
private fun FieldBlock(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
private fun ActualNote() {
    val palette = currentHomePalette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(palette.noteBg)
            .border(1.dp, palette.noteBorder, RoundedCornerShape(13.dp))
            .padding(11.dp),
    ) {
        Text(
            stringResource(R.string.per_actual_note),
            fontSize = 10.sp,
            lineHeight = 15.sp,
            color = palette.noteText,
        )
    }
}

@Composable
private fun PeriodRow(
    record: PeriodRecord,
    editing: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val palette = currentHomePalette
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DinsCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, palette.cardBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val range = stringResource(
                    R.string.per_range,
                    record.start.toString(),
                    record.end?.toString() ?: stringResource(R.string.per_ongoing_word),
                )
                Text(range, style = MaterialTheme.typography.bodyLarge)
                Text(
                    record.status.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DinsGhostButton(
                    text = stringResource(R.string.per_edit_action),
                    onClick = onEdit,
                )
                DinsGhostButton(
                    text = stringResource(R.string.per_delete),
                    onClick = onDelete,
                )
            }
        }
    }
    if (editing) {
        Text(
            stringResource(R.string.per_edit),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp),
        )
    }
}
