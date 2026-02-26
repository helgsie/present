package `is`.hi.present.ui.sharedWishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `is`.hi.present.data.repository.WishlistsRepository
import `is`.hi.present.ui.wishlists.WishlistUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedWishlistViewModel @Inject constructor (
    private val repo: WishlistsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SharedWishlistUiState())
    val uiState: StateFlow<SharedWishlistUiState> = _uiState

    init {
        loadSharedWishlists()
    }

    fun loadSharedWishlists() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        try {
            val wishlists = repo.getSharedWishlists().map {
                WishlistUi(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    iconKey = it.iconKey
                )
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                wishlists = wishlists
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = e.message ?: "Failed to load shared wishlists"
            )
        }
    }
}