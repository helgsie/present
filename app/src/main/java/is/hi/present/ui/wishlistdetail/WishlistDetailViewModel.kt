package `is`.hi.present.ui.wishlistdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hi.present.data.repository.WishlistItemRepository
import `is`.hi.present.data.repository.WishlistsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishlistDetailViewModel @Inject constructor(
    private val repo: WishlistsRepository,
    private val itemRepo: WishlistItemRepository
) : ViewModel() {
    private val _effects = Channel<WishlistDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val _uiState = MutableStateFlow(WishlistDetailUiState())
    val uiState: StateFlow<WishlistDetailUiState> = _uiState.asStateFlow()

    fun loadAll(wishlistId: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        try {
            val w = repo.getWishlistById(wishlistId)
            val items = itemRepo.getWishlistItems(wishlistId).map {
                WishlistItemUi(
                    id = it.id,
                    name = it.name,
                    notes = it.notes,
                    price = it.price
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

    fun createWishlistItem(
        wishlistId: String,
        name: String,
        notes: String? = null,
        url: String? = null,
        price: Double? = null
    ) = viewModelScope.launch {
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Name má ekki vera tómt")
            return@launch
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        try {
            itemRepo.createWishlistItem(
                wishlistId = wishlistId,
                name = name.trim(),
                notes = notes?.trim()?.takeIf { it.isNotBlank() },
                url = url?.trim()?.takeIf { it.isNotBlank() },
                price = price
            )

            val items = itemRepo.getWishlistItems(wishlistId).map { item ->
                WishlistItemUi(
                    id = item.id,
                    name = item.name,
                    notes = item.notes,
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
