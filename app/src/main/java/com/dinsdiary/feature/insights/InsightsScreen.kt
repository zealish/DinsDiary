package com.dinsdiary.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dinsdiary.R
import com.dinsdiary.domain.model.MOOD_EMOJI
import com.dinsdiary.domain.usecase.InsightsData
import com.dinsdiary.ui.i18n.moodLabel
import com.dinsdiary.ui.i18n.systemSymptomLabel
import com.dinsdiary.ui.theme.DinsCardShape
import com.dinsdiary.ui.theme.DinsSerif
import com.dinsdiary.ui.theme.currentHomePalette

/**
 * Observed-history summaries (PRD §6.8, `mockup.html` insights screen).
 * Observed stats only — no diagnostic conclusions, estimates labeled.
 */
@Composable
fun InsightsScreen(
    onOpenFertility: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InsightsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading || state.data == null) {
        Column(modifier.fillMaxSize().padding(24.dp)) {
            CircularProgressIndicator()
        }
        return
    }
    val data = state.data ?: return

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        InsightsHeader(onOpenFertility = onOpenFertility)
        StatsRow(data = data)
        HistoryCard(data = data)
        DurationCard(data = data)
        PatternCard(
            title = stringResource(R.string.ins_mood_title),
            rows = data.moodCounts.entries.sortedByDescending { it.value }.take(3)
                .mapIndexed { index, (mood, count) ->
                    PatternRow(
                        icon = MOOD_EMOJI[mood] ?: "☺",
                        title = moodLabel(mood),
                        sub = if (index == 0) {
                            stringResource(R.string.ins_top_mood)
                        } else {
                            stringResource(R.string.ins_logged_sub)
                        },
                        count = count,
                        lav = index > 0,
                    )
                },
            empty = stringResource(R.string.ins_no_moods),
        )
        PatternCard(
            title = stringResource(R.string.ins_symptom_title),
            rows = data.symptomCounts.entries.sortedByDescending { it.value }.take(3)
                .mapIndexed { index, (id, count) ->
                    PatternRow(
                        icon = "✿",
                        title = systemSymptomLabel(id, state.symptomNames[id] ?: id),
                        sub = if (index == 0) {
                            stringResource(R.string.ins_top_symptom)
                        } else {
                            stringResource(R.string.ins_logged_sub)
                        },
                        count = count,
                        lav = index > 0,
                    )
                },
            empty = stringResource(R.string.ins_no_symptoms),
        )
        DisclaimerNote()
    }
}

@Composable
private fun InsightsHeader(onOpenFertility: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(R.string.ins_eyebrow),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.6.sp,
                color = currentHomePalette.eyebrow,
            )
            Text(
                stringResource(R.string.ins_title),
                fontFamily = DinsSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 25.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                stringResource(R.string.ins_subtitle),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val palette = currentHomePalette
        val fertilityLabel = stringResource(R.string.ins_fertility_cd)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(palette.roundBg)
                .clickable(onClick = onOpenFertility)
                .semantics {
                    contentDescription = fertilityLabel
                    role = Role.Button
                },
            contentAlignment = Alignment.Center,
        ) {
            Text("⌁", fontSize = 17.sp, color = palette.roundIcon)
        }
    }
}

