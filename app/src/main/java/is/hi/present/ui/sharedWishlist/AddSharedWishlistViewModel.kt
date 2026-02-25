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

    fun clearError() {
        _uiState.value = _uiState.value.copy(
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

            val raw = e.message.orEmpty()

            val friendlyMessage = when {
                raw.contains("Owner cannot join own wishlist", ignoreCase = true) ->
                    "Owner cannot join own wishlist"

                raw.contains("Invalid link", ignoreCase = true) ->
                    "Invalid invite code"

                else ->
                    "Could not join wishlist"
            }

            _uiState.value = AddSharedWishlistUiState(
                isLoading = false,
                error = friendlyMessage
            )
        }
    }
}