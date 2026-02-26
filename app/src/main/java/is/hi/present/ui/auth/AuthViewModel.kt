package `is`.hi.present.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hi.present.data.repository.AuthRepository
import `is`.hi.present.util.SharedPreferenceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val sharedPref: SharedPreferenceHelper
) : ViewModel() {

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authUiState.value = AuthUiState.Error("Email and password cannot be empty")
            return
        }

        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            try {
                repository.signUp(email, password)
                saveToken()
                _authUiState.value = AuthUiState.Success("Registered user successfully")
            } catch (e: Exception) {
                _authUiState.value = AuthUiState.Error("Sign up failed: ${e.message}")
            }
        }
    }

    private fun saveToken() {
        val accessToken = repository.getAccessToken()
        if (!accessToken.isNullOrBlank()) {
            sharedPref.saveStringData("accessToken", accessToken)
        }
    }

    fun getToken(): String? = sharedPref.getStringData("accessToken")

    fun signIn(email: String, password: String) {
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

                saveToken()
                _authUiState.value = AuthUiState.Success("Signed in successfully")

            } catch (e: Exception) {
                _authUiState.value = AuthUiState.Error("Sign in failed: ${e.message}")
            }
        }
    }

    fun signOut(onComplete: () -> Unit) {
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

    fun resetAuthState() {
        _authUiState.value = AuthUiState.Idle
    }

    fun deleteAccount(onComplete: () -> Unit) {
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