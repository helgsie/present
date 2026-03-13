package `is`.hi.present.ui.wishlist.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `is`.hi.present.data.repository.WishlistRepository
import `is`.hi.present.ui.wishlist.components.WishlistIcon
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishlistsViewModel @Inject constructor(
    private val repo: WishlistRepository
) : ViewModel() {

    // ---- STATE -----
    private val _uiState = MutableStateFlow(WishlistsUiState())
    val uiState: StateFlow<WishlistsUiState> = _uiState.asStateFlow()

    // ---- INTERNAL FIELDS ----
    private var currentOwnerId: String? = null
    private var observeJob: Job? = null

    // ---- LOAD WISHLISTS -----
    fun loadWishlists(ownerId: String) {
        val ownerChanged = currentOwnerId != ownerId
        currentOwnerId = ownerId

        if (ownerChanged) {
            observeJob?.cancel()

            observeJob = viewModelScope.launch {
                repo.observeWishlists(ownerId)
                    .map { wishlists ->
                        wishlists
                            .sortedByDescending { it.updatedAt }
                            .map { w ->
                                WishlistUi(
                                    id = w.id,
                                    title = w.title,
                                    description = w.description,
                                    iconKey = w.iconKey
                                )
                            }
                    }
                    .distinctUntilChanged()
                    .collect { uiWishlists ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                wishlists = uiWishlists
                            )
                        }
                    }
            }
            _uiState.update { it.copy(isLoading = true) }
        }
        viewModelScope.launch {
            repo.refreshWishlists(ownerId)
                .onSuccess {
                    _uiState.update { it.copy(offlineBanner = null) }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
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
                            offlineBanner = null,
                            isRefreshing = false,
                            isLoading = false,
                            errorMessage = null
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

    // ----- WISHLIST ACTIONS -----
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
                _uiState.update { it.copy(isLoading = false) }
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