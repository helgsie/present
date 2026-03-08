package `is`.hi.present.ui.sharedWishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `is`.hi.present.data.repository.WishlistRepository
import `is`.hi.present.ui.wishlists.WishlistUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class SharedWishlistViewModel @Inject constructor (
    private val repo: WishlistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SharedWishlistUiState())
    val uiState: StateFlow<SharedWishlistUiState> = _uiState.asStateFlow()

    init {
        loadSharedWishlists()
    }

    fun loadSharedWishlists() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        val result = repo.fetchSharedWishlistsRemote()

        result
            .onSuccess { shared ->
                val wishlists = shared.map {
                    WishlistUi(
                        id = it.id,
                        title = it.title,
                        description = it.description,
                        iconKey = it.iconKey
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
}