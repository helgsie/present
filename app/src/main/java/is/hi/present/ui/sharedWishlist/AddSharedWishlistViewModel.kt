package `is`.hi.present.ui.sharedWishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hi.present.data.repository.WishlistsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddSharedWishlistViewModel(
    private val repo: WishlistsRepository = WishlistsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddSharedWishlistUiState())
    val uiState: StateFlow<AddSharedWishlistUiState> = _uiState

    fun clearJoinedState() {
        _uiState.value = _uiState.value.copy(
            joinedWishlistId = null,
            error = null
        )
    }

    fun joinByToken(code: String) = viewModelScope.launch {
        if (code.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Code má ekki vera tómt"
            )
            return@launch
        }

        _uiState.value = AddSharedWishlistUiState(isLoading = true)

        try {
            val wishlistId = repo.joinByToken(code)
            _uiState.value = AddSharedWishlistUiState(
                isLoading = false,
                joinedWishlistId = wishlistId
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = AddSharedWishlistUiState(
                isLoading = false,
                error = e.message ?: "Join failed"
            )
        }
    }
}