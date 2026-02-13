package `is`.hi.present.ui.wishlistdetail

data class WishlistDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val title: String = "",
    val description: String? = null
)