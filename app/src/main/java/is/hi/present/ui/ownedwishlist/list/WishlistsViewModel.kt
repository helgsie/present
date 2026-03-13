package `is`.hi.present.ui.ownedwishlist.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `is`.hi.present.BuildConfig
import `is`.hi.present.data.repository.WishlistRepository
import `is`.hi.present.ui.components.WishlistIcon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STORAGE_URL = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/wishlist-images/"

private fun toPublicImageUrl(path: String): String {
    return if (path.startsWith("http://") || path.startsWith("https://")) {
        path
    } else {
        "$STORAGE_URL$path"
    }
}

@HiltViewModel
class WishlistsViewModel @Inject constructor(
    private val repo: WishlistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistsUiState())
    val uiState: StateFlow<WishlistsUiState> = _uiState.asStateFlow()

    fun loadWishlists(ownerId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            repo.fetchMyWishlistCards()
                .onSuccess { cards ->
                    val uiWishlists = cards.map { dto ->
                        WishlistUi(
                            id = dto.id,
                            title = dto.title,
                            description = dto.description,
                            iconKey = dto.iconKey,
                            itemCount = dto.itemCount.toInt(),
                            isShared = dto.isShared,
                            previewImageUrls = dto.previewImageUrls.mapNotNull(::toPublicImageUrl)                        )
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            wishlists = uiWishlists,
                            errorMessage = null,
                            offlineBanner = null
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            offlineBanner = if (state.wishlists.isNotEmpty())
                                "Ekkert netsamband. Vistuð gögn eru sýnd."
                            else
                                "Ekkert netsamband."
                        )
                    }
                }
        }
    }

    fun refresh(ownerId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            repo.fetchMyWishlistCards()
                .onSuccess { cards ->
                    val uiWishlists = cards.map { dto ->
                        WishlistUi(
                            id = dto.id,
                            title = dto.title,
                            description = dto.description,
                            iconKey = dto.iconKey,
                            itemCount = dto.itemCount.toInt(),
                            isShared = dto.isShared,
                            previewImageUrls = dto.previewImageUrls.mapNotNull(::toPublicImageUrl)                        )
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            wishlists = uiWishlists,
                            errorMessage = null,
                            offlineBanner = null,
                            offlineDialog = null
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            isRefreshing = false,
                            isLoading = false,
                            offlineBanner = if (state.wishlists.isNotEmpty())
                                "Ekkert netsamband. Vistuð gögn eru sýnd."
                            else
                                "Ekkert netsamband.",
                            offlineDialog = OfflineDialog(
                                title = "Ekkert netsamband",
                                message = "Vinsamlegast athugaðu netsamband og/eða reyndu aftur."
                            )
                        )
                    }
                }
        }
    }

    fun consumeOfflineDialog() {
        _uiState.update { it.copy(offlineDialog = null) }
    }

    fun createWishlist(
        ownerId: String,
        title: String,
        description: String? = null,
        onDone: (() -> Unit)? = null,
        icon: WishlistIcon
    ) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, offlineBanner = null) }

        repo.createWishlist(ownerId, title, description, icon)
            .onSuccess {
                loadWishlists(ownerId)
                onDone?.invoke()
            }
            .onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Tókst ekki að búa til óskalista"
                    )
                }
            }
    }
}
