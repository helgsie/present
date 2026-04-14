package `is`.hi.present.ui.sharedwishlist.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `is`.hi.present.BuildConfig
import `is`.hi.present.data.repository.AuthRepository
import `is`.hi.present.data.repository.WishlistItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STORAGE_URL = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/wishlist-images/"
@HiltViewModel
class SharedItemDetailViewModel @Inject constructor(
    private val itemRepo: WishlistItemRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SharedItemDetailUiState())
    val uiState: StateFlow<SharedItemDetailUiState> = _uiState.asStateFlow()

    private fun toPublicImageUrl(path: String): String {
        return if (path.startsWith("http://") || path.startsWith("https://")) {
            path
        } else {
            "$STORAGE_URL$path"
        }
    }
    fun load(itemId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val itemResult = itemRepo.fetchWishlistItemRemoteById(itemId)

            itemResult
                .onSuccess { item ->
                    val currentUserId = authRepo.getCurrentUserId()

                    val claims = itemRepo
                        .getClaimsForItems(listOf(item.id))
                        .getOrElse { emptyList() }

                    val claim = claims.firstOrNull()
                    val isClaimedByMe = claim?.claimedBy == currentUserId
                    val claimedByName = if (claim != null && !isClaimedByMe) {
                        authRepo.getProfile(claim.claimedBy)?.display_name
                    } else null

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            id = item.id,
                            name = item.name,
                            notes = item.notes,
                            url = item.url,
                            price = item.price,
                            imagePath = item.imagePath?.let(::toPublicImageUrl),
                            category = item.category,
                            isClaimed = claim != null,
                            isClaimedByMe = isClaimedByMe,
                            claimedByName = claimedByName,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Ekki tókst að sækja gjöf."
                        )
                    }
                }
        }
    }

    fun claim(itemId: String) {
        viewModelScope.launch {
            itemRepo.claimItem(itemId)
                .onSuccess {
                    load(itemId)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            errorMessage = e.message ?: "Tókst ekki að taka frá gjöf"
                        )
                    }
                }
        }
    }

    fun release(itemId: String) {
        viewModelScope.launch {
            itemRepo.releaseClaim(itemId)
                .onSuccess {
                    load(itemId)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            errorMessage = e.message ?: "Tókst ekki að hætta við frátekningu"
                        )
                    }
                }
        }
    }
}