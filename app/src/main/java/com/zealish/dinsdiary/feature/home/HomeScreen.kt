package com.zealish.dinsdiary.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.zealish.dinsdiary.domain.cycle.CycleOverview
import com.zealish.dinsdiary.domain.cycle.CyclePhase
import com.zealish.dinsdiary.ui.components.DinsSecondaryButton
import com.zealish.dinsdiary.ui.theme.DinsCardShape
import com.zealish.dinsdiary.ui.theme.DinsSerif
import com.zealish.dinsdiary.ui.theme.currentHomePalette
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Home dashboard (PRD §6.2), layout and styling per mockup.html:
 * uppercase eyebrow + serif greeting, soft-gradient current-cycle card,
 * next-period estimate card, observed stat tiles, wrapping choice pills,
 * secondary quick-action buttons. Future dates are always labeled
 * estimates; recorded bleeding is fact.
 */
@Composable
fun HomeScreen(
    onOpenCalendar: () -> Unit,
    onOpenJournal: () -> Unit,
    onLogPeriod: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading || state.overview == null) {
        Column(modifier.fillMaxSize().padding(24.dp)) {
            CircularProgressIndicator()
        }
        return
    }
    val overview = state.overview ?: return
    val name = state.displayName?.takeIf { it.isNotBlank() }?.let { ", $it" } ?: ""

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        GreetingHeader(name = name, onOpenProfile = onOpenProfile, overview = overview)
        CurrentCycleCard(overview = overview)
        NextPeriodCard(overview = overview, onOpenCalendar = onOpenCalendar)
        StatsRow(overview = overview)
        MoodCard(onOpenJournal = onOpenJournal)
        QuickActionsCard(onLogPeriod = onLogPeriod, onOpenJournal = onOpenJournal)

        Text(
            stringResource(R.string.home_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GreetingHeader(name: String, onOpenProfile: () -> Unit, overview: CycleOverview) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                overview.today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()))
                    .uppercase(Locale.getDefault()),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.6.sp,
                color = currentHomePalette.eyebrow,
            )
            Text(
                stringResource(R.string.home_greeting, name),
                fontFamily = DinsSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 29.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(currentHomePalette.roundBg)
                .clickable(onClick = onOpenProfile),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "☺",
                fontSize = 17.sp,
                color = currentHomePalette.roundIcon,
            )
        }
    }
}

@Composable
private fun CurrentCycleCard(overview: CycleOverview) {
    val palette = currentHomePalette
    val isEstimatedPhase = !overview.isBleedingToday && !overview.insufficientData
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DinsCardShape)
            .background(Brush.linearGradient(listOf(palette.softTop, palette.softBottom)))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Pill(
                    text = overview.cycleDay?.let { stringResource(R.string.home_cycle_day, it) }
                        ?: stringResource(R.string.home_getting_started),
                )
                Text(
                    stringResource(R.string.home_current_cycle),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                phaseTitle(overview.phase),
                fontFamily = DinsSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (isEstimatedPhase) {
                Pill(text = stringResource(R.string.home_estimated))
            }
            Text(
                phaseSubtitle(overview),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val length = overview.estimate.estimatedCycleLengthDays
            val day = overview.cycleDay
            if (day != null && length != null && length > 0) {
                CycleProgress(fraction = day.toFloat() / length.toFloat())
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        periodEndedLine(overview),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        stringResource(R.string.home_cycle_day_progress, day, length),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun phaseTitle(phase: CyclePhase): String = when (phase) {
    CyclePhase.MENSTRUATION -> stringResource(R.string.home_phase_menstruation)
    CyclePhase.UNKNOWN -> stringResource(R.string.home_phase_unknown)
    CyclePhase.OVULATION -> stringResource(R.string.home_phase_ovulation)
    CyclePhase.FERTILE_WINDOW -> stringResource(R.string.home_phase_fertile)
    CyclePhase.FOLLICULAR -> stringResource(R.string.home_phase_follicular)
    CyclePhase.LUTEAL -> stringResource(R.string.home_phase_luteal)
}

@Composable
private fun phaseSubtitle(overview: CycleOverview): String = when {
    overview.isBleedingToday -> stringResource(R.string.home_phase_bleeding)
    overview.insufficientData -> stringResource(R.string.home_phase_insufficient)
    else -> stringResource(R.string.home_phase_default)
}

@Composable
private fun periodEndedLine(overview: CycleOverview): String {
    val ended = overview.lastPeriodEnd
    return when {
        ended != null ->
            stringResource(
                R.string.home_period_ended,
                ended.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())),
            )
        overview.isBleedingToday -> stringResource(R.string.home_period_in_progress)
        else -> stringResource(R.string.home_based_on_records)
    }
}

@Composable
private fun CycleProgress(fraction: Float) {
    val palette = currentHomePalette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(RoundedCornerShape(50))
            .background(palette.trackBg),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .background(Brush.horizontalGradient(listOf(palette.progStart, palette.progEnd))),
        )
    }
}

