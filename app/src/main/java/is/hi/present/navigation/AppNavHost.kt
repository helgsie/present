package `is`.hi.present.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import `is`.hi.present.ui.auth.AuthScreen
import `is`.hi.present.ui.auth.AuthViewModel
import `is`.hi.present.ui.components.LoadingComponent
import `is`.hi.present.home.LoginTest

@Composable
fun AppNavHost(authViewModel: AuthViewModel = AuthViewModel()) {
    val navController = rememberNavController()
    val context = LocalContext.current

    var isCheckingAuth by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf("auth") }

    LaunchedEffect(Unit) {
        var isLoggedIn = false
        try {
            val token = authViewModel.getToken(context)
            if (!token.isNullOrEmpty()) {
                authViewModel.refreshSession() // suspend function
                isLoggedIn = true
            }
        } catch (e: Exception) {
            isLoggedIn = false
        }
        startDestination = if (isLoggedIn) "home" else "auth"
        isCheckingAuth = false
    }


    if (isCheckingAuth) {
        LoadingComponent()
    } else {
        NavHost(navController = navController, startDestination = startDestination) {
            composable("auth") {
                AuthScreen(
                    viewModel = authViewModel,
                    onSuccess = {
                        navController.navigate("home") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }
            composable("home") {
                LoginTest(
                    viewModel = authViewModel,
                    onLogout = {
                        authViewModel.signOut(context) {
                            navController.navigate("auth") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    }
}
