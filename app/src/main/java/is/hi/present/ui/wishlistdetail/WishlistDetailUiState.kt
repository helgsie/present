package `is`.hi.present.ui.wishlistdetail

data class WishlistItemUi(
    val id: String,
    val title: String,
    val description: String? = null,
    val price: Double? = null
)

data class WishlistDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    val id: String? = null,
    val title: String = "",
    val description: String? = null,
    val iconKey: String = "favorite",

    val item: List<WishlistItemUi> = emptyList()
) {
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && item.isEmpty()
}