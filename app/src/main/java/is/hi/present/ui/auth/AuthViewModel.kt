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
    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState
    fun signUp(
        context: Context,
        userEmail: String,
        userPassword: String,
    ) {
        if (userEmail.isBlank() || userPassword.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try{
                repository.signUp(userEmail, userPassword)
                saveToken(context)
                _authState.value = AuthState.Success("Registered user successfully")
            } catch (e: Exception){
                _authState.value = AuthState.Error("Error: ${e.message}")

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
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.signIn(userEmail, userPassword)
                saveToken(context)
                _authState.value = AuthState.Success("Signed in successfully")
            } catch (e: Exception){
                _authState.value = AuthState.Error("Error: ${e.message}")
            }
        }
    }

    fun signOut(
        context: Context,
        onComplete: () -> Unit
    ){
        val sharedPref = SharedPreferenceHelper(context)
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.signOut()
                sharedPref.clearPreferences()
                _authState.value = AuthState.Idle
                onComplete()
            } catch (e: Exception){
                _authState.value = AuthState.Error("Error: ${e.message}")
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
                    _authState.value = AuthState.Error("User is not logged in")
                } else {
                    repository.retrieveUser()
                    repository.refreshCurrentSession()
                    saveToken(context)
                    _authState.value = AuthState.Success("User is already logged in")
                }
            } catch (e: Exception){
                _authState.value = AuthState.Error("Error: ${e.message}")
            }
        }
    }

    suspend fun refreshSession() {
        repository.refreshCurrentSession()
    }

}