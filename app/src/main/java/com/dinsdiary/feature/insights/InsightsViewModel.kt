package com.dinsdiary.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinsdiary.domain.cycle.PeriodRecord
import com.dinsdiary.domain.model.JournalEntry
import com.dinsdiary.domain.model.Symptom
import com.dinsdiary.domain.model.UserProfile
import com.dinsdiary.domain.repository.JournalRepository
import com.dinsdiary.domain.repository.MenstrualPeriodRepository
import com.dinsdiary.domain.repository.UserProfileRepository
import com.dinsdiary.domain.usecase.GetInsightsUseCase
import com.dinsdiary.domain.usecase.InsightsData
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class InsightsUiState(
    val isLoading: Boolean = true,
    val data: InsightsData? = null,
    val symptomNames: Map<String, String> = emptyMap(),
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    periodRepository: MenstrualPeriodRepository,
    profileRepository: UserProfileRepository,
    journalRepository: JournalRepository,
    private val insightsUseCase: GetInsightsUseCase,
    clock: Clock,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now(clock)

    val uiState: StateFlow<InsightsUiState> = combine(
        periodRepository.observePeriods(),
        profileRepository.observeProfile(),
        journalRepository.observeRange(today.minusDays(180), today),
        journalRepository.observeSymptoms(),
    ) { periods: List<PeriodRecord>, profile: UserProfile, entries: List<JournalEntry>, catalog: List<Symptom> ->
        InsightsUiState(
            isLoading = false,
            data = insightsUseCase.build(periods, entries, profile, java.util.Locale.getDefault()),
            symptomNames = catalog.associate { it.id to it.nameKey },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())
}
