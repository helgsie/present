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
    fun signUp(
        context: Context,
        userEmail: String,
        userPassword: String,
    ) {
        if (userEmail.isBlank() || userPassword.isBlank()) {
            _authUiState.value = AuthUiState.Error("Email and password cannot be empty")
            return
        }
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            try{
                repository.signUp(userEmail, userPassword)
                saveToken(context)
                _authUiState.value = AuthUiState.Success("Registered user successfully")
            } catch (e: Exception){
                _authUiState.value = AuthUiState.Error("Error: ${e.message}")

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

    fun signIn(
        context: Context,
        userEmail: String,
        userPassword: String
    ){
        if (userEmail.isBlank() || userPassword.isBlank()) {
            _authUiState.value = AuthUiState.Error("Email and password cannot be empty")
            return
        }
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            try {
                repository.signIn(userEmail, userPassword)
                saveToken(context)
                _authUiState.value = AuthUiState.Success("Signed in successfully")
            } catch (e: Exception){
                _authUiState.value = AuthUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun signOut(
        context: Context,
        onComplete: () -> Unit
    ){
        val sharedPref = SharedPreferenceHelper(context)
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            try {
                repository.signOut()
                sharedPref.clearPreferences()
                _authUiState.value = AuthUiState.Idle
                onComplete()
            } catch (e: Exception){
                _authUiState.value = AuthUiState.Error("Error: ${e.message}")
            }
        }
    }
    fun isUserLoggedIn(
        context: Context
    ){
        viewModelScope.launch {
            try {
                val token = getToken(context)
                if(token.isNullOrEmpty()){
                    _authUiState.value = AuthUiState.Error("User is not logged in")
                } else {
                    repository.retrieveUser()
                    repository.refreshCurrentSession()
                    saveToken(context)
                    _authUiState.value = AuthUiState.Success("User is already logged in")
                }
            } catch (e: Exception){
                _authUiState.value = AuthUiState.Error("Error: ${e.message}")
            }
        }
    }

    suspend fun refreshSession() {
        repository.refreshCurrentSession()
    }

}