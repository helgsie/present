package `is`.hi.present.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import `is`.hi.present.ui.components.LoadingComponent

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = viewModel(),
    onSuccess: () -> Unit
){
    val context = LocalContext.current
    val authState by viewModel.authUiState

    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    val hasCheckedLogin = remember { mutableStateOf(false) }

    LaunchedEffect(hasCheckedLogin.value) {
        if (!hasCheckedLogin.value) {
            viewModel.isUserLoggedIn(context)
            hasCheckedLogin.value = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        // Titill
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

        // Netfang
        TextField(
            value = userEmail,
            onValueChange = { userEmail = it },
            label = { Text(text = "Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lykilorð
        TextField(
            value = userPassword,
            onValueChange = { userPassword = it },
            label = { Text(text = "Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sign in & sign out takkar
        Button(
            onClick = {
                viewModel.signIn(context, userEmail, userPassword)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Sign In")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                viewModel.signUp(context, userEmail, userPassword)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Sign Up")
        }

        Spacer(modifier = Modifier.height(24.dp))

        when(val state = authState){
            is AuthUiState.Idle -> {
                Text(
                    text = "Please sign in or sign up",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            is AuthUiState.Loading -> {
                    LoadingComponent()
            }
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
            is AuthUiState.Success -> {
                LaunchedEffect(state) {
                    onSuccess()
                }
            }
            is AuthUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}