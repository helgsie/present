package `is`.hi.present.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hi.present.data.repository.AuthRepository
import `is`.hi.present.core.util.SharedPreferenceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import `is`.hi.present.core.local.AppDatabase
import `is`.hi.present.core.local.dao.PendingOpDao
import `is`.hi.present.core.local.entity.PendingOpEntity
import `is`.hi.present.core.local.entity.PendingOpType
import `is`.hi.present.core.sync.SyncManager
import `is`.hi.present.core.sync.SyncScheduler
import `is`.hi.present.data.dto.PendingProfilePayload
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val sharedPref: SharedPreferenceHelper,
    private val appDatabase: AppDatabase,
    private val syncScheduler: SyncScheduler,
    private val syncManager: SyncManager,
    private val pendingOpDao: PendingOpDao
) : ViewModel() {

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private companion object {
        const val PREF_ACCESS_TOKEN = "accessToken"
        const val PREF_USER_ID = "userId"
        const val PREF_USER_EMAIL = "userEmail"
        const val PREF_DISPLAY_NAME = "displayName"
    }

    private val _authStatus = MutableStateFlow<AuthStatus>(AuthStatus.Loading)
    val authStatus: StateFlow<AuthStatus> = _authStatus.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _currentDisplayName = MutableStateFlow("")
    val currentDisplayName: StateFlow<String> = _currentDisplayName.asStateFlow()

    init {
        resolveAuthStatus()
    }

    private fun resolveAuthStatus() {
        viewModelScope.launch {
            _authStatus.value = AuthStatus.Loading

            // cached auth session þegar offline
            val cachedUserId = sharedPref.getStringData(PREF_USER_ID)
            if (!cachedUserId.isNullOrBlank()) {
                _currentUserEmail.value = sharedPref.getStringData(PREF_USER_EMAIL)
                _currentDisplayName.value = sharedPref.getStringData(PREF_DISPLAY_NAME) ?: ""
                _authStatus.value = AuthStatus.LoggedIn(cachedUserId)
                return@launch
            }

            // reyna að tengjast Supabase
            try {
                val userId = repo.getCurrentUserId()
                if (!userId.isNullOrBlank()) {
                    sharedPref.saveStringData(PREF_USER_ID, userId)
                    _authStatus.value = AuthStatus.LoggedIn(userId)
                    runCatching { repo.getProfile(userId) }
                        .getOrNull()?.let { _currentDisplayName.value = it.display_name }
                } else {
                    _authStatus.value = AuthStatus.LoggedOut
                }
            } catch (_: Exception) {
                _authStatus.value = AuthStatus.LoggedOut
            }
        }
    }

    private fun saveToken() {
        val accessToken = repo.getAccessToken()
        if (!accessToken.isNullOrBlank()) {
            sharedPref.saveStringData(PREF_ACCESS_TOKEN, accessToken)
        }
    }

    private fun saveUserId(userId: String) {
        if (userId.isNotBlank()) {
            sharedPref.saveStringData(PREF_USER_ID, userId)
        }
    }

    private fun saveUserEmail(email: String?) {
        if (!email.isNullOrBlank()) {
            sharedPref.saveStringData(PREF_USER_EMAIL, email)
            _currentUserEmail.value = email
        }
    }

    private fun clearCachedAuth() {
        sharedPref.clearPreferences()
        _currentUserEmail.value = null
        _currentDisplayName.value = ""
    }

    private suspend fun clearLocalDatabase() {
        withContext(Dispatchers.IO) {
            appDatabase.clearAllTables()
        }
    }


    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authUiState.value = AuthUiState.Error("Email and password cannot be empty")
            return
        }

        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            try {
                repo.signUp(email, password)
                val user = repo.retrieveUser()
                val userId = user?.id

                if (userId == null) {
                    _authUiState.value = AuthUiState.Error("Sign up succeeded but user could not be read")
                    _authStatus.value = AuthStatus.LoggedOut
                    return@launch
                }
                saveToken()
                saveUserId(userId)
                saveUserEmail(user.email)
                syncScheduler.rescheduleAllSync()
                _authStatus.value = AuthStatus.LoggedIn(userId, isNewUser = true)
                _authUiState.value = AuthUiState.Success("Registered user successfully")
            } catch (e: Exception) {
                _authUiState.value = AuthUiState.Error("Sign up failed: ${e.message}")
                _authStatus.value = AuthStatus.LoggedOut
            }
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authUiState.value = AuthUiState.Error("Email and password cannot be empty")
            return
        }
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            try {
                repo.signIn(email, password)
                val user = repo.retrieveUser()

                if (user == null) {
                    _authUiState.value = AuthUiState.Error("User not found. Please sign up.")
                    _authStatus.value = AuthStatus.LoggedOut
                    return@launch
                }

                saveUserEmail(user.email)
                runCatching { repo.getProfile(user.id) }
                    .getOrNull()?.let {
                        _currentDisplayName.value = it.display_name
                        sharedPref.saveStringData(PREF_DISPLAY_NAME, it.display_name)
                    }
                saveToken()
                saveUserId(user.id)
                syncScheduler.rescheduleAllSync()
                _authStatus.value = AuthStatus.LoggedIn(user.id)
                _authUiState.value = AuthUiState.Success("Signed in successfully")

            } catch (e: Exception) {
                _authUiState.value = AuthUiState.Error("Sign in failed: ${e.message}")
                _authStatus.value = AuthStatus.LoggedOut
            }
        }
    }

    fun signOut(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.SignOutLoading

            runCatching { syncManager.replayPendingOps() }
            runCatching { repo.signOut() }
            runCatching { clearLocalDatabase() }
            runCatching { syncScheduler.cancelAllSync() }

            clearCachedAuth()
            _authStatus.value = AuthStatus.LoggedOut
            _authUiState.value = AuthUiState.Idle
            onComplete()
        }
    }

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.DeleteLoading
            try {
                repo.deleteAccount()
                runCatching { clearLocalDatabase() }
                runCatching { syncScheduler.cancelAllSync() }
                clearCachedAuth()
                _authStatus.value = AuthStatus.LoggedOut
                _authUiState.value = AuthUiState.Idle
                onComplete()
            } catch (e: Exception) {
                _authUiState.value = AuthUiState.Error("Failed to delete account: ${e.message}")
            }
        }
    }

    fun resetAuthState() {
        _authUiState.value = AuthUiState.Idle
    }

    fun onProfileSetupComplete() {
        val current = _authStatus.value
        if (current is AuthStatus.LoggedIn) {
            _authStatus.value = AuthStatus.LoggedIn(current.userId, isNewUser = false)
        }
    }

    fun updateDisplayName(
        name: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val userId = repo.getCurrentUserId() ?: run {
                onResult(Result.failure(IllegalStateException("Not logged in")))
                return@launch
            }

            _currentDisplayName.value = name
            sharedPref.saveStringData(PREF_DISPLAY_NAME, name)

            val payload = PendingProfilePayload(userId = userId, displayName = name)
            pendingOpDao.insert(
                PendingOpEntity(
                    type = PendingOpType.PROFILE_UPDATE,
                    entityId = userId,
                    parentId = null,
                    payloadJson = Json.encodeToString(payload),
                    createdAt = System.currentTimeMillis()
                )
            )
            syncScheduler.enqueueOneTimeSync()
            onResult(Result.success(Unit))
        }
    }
}