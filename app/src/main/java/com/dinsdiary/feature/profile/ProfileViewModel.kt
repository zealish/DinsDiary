package com.dinsdiary.feature.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinsdiary.data.mapper.DEFAULT_LUTEAL_PHASE_DAYS
import com.dinsdiary.data.prefs.AppPrefs
import com.dinsdiary.domain.model.UserProfile
import com.dinsdiary.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val themeMode: String = AppPrefs.THEME_SYSTEM,
    val language: String = AppPrefs.LANG_ID,
)

data class ProfileEditForm(
    val name: String = "",
    val cycleLength: String = "",
    val periodLength: String = "",
    val lutealPhase: String = DEFAULT_LUTEAL_PHASE_DAYS.toString(),
)

/** Profile + cycle preferences + appearance (PRD §6.1, §6.10). */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: UserProfileRepository,
    private val prefs: AppPrefs,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        profileRepository.observeProfile(),
        prefs.themeMode,
        prefs.language,
    ) { profile, theme, language ->
        ProfileUiState(isLoading = false, profile = profile, themeMode = theme, language = language)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    var editing by mutableStateOf(false)
        private set
    var form by mutableStateOf(ProfileEditForm())
        private set
    var errorCode by mutableStateOf<String?>(null)
        private set
    var infoCode by mutableStateOf<String?>(null)
        private set

    fun startEditing(current: UserProfile) {
        form = ProfileEditForm(
            name = current.displayName.orEmpty(),
            cycleLength = current.preferredCycleLengthDays?.toString().orEmpty(),
            periodLength = current.preferredPeriodLengthDays?.toString().orEmpty(),
            lutealPhase = current.lutealPhaseLengthDays.toString(),
        )
        editing = true
        errorCode = null
    }

    fun cancelEditing() {
        editing = false
        errorCode = null
    }

    fun updateForm(form: ProfileEditForm) {
        this.form = form
        errorCode = null
    }

    suspend fun save(): Boolean {
        val cycle = form.cycleLength.ifBlank { null }?.toIntOrNull()
        if (form.cycleLength.isNotBlank() && (cycle == null || cycle !in 20..40)) {
            errorCode = "cycle_range"
            return false
        }
        val period = form.periodLength.ifBlank { null }?.toIntOrNull()
        if (form.periodLength.isNotBlank() && (period == null || period !in 1..10)) {
            errorCode = "period_range"
            return false
        }
        val luteal = form.lutealPhase.toIntOrNull()
        if (luteal == null || luteal !in 8..20) {
            errorCode = "luteal_range"
            return false
        }
        profileRepository.saveProfile(
            UserProfile(
                displayName = form.name.ifBlank { null },
                dateOfBirth = uiState.value.profile?.dateOfBirth,
                preferredCycleLengthDays = cycle,
                preferredPeriodLengthDays = period,
                lutealPhaseLengthDays = luteal,
            ),
        )
        editing = false
        infoCode = "saved"
        return true
    }

    suspend fun setTheme(mode: String) {
        prefs.setThemeMode(mode)
    }

    suspend fun setLanguage(code: String) {
        prefs.setLanguage(code)
    }
}
