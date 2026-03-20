package `is`.hi.present.ui.ownedwishlist.invite

data class JoinWishlistUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val wishlistId: String? = null
)
