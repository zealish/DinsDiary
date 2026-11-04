@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dinsdiary.feature.journal

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.dinsdiary.ui.components.DinsDropdown
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dinsdiary.R
import com.dinsdiary.domain.model.MOOD_EMOJI
import com.dinsdiary.ui.components.DatePickerDialogHost
import com.dinsdiary.ui.components.DinsGhostButton
import com.dinsdiary.ui.components.DinsPrimaryButton
import com.dinsdiary.ui.i18n.moodLabel
import com.dinsdiary.ui.i18n.systemSymptomLabel
import com.dinsdiary.ui.theme.DinsCardShape
import com.dinsdiary.ui.theme.DinsSerif
import com.dinsdiary.ui.theme.currentHomePalette
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private val HEADER_DATE = DateTimeFormatter.ofPattern("EEEE · d MMMM yyyy", Locale.getDefault())
private val SHORT_DATE = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())

/** Stable key for the symptom "none" chip; display text resolves via resources. */
private const val NONE_KEY = "none"

/** Mockup energy order (normal first); entries are stored keys. */
private val ENERGY_ORDER = listOf("normal", "low", "high")

/** Stored severity key to display label. */
@Composable
private fun severityLabel(key: String): String = stringResource(
    when (key) {
        "mild" -> R.string.journal_severity_mild
        "severe" -> R.string.journal_severity_severe
        else -> R.string.journal_severity_moderate
    },
)

/** Stored energy key to display label. */
@Composable
private fun energyLabel(key: String): String = stringResource(
    when (key) {
        "low" -> R.string.journal_energy_low
        "high" -> R.string.journal_energy_high
        else -> R.string.journal_energy_normal
    },
)

/** Stored sleep bucket key to display label. */
@Composable
private fun sleepBucketLabel(key: String): String = stringResource(
    when (key) {
        "under6" -> R.string.journal_sleep_under6
        "over8" -> R.string.journal_sleep_over8
        else -> R.string.journal_sleep_h78
    },
)

/** Symptom "none" key to display label. */
@Composable
private fun noneLabel(): String = stringResource(R.string.journal_none)

/**
 * Daily check-in (PRD §6.6, `mockup.html` journal screen):
 * mood, symptoms + severity, energy/sleep selects, notes.
 * ‹ › arrows + tappable date keep previous-entry editing reachable (PRD §6.6).
 */
