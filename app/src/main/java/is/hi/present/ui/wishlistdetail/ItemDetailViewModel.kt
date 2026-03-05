package `is`.hi.present.ui.wishlistdetail


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hi.present.data.repository.WishlistItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import android.net.Uri

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
                priceText = item.price?.toInt()?.toString().orEmpty(),
                imageUrl = item.imagePath
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = e.message ?: "Failed to load item"
            )
        }
    }

    fun save(itemId: String,
             wishlistId: String,
             context: Context,
             selectedImageUri: Uri?) = viewModelScope.launch {
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

            val newImagePath =
                if (selectedImageUri != null) {
                    itemRepo.uploadItemImage(context, wishlistId, selectedImageUri)
                } else {
                    s.imageUrl
                }

            itemRepo.updateWishlistItem(
                itemId = itemId,
                name = s.name.trim(),
                notes = s.notes.trim().ifBlank { null },
                price = price,
                imagePath = newImagePath
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

    fun uploadNewImage(
        context: Context,
        wishlistId: String,
        selectedImageUri: Uri
    ) = viewModelScope.launch {
        try {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val filename = itemRepo.uploadItemImage(
                context = context,
                wishlistId = wishlistId,
                selectedImageUri = selectedImageUri
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                imageUrl = filename
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = e.message ?: "Failed to upload image"
            )
        }
    }

    fun onNameChange(v: String) { _uiState.value = _uiState.value.copy(name = v) }
    fun onNotesChange(v: String) { _uiState.value = _uiState.value.copy(notes = v) }
    fun onPriceChange(v: String) { _uiState.value = _uiState.value.copy(priceText = v) }
    fun onImageSelected(uriString: String?) {
        _uiState.value = _uiState.value.copy(imageUrl = uriString)
    }
}