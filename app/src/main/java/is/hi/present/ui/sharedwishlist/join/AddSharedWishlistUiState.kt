package `is`.hi.present.ui.sharedwishlist.join

data class AddSharedWishlistUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val joinedWishlistId: String? = null
)