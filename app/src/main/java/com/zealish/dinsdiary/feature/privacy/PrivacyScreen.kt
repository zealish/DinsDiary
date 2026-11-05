package com.zealish.dinsdiary.feature.privacy

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zealish.dinsdiary.R
import kotlinx.coroutines.launch

/** Privacy & data management (PRD §6.10, mockup). */
@Composable
fun PrivacyScreen(
    modifier: Modifier = Modifier,
    viewModel: PrivacyViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val json = viewModel.exportJson()
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray())
                }
                viewModel.exportDone(uri.lastPathSegment)
            }
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.pri_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(R.string.pri_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.pri_local_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.pri_local_body),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.pri_lock_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.pri_lock_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = {}, enabled = false) { Text(stringResource(R.string.pri_coming_soon)) }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.pri_export_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.pri_export_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = { exportLauncher.launch("dinsdiary-export.json") }) {
                    Text(stringResource(R.string.pri_export_action))
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.pri_controls_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.pri_controls_body),
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedButton(onClick = viewModel::askDelete) {
                    Text(stringResource(R.string.pri_delete_all))
                }
            }
        }

        viewModel.message?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
    }

    if (viewModel.confirmDelete) {
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text(stringResource(R.string.pri_confirm_title)) },
            text = {
                Text(
                    stringResource(R.string.pri_confirm_body),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { scope.launch { viewModel.confirmClear() } },
                ) { Text(stringResource(R.string.pri_confirm_delete)) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) { Text(stringResource(R.string.pri_confirm_keep)) }
            },
        )
    }
}
