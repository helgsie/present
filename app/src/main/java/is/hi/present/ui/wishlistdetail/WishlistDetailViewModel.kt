package `is`.hi.present.ui.wishlistdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hi.present.data.repository.AuthRepository
import `is`.hi.present.data.repository.WishlistItemRepository
import `is`.hi.present.data.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    // ---- INTERNAL FIELDS -----
    private var observeJob: Job? = null
    private var currentWishlistId: String? = null

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

                    val itemUi = items.map {
                        WishlistItemUi(
                            id = it.id,
                            name = it.name,
                            notes = it.notes,
                            price = it.price,
                            imagePath = it.imagePath
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

    // ----- REFRESH BY USER ------
    fun refresh(wishlistId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            refreshInternal(wishlistId, fromUser = true)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    // ----- INTERNAL REFRESH HELPER ------
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

    fun createWishlistItem(
        wishlistId: String,
        name: String,
        notes: String? = null,
        url: String? = null,
        price: Double? = null,
        imagePath: String? = null
    ) = viewModelScope.launch {
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Gefa þarf gjöf nafn")
            return@launch
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        itemRepo.createWishlistItem(
            wishlistId = wishlistId,
            name = name.trim(),
            notes = notes?.trim()?.takeIf { it.isNotBlank() },
            url = url?.trim()?.takeIf { it.isNotBlank() },
            price = price,
            imagePath = imagePath
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
}
