package `is`.hi.present.ui.wishlistdetail

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hi.present.BuildConfig
import `is`.hi.present.data.repository.WishlistItemRepository
import `is`.hi.present.data.repository.WishlistsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import `is`.hi.present.data.repository.AuthRepository
import `is`.hi.present.ui.components.WishlistIcon
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val itemRepo: WishlistItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemDetailUiState())
    val uiState: StateFlow<ItemDetailUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ItemDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun load(itemId: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        try {
            val item = itemRepo.getWishlistItemById(itemId)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                name = item.name,
                notes = item.notes.orEmpty(),
                priceText = item.price?.toString().orEmpty(),
                imageUrl = item.imagePath
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = e.message ?: "Failed to load item"
            )
        }
    }

    fun save(itemId: String) = viewModelScope.launch {
        val s = _uiState.value
        if (s.name.trim().isBlank()) {
            _uiState.value = s.copy(errorMessage = "Name má ekki vera tómt")
            return@launch
        }

        _uiState.value = s.copy(isLoading = true, errorMessage = null)
        try {
            val price = s.priceText.trim()
                .takeIf { it.isNotBlank() }
                ?.replace(',', '.')
                ?.toDoubleOrNull()

            itemRepo.updateWishlistItem(
                itemId = itemId,
                name = s.name.trim(),
                notes = s.notes.trim().ifBlank { null },
                price = price
            )
            _effects.send(ItemDetailEffect.NavigateBack)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = e.message ?: "Failed to save item"
            )
        }
    }

    fun delete(itemId: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        try {
            itemRepo.deleteWishlistItem(itemId)
            _effects.send(ItemDetailEffect.NavigateBack)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = e.message ?: "Failed to delete item"
            )
        }
    }

    fun onNameChange(v: String) { _uiState.value = _uiState.value.copy(name = v) }
    fun onNotesChange(v: String) { _uiState.value = _uiState.value.copy(notes = v) }
    fun onPriceChange(v: String) { _uiState.value = _uiState.value.copy(priceText = v) }
}