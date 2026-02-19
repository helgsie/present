package `is`.hi.present.ui.auth

sealed class AuthUiState {
    object Idle: AuthUiState()
    object Loading: AuthUiState()
    object SignOutLoading : AuthUiState()
    object DeleteLoading : AuthUiState()
    data class Success(val message: String): AuthUiState()
    data class Error(val message: String): AuthUiState()
}