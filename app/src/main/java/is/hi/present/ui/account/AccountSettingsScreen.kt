package `is`.hi.present.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `is`.hi.present.ui.auth.AuthUiState
import `is`.hi.present.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
    onAccountDeleted: () -> Unit,
) {
    var showConfirm by remember { mutableStateOf(false) }
    val authState by viewModel.authUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            when (authState) {
                is AuthUiState.DeleteLoading -> Text("Deleting account...")
                is AuthUiState.SignOutLoading -> Text("Signing out...")
                is AuthUiState.Error -> Text(
                    (authState as AuthUiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
                else -> Unit
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.signOut {
                        onSignedOut()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }

            Spacer(Modifier.height(16.dp))

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                onClick = { showConfirm = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Delete Account",
                    color = MaterialTheme.colorScheme.onError
                )
            }

            if (showConfirm) {
                AlertDialog(
                    onDismissRequest = { showConfirm = false },
                    title = { Text("Delete Account") },
                    text = {
                        Text("Are you sure you want to permanently delete your account? This action cannot be undone.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showConfirm = false
                                viewModel.deleteAccount {
                                    onAccountDeleted()
                                }
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirm = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}