package `is`.hi.present.ui.auth

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hi.present.data.repository.AuthRepository
import `is`.hi.present.util.SharedPreferenceHelper
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel(){
    private val _authUiState = mutableStateOf<AuthUiState>(AuthUiState.Idle)
    val authUiState: State<AuthUiState> = _authUiState
    fun signUp(context: Context, email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authUiState.value = AuthUiState.Error("Email and password cannot be empty")
            return
        }

        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            try {
                repository.signUp(email, password)
                saveToken(context)
                _authUiState.value = AuthUiState.Success("Registered user successfully")
            } catch (e: Exception) {
                _authUiState.value = AuthUiState.Error("Sign up failed: ${e.message}")
            }
        }
    }

    private fun saveToken(context: Context){
        val accessToken = repository.getAccessToken()
        val sharedPref = SharedPreferenceHelper(context)
        sharedPref.saveStringData("accessToken",accessToken)
    }

    fun getToken(context: Context): String? {
        val sharedPref = SharedPreferenceHelper(context)
        return sharedPref.getStringData("accessToken")
    }

    fun signIn(context: Context, email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authUiState.value = AuthUiState.Error("Email and password cannot be empty")
            return
        }
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            try {
                repository.signIn(email, password)
                val user = repository.retrieveUser()

                if (user == null) {

                    _authUiState.value = AuthUiState.Error("User not found. Please sign up.")
                    return@launch
                }

                repository.getProfile(user.id)

                saveToken(context)
                _authUiState.value = AuthUiState.Success("Signed in successfully")

            } catch (e: Exception) {
                _authUiState.value = AuthUiState.Error("Sign in failed: ${e.message}")
            }
        }
    }

    fun signOut(context: Context, onComplete: () -> Unit) {
        val sharedPref = SharedPreferenceHelper(context)
        viewModelScope.launch {
            _authUiState.value = AuthUiState.SignOutLoading
            try {
                repository.signOut()
                sharedPref.clearPreferences()
                _authUiState.value = AuthUiState.Idle
                onComplete()
            } catch (e: Exception) {
                _authUiState.value = AuthUiState.Error("Failed to sign out: ${e.message}")
            }
        }
    }

    fun isUserLoggedIn(context: Context) {
        viewModelScope.launch {
            try {
                val token = SharedPreferenceHelper(context).getStringData("accessToken")
                if (token.isNullOrEmpty()) {
                    _authUiState.value = AuthUiState.Idle
                    return@launch
                }

                repository.refreshCurrentSession()
                val user = repository.retrieveUser()

                if (user == null) {
                    _authUiState.value = AuthUiState.Error("User not found. Please sign up.")
                    return@launch
                }

                saveToken(context)
                _authUiState.value = AuthUiState.Success("User is already logged in")

            } catch (e: Exception) {
                _authUiState.value = AuthUiState.Error("Error checking login: ${e.message}")
            }
        }
    }

    suspend fun refreshSession() {
        repository.refreshCurrentSession()
    }

    fun deleteAccount(context: Context, onComplete: () -> Unit) {
        val sharedPref = SharedPreferenceHelper(context)
        viewModelScope.launch {
            _authUiState.value = AuthUiState.DeleteLoading
            try {
                repository.deleteAccount()
                sharedPref.clearPreferences()
                _authUiState.value = AuthUiState.Idle
                onComplete()
            } catch (e: Exception) {
                _authUiState.value = AuthUiState.Error("Failed to delete account: ${e.message}")
            }
        }
    }

}