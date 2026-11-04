package com.dinsdiary.feature.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.dinsdiary.data.mapper.DEFAULT_LUTEAL_PHASE_DAYS
import com.dinsdiary.data.mapper.newId
import com.dinsdiary.domain.cycle.PeriodRecord
import com.dinsdiary.domain.cycle.PeriodStatus
import com.dinsdiary.domain.model.UserProfile
import com.dinsdiary.domain.repository.MenstrualPeriodRepository
import com.dinsdiary.domain.repository.UserProfileRepository
import com.dinsdiary.data.prefs.AppPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

data class OnboardingForm(
    val name: String = "",
    val dateOfBirth: String = "",
    val cycleLengthDays: Int = 28,
    val periodLengthDays: Int = 5,
    val lastPeriodStart: String = "",
    val lutealPhaseDays: Int = DEFAULT_LUTEAL_PHASE_DAYS,
    val consent: Boolean = false,
)

/**
 * Three-step setup (PRD §6.1, mockup). All fields skippable except consent;
 * preferences are stored as user input, never observed truth.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileRepository: UserProfileRepository,
    private val periodRepository: MenstrualPeriodRepository,
    private val prefs: AppPrefs,
) : ViewModel() {

    var form by mutableStateOf(OnboardingForm())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var step by mutableStateOf(1)
        private set

    fun update(form: OnboardingForm) {
        this.form = form
        errorMessage = null
    }

    fun next() {
        if (step < 3) step++
    }

    fun back() {
        if (step > 1) step--
    }

    /** Returns true when setup completed and persisted. */
    suspend fun finish(): Boolean {
        if (!form.consent) {
            errorMessage = "Please confirm you understand how your diary works."
            return false
        }
        val dob = form.dateOfBirth.ifBlank { null }?.let {
            runCatching { LocalDate.parse(it) }.getOrNull()
                ?: return fail("Date of birth must use YYYY-MM-DD, or be left empty.")
        }
        val lastStart = form.lastPeriodStart.ifBlank { null }?.let {
            runCatching { LocalDate.parse(it) }.getOrNull()
                ?: return fail("Last period date must use YYYY-MM-DD, or be left empty.")
        }
        profileRepository.saveProfile(
            UserProfile(
                displayName = form.name.ifBlank { null },
                dateOfBirth = dob,
                preferredCycleLengthDays = form.cycleLengthDays,
                preferredPeriodLengthDays = form.periodLengthDays,
                lutealPhaseLengthDays = form.lutealPhaseDays,
            ),
        )
        if (lastStart != null) {
            periodRepository.savePeriod(
                PeriodRecord(
                    id = newId(),
                    start = lastStart,
                    end = null,
                    status = PeriodStatus.ACTIVE,
                ),
                notes = null,
            )
        }
        prefs.setOnboardingCompleted(true)
        return true
    }

    suspend fun skip() {
        profileRepository.saveProfile(
            UserProfile(
                displayName = null,
                dateOfBirth = null,
                preferredCycleLengthDays = null,
                preferredPeriodLengthDays = null,
                lutealPhaseLengthDays = DEFAULT_LUTEAL_PHASE_DAYS,
            ),
        )
        prefs.setOnboardingCompleted(true)
    }

    private fun fail(message: String): Boolean {
        errorMessage = message
        return false
    }
}
