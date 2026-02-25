package `is`.hi.present.ui.wishlistdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hi.present.data.repository.WishlistItemRepository
import `is`.hi.present.data.repository.WishlistsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import `is`.hi.present.data.repository.AuthRepository

class WishlistDetailViewModel(
    private val repo: WishlistsRepository = WishlistsRepository(),
    private val repoAuth: AuthRepository = AuthRepository(),
    private val itemRepo: WishlistItemRepository = WishlistItemRepository()
) : ViewModel() {
    private val _effects = Channel<WishlistDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val _uiState = MutableStateFlow(WishlistDetailUiState())
    val uiState: StateFlow<WishlistDetailUiState> = _uiState

    fun loadAll(wishlistId: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        try {
            val w = repo.getWishlistById(wishlistId)
            val currentUserId = repoAuth.getCurrentUserId()

            val items = itemRepo.getWishlistItems(wishlistId).map {
                WishlistItemUi(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    price = it.price
                )
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                title = w.title,
                description = w.description,
                item = items,
                isOwner = (w.ownerId == currentUserId),
                errorMessage = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = e.message ?: "Failed to load wishlist"
            )
        }
    }

    fun createWishlistItem(
        wishlistId: String,
        title: String,
        description: String? = null,
        url: String? = null,
        price: Double? = null
    ) = viewModelScope.launch {
        if (title.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Title má ekki vera tómt")
            return@launch
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        try {
            itemRepo.createWishlistItem(
                wishlistId = wishlistId,
                title = title.trim(),
                description = description?.trim()?.takeIf { it.isNotBlank() },
                url = url?.trim()?.takeIf { it.isNotBlank() },
                price = price
            )

            val items = itemRepo.getWishlistItems(wishlistId).map { item ->
                WishlistItemUi(
                    id = item.id,
                    title = item.title,
                    description = item.description,
                    price = item.price
                )
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                item = items,
                errorMessage = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = e.message ?: "Tókst ekki að búa til item"
            )
        }
    }

    fun onShareClicked(wishlistId: String) = viewModelScope.launch {
        try {
            val code = repo.createShareCode(wishlistId)
            _effects.send(WishlistDetailEffect.ShowShareCode(code))
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = e.message ?: "Tókst ekki að búa til invite code"
            )
        }
    }
}
