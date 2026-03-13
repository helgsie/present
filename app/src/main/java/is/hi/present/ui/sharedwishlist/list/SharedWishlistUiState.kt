package `is`.hi.present.ui.sharedwishlist.list

import `is`.hi.present.ui.wishlist.list.WishlistUi

data class SharedWishlistUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val wishlists: List<WishlistUi> = emptyList(),
    val isRefreshing: Boolean = false,
    ) {
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && wishlists.isEmpty()
}