package `is`.hi.present.ui.wishlistdetail

data class WishlistItemUi(
    val id: String,
    val name: String,
    val notes: String? = null,
    val price: Double? = null,
    val imagePath: String? = null
)
data class WishlistDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val title: String = "",
    val description: String? = null,
    val item: List<WishlistItemUi> = emptyList(),
    val isOwner: Boolean = false
) {
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && item.isEmpty()
}