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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import `is`.hi.present.navigation.Routes
import `is`.hi.present.ui.auth.AuthState
import `is`.hi.present.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    viewModel: AuthViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    var showConfirm by remember { mutableStateOf(false) }
    val authState by viewModel.authState


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
            if (authState is AuthState.Loading) {
                Text("Deleting account...")
            }
            if (authState is AuthState.Error) {
                Text(
                    (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.signOut(context) {
                        navController.navigate(Routes.AUTH) {
                            popUpTo(Routes.WISHLISTS) { inclusive = true }
                        }
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
                onClick = {
                    showConfirm = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Delete Account",
                    color = MaterialTheme.colorScheme.onError
                )
            }

            // 👇 Confirmation Dialog
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
                                viewModel.deleteAccount(context) {
                                    navController.navigate(Routes.AUTH) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showConfirm = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}