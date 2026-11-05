package com.zealish.dinsdiary.feature.home

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

data class HomeUiState(
    val isLoading: Boolean = true,
    val displayName: String? = null,
    val overview: CycleOverview? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    periodRepository: MenstrualPeriodRepository,
    profileRepository: UserProfileRepository,
    overviewUseCase: GetCycleOverviewUseCase,
) : ViewModel() {

    private val periods = periodRepository.observePeriods()
    private val profile = profileRepository.observeProfile()

    val uiState: StateFlow<HomeUiState> = combine(
        overviewUseCase.observe(periods, profile),
        profile,
    ) { overview, prefs ->
        HomeUiState(isLoading = false, displayName = prefs.displayName, overview = overview)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
