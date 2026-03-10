package `is`.hi.present.ui.wishlistdetail

import androidx.lifecycle.ViewModel
import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import `is`.hi.present.data.repository.AuthRepository
import `is`.hi.present.BuildConfig
import `is`.hi.present.data.repository.WishlistItemRepository
import `is`.hi.present.data.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import `is`.hi.present.ui.components.WishlistIcon
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
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
class WishlistDetailViewModel @Inject constructor(
    private val wishlistRepo: WishlistRepository,
    private val authRepo: AuthRepository,
    private val itemRepo: WishlistItemRepository
) : ViewModel() {
    // ----- STATE / EFFECTS -----
    private val _effects = Channel<WishlistDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val _uiState = MutableStateFlow(WishlistDetailUiState())
    val uiState: StateFlow<WishlistDetailUiState> = _uiState.asStateFlow()

    // ---- INTERNAL OBSERVER STATE -----
    private var observeJob: Job? = null
    private var currentWishlistId: String? = null

    // ---- LOAD / OBSERVE -----
    fun loadAll(wishlistId: String) {
        ensureObserver(wishlistId)

        viewModelScope.launch {
            refreshInternal(wishlistId, fromUser = false)
        }
    }

    private fun ensureObserver(wishlistId: String) {
        if (currentWishlistId == wishlistId && observeJob?.isActive == true) {
            return
        }

        currentWishlistId = wishlistId
        observeJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        observeJob = viewModelScope.launch {
            val wishlistFlow = wishlistRepo.observeWishlistById(wishlistId)
            val itemsFlow = itemRepo.observeWishlistItems(wishlistId)

            wishlistFlow
                .distinctUntilChanged()
                .combine(itemsFlow.distinctUntilChanged()) { wishlist, items ->
                    val currentUserId = authRepo.getCurrentUserId()
                    val isOwner = wishlist?.ownerId == currentUserId

                    val itemUi = if (isOwner) {
                        items.map {
                            WishlistItemUi(
                                id = it.id,
                                name = it.name,
                                notes = it.notes,
                                price = it.price,
                                imagePath = it.imagePath?.let(::toPublicImageUrl),
                                isClaimed = false,
                                isClaimedByMe = false
                            )
                        }
                    } else {
                        val claims = itemRepo.getClaimsForItems(items.map { it.id })
                            .getOrDefault(emptyList())
                        val claimByItemId = claims.associateBy { it.itemId }

                        items.map { item ->
                            val claim = claimByItemId[item.id]
                            WishlistItemUi(
                                id = item.id,
                                name = item.name,
                                notes = item.notes,
                                price = item.price,
                                imagePath = item.imagePath?.let(::toPublicImageUrl),
                                isClaimed = claim != null,
                                isClaimedByMe = claim?.claimedBy == currentUserId
                            )
                        }
                    }
                    Triple(wishlist, currentUserId, itemUi)
                }
                .collect { (wishlist, currentUserId, itemUi) ->
                    if (wishlist == null) {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                title = "",
                                description = "",
                                items = emptyList(),
                                isOwner = false,
                                errorMessage = state.errorMessage
                            )
                        }
                        return@collect
                    }

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            title = wishlist.title,
                            description = wishlist.description,
                            items = itemUi,
                            isOwner = (wishlist.ownerId == currentUserId),
                            errorMessage = null
                        )
                    }
                }
        }
    }

    // ----- REFRESH ------
    fun refresh(wishlistId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            refreshInternal(wishlistId, fromUser = true)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private suspend fun refreshInternal(
        wishlistId: String,
        fromUser: Boolean
    ) {
        val wishlistResult = wishlistRepo.refreshWishlistById(wishlistId)
        val itemsResult = itemRepo.refreshWishlistItems(wishlistId)

        val failed = wishlistResult.isFailure || itemsResult.isFailure

        if (!failed) {
            _uiState.update {
                it.copy(
                    offlineBanner = null,
                    errorMessage = null
                )
            }
            return
        }

        _uiState.update { state ->
            val hasCachedData = state.items.isNotEmpty() || state.title.isNotBlank()

            if (hasCachedData) {
                state.copy(
                    errorMessage = null,
                    offlineBanner = "Ekkert netsamband. Vistuð gögn eru sýnd."
                )
            } else {
                state.copy(
                    errorMessage = "Ekki tókst að sækja gögn.",
                    offlineBanner = null
                )
            }
        }
    }

    // ---- ITEM ACTIONS ----
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
            _uiState.value = _uiState.value.copy(errorMessage = "Gefa þarf gjöf nafn")
            return@launch
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        val imageUrl = selectedImageUri?.let { uri ->
            itemRepo.uploadItemImage(context, wishlistId, uri).getOrThrow()
        }

        itemRepo.createWishlistItem(
            wishlistId = wishlistId,
            name = name.trim(),
            notes = notes?.trim()?.takeIf { it.isNotBlank() },
            url = url?.trim()?.takeIf { it.isNotBlank() },
            price = price,
            imagePath = imageUrl
        )
            .onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
            }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Tókst ekki að búa til item"
                )
            }
    }

    fun claimItem(wishlistId: String, itemId: String) = viewModelScope.launch {
        itemRepo.claimItem(itemId)
            .onSuccess { loadAll(wishlistId) }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Tókst ekki að taka frá gjöf"
                )
            }
    }

    fun releaseClaim(wishlistId: String, itemId: String) = viewModelScope.launch {
        itemRepo.releaseClaim(itemId)
            .onSuccess { loadAll(wishlistId) }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Tókst ekki að hætta við frátekningu"
                )
            }
    }

    // ----- WISHLIST ACTIONS -----
    fun updateWishlist(
        wishlistId: String,
        title: String,
        description: String?,
        iconKey: String,
        onDone: (() -> Unit)? = null
    ) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        wishlistRepo.updateWishlist(
            wishlistId = wishlistId,
            title = title,
            description = description?.trim()?.takeIf { it.isNotBlank() },
            icon = WishlistIcon.fromKey(iconKey)
        )
            .onSuccess {
                loadAll(wishlistId)
                _effects.send(WishlistDetailEffect.WishlistSaved)
            }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Tókst ekki að uppfæra óskalista"
                )
            }
    }

    fun deleteWishlist(
        wishlistId: String,
        onDone: (() -> Unit)? = null
    ) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)

        wishlistRepo.deleteWishlist(wishlistId)
            .onSuccess {
                _uiState.value = WishlistDetailUiState(
                    isLoading = false
                )
                _effects.send(WishlistDetailEffect.NavigateBack)
            }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Tókst ekki að eyða óskalista"
                )
            }
    }

    fun onShareClicked(wishlistId: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        wishlistRepo.createShareCode(wishlistId)
            .onSuccess { code ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                _effects.send(WishlistDetailEffect.ShowShareCode(code))
            }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Tókst ekki að búa til invite code"
                )
            }
    }

    fun onSharedWith(wishlistId: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        wishlistRepo.getSharedWithEmails(wishlistId)
            .onSuccess { emails ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    sharedWithEmails = emails,
                    errorMessage = null
                )
            }
            .onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to load shared users"
                )
            }
    }
}
