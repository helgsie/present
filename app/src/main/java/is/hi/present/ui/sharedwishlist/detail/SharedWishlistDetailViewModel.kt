package `is`.hi.present.ui.sharedwishlist.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `is`.hi.present.BuildConfig
import `is`.hi.present.data.repository.AuthRepository
import `is`.hi.present.data.repository.WishlistItemRepository
import `is`.hi.present.data.repository.WishlistRepository
import `is`.hi.present.ui.ownedwishlist.detail.WishlistItemUi
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

data class SharedWishlistDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val title: String = "",
    val description: String? = null,
    val items: List<WishlistItemUi> = emptyList(),
    val didLeaveWishlist: Boolean = false
) {
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && items.isEmpty()
}

@HiltViewModel
class SharedWishlistDetailViewModel @Inject constructor(
    private val wishlistRepo: WishlistRepository,
    private val itemRepo: WishlistItemRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SharedWishlistDetailUiState())
    val uiState: StateFlow<SharedWishlistDetailUiState> = _uiState.asStateFlow()

    fun load(wishlistId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    didLeaveWishlist = false
                )
            }

            val wishlistResult = wishlistRepo.fetchWishlistRemoteById(wishlistId)
            val itemsResult = itemRepo.fetchWishlistItemsRemote(wishlistId)

            if (wishlistResult.isFailure || itemsResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ekki tókst að sækja óskalista."
                    )
                }
                return@launch
            }

            val currentUserId = authRepo.getCurrentUserId()
            val wishlist = wishlistResult.getOrThrow()
            val items = itemsResult.getOrThrow()
            val claims = itemRepo
                .getClaimsForItems(items.map { it.id })
                .getOrElse { emptyList() }

            val claimByItemId = claims.associateBy { it.itemId }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    title = wishlist.title,
                    description = wishlist.description,
                    items = items.map { item ->
                        val claim = claimByItemId[item.id]
                        WishlistItemUi(
                            id = item.id,
                            name = item.name,
                            notes = item.notes,
                            price = item.price,
                            imagePath = item.imagePath?.let(::toPublicImageUrl),
                            isClaimed = claim != null,
                            isClaimedByMe = claim?.claimedBy == currentUserId,
                            claimedByUserName = claim?.claimedBy
                        )
                    }
                )
            }
        }
    }

    fun claimItem(wishlistId: String, itemId: String) = viewModelScope.launch {
        itemRepo.claimItem(itemId)
            .onSuccess { result ->
                when (result) {
                    "ok" -> load(wishlistId)

                    "access_revoked" -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Það er búið að fjarlægja þig af þessum óskalista."
                        )
                    }

                    "already_claimed" -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Það er þegar búið að taka þessa gjöf frá."
                        )
                        load(wishlistId)
                    }

                    "not_found" -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Gjöfin fannst ekki."
                        )
                    }

                    else -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Tókst ekki að taka frá gjöf"
                        )
                    }
                }
            }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Tókst ekki að taka frá gjöf"
                )
            }
    }

    fun releaseClaim(wishlistId: String, itemId: String) = viewModelScope.launch {
        itemRepo.releaseClaim(itemId)
            .onSuccess { result ->
                when (result) {
                    "ok" -> load(wishlistId)

                    "access_revoked" -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Það er búið að fjarlægja þig af þessum óskalista."
                        )
                    }

                    "not_found" -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Gjöfin fannst ekki."
                        )
                    }

                    else -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Tókst ekki að hætta við frátekningu"
                        )
                    }
                }
            }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Tókst ekki að hætta við frátekningu"
                )
            }
    }

    fun leaveSharedWishlist(wishlistId: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        wishlistRepo.leaveSharedWishlist(wishlistId)
            .onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null,
                    didLeaveWishlist = true
                )
            }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Tókst ekki að yfirgefa lista"
                )
            }
    }

    fun consumeLeaveSuccess() {
        _uiState.update {
            it.copy(didLeaveWishlist = false)
        }
    }
}