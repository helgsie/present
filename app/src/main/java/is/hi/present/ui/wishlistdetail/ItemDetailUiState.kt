package `is`.hi.present.ui.wishlistdetail

data class ItemDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val name: String = "",
    val notes: String = "",
    val priceText: String = "",
    val imageUrl: String? = null
)