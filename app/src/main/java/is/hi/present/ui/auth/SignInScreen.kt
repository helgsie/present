package `is`.hi.present.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import `is`.hi.present.ui.components.LoadingComponent

@Composable
fun SignInScreen(
    viewModel: AuthViewModel,
    onGoToSignUp: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val authState by viewModel.authUiState

    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Error) {
            val raw = (authState as AuthUiState.Error).message
            snackbarHostState.showSnackbar(AuthErrorMessage(raw))
            viewModel.resetAuthState()
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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.resetAuthState()
                    viewModel.signIn(context, userEmail.trim(), userPassword)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign In")
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Don't have an account? Sign Up",
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
                is AuthUiState.Success -> LaunchedEffect(Unit) { onSuccess() }
                is AuthUiState.Error -> {}
            }
        }
    }
}
