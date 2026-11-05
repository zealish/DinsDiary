package com.zealish.dinsdiary.feature.period

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zealish.dinsdiary.data.mapper.newId
import com.zealish.dinsdiary.domain.cycle.PeriodRecord
import com.zealish.dinsdiary.domain.cycle.PeriodStatus
import com.zealish.dinsdiary.domain.model.DailyRecord
import com.zealish.dinsdiary.domain.model.FlowIntensity
import com.zealish.dinsdiary.domain.repository.DailyRecordRepository
import com.zealish.dinsdiary.domain.repository.InvalidPeriodException
import com.zealish.dinsdiary.domain.repository.MenstrualPeriodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

data class PeriodForm(
    val editingId: String? = null,
    val start: String = "",
    val end: String = "",
    val ongoing: Boolean = true,
)

data class DailyFlowForm(
    val date: String = LocalDate.now().toString(),
    val flow: FlowIntensity? = null,
    val spotting: Boolean = false,
    val notes: String = "",
)

/**
 * Period + daily flow logging (PRD §6.3). Destructive deletes require
 * explicit confirmation in the UI before reaching the repository.
 */
@HiltViewModel
class PeriodViewModel @Inject constructor(
    private val periodRepository: MenstrualPeriodRepository,
    private val dailyRepository: DailyRecordRepository,
    clock: Clock,
) : ViewModel() {
    private val today = LocalDate.now(clock)

    val periods: StateFlow<List<PeriodRecord>> = periodRepository.observePeriods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var form by mutableStateOf(PeriodForm())
        private set
    var flowForm by mutableStateOf(DailyFlowForm(date = today.toString()))
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var infoMessage by mutableStateOf<String?>(null)
        private set
    var deleteTarget by mutableStateOf<PeriodRecord?>(null)
        private set

    fun updateForm(form: PeriodForm) {
        this.form = form
        errorMessage = null
    }

    fun updateFlowForm(form: DailyFlowForm) {
        flowForm = form
        errorMessage = null
        infoMessage = null
    }

    fun edit(record: PeriodRecord) {
        form = PeriodForm(
            editingId = record.id,
            start = record.start.toString(),
            end = record.end?.toString().orEmpty(),
            ongoing = record.status != PeriodStatus.COMPLETED,
        )
        errorMessage = null
    }

    fun resetForm() {
        form = PeriodForm()
        errorMessage = null
    }

    fun askDelete(record: PeriodRecord) {
        deleteTarget = record
    }

    fun cancelDelete() {
        deleteTarget = null
    }

    suspend fun confirmDelete() {
        deleteTarget?.let { periodRepository.deletePeriod(it.id) }
        deleteTarget = null
    }

    /** Returns true when the period was stored. */
    suspend fun savePeriod(): Boolean {
        val start = parseDate(form.start, "Start date") ?: return false
        val end = form.end.ifBlank { null }?.let {
            parseDate(it, "End date") ?: return false
        }
        if (end != null && end.isBefore(start)) {
            errorMessage = "End date cannot precede start date."
            return false
        }
        try {
            periodRepository.savePeriod(
                PeriodRecord(
                    id = form.editingId ?: newId(),
                    start = start,
                    end = if (form.ongoing) null else end,
                    status = if (form.ongoing && end == null) {
                        PeriodStatus.ACTIVE
                    } else {
                        PeriodStatus.COMPLETED
                    },
                ),
                notes = null,
            )
        } catch (e: InvalidPeriodException) {
            errorMessage = "Could not save: end date precedes start date."
            return false
        }
        resetForm()
        infoMessage = "Period saved."
        return true
    }

    suspend fun saveDailyFlow(): Boolean {
        val date = parseDate(flowForm.date, "Flow date") ?: return false
        if (flowForm.flow == null && !flowForm.spotting) {
            errorMessage = "Choose a flow intensity or mark spotting."
            return false
        }
        dailyRepository.save(
            DailyRecord(
                date = date,
                periodId = null,
                flow = flowForm.flow,
                spotting = flowForm.spotting,
                notes = flowForm.notes.ifBlank { null },
            ),
        )
        flowForm = DailyFlowForm(date = date.toString())
        infoMessage = "Daily flow saved for $date."
        return true
    }

    private fun parseDate(raw: String, field: String): LocalDate? {
        if (raw.isBlank()) {
            errorMessage = "$field is required (YYYY-MM-DD)."
            return null
        }
        return runCatching { LocalDate.parse(raw) }.getOrNull().also {
            if (it == null) errorMessage = "$field must use YYYY-MM-DD."
        }
    }
}
