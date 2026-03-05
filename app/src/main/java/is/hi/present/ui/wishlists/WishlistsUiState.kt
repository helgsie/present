package `is`.hi.present.ui.wishlists

import `is`.hi.present.ui.components.WishlistIcon

data class WishlistUi(
    val id: String,
    val title: String,
    val description: String? = null,
    val iconKey: String = WishlistIcon.FAVORITE.key
)

data class OfflineDialog(
    val title: String,
    val message: String
)

data class WishlistsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val offlineBanner: String? = null,
    val offlineDialog: OfflineDialog? = null,
    val wishlists: List<WishlistUi> = emptyList(),
    val needsAuth: Boolean = false
) {
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && wishlists.isEmpty()
}