@Composable
private fun NextPeriodCard(overview: CycleOverview, onOpenCalendar: () -> Unit) {
    val palette = currentHomePalette
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DinsCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, palette.cardBorder),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.home_next_period),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                if (overview.estimate.predictedNextStart != null) {
                    Pill(text = stringResource(R.string.home_estimated))
                }
            }
            val next = overview.estimate.predictedNextStart
            if (next == null) {
                Text(
                    stringResource(R.string.home_no_history),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        next.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())),
                        fontFamily = DinsSerif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Column(modifier = Modifier.padding(bottom = 4.dp)) {
                        Text(
                            daysToGoLine(overview, next),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            stringResource(R.string.home_based_on_history),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                DinsSecondaryButton(onClick = onOpenCalendar, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.home_view_calendar))
                }
            }
        }
    }
}

@Composable
private fun daysToGoLine(overview: CycleOverview, next: java.time.LocalDate): String {
    val days = ChronoUnit.DAYS.between(overview.today, next)
    return when {
        days < 0 -> stringResource(R.string.home_overdue)
        days == 0L -> stringResource(R.string.home_expected_today)
        days == 1L -> stringResource(R.string.home_day_to_go)
        else -> stringResource(R.string.home_days_to_go, days)
    }
}

@Composable
private fun StatsRow(overview: CycleOverview) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        val length = overview.estimate.estimatedCycleLengthDays
        val cycles = overview.estimate.validIntervals.size
        StatCard(
            label = stringResource(R.string.home_stat_cycle_avg),
            value = length?.let { stringResource(R.string.home_stat_days_value, it) } ?: "–",
            footnote = if (cycles > 0) {
                if (cycles == 1) {
                    stringResource(R.string.home_stat_based_cycles_one)
                } else {
                    stringResource(R.string.home_stat_based_cycles_other, cycles)
                }
            } else {
                stringResource(R.string.home_stat_based_settings)
            },
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = stringResource(R.string.home_stat_period_duration),
            value = overview.averagePeriodLengthDays?.let { stringResource(R.string.home_stat_days_value, it) }
                ?: "–",
            footnote = if (overview.periodAverageObserved) {
                stringResource(R.string.home_stat_recent_avg)
            } else {
                stringResource(R.string.home_stat_from_settings)
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, footnote: String, modifier: Modifier = Modifier) {
    val palette = currentHomePalette
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(palette.statBg)
            .border(1.dp, palette.statBorder, RoundedCornerShape(15.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            fontFamily = DinsSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(footnote, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private val MOOD_LABELS = listOf(
    R.string.home_mood_good,
    R.string.home_mood_calm,
    R.string.home_mood_tired,
    R.string.home_mood_crampy,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoodCard(onOpenJournal: () -> Unit) {
    val palette = currentHomePalette
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DinsCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, palette.cardBorder),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.home_mood_title),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = stringResource(R.string.home_mood_log_today),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onOpenJournal),
                )
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                MOOD_LABELS.forEach { moodRes ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(palette.choiceBg)
                            .border(1.dp, palette.choiceBorder, RoundedCornerShape(50))
                            .clickable(onClick = onOpenJournal)
                            .padding(horizontal = 11.dp, vertical = 8.dp),
                    ) {
                        Text(stringResource(moodRes), fontSize = 11.sp, color = palette.choiceText)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsCard(onLogPeriod: () -> Unit, onOpenJournal: () -> Unit) {
    val palette = currentHomePalette
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DinsCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, palette.cardBorder),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(R.string.home_quick_actions),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DinsSecondaryButton(onClick = onLogPeriod, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.home_quick_log_period))
                }
                DinsSecondaryButton(onClick = onOpenJournal, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.home_quick_journal))
                }
            }
        }
    }
}

@Composable
private fun Pill(text: String) {
    val palette = currentHomePalette
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(palette.pillBg)
            .padding(horizontal = 9.dp, vertical = 6.dp),
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = palette.pillText)
    }
}
