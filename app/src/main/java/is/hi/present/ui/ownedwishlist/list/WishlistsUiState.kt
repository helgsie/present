package `is`.hi.present.ui.ownedwishlist.list

import `is`.hi.present.ui.components.WishlistIcon

data class WishlistUi(
    val id: String,
    val title: String,
    val description: String? = null,
    val iconKey: String = WishlistIcon.FAVORITE.key,
    val itemCount: Int = 0,
    val isShared: Boolean = false,
    val previewImageUrls: List<String> = emptyList(),
    val ownerDisplayName: String? = null
)

data class OfflineDialog(
    val title: String,
    val message: String
)

data class WishlistsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val wishlists: List<WishlistUi> = emptyList(),
    val errorMessage: String? = null,
    val offlineBanner: String? = null,
    val offlineDialog: OfflineDialog? = null,
    val needsAuth: Boolean = false
) {
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && wishlists.isEmpty()
}