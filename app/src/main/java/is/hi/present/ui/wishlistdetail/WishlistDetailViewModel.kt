package `is`.hi.present.ui.wishlistdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hi.present.data.repository.WishlistItemRepository
import `is`.hi.present.data.repository.WishlistsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WishlistDetailViewModel(
    private val repo: WishlistsRepository = WishlistsRepository(),
    private val itemRepo: WishlistItemRepository = WishlistItemRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistDetailUiState())
    val uiState: StateFlow<WishlistDetailUiState> = _uiState

    fun loadAll(wishlistId: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        try {
            val w = repo.getWishlistById(wishlistId)
            val items = itemRepo.getWishlistItems(wishlistId).map { it ->
                WishlistItemUi(
                    id = it.id,
                    title = it.title,
                    description = it.description
                )
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                title = w.title,
                description = w.description,
                item = items,
                errorMessage = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = e.message ?: "Failed to load wishlist"
            )
        }
    }
}
