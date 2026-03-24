package `is`.hi.present.ui.ownedwishlist.item

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `is`.hi.present.data.repository.WishlistItemRepository
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

    private val _effects = Channel<ItemDetailEffect>(Channel.Factory.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun load(itemId: String) {
        viewModelScope.launch {
            itemRepo.observeWishlistItem(itemId).collect { item ->
                if (item != null) {
                    _uiState.value = _uiState.value.copy(
                        name = item.name,
                        notes = item.notes.orEmpty(),
                        url = item.url.orEmpty(),
                        priceText = item.price?.toInt()?.toString().orEmpty(),
                        imageUrl = itemRepo.getWishlistImage(item.imagePath).getOrNull()
                    )
                }
            }
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            itemRepo.fetchWishlistItemRemoteById(itemId)
            .onSuccess { item ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    name = item.name,
                    notes = item.notes.orEmpty(),
                    url = item.url.orEmpty(),
                    priceText = item.price?.toInt()?.toString().orEmpty(),
                    imageUrl = itemRepo.getWishlistImage(item.imagePath).getOrNull(),
                    errorMessage = null
                )
            }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Tókst ekki að sækja gjöf"
                )
            }
        }
    }

    fun save(itemId: String, wishlistId: String, context: Context, selectedImageUri: Uri?
        ) = viewModelScope.launch {
        val s = _uiState.value
        if (s.name.trim().isBlank()) {
            _uiState.value = s.copy(errorMessage = "Nafn má ekki vera tómt")
            return@launch
        }

        _uiState.value = s.copy(isLoading = true, errorMessage = null)
        val price = s.priceText.trim()
            .takeIf { it.isNotBlank() }
            ?.replace(',', '.')
            ?.toDoubleOrNull()

        val newImagePath =
            if (selectedImageUri != null) {
                itemRepo.uploadItemImage(context, wishlistId, selectedImageUri).getOrThrow()
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
            .onSuccess { _effects.send(ItemDetailEffect.NavigateBack) }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Tókst ekki að vista gjöf"
                )
            }
    }

    fun delete(itemId: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        itemRepo.deleteWishlistItem(itemId)
            .onSuccess {
                _effects.send(ItemDetailEffect.NavigateBack)
            }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Tókst ekki að eyða gjöf"
                )
            }
    }

    fun uploadNewImage(
        context: Context,
        wishlistId: String,
        selectedImageUri: Uri
    ) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        itemRepo.uploadItemImage(
            context = context,
            wishlistId = wishlistId,
            selectedImageUri = selectedImageUri
        )
            .onSuccess { filename ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    imageUrl = itemRepo.getWishlistImage(filename).getOrNull()
                )
            }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Tókst ekki að hlaða inn mynd"
                )
            }
    }

    fun onNameChange(v: String) { _uiState.value = _uiState.value.copy(name = v) }
    fun onNotesChange(v: String) { _uiState.value = _uiState.value.copy(notes = v) }
    fun onPriceChange(v: String) { _uiState.value = _uiState.value.copy(priceText = v) }
    fun onUrlChange(v: String) { _uiState.value = _uiState.value.copy(url = v) }

    fun onImageSelected(uriString: String?) {
        _uiState.value = _uiState.value.copy(imageUrl = uriString)
    }
}