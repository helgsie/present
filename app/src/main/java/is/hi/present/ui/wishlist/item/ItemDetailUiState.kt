package `is`.hi.present.ui.wishlist.item

data class ItemDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val name: String = "",
    val notes: String = "",
    val priceText: String = "",
    val imageUrl: String? = null
)