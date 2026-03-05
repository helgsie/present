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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishlistDetailViewModel @Inject constructor(
    private val repo: WishlistRepository,
    private val repoAuth: AuthRepository,
    private val itemRepo: WishlistItemRepository
) : ViewModel() {

    private val _effects = Channel<WishlistDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val _uiState = MutableStateFlow(WishlistDetailUiState())
    val uiState: StateFlow<WishlistDetailUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var currentWishlistId: String? = null

    fun loadAll(wishlistId: String, cacheToRoom: Boolean) {
        if (currentWishlistId == wishlistId && observeJob?.isActive == true) {
            return
        }
        currentWishlistId = wishlistId
        observeJob?.cancel()

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        observeJob = viewModelScope.launch {
            val wishlistFlow = repo.observeWishlistById(wishlistId)
            val itemsFlow = itemRepo.observeWishlistItems(wishlistId)

            wishlistFlow
                .distinctUntilChanged()
                .combine(itemsFlow.distinctUntilChanged()) { wishlist, items ->
                    val currentUserId = repoAuth.getCurrentUserId()

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
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            title = "",
                            description = "",
                            item = emptyList(),
                            isOwner = false,
                            errorMessage = _uiState.value.errorMessage
                        )
                        return@collect
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        title = wishlist.title,
                        description = wishlist.description,
                        item = itemUi,
                        isOwner = (wishlist.ownerId == currentUserId),
                        errorMessage = _uiState.value.errorMessage
                    )
                }
        }

        viewModelScope.launch {
            repo.refreshWishlistById(wishlistId)
                .onFailure { e ->
                    val friendlyMessage = when {
                        e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                            "Ekkert netsamband - birtir geymd gögn"

                        else -> "Ekki tókst að uppfæra - birtir geymd gögn"
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = _uiState.value.errorMessage ?: friendlyMessage
                    )
                }

            itemRepo.refreshWishlistItems(wishlistId)
                .onFailure { e ->
                    val friendlyMessage = when {
                        e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                            "Ekkert netsamband - birtir geymd gögn"

                        else -> "Ekki tókst að uppfæra items - birtir geymd gögn"
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = _uiState.value.errorMessage ?: friendlyMessage
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

        repo.createShareCode(wishlistId)
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
