package `is`.hi.present.ui.ownedwishlist.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `is`.hi.present.data.repository.WishlistItemRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import `is`.hi.present.data.repository.WishlistRepository
import `is`.hi.present.ui.components.WishlistIcon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class WishlistsViewModel @Inject constructor(
    private val repo: WishlistRepository,
    private val itemRepo: WishlistItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistsUiState())
    val uiState: StateFlow<WishlistsUiState> = _uiState.asStateFlow()

    private var currentOwnerId: String? = null
    private var observeJob: Job? = null

    fun loadWishlists(ownerId: String) {
        val ownerChanged = currentOwnerId != ownerId
        currentOwnerId = ownerId

        if (ownerChanged || observeJob?.isActive != true) {
            observeJob?.cancel()

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            observeJob = viewModelScope.launch {
                repo.observeWishlists(ownerId)
                    .map { wishlists ->
                        wishlists
                            .sortedByDescending { it.updatedAt }
                            .map { w ->
                                val items = itemRepo.getWishlistItemsLocal(w.id)
                                WishlistUi(
                                    id = w.id,
                                    title = w.title,
                                    description = w.description,
                                    iconKey = w.iconKey,
                                    itemCount = items.size,
                                    isShared = w.isShared,
                                    previewImageUrls = items
                                        .mapNotNull { itemRepo.getWishlistImage(it.imagePath).getOrNull() }
                                        .take(3)
                                )
                            }
                    }
                    .distinctUntilChanged()
                    .collect { uiWishlists ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                wishlists = uiWishlists,
                                errorMessage = null
                            )
                        }
                    }
            }
        }

        viewModelScope.launch {
            repo.refreshWishlists(ownerId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            offlineBanner = null,
                            errorMessage = null,
                            isLoading = false,
                            isRefreshing = false
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

            repo.refreshWishlists(ownerId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
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
                println("CREATE SUCCESS")
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                onDone?.invoke()
            }
            .onFailure { e ->
                println("CREATE FAILURE: ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Tókst ekki að búa til óskalista"
                    )
                }
            }
    }
}