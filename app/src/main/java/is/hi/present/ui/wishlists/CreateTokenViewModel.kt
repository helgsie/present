package `is`.hi.present.ui.wishlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `is`.hi.present.data.repository.WishlistsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTokenViewModel @Inject constructor (
    private val repo: WishlistsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinWishlistUiState())
    val uiState: StateFlow<JoinWishlistUiState> = _uiState

    fun join(token: String) = viewModelScope.launch {
        _uiState.value = JoinWishlistUiState(isLoading = true)
        try {
            val wishlistId = repo.joinByToken(token)
            _uiState.value = JoinWishlistUiState(isLoading = false, wishlistId = wishlistId)
        } catch (e: Exception) {
            _uiState.value = JoinWishlistUiState(isLoading = false, error = e.message ?: "Join failed")
        }
    }
}