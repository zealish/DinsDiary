package com.zealish.dinsdiary.feature.privacy

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.zealish.dinsdiary.domain.usecase.BuildExportJsonUseCase
import com.zealish.dinsdiary.domain.usecase.ClearUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Export + delete controls (PRD §6.10). Destructive actions need confirmation. */
@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val clearUserData: ClearUserDataUseCase,
    private val buildExport: BuildExportJsonUseCase,
) : ViewModel() {

    fun exportDone(fileName: String?) {
        message = if (fileName == null) "Export cancelled." else "Exported. Keep the file somewhere safe."
    }

    var confirmDelete by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    fun askDelete() {
        confirmDelete = true
    }

    fun cancelDelete() {
        confirmDelete = false
    }

    suspend fun confirmClear(): Boolean {
        clearUserData.clear()
        confirmDelete = false
        message = "All diary data deleted. Restart the app to begin again."
        return true
    }

    /** Machine-readable backup of the user's own records (schema v1). */
    suspend fun exportJson(): String = buildExport()
}
