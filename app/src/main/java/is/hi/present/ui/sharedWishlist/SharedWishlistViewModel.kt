package `is`.hi.present.ui.sharedWishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `is`.hi.present.BuildConfig
import `is`.hi.present.data.repository.WishlistRepository
import `is`.hi.present.ui.wishlists.WishlistUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

private const val STORAGE_URL = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/wishlist-images/"

private fun toPublicImageUrl(path: String?): String? {
    if (path.isNullOrBlank()) return null
    return if (path.startsWith("http://") || path.startsWith("https://")) {
        path
    } else {
        "$STORAGE_URL$path"
    }
}

@HiltViewModel
class SharedWishlistViewModel @Inject constructor(
    private val repo: WishlistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SharedWishlistUiState())
    val uiState: StateFlow<SharedWishlistUiState> = _uiState.asStateFlow()

    init {
        loadSharedWishlists()
    }

    fun loadSharedWishlists() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        repo.fetchSharedWishlistCards()
            .onSuccess { cards ->
                val wishlists = cards.map { dto ->
                    WishlistUi(
                        id = dto.id,
                        title = dto.title,
                        description = dto.description,
                        iconKey = dto.iconKey,
                        itemCount = dto.itemCount.toInt(),
                        isShared = dto.isShared,
                        previewImageUrl = toPublicImageUrl(dto.previewImageUrl)
                    )
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    wishlists = wishlists,
                    errorMessage = null
                )
            }
            .onFailure { e ->
                val friendly = when (e) {
                    is UnknownHostException,
                    is SocketTimeoutException -> "Netsamband þarf fyrir shared wishlists"
                    else -> "Tókst ekki að sækja shared wishlists"
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = friendly
                )
            }
    }

    fun leaveSharedWishlist(wishlistId: String) {
        viewModelScope.launch {
            repo.leaveSharedWishlist(wishlistId)
                .onSuccess {
                    loadSharedWishlists()
                }
                .onFailure {
                    _uiState.update {
                        it.copy(errorMessage = "Tókst ekki að yfirgefa lista")
                    }
                }
        }
    }
}