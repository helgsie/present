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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STORAGE_URL = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/wishlist-images/"

@HiltViewModel
class WishlistDetailViewModel @Inject constructor(
    private val repo: WishlistsRepository,
    private val repoAuth: AuthRepository,
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
            val currentUserId = repoAuth.getCurrentUserId()
            val isOwner = (w.ownerId == currentUserId)
            val rawItems = itemRepo.getWishlistItems(wishlistId)
            val items = if (isOwner) {
                rawItems.map { item ->
                    WishlistItemUi(
                        id = item.id,
                        name = item.name,
                        notes = item.notes,
                        price = item.price,
                        imagePath = item.imagePath?.let { path -> "$STORAGE_URL$path" },
                        isClaimed = false,
                        isClaimedByMe = false
                    )
                }
            } else {
                val claims = itemRepo.getClaimsForItems(rawItems.map { it.id })
                val claimByItemId = claims.associateBy { it.itemId }

                rawItems.map { item ->
                    val claim = claimByItemId[item.id]

                    WishlistItemUi(
                        id = item.id,
                        name = item.name,
                        notes = item.notes,
                        price = item.price,
                        imagePath = item.imagePath?.let { path -> "$STORAGE_URL$path" },
                        isClaimed = claim != null,
                        isClaimedByMe = claim?.claimedBy == currentUserId
                    )
                }
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
        name: String,
        notes: String? = null,
        url: String? = null,
        price: Double? = null,
        selectedImageUri: Uri? = null,
        context: Context
    ) = viewModelScope.launch {
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Name má ekki vera tómt")
            return@launch
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        try {
            val imageUrl = selectedImageUri?.let { uri ->
                itemRepo.uploadItemImage(context, wishlistId, uri)
            }
            itemRepo.createWishlistItem(
                wishlistId = wishlistId,
                name = name.trim(),
                notes = notes?.trim()?.takeIf { it.isNotBlank() },
                url = url?.trim()?.takeIf { it.isNotBlank() },
                price = price,
                imagePath = imageUrl
            )

            val items = itemRepo.getWishlistItems(wishlistId).map { item ->
                WishlistItemUi(
                    id = item.id,
                    name = item.name,
                    notes = item.notes,
                    price = item.price,
                    imagePath = item.imagePath?.let { path -> "$STORAGE_URL$path" }
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

    fun claimItem(wishlistId: String, itemId: String) = viewModelScope.launch {
        try {
            itemRepo.claimItem(itemId)
            loadAll(wishlistId)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = e.message ?: "Could not claim item"
            )
        }
    }

    fun releaseClaim(wishlistId: String, itemId: String) = viewModelScope.launch {
        try {
            itemRepo.releaseClaim(itemId)
            loadAll(wishlistId)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = e.message ?: "Could not release claim"
            )
        }
    }

}
