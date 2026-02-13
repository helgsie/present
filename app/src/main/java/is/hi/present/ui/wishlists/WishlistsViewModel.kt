package `is`.hi.present.ui.wishlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import `is`.hi.present.data.repository.WishlistsRepository
import `is`.hi.present.ui.Enums.WishlistIcon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WishlistsViewModel(
    private val repo: WishlistsRepository = WishlistsRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(WishlistsUiState(isLoading = true))
    val uiState: StateFlow<WishlistsUiState> = _uiState

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
                wishlists = wishlists
            )
        } catch (e: Exception) {
            _uiState.value = WishlistsUiState(
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
}
