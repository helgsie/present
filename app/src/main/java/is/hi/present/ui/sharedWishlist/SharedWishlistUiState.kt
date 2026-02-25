package `is`.hi.present.ui.sharedWishlist

import `is`.hi.present.ui.wishlists.WishlistUi

data class SharedWishlistUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val wishlists: List<WishlistUi> = emptyList()
) {
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && wishlists.isEmpty()
}