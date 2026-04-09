package `is`.hi.present.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SetupProfileScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onDone: () -> Unit,
) {
    var displayName by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Velkomin!",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Veldu nafn sem vinir þínir munu sjá.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = displayName,
                onValueChange = {
                    if (it.length <= 30) displayName = it
                    error = null
                },
                label = { Text("Birtingarnafn") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = error != null,
                supportingText = error?.let { msg -> { Text(msg) } }
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val trimmed = displayName.trim()
                    if (trimmed.isBlank()) {
                        error = "Nafn má ekki vera tómt"
                        return@Button
                    }
                    isSaving = true
                    error = null
                    viewModel.updateDisplayName(trimmed) { result ->
                        isSaving = false
                        if (result.isSuccess) {
                            onDone()
                        } else {
                            error = "Tókst ekki að vista nafn"
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Áfram")
                }
            }

        }
    }
}