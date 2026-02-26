package `is`.hi.present.ui.wishlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `is`.hi.present.data.repository.WishlistsRepository
import `is`.hi.present.ui.components.WishlistIcon
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class WishlistsViewModel @Inject constructor(
    private val repo: WishlistsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistsUiState(isLoading = true))
    val uiState: StateFlow<WishlistsUiState> = _uiState.asStateFlow()

    init {
        loadWishlists()
    }

    fun loadWishlists() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        try {
            val wishlists = repo.getWishlists()
                .sortedByDescending { it.createdAt ?: "" }
                .map { w ->
                    WishlistUi(
                        id = w.id,
                        title = w.title,
                        description = w.description,
                        iconKey = w.iconKey
                    )
                }

            _uiState.value = WishlistsUiState(
                isLoading = false,
                wishlists = wishlists,
                errorMessage = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = e.message ?: "Failed to fetch wishlists"
            )
        }
    }

    fun createWishlist(
        title: String,
        description: String? = null,
        onDone: (() -> Unit)? = null,
        icon: WishlistIcon
    ) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        try {
            repo.createWishlist(title, description, icon)
            loadWishlists()
            onDone?.invoke()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = e.message ?: "Failed to create wishlist"
            )
        }
    }

    fun updateWishlist(
        wishlistId: String,
        title: String,
        description: String?,
        icon: WishlistIcon,
        onDone: (() -> Unit)? = null
    ) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        try {
            repo.updateWishlist(
                wishlistId = wishlistId,
                title = title,
                description = description,
                icon = icon
            )
            loadWishlists()
            onDone?.invoke()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = e.message ?: "Failed to update wishlist"
            )
        }
    }

    fun deleteWishlist(
        wishlistId: String,
        onDone: (() -> Unit)? = null
    ) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        try {
            repo.deleteWishlist(wishlistId)
            loadWishlists()
            onDone?.invoke()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = e.message ?: "Failed to delete wishlist"
            )
        }
    }
}