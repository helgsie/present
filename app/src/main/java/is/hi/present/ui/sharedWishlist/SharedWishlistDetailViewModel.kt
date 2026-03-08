package `is`.hi.present.ui.sharedWishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `is`.hi.present.data.repository.WishlistItemRepository
import `is`.hi.present.data.repository.WishlistRepository
import `is`.hi.present.ui.wishlistdetail.WishlistItemUi
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SharedWishlistDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val title: String = "",
    val description: String? = null,
    val items: List<WishlistItemUi> = emptyList()
) {
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && items.isEmpty()
}

@HiltViewModel
class SharedWishlistDetailViewModel @Inject constructor(
    private val wishlistRepo: WishlistRepository,
    private val itemRepo: WishlistItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SharedWishlistDetailUiState())
    val uiState: StateFlow<SharedWishlistDetailUiState> = _uiState.asStateFlow()

    fun load(wishlistId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val wishlistResult = wishlistRepo.fetchWishlistRemoteById(wishlistId)
            val itemsResult = itemRepo.fetchWishlistItemsRemote(wishlistId)

            if (wishlistResult.isFailure || itemsResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ekki tókst að sækja óskalista."
                    )
                }
                return@launch
            }

            val wishlist = wishlistResult.getOrThrow()
            val items = itemsResult.getOrThrow()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    title = wishlist.title,
                    description = wishlist.description,
                    items = items.map { item ->
                        WishlistItemUi(
                            id = item.id,
                            name = item.name,
                            notes = item.notes,
                            price = item.price,
                            imagePath = item.imagePath
                        )
                    }
                )
            }
        }
    }
}