package `is`.hi.present.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import `is`.hi.present.ui.components.LoadingComponent
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SignInScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onGoToSignUp: () -> Unit,
    onSuccess: () -> Unit
) {
    val authState by viewModel.authUiState.collectAsState()

    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
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
                text = "Present",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.tertiary,
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
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else
                        Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = image,
                            contentDescription = if (passwordVisible)
                                "Hide password"
                            else
                                "Show password"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.resetAuthState()
                    viewModel.signIn(userEmail.trim(), userPassword)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skrá inn")
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Nýskráning",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.resetAuthState()
                        onGoToSignUp()
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
                        text = "Eyði aðgangi...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is AuthUiState.SignOutLoading -> {
                    Text(
                        text = "Skrái út...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is AuthUiState.Success -> LaunchedEffect(Unit) { onSuccess() }
                is AuthUiState.Error -> {}
            }
        }
    }
}
