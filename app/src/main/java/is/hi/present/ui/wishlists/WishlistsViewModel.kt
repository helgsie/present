package `is`.hi.present.ui.wishlists

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import `is`.hi.present.data.repository.WishlistsRepository
import `is`.hi.present.data.supabase.SupabaseClientProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WishlistsViewModel(
    private val repo: WishlistsRepository = WishlistsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistsUiState(isLoading = true))
    val uiState: StateFlow<WishlistsUiState> = _uiState

    init {
        loadWishlists()
    }

    fun loadWishlists() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        try {
            // commented out is for testing owner id of the wishlist
            //val client = SupabaseClientProvider.client
            ensureAuthenticated()

            //val uid = client.auth.currentUserOrNull()?.id
            //android.util.Log.d("AUTH", "uid=$uid")

            val wishlists = repo.getWishlists()
                .sortedByDescending { it.createdAt ?: "" }
                .map { w ->
                    WishlistUi(
                        id = w.id,
                        title = w.title,
                        description = w.description,
                        icon = iconForTitle(w.title)
                    )
                }

            _uiState.value = WishlistsUiState(
                isLoading = false,
                wishlists = wishlists
            )
        } catch (e: Exception) {
            _uiState.value = WishlistsUiState(
                isLoading = false,
                errorMessage = e.message ?: "Failed to fetch wishlists"
            )
        }
    }

    private suspend fun ensureAuthenticated() {
        val client = SupabaseClientProvider.client
        if (client.auth.currentUserOrNull() == null) {
            client.auth.signInAnonymously()
        }
    }

    private fun iconForTitle(title: String) = when {
        title.contains("birthday", ignoreCase = true) -> Icons.Default.CardGiftcard
        title.contains("shop", ignoreCase = true) -> Icons.Default.ShoppingBag
        else -> Icons.Default.Favorite
    }
}
