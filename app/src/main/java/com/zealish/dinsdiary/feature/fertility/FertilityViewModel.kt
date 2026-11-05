package com.zealish.dinsdiary.feature.fertility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zealish.dinsdiary.domain.cycle.CycleOverview
import com.zealish.dinsdiary.domain.repository.MenstrualPeriodRepository
import com.zealish.dinsdiary.domain.repository.UserProfileRepository
import com.zealish.dinsdiary.domain.usecase.GetCycleOverviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class FertilityUiState(
    val isLoading: Boolean = true,
    val overview: CycleOverview? = null,
    val lutealPhaseDays: Int = 14,
    val cycleLengthDays: Int = 28,
)

@HiltViewModel
class FertilityViewModel @Inject constructor(
    periodRepository: MenstrualPeriodRepository,
    profileRepository: UserProfileRepository,
    overviewUseCase: GetCycleOverviewUseCase,
) : ViewModel() {

    private val periods = periodRepository.observePeriods()
    private val profile = profileRepository.observeProfile()

    val uiState: StateFlow<FertilityUiState> = combine(
        overviewUseCase.observe(periods, profile),
        profile,
    ) { overview, prefs ->
        FertilityUiState(
            isLoading = false,
            overview = overview,
            lutealPhaseDays = prefs.lutealPhaseLengthDays,
            cycleLengthDays = prefs.preferredCycleLengthDays ?: 28,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FertilityUiState())
}
