package com.dinsdiary.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinsdiary.domain.cycle.CalendarMarks
import com.dinsdiary.domain.repository.JournalRepository
import com.dinsdiary.domain.repository.MenstrualPeriodRepository
import com.dinsdiary.domain.repository.UserProfileRepository
import com.dinsdiary.domain.usecase.GetCalendarMarksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/**
 * Monthly calendar state (PRD §6.4).
 *
 * @param hasJournal whether a journal entry exists for [selected].
 */
data class CalendarUiState(
    val month: YearMonth,
    val selected: LocalDate,
    val today: LocalDate,
    val marks: CalendarMarks = CalendarMarks(emptySet(), emptySet(), emptySet(), null),
    val hasJournal: Boolean = false,
    val isLoading: Boolean = true,
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    periodRepository: MenstrualPeriodRepository,
    profileRepository: UserProfileRepository,
    journalRepository: JournalRepository,
    private val marksUseCase: GetCalendarMarksUseCase,
    clock: Clock,
) : ViewModel() {

    private val today = LocalDate.now(clock)
    private val monthFlow = MutableStateFlow(YearMonth.from(today))
    private val selectedFlow = MutableStateFlow(today)
    // Journal presence follows the selected date (mockup "No journal entry yet" row).
    private val journalForSelected = selectedFlow.flatMapLatest { journalRepository.observeDay(it) }

    val uiState: StateFlow<CalendarUiState> = combine(
        periodRepository.observePeriods(),
        profileRepository.observeProfile(),
        monthFlow,
        selectedFlow,
        journalForSelected,
    ) { periods, profile, month, selected, journal ->
        CalendarUiState(
            month = month,
            selected = selected,
            today = today,
            marks = marksUseCase.build(periods, profile),
            hasJournal = journal != null,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState(monthFlow.value, today, today))

    fun previousMonth() {
        monthFlow.value = monthFlow.value.minusMonths(1)
    }

    fun nextMonth() {
        monthFlow.value = monthFlow.value.plusMonths(1)
    }

    fun select(date: LocalDate) {
        selectedFlow.value = date
        if (YearMonth.from(date) != monthFlow.value) {
            monthFlow.value = YearMonth.from(date)
        }
    }
}