@Composable
private fun StatsRow(data: InsightsData) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatTile(
            label = stringResource(R.string.ins_avg_cycle),
            value = data.cycleStats?.let { formatDays(it.average) } ?: "–",
            footnote = data.cycleStats?.let {
                if (it.count == 1) {
                    stringResource(R.string.ins_cycles_recorded_one, it.count)
                } else {
                    stringResource(R.string.ins_cycles_recorded_many, it.count)
                }
            } ?: stringResource(R.string.ins_no_cycles),
            modifier = Modifier.weight(1f),
        )
        StatTile(
            label = stringResource(R.string.ins_avg_period),
            value = data.averagePeriodDays?.let { formatDays(it) } ?: "–",
            footnote = if (data.periodsCount > 0) {
                if (data.periodsCount == 1) {
                    stringResource(R.string.ins_periods_last_one, data.periodsCount)
                } else {
                    stringResource(R.string.ins_periods_last_many, data.periodsCount)
                }
            } else {
                stringResource(R.string.ins_no_periods)
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatTile(label: String, value: String, footnote: String, modifier: Modifier = Modifier) {
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

@Composable
private fun HistoryCard(data: InsightsData) {
    InsightCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.ins_history_title),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            if (data.cycleIntervals.isNotEmpty()) {
                DinsPill(text = stringResource(R.string.ins_observed))
            }
        }
        if (data.cycleIntervals.isEmpty()) {
            Text(
                stringResource(R.string.ins_history_empty),
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    val palette = currentHomePalette
                    data.cycleIntervals.forEach { days ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 5.dp)
                                    .height((days * 3).dp.coerceAtMost(96.dp))
                                    .clip(RoundedCornerShape(8.dp, 8.dp, 2.dp, 2.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(palette.progStart, palette.progEnd),
                                        ),
                                    ),
                            )
                            Text(
                                stringResource(R.string.ins_bar_days, days),
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                HorizontalDivider(
                    thickness = 1.dp,
                    color = currentHomePalette.cardBorder,
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    data.cycleIntervalLabels.forEach { month ->
                        Text(
                            month,
                            modifier = Modifier.weight(1f),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DurationCard(data: InsightsData) {
    InsightCard {
        Text(
            stringResource(R.string.ins_duration_title),
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                data.averagePeriodDays?.let { formatDays(it) } ?: "–",
                fontFamily = DinsSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                if (data.periodsCount > 0) {
                    if (data.periodsCount == 1) {
                        stringResource(R.string.ins_duration_avg_one, data.periodsCount)
                    } else {
                        stringResource(R.string.ins_duration_avg_many, data.periodsCount)
                    }
                } else {
                    stringResource(R.string.ins_duration_empty)
                },
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }
        data.averagePeriodDays?.let { avg ->
            val palette = currentHomePalette
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.trackBg),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((avg / 7f).toFloat().coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(palette.progStart, palette.progEnd),
                            ),
                        ),
                )
            }
        }
    }
}

private data class PatternRow(
    val icon: String,
    val title: String,
    val sub: String,
    val count: Int,
    val lav: Boolean,
)

@Composable
private fun PatternCard(title: String, rows: List<PatternRow>, empty: String) {
    InsightCard {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
        if (rows.isEmpty()) {
            Text(empty, style = MaterialTheme.typography.bodyMedium)
        } else {
            Column {
                rows.forEachIndexed { index, row ->
                    if (index > 0) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = currentHomePalette.cardBorder,
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 11.dp),
                        horizontalArrangement = Arrangement.spacedBy(11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(currentHomePalette.secondaryBg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(row.icon, fontSize = 17.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                row.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                row.sub,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        DinsPill(
                            text = if (row.count == 1) {
                                stringResource(R.string.ins_logs_one, row.count)
                            } else {
                                stringResource(R.string.ins_logs_many, row.count)
                            },
                            lav = row.lav,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DisclaimerNote() {
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
            stringResource(R.string.ins_disclaimer),
            fontSize = 10.sp,
            lineHeight = 15.sp,
            color = palette.noteText,
        )
    }
}

@Composable
private fun InsightCard(content: @Composable () -> Unit) {
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
private fun DinsPill(text: String, lav: Boolean = false) {
    val palette = currentHomePalette
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(if (lav) palette.lavBg else palette.pillBg)
            .padding(horizontal = 9.dp, vertical = 6.dp),
    ) {
        Text(
            text,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (lav) palette.lavText else palette.pillText,
        )
    }
}

@Composable
private fun formatDays(value: Double): String {
    val number = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
    return if (value == 1.0) {
        stringResource(R.string.ins_days_one, number)
    } else {
        stringResource(R.string.ins_days_many, number)
    }
}