@Composable
fun JournalScreen(
    modifier: Modifier = Modifier,
    viewModel: JournalViewModel = hiltViewModel(),
) {
    var pickerOpen by remember { mutableStateOf(false) }
    val date by viewModel.date.collectAsStateWithLifecycle()
    val entry by viewModel.entry.collectAsStateWithLifecycle()
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    LaunchedEffect(date, entry) {
        viewModel.prefillIfNeeded(date, entry)
    }
    val isToday = viewModel.isCurrentDay(date)

    // DinsDropdown renders plain strings, so resolve key<->label maps here in
    // composable scope; state itself stays on stable keys.
    val energyLabelByKey = ENERGY_ORDER.associateWith { energyLabel(it) }
    val energyKeyByLabel = energyLabelByKey.entries.associate { (k, v) -> v to k }
    val sleepLabelByKey = JournalViewModel.SLEEP_BUCKET_LABELS.associateWith { sleepBucketLabel(it) }
    val sleepKeyByLabel = sleepLabelByKey.entries.associate { (k, v) -> v to k }
    val symptomById = catalog.associateBy { it.id }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.journal_eyebrow),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.6.sp,
                    color = currentHomePalette.eyebrow,
                )
                Text(
                    stringResource(R.string.journal_title),
                    fontFamily = DinsSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 25.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    DinsGhostButton(onClick = { viewModel.changeDate(date.minusDays(1)) }) {
                        Text("‹", fontSize = 16.sp)
                    }
                    Text(
                        date.format(HEADER_DATE),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { pickerOpen = true },
                    )
                    DinsGhostButton(onClick = { viewModel.changeDate(date.plusDays(1)) }) {
                        Text("›", fontSize = 16.sp)
                    }
                }
            }
            JournalPill(if (isToday) stringResource(R.string.journal_today) else date.format(SHORT_DATE))
        }

        JournalCard {
            Text(stringResource(R.string.journal_mood_title), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            ChipFlow(
                options = JournalViewModel.MOODS,
                label = { mood -> (MOOD_EMOJI[mood]?.let { "$it " } ?: "") + moodLabel(mood) },
                selected = setOfNotNull(viewModel.mood),
                onToggle = viewModel::toggleMood,
            )
        }

        JournalCard {
            Text(stringResource(R.string.journal_symptoms_title), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            if (catalog.isEmpty()) {
                Text(stringResource(R.string.journal_symptoms_loading), style = MaterialTheme.typography.bodyMedium)
            } else {
                ChipFlow(
                    options = catalog.map { it.id } + NONE_KEY,
                    label = { id ->
                        if (id == NONE_KEY) {
                            noneLabel()
                        } else {
                            systemSymptomLabel(id, fallback = symptomById[id]?.nameKey ?: id)
                        }
                    },
                    selected = if (viewModel.selectedSymptoms.isEmpty()) {
                        setOf(NONE_KEY)
                    } else {
                        viewModel.selectedSymptoms
                    },
                    onToggle = { id ->
                        if (id == NONE_KEY) {
                            viewModel.clearSymptoms()
                        } else {
                            viewModel.toggleSymptom(id)
                        }
                    },
                )
            }
            Text(stringResource(R.string.journal_severity_title), style = MaterialTheme.typography.titleSmall)
            ChipFlow(
                options = JournalViewModel.SEVERITY_LEVELS.keys.toList(),
                label = { key -> severityLabel(key) },
                selected = setOfNotNull(viewModel.severityKey()),
                onToggle = viewModel::setSeverity,
            )
        }

        JournalCard {
            Text(stringResource(R.string.journal_day_title), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DinsDropdown(
                    label = stringResource(R.string.journal_energy_label),
                    options = ENERGY_ORDER.map { energyLabelByKey.getValue(it) },
                    selected = viewModel.energyLabel?.let { energyLabelByKey[it] },
                    onSelect = { label -> energyKeyByLabel[label]?.let(viewModel::setEnergy) },
                    modifier = Modifier.weight(1f),
                )
                DinsDropdown(
                    label = stringResource(R.string.journal_sleep_label),
                    options = JournalViewModel.SLEEP_BUCKET_LABELS.map { sleepLabelByKey.getValue(it) },
                    selected = viewModel.sleepBucket?.let { sleepLabelByKey[it] },
                    onSelect = { label -> sleepKeyByLabel[label]?.let(viewModel::selectSleepBucket) },
                    modifier = Modifier.weight(1f),
                )
            }
            Text(stringResource(R.string.journal_notes_label), style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = viewModel.notes,
                onValueChange = viewModel::updateNotes,
                placeholder = { Text(stringResource(R.string.journal_notes_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
        }

        viewModel.infoCode?.let { code ->
            Text(
                when (code) {
                    "need_input" -> stringResource(R.string.journal_need_input)
                    else -> stringResource(R.string.journal_saved_for, date.format(SHORT_DATE))
                },
                color = MaterialTheme.colorScheme.primary,
            )
        }
        DinsPrimaryButton(
            text = if (isToday) {
                stringResource(R.string.journal_save_today)
            } else {
                stringResource(R.string.journal_save)
            },
            onClick = { scope.launch { viewModel.save() } },
            modifier = Modifier.fillMaxWidth(),
        )
    }
    // Calendar jump-to-date; ‹ › arrows stay for adjacent days.
    if (pickerOpen) {
        DatePickerDialogHost(
            current = date,
            onConfirm = {
                viewModel.changeDate(it)
                pickerOpen = false
            },
            onDismiss = { pickerOpen = false },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipFlow(
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    label: @Composable (String) -> String = { it },
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = option in selected,
                onClick = { onToggle(option) },
                label = { Text(label(option), fontSize = 10.sp) },
            )
        }
    }
}


@Composable
private fun JournalCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DinsCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, currentHomePalette.cardBorder),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun JournalPill(text: String) {
    val palette = currentHomePalette
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(palette.pillBg)
            .padding(horizontal = 9.dp, vertical = 6.dp),
    ) {
        Text(
            text,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = palette.pillText,
        )
    }
}
