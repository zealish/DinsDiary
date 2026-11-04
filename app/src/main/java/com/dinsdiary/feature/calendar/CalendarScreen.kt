package com.dinsdiary.feature.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dinsdiary.R
import com.dinsdiary.ui.components.DinsGhostButton
import com.dinsdiary.ui.components.DinsPrimaryButton
import com.dinsdiary.ui.theme.DinsCardShape
import com.dinsdiary.ui.theme.DinsSerif
import com.dinsdiary.ui.theme.currentHomePalette
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Mockup day radius (`.day` 11px). */
private val DayShape = RoundedCornerShape(11.dp)

/**
 * Monthly calendar (PRD §6.4), layout and styling per the `mockup.html`
 * calendar screen. Actual vs predicted stay distinct by fill style, border
 * style, and text — never color alone.
 */
@Composable
fun CalendarScreen(
    onLogPeriod: () -> Unit,
    onOpenJournal: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        CalendarHeader(month = state.month, onPrevious = viewModel::previousMonth)
        MonthCard(
            state = state,
            onSelect = viewModel::select,
            onPrevious = viewModel::previousMonth,
            onNext = viewModel::nextMonth,
        )
        SelectedDayCard(state = state, onOpenJournal = onOpenJournal)
        DinsPrimaryButton(
            text = stringResource(R.string.cal_log_period),
            onClick = onLogPeriod,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CalendarHeader(month: YearMonth, onPrevious: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(R.string.cal_eyebrow),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.6.sp,
                color = currentHomePalette.eyebrow,
            )
            Text(
                month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                fontFamily = DinsSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 25.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                stringResource(R.string.cal_tap_hint),
                fontSize = 11.sp,
                color = currentCalendarPalette.mutedText,
            )
        }
        RoundNavButton(
            text = "‹",
            description = stringResource(R.string.cal_prev_month),
            onClick = onPrevious,
        )
    }
}

@Composable
private fun RoundNavButton(text: String, description: String, onClick: () -> Unit) {
    val palette = currentHomePalette
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(palette.roundBg)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = description
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 17.sp, color = palette.roundIcon)
    }
}

@Composable
private fun MonthCard(
    state: CalendarUiState,
    onSelect: (LocalDate) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DinsCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, currentHomePalette.cardBorder),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DinsGhostButton(onClick = onPrevious) { Text("‹", fontSize = 16.sp) }
                Text(
                    state.month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                DinsGhostButton(onClick = onNext) { Text("›", fontSize = 16.sp) }
            }
            MonthGrid(state = state, onSelect = onSelect)
            LegendRow()
        }
    }
}

@Composable
private fun MonthGrid(state: CalendarUiState, onSelect: (LocalDate) -> Unit) {
    // Mockup weekday order: Sunday-first.
    val headers = listOf(
        R.string.cal_weekday_sun,
        R.string.cal_weekday_mon,
        R.string.cal_weekday_tue,
        R.string.cal_weekday_wed,
        R.string.cal_weekday_thu,
        R.string.cal_weekday_fri,
        R.string.cal_weekday_sat,
    )
    Row(modifier = Modifier.fillMaxWidth()) {
        headers.forEach {
            Text(
                stringResource(it),
                modifier = Modifier.weight(1f),
                fontSize = 9.sp,
                color = currentCalendarPalette.headText,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
    val first = state.month.atDay(1)
    // Sunday == 7 maps to zero leading blanks.
    val leading = first.dayOfWeek.value % 7
    val days = (1..state.month.lengthOfMonth()).map { state.month.atDay(it) }
    val cells: List<LocalDate?> = List(leading) { null } + days
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        cells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                week.forEach { date ->
                    if (date == null) {
                        Box(modifier = Modifier.weight(1f).aspectRatioSquare())
                    } else {
                        DayCell(
                            date = date,
                            state = state,
                            onSelect = onSelect,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                repeat(7 - week.size) {
                    Box(modifier = Modifier.weight(1f).aspectRatioSquare())
                }
            }
        }
    }
}

/** Square cells for the 7-column grid. */
private fun Modifier.aspectRatioSquare(): Modifier = aspectRatio(1f)

@Composable
private fun DayCell(
    date: LocalDate,
    state: CalendarUiState,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = currentCalendarPalette
    val marks = state.marks
    val bleeding = date in marks.bleedingDates
    val predicted = date in marks.predictedDates
    val fertile = date in marks.fertileDates || date == marks.ovulationDate
    val selected = date == state.selected
    val isToday = date == state.today

    val recordedSuffix = stringResource(R.string.cal_desc_recorded)
    val predictedSuffix = stringResource(R.string.cal_desc_predicted)
    val ovulationSuffix = stringResource(R.string.cal_desc_ovulation)
    val fertileSuffix = stringResource(R.string.cal_desc_fertile)
    val todaySuffix = stringResource(R.string.cal_desc_today)
    val selectedSuffix = stringResource(R.string.cal_desc_selected)
    val description = buildString {
        append(date.dayOfMonth)
        when {
            bleeding -> append(recordedSuffix)
            predicted -> append(predictedSuffix)
            date == marks.ovulationDate -> append(ovulationSuffix)
            fertile -> append(fertileSuffix)
        }
        if (isToday) append(todaySuffix)
        if (selected) append(selectedSuffix)
    }
    // Mockup: solid fill = recorded, dashed outline = predicted,
    // lavender = fertile, plum fill = selected, plum ring = today.
    val background = when {
        selected -> palette.selectedBg
        bleeding -> palette.periodBg
        predicted -> palette.predBg
        fertile -> palette.fertileBg
        else -> Color.Transparent
    }
    val textColor = when {
        selected -> palette.selectedText
        bleeding -> palette.periodText
        predicted -> palette.predText
        fertile -> palette.fertileText
        else -> palette.dayText
    }
    Box(
        modifier = modifier
            .aspectRatioSquare()
            .clip(DayShape)
            .background(background)
            .then(
                when {
                    selected || !(predicted || isToday) -> Modifier
                    predicted -> Modifier.dashedBorder(1.dp, palette.predBorder, 11.dp)
                    else -> Modifier.border(2.dp, palette.todayOutline, DayShape)
                },
            )
            .clickable(onClick = { onSelect(date) })
            .semantics {
                contentDescription = description
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            date.dayOfMonth.toString(),
            fontSize = 11.sp,
            fontWeight = if (bleeding || selected) FontWeight.ExtraBold else FontWeight.Normal,
            color = textColor,
        )
    }
}

/** Mockup dashed predicted-day outline (`.day.pred`). */
private fun Modifier.dashedBorder(width: Dp, color: Color, radius: Dp): Modifier = drawBehind {
    val stroke = width.toPx()
    val interval = floatArrayOf(4.dp.toPx(), 3.dp.toPx())
    drawRoundRect(
        color = color,
        topLeft = Offset(stroke / 2, stroke / 2),
        size = Size(size.width - stroke, size.height - stroke),
        cornerRadius = CornerRadius(radius.toPx(), radius.toPx()),
        style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(interval)),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LegendRow() {
    val palette = currentCalendarPalette
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LegendItem(
            swatch = {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(palette.periodBg),
                )
            },
            text = stringResource(R.string.cal_legend_recorded),
        )
        LegendItem(
            swatch = {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(palette.predBg)
                        .dashedBorder(1.dp, palette.predBorder, 3.dp),
                )
            },
            text = stringResource(R.string.cal_legend_predicted),
        )
        LegendItem(
            swatch = {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(palette.fertileBg),
                )
            },
            text = stringResource(R.string.cal_legend_fertile),
        )
    }
}

@Composable
private fun LegendItem(swatch: @Composable () -> Unit, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        swatch()
        Text(text, fontSize = 9.sp, color = currentCalendarPalette.mutedText)
    }
}

@Composable
private fun SelectedDayCard(state: CalendarUiState, onOpenJournal: () -> Unit) {
    val palette = currentCalendarPalette
    val date = state.selected
    val marks = state.marks
    val kind = when {
        date in marks.bleedingDates -> DayKind.RECORDED
        date in marks.predictedDates -> DayKind.PREDICTED
        date == marks.ovulationDate -> DayKind.OVULATION
        date in marks.fertileDates -> DayKind.FERTILE
        else -> DayKind.NONE
    }
    val statusTitle = when (kind) {
        DayKind.RECORDED -> stringResource(R.string.cal_status_recorded)
        DayKind.PREDICTED -> stringResource(R.string.cal_status_predicted)
        DayKind.OVULATION -> stringResource(R.string.cal_status_ovulation)
        DayKind.FERTILE -> stringResource(R.string.cal_status_fertile)
        DayKind.NONE -> stringResource(R.string.cal_status_none)
    }
    val statusSubtitle = when (kind) {
        DayKind.RECORDED -> stringResource(R.string.cal_subtitle_recorded)
        DayKind.PREDICTED -> stringResource(R.string.cal_subtitle_predicted)
        DayKind.OVULATION -> stringResource(R.string.cal_subtitle_ovulation)
        DayKind.FERTILE -> stringResource(R.string.cal_subtitle_fertile)
        DayKind.NONE -> stringResource(R.string.cal_subtitle_none)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DinsCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, currentHomePalette.cardBorder),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Text(
                stringResource(
                    R.string.cal_selected_date,
                    date.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())),
                ),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            DetailRow(glyph = "☼", title = statusTitle, subtitle = statusSubtitle)
            val journalTitle = if (state.hasJournal) {
                stringResource(R.string.cal_journal_saved)
            } else {
                stringResource(R.string.cal_journal_empty)
            }
            val journalSubtitle = if (state.hasJournal) {
                stringResource(R.string.cal_journal_saved_sub)
            } else {
                stringResource(R.string.cal_journal_empty_sub)
            }
            val journalAction = if (state.hasJournal) {
                stringResource(R.string.cal_open)
            } else {
                stringResource(R.string.cal_add)
            }
            DetailRow(
                glyph = "✎",
                title = journalTitle,
                subtitle = journalSubtitle,
                actionLabel = journalAction,
                onAction = onOpenJournal,
            )
            if (kind == DayKind.PREDICTED || kind == DayKind.FERTILE || kind == DayKind.OVULATION) {
                Text(
                    stringResource(R.string.cal_estimate_note),
                    fontSize = 10.sp,
                    color = palette.mutedText,
                )
            }
        }
    }
}

private enum class DayKind { RECORDED, PREDICTED, OVULATION, FERTILE, NONE }

@Composable
private fun DetailRow(
    glyph: String,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val palette = currentHomePalette
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(palette.secondaryBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(glyph, fontSize = 15.sp, color = palette.secondaryText)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 9.sp, color = currentCalendarPalette.mutedText)
        }
        if (actionLabel != null && onAction != null) {
            DinsGhostButton(onClick = onAction) {
                Text(actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
