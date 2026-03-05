package `is`.hi.present.ui.auth

sealed class AuthStatus {
    data object Loading : AuthStatus()
    data object LoggedOut : AuthStatus()
    data class LoggedIn(val userId: String) : AuthStatus()
}