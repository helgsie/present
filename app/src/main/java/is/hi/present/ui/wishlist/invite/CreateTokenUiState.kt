package `is`.hi.present.ui.wishlist.invite

data class JoinWishlistUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val wishlistId: String? = null
)
