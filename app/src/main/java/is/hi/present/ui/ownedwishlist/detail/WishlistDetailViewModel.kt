package `is`.hi.present.ui.ownedwishlist.detail

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `is`.hi.present.BuildConfig
import `is`.hi.present.data.repository.AuthRepository
import `is`.hi.present.data.repository.WishlistItemRepository
import `is`.hi.present.data.repository.WishlistRepository
import `is`.hi.present.ui.components.WishlistIcon
import kotlinx.coroutines.Job
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
    return when {
        path.startsWith("http://") || path.startsWith("https://") -> path
        path.startsWith("pending://") -> "file://${path.removePrefix("pending://")}"
        else -> "$STORAGE_URL$path"
    }
}

@HiltViewModel
class WishlistDetailViewModel @Inject constructor(
    private val wishlistRepo: WishlistRepository,
    private val authRepo: AuthRepository,
    private val itemRepo: WishlistItemRepository
) : ViewModel() {

    private val _effects = Channel<WishlistDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val _uiState = MutableStateFlow(WishlistDetailUiState())
    val uiState: StateFlow<WishlistDetailUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var currentWishlistId: String? = null
    private var isReorderingLocally = false

    fun loadAll(wishlistId: String) {
        ensureObserver(wishlistId)

        viewModelScope.launch {
            refreshInternal(wishlistId, fromUser = false)
        }
    }

    // Tryggir að það sé bara einn observer fyrir current wishlist
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

                    val itemUi = items.map { item ->
                        WishlistItemUi(
                            id = item.id,
                            name = item.name,
                            notes = item.notes,
                            price = item.price,
                            imagePath = item.imagePath?.let(::toPublicImageUrl),
                            category = item.category,
                            isClaimed = false,
                            isClaimedByMe = false
                        )
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
                                isShared = false,
                                errorMessage = state.errorMessage
                            )
                        }
                        return@collect
                    }

                    if (wishlist.ownerId != currentUserId) {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                title = "",
                                description = "",
                                items = emptyList(),
                                isOwner = false,
                                isShared = false,
                                errorMessage = "Þessi skjár er aðeins fyrir eigin óskalista."
                            )
                        }
                        return@collect
                    }

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            title = wishlist.title,
                            description = wishlist.description,
                            items = if (isReorderingLocally) state.items else itemUi,
                            isOwner = true,
                            isShared = wishlist.isShared,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun refresh(wishlistId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            refreshInternal(wishlistId, fromUser = true)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    // Refreshar wishlist og items í local cache og setur offline/error state ef þarf
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
    fun onMoveItem(fromIndex: Int, toIndex: Int) {
        val currentItems = _uiState.value.items

        if (fromIndex !in currentItems.indices || toIndex !in currentItems.indices) return
        if (fromIndex == toIndex) return

        isReorderingLocally = true

        val updatedItems = WishlistReorderUtils.moveItem(
            items = currentItems,
            fromIndex = fromIndex,
            toIndex = toIndex
        )

        _uiState.update {
            it.copy(items = updatedItems)
        }
    }

    fun persistReorderedItems() = viewModelScope.launch {
        val wishlistId = currentWishlistId ?: return@launch
        val items = _uiState.value.items

        _uiState.update { it.copy(errorMessage = null) }

        itemRepo.updateWishlistItemOrder(
            wishlistId = wishlistId,
            orderedItemIds = items.map { it.id }
        )
            .onSuccess {
                isReorderingLocally = false
            }
            .onFailure { error ->
                isReorderingLocally = false
                _uiState.update {
                    it.copy(
                        errorMessage = error.message ?: "Tókst ekki að vista röðun"
                    )
                }
            }
    }

    // Býr til nýtt item og hleður mynd upp ef user valdi mynd
    fun createWishlistItem(
        wishlistId: String,
        name: String,
        notes: String? = null,
        url: String? = null,
        price: Double? = null,
        category: String? = null,
        selectedImageUri: Uri? = null,
        context: Context,
        onDone: (() -> Unit)? = null
    ) = viewModelScope.launch {
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Gefa þarf gjöf nafn")
            return@launch
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        try {
            val imageUrl = selectedImageUri?.let { uri ->
                itemRepo.uploadItemImage(context, wishlistId, uri).getOrThrow()
            }

            itemRepo.createWishlistItem(
                wishlistId = wishlistId,
                name = name.trim(),
                notes = notes?.trim()?.takeIf { it.isNotBlank() },
                url = url?.trim()?.takeIf { it.isNotBlank() },
                price = price,
                imagePath = imageUrl,
                category = category
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                    onDone?.invoke()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Tókst ekki að vista gjöf"
                    )
                }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Tókst ekki að hlaða upp mynd"
            )
        }
    }



    // Uppfærir title/description/icon á wishlist og sendir save effect
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

    // Eyðir wishlist ef current user er owner og fer til baka
    fun deleteWishlist(
        wishlistId: String,
        onDone: (() -> Unit)? = null
    ) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)

        wishlistRepo.deleteWishlist(wishlistId)
            .onSuccess {
                _uiState.value = WishlistDetailUiState(isLoading = false)
                _effects.send(WishlistDetailEffect.NavigateBack)
            }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Tókst ekki að eyða óskalista"
                )
            }
    }


    // Býr til invite code til að deila wishlist
    fun onShareClicked(wishlistId: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        wishlistRepo.createShareCode(wishlistId)
            .onSuccess { code ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
                _effects.send(WishlistDetailEffect.ShowShareCode(code))
            }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Tókst ekki að búa til invite code"
                )
            }
    }

    // Hleður lista af þeim sem hafa aðgang að wishlist
    fun onSharedWith(wishlistId: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(sharedWithError = null)

        wishlistRepo.getSharedWithUsers(wishlistId)
            .onSuccess { users ->
                _uiState.value = _uiState.value.copy(
                    sharedWithUsers = users,
                    sharedWithError = null
                )
            }
            .onFailure {
                if (_uiState.value.sharedWithUsers.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        sharedWithError = "Ekkert netsamband. Ekki hægt að sækja þátttakendur."
                    )
                }
            }
    }

    // Fjarlægir einn shared user af wishlist og hleður skjánum aftur
    fun removeSharedUser(wishlistId: String, userId: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        wishlistRepo.removeFromWishlist(
            wishlistId = wishlistId,
            userId = userId
        )
            .onSuccess {
                onSharedWith(wishlistId)
                loadAll(wishlistId)
            }
            .onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Tókst ekki að fjarlægja aðgang"
                )
            }
    }


    // Sækir fresh detail gögn + claims og rebuildar item listann handvirkt
    private suspend fun reloadDetailState(wishlistId: String) {
        val wishlistResult = wishlistRepo.fetchWishlistRemoteById(wishlistId)
        val itemsResult = itemRepo.fetchWishlistItemsRemote(wishlistId)

        if (wishlistResult.isFailure || itemsResult.isFailure) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Ekki tókst að sækja óskalista."
                )
            }
            return
        }

        val currentUserId = authRepo.getCurrentUserId()
        val wishlist = wishlistResult.getOrThrow()
        val items = itemsResult.getOrThrow()

        if (wishlist.ownerId != currentUserId) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    title = "",
                    description = "",
                    items = emptyList(),
                    isOwner = false,
                    isShared = false,
                    errorMessage = "Þessi skjár er aðeins fyrir eigin óskalista."
                )
            }
            return
        }

        val itemUi = items.map { item ->
            WishlistItemUi(
                id = item.id,
                name = item.name,
                notes = item.notes,
                price = item.price,
                imagePath = item.imagePath?.let(::toPublicImageUrl),
                isClaimed = false,
                isClaimedByMe = false
            )
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                title = wishlist.title,
                description = wishlist.description,
                items = if (isReorderingLocally) it.items else itemUi,
                isOwner = true,
                isShared = wishlist.isShared,
                errorMessage = null
            )
        }
    }
}