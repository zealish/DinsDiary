@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.zealish.dinsdiary.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import com.zealish.dinsdiary.ui.components.DatePickerField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zealish.dinsdiary.R
import kotlinx.coroutines.launch

/**
 * Three-step setup flow (PRD §6.1, mockup): profile, cycle basics, privacy.
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val form = viewModel.form
    val scope = rememberCoroutineScope()

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(R.string.onb_step, viewModel.step, 3),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        when (viewModel.step) {
            1 -> ProfileStep(
                form = form,
                onChange = viewModel::update,
                onNext = viewModel::next,
                onSkip = { scope.launch { viewModel.skip(); onFinished() } },
            )
            2 -> CycleStep(
                form = form,
                onChange = viewModel::update,
                onNext = viewModel::next,
                onBack = viewModel::back,
            )
            else -> PrivacyStep(
                form = form,
                error = viewModel.errorMessage,
                onChange = viewModel::update,
                onBack = viewModel::back,
                onFinish = { scope.launch { if (viewModel.finish()) onFinished() } },
            )
        }
    }
}

@Composable
private fun ProfileStep(
    form: OnboardingForm,
    onChange: (OnboardingForm) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    Text(stringResource(R.string.onb_profile_title), style = MaterialTheme.typography.headlineMedium)
    Text(
        stringResource(R.string.onb_profile_sub),
        style = MaterialTheme.typography.bodyMedium,
    )
    TextField(
        value = form.name,
        onValueChange = { onChange(form.copy(name = it)) },
        label = { Text(stringResource(R.string.onb_name)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    DatePickerField(
        value = form.dateOfBirth,
        onValueChange = { onChange(form.copy(dateOfBirth = it)) },
        label = stringResource(R.string.onb_dob),
        modifier = Modifier.fillMaxWidth(),
    )
    Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.onb_continue))
    }
    TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.onb_skip))
    }
}

@Composable
private fun CycleStep(
    form: OnboardingForm,
    onChange: (OnboardingForm) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    Text(stringResource(R.string.onb_cycle_title), style = MaterialTheme.typography.headlineMedium)
    Text(
        stringResource(R.string.onb_cycle_sub),
        style = MaterialTheme.typography.bodyMedium,
    )
    Text(stringResource(R.string.onb_avg_length, form.cycleLengthDays))
    Slider(
        value = form.cycleLengthDays.toFloat(),
        onValueChange = { onChange(form.copy(cycleLengthDays = it.toInt())) },
        valueRange = 20f..40f,
        steps = 19,
    )
    Text(stringResource(R.string.onb_period_label))
    ChipRow(
        options = (3..7).map { it to stringResource(R.string.onb_days, it) },
        selected = form.periodLengthDays,
        onSelect = { onChange(form.copy(periodLengthDays = it)) },
    )
    DatePickerField(
        value = form.lastPeriodStart,
        onValueChange = { onChange(form.copy(lastPeriodStart = it)) },
        label = stringResource(R.string.onb_last_period),
        modifier = Modifier.fillMaxWidth(),
    )
    Text(stringResource(R.string.onb_luteal))
    ChipRow(
        options = listOf(12, 13, 14, 15, 16).map { it to stringResource(R.string.onb_days, it) },
        selected = form.lutealPhaseDays,
        onSelect = { onChange(form.copy(lutealPhaseDays = it)) },
    )
    Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.onb_continue))
    }
    OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.onb_back))
    }
}

@Composable
private fun PrivacyStep(
    form: OnboardingForm,
    error: String?,
    onChange: (OnboardingForm) -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit,
) {
    Text(stringResource(R.string.onb_privacy_title), style = MaterialTheme.typography.headlineMedium)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.onb_private_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.onb_private_body),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.selectable(
            selected = form.consent,
            onClick = { onChange(form.copy(consent = !form.consent)) },
        ),
    ) {
        Checkbox(checked = form.consent, onCheckedChange = { onChange(form.copy(consent = it)) })
        Text(stringResource(R.string.onb_consent))
    }
    if (error != null) {
        Text(error, color = MaterialTheme.colorScheme.error)
    }
    Button(onClick = onFinish, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.onb_finish))
    }
    OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.onb_back))
    }
}

@Composable
private fun <T> ChipRow(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (value, label) ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelect(value) },
                label = { Text(label) },
            )
        }
    }
}
