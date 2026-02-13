package `is`.hi.present.ui.wishlistdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hi.present.data.repository.WishlistsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WishlistDetailViewModel(
    private val repo: WishlistsRepository = WishlistsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistDetailUiState())
    val uiState: StateFlow<WishlistDetailUiState> = _uiState

    fun load(wishlistId: String) = viewModelScope.launch {
        _uiState.value = WishlistDetailUiState(isLoading = true)

        try {
            val w = repo.getWishlistById(wishlistId)
            _uiState.value = WishlistDetailUiState(
                isLoading = false,
                title = w.title,
                description = w.description
            )
        } catch (e: Exception) {
            _uiState.value = WishlistDetailUiState(
                isLoading = false,
                errorMessage = e.message ?: "Failed to load wishlist"
            )
        }
    }
}
