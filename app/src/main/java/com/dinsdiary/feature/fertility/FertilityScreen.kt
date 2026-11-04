package com.dinsdiary.feature.fertility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dinsdiary.R
import com.dinsdiary.domain.cycle.Confidence
import com.dinsdiary.domain.cycle.CycleEstimate
import com.dinsdiary.ui.theme.DinsCardShape
import com.dinsdiary.ui.theme.DinsSerif
import com.dinsdiary.ui.theme.currentHomePalette
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Calendar-based fertility estimates (PRD §6.7, `mockup.html` fertility screen).
 * Never ovulation confirmation, never contraception guidance.
 */
@Composable
fun FertilityScreen(
    modifier: Modifier = Modifier,
    viewModel: FertilityViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading || state.overview == null) {
        Column(modifier.fillMaxSize().padding(24.dp)) {
            CircularProgressIndicator()
        }
        return
    }
    val estimate = state.overview?.estimate ?: return
    val locale = Locale.getDefault()
    val shortFmt = DateTimeFormatter.ofPattern("d MMM", locale)
    val longFmt = DateTimeFormatter.ofPattern("d MMMM yyyy", locale)

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        FertilityHeader()
        OvulationHero(estimate = estimate, shortFmt = shortFmt, longFmt = longFmt)
        WindowCard(
            estimate = estimate,
            lutealPhaseDays = state.lutealPhaseDays,
            cycleLengthDays = state.cycleLengthDays,
            locale = locale,
            longFmt = longFmt,
        )
        HowCard(
            estimate = estimate,
            lutealPhaseDays = state.lutealPhaseDays,
            longFmt = longFmt,
        )
        FertilityNote()
    }
}

@Composable
private fun FertilityHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            stringResource(R.string.fer_eyebrow),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.6.sp,
            color = currentHomePalette.eyebrow,
        )
        Text(
            stringResource(R.string.fer_title),
            fontFamily = DinsSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 25.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            stringResource(R.string.fer_subtitle),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OvulationHero(
    estimate: CycleEstimate,
    shortFmt: DateTimeFormatter,
    longFmt: DateTimeFormatter,
) {
    val palette = currentHomePalette
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DinsCardShape,
        colors = CardDefaults.cardColors(containerColor = palette.secondaryBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder),
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("✿", fontSize = 34.sp, color = palette.secondaryText)
            Text(
                stringResource(R.string.fer_ovulation_label),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.6.sp,
                color = palette.eyebrow,
            )
            Text(
                estimate.estimatedOvulationDate?.format(shortFmt)
                    ?: stringResource(R.string.fer_no_data),
                fontFamily = DinsSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            estimate.predictedNextStart?.let {
                Text(
                    stringResource(R.string.fer_based_on, it.format(longFmt)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            DinsFertilityPill(text = confidenceLabel(estimate.confidence), lav = true)
        }
    }
}

@Composable
private fun WindowCard(
    estimate: CycleEstimate,
    lutealPhaseDays: Int,
    cycleLengthDays: Int,
    locale: Locale,
    longFmt: DateTimeFormatter,
) {
    FertilityCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.fer_window_title),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            if (estimate.fertileWindowStart != null && estimate.fertileWindowEnd != null) {
                DinsFertilityPill(text = stringResource(R.string.fer_estimate), lav = true)
            }
        }
        val start = estimate.fertileWindowStart
        val end = estimate.fertileWindowEnd
        if (start != null && end != null) {
            Text(
                windowRange(start, end, locale, longFmt),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            if (cycleLengthDays > 0) {
                val days = ChronoUnit.DAYS.between(start, end) + 1
                val fraction = (days.toFloat() / cycleLengthDays).coerceIn(0.06f, 1f)
                WindowProgress(fraction = fraction)
            }
        } else {
            Text(
                stringResource(R.string.fer_window_empty),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            stringResource(R.string.fer_luteal_note, lutealPhaseDays),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun windowRange(
    start: LocalDate,
    end: LocalDate,
    locale: Locale,
    longFmt: DateTimeFormatter,
): String {
    return if (start.year == end.year && start.month == end.month) {
        stringResource(
            R.string.fer_window_range_compact,
            start.dayOfMonth,
            end.dayOfMonth,
            start.month.getDisplayName(TextStyle.FULL, locale),
        )
    } else {
        stringResource(
            R.string.fer_window_range,
            start.format(longFmt),
            end.format(longFmt),
        )
    }
}

@Composable
private fun WindowProgress(fraction: Float) {
    val palette = currentHomePalette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(palette.trackBg),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.horizontalGradient(listOf(palette.progStart, palette.progEnd))),
        )
    }
}

@Composable
private fun HowCard(
    estimate: CycleEstimate,
    lutealPhaseDays: Int,
    longFmt: DateTimeFormatter,
) {
    FertilityCard {
        Text(
            stringResource(R.string.fer_how_title),
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Column {
            HowRow(
                number = "1",
                title = stringResource(R.string.fer_how_next_title),
                sub = estimate.predictedNextStart?.format(longFmt)
                    ?: stringResource(R.string.fer_no_data),
                showDivider = false,
            )
            HorizontalDivider(thickness = 1.dp, color = currentHomePalette.cardBorder)
            HowRow(
                number = "2",
                title = stringResource(R.string.fer_how_luteal_title),
                sub = stringResource(R.string.fer_how_luteal_sub, lutealPhaseDays),
                showDivider = false,
            )
            HorizontalDivider(thickness = 1.dp, color = currentHomePalette.cardBorder)
            HowRow(
                number = "3",
                title = stringResource(R.string.fer_how_ovulation_title),
                sub = estimate.estimatedOvulationDate?.format(longFmt)
                    ?: stringResource(R.string.fer_no_data),
                showDivider = false,
            )
        }
    }
}

@Composable
private fun HowRow(number: String, title: String, sub: String, showDivider: Boolean) {
    if (showDivider) {
        HorizontalDivider(thickness = 1.dp, color = currentHomePalette.cardBorder)
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
            Text(
                number,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = currentHomePalette.secondaryText,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(
                sub,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FertilityNote() {
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
            stringResource(R.string.fer_disclaimer),
            fontSize = 10.sp,
            lineHeight = 15.sp,
            color = palette.noteText,
        )
    }
}

@Composable
private fun FertilityCard(content: @Composable () -> Unit) {
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
private fun DinsFertilityPill(text: String, lav: Boolean = false) {
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
private fun confidenceLabel(confidence: Confidence): String = when (confidence) {
    Confidence.INSUFFICIENT_DATA -> stringResource(R.string.fer_confidence_insufficient)
    Confidence.LOW -> stringResource(R.string.fer_confidence_low)
    Confidence.MODERATE -> stringResource(R.string.fer_confidence_moderate)
    Confidence.HIGHER -> stringResource(R.string.fer_confidence_higher)
}
