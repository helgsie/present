package `is`.hi.present.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import `is`.hi.present.ui.components.LoadingComponent

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onGoToSignIn: () -> Unit,
    onSuccess: () -> Unit
) {
    val authState by viewModel.authUiState.collectAsState()
    val scope = rememberCoroutineScope()

    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.Error -> {
                val raw = (authState as AuthUiState.Error).message
                snackbarHostState.showSnackbar(AuthErrorMessage(raw))
                viewModel.resetAuthState()
            }
            is AuthUiState.Success -> onSuccess()
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            TextField(
                value = userEmail,
                onValueChange = { userEmail = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = userPassword,
                onValueChange = { userPassword = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.resetAuthState()

                    if (userPassword != confirmPassword) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Passwords do not match.")
                        }
                        return@Button
                    }

                    viewModel.signUp(
                        userEmail.trim(),
                        userPassword
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = authState !is AuthUiState.Loading
            ) {
                Text("Sign Up")
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Already have an account? Sign In",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.resetAuthState()
                        onGoToSignIn()
                    },
                textAlign = TextAlign.Center,
                textDecoration = TextDecoration.Underline,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (authState) {
                is AuthUiState.Idle -> Text("")
                is AuthUiState.Loading -> LoadingComponent()
                is AuthUiState.DeleteLoading -> {
                    Text(
                        text = "Deleting account...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is AuthUiState.SignOutLoading -> {
                    Text(
                        text = "Signing out...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is AuthUiState.Success -> LaunchedEffect(Unit) { onSuccess() }
                is AuthUiState.Error -> {}
            }
        }
    }
}
