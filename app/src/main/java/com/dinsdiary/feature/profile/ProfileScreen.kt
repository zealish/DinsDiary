package com.dinsdiary.feature.profile
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dinsdiary.R
import com.dinsdiary.data.prefs.AppPrefs
import com.dinsdiary.ui.components.DinsDropdown
import com.dinsdiary.ui.components.DinsGhostButton
import com.dinsdiary.ui.components.DinsPrimaryButton
import com.dinsdiary.ui.components.DinsSecondaryButton
import com.dinsdiary.ui.theme.DinsCardShape
import com.dinsdiary.ui.theme.DinsSerif
import com.dinsdiary.ui.theme.currentHomePalette
import kotlinx.coroutines.launch
private val languageNames = mapOf(
    AppPrefs.LANG_ID to "Bahasa Indonesia",
    AppPrefs.LANG_EN to "English",
)

@Composable
private fun errorText(code: String): String = when (code) {
    "cycle_range" -> stringResource(R.string.pro_error_cycle)
    "period_range" -> stringResource(R.string.pro_error_period)
    else -> stringResource(R.string.pro_error_luteal)
}

/** Profile & settings (PRD §6.1, §6.10, §6.12, `mockup.html` profile screen). */
@Composable
fun ProfileScreen(
    onOpenReminders: () -> Unit,
    onOpenPrivacy: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current as? Activity

    if (state.isLoading || state.profile == null) {
        Column(modifier.fillMaxSize().padding(24.dp)) {
            CircularProgressIndicator()
        }
        return
    }
    val profile = state.profile ?: return
    val name = profile.displayName ?: stringResource(R.string.pro_default_name)
    val initial = profile.displayName?.trim()?.firstOrNull()?.uppercase() ?: "D"

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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.pro_eyebrow),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.6.sp,
                    color = currentHomePalette.eyebrow,
                )
                Text(
                    stringResource(R.string.pro_title),
                    fontFamily = DinsSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 25.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    stringResource(R.string.pro_subtitle),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Avatar(initial = initial, size = 38.dp, fontSize = 15.sp)
        }

        val palette = currentHomePalette
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(DinsCardShape)
                .background(Brush.linearGradient(listOf(palette.softTop, palette.softBottom)))
                .padding(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(initial = initial, size = 52.dp, fontSize = 20.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        stringResource(R.string.pro_identity_sub),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (viewModel.editing) {
                        TextField(
                            value = viewModel.form.name,
                            onValueChange = { viewModel.updateForm(viewModel.form.copy(name = it)) },
                            label = { Text(stringResource(R.string.pro_name_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
                }
                if (!viewModel.editing) {
                    DinsGhostButton(text = stringResource(R.string.pro_edit), onClick = { viewModel.startEditing(profile) })
                }
            }
        }

        ProfileCard {
            Text(
                stringResource(R.string.pro_prefs_title),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (viewModel.editing) {
                val form = viewModel.form
                TextField(
                    value = form.cycleLength,
                    onValueChange = { viewModel.updateForm(form.copy(cycleLength = it)) },
                    label = { Text(stringResource(R.string.pro_cycle_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                TextField(
                    value = form.periodLength,
                    onValueChange = { viewModel.updateForm(form.copy(periodLength = it)) },
                    label = { Text(stringResource(R.string.pro_period_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                TextField(
                    value = form.lutealPhase,
                    onValueChange = { viewModel.updateForm(form.copy(lutealPhase = it)) },
                    label = { Text(stringResource(R.string.pro_luteal_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                viewModel.errorCode?.let {
                    Text(errorText(it), color = MaterialTheme.colorScheme.error)
                }
                DinsPrimaryButton(
                    text = stringResource(R.string.pro_save),
                    onClick = { scope.launch { viewModel.save() } },
                    modifier = Modifier.fillMaxWidth(),
                )
                DinsGhostButton(
                    text = stringResource(R.string.pro_cancel),
                    onClick = viewModel::cancelEditing,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                PrefRow(
                    label = stringResource(R.string.pro_cycle_row),
                    sub = stringResource(R.string.pro_personal_pref),
                    value = profile.preferredCycleLengthDays?.let { stringResource(R.string.pro_days_value, it) } ?: "—",
                )
                PrefRow(
                    label = stringResource(R.string.pro_period_row),
                    sub = stringResource(R.string.pro_personal_pref),
                    value = profile.preferredPeriodLengthDays?.let { stringResource(R.string.pro_days_value, it) } ?: "—",
                )
                PrefRow(
                    label = stringResource(R.string.pro_luteal_row),
                    sub = stringResource(R.string.pro_default_assumption),
                    value = stringResource(R.string.pro_days_value, profile.lutealPhaseLengthDays),
                )
                DinsSecondaryButton(
                    text = stringResource(R.string.pro_edit_prefs),
                    onClick = { viewModel.startEditing(profile) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        ProfileCard {
            SettingRow(
                icon = "♧",
                title = stringResource(R.string.pro_reminders),
                sub = stringResource(R.string.pro_reminders_sub),
                onClick = onOpenReminders,
            )
            HorizontalDivider(color = palette.cardBorder)
            SettingRow(
                icon = "♢",
                title = stringResource(R.string.pro_privacy),
                sub = stringResource(R.string.pro_privacy_sub),
                onClick = onOpenPrivacy,
            )
            HorizontalDivider(color = palette.cardBorder)
            Row(
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconBox(icon = "☼")
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.pro_appearance),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        stringResource(R.string.pro_appearance_sub),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val themeSystem = stringResource(R.string.pro_theme_system)
                val themeLight = stringResource(R.string.pro_theme_light)
                val themeDark = stringResource(R.string.pro_theme_dark)
                DinsDropdown(
                    options = listOf(themeSystem, themeLight, themeDark),
                    selected = when (state.themeMode) {
                        AppPrefs.THEME_LIGHT -> themeLight
                        AppPrefs.THEME_DARK -> themeDark
                        else -> themeSystem
                    },
                    onSelect = { label ->
                        val mode = when (label) {
                            themeLight -> AppPrefs.THEME_LIGHT
                            themeDark -> AppPrefs.THEME_DARK
                            else -> AppPrefs.THEME_SYSTEM
                        }
                        scope.launch { viewModel.setTheme(mode) }
                    },
                    modifier = Modifier.width(126.dp),
                )
            }
            HorizontalDivider(color = palette.cardBorder)
            Row(
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconBox(icon = "🌐")
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.pro_language),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        stringResource(R.string.pro_language_sub),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DinsDropdown(
                    options = languageNames.values.toList(),
                    selected = languageNames[state.language],
                    onSelect = { label ->
                        val code = languageNames.entries.firstOrNull { it.value == label }?.key
                            ?: AppPrefs.LANG_ID
                        scope.launch {
                            viewModel.setLanguage(code)
                            activity?.recreate()
                        }
                    },
                    modifier = Modifier.width(170.dp),
                )
            }
        }

        viewModel.infoCode?.let {
            Text(stringResource(R.string.pro_saved), color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun Avatar(initial: String, size: androidx.compose.ui.unit.Dp, fontSize: androidx.compose.ui.unit.TextUnit) {
    val palette = currentHomePalette
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(palette.pillBg),
        contentAlignment = Alignment.Center,
    ) {
        Text(initial, fontSize = fontSize, fontWeight = FontWeight.Bold, color = palette.pillText)
    }
}

@Composable
private fun ProfileCard(content: @Composable () -> Unit) {
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
private fun PrefRow(label: String, sub: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                sub,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SettingRow(icon: String, title: String, sub: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBox(icon = icon)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                sub,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            "›",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun IconBox(icon: String) {
    val palette = currentHomePalette
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(palette.secondaryBg),
        contentAlignment = Alignment.Center,
    ) {
        Text(icon, fontSize = 17.sp, color = palette.secondaryText)
    }
}
