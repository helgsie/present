package `is`.hi.present.ui.wishlists

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.graphics.vector.ImageVector

data class WishlistUi(
    val id: String,
    val title: String,
    val description: String? = null,
    val icon: ImageVector = Icons.Filled.Favorite
)

data class WishlistsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val wishlists: List<WishlistUi> = emptyList()
) {
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && wishlists.isEmpty()
}
