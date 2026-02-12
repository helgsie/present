package `is`.hi.present.ui.wishlists

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import devSignInAsWishlistUser
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
            //if (!requireAuthenticated()) {
              //  _uiState.value = WishlistsUiState(
                //    isLoading = false,
                  //  errorMessage = "Please sign in to view wishlists.",
                    //needsAuth = true
               // )
              //  return@launch
          //  }

            //val uid = client.auth.currentUserOrNull()?.id
            //android.util.Log.d("AUTH", "uid=$uid")
            //bara notað fyrir Dev purpose
            devSignInAsWishlistUser()

            val wishlists = repo.getWishlists()
                .sortedByDescending { it.createdAt ?: "" }
                .map { w ->
                    WishlistUi(
                        id = w.id,
                        title = w.title,
                        description = w.description,
                        icon = Icons.Default.Favorite
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

    private fun requireAuthenticated(): Boolean {
        val client = SupabaseClientProvider.client
        return client.auth.currentUserOrNull() != null
    }

}
