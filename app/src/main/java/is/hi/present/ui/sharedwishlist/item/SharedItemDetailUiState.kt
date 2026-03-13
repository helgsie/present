package `is`.hi.present.ui.sharedwishlist.item

data class SharedItemDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    val id: String = "",
    val name: String = "",
    val notes: String? = null,
    val price: Double? = null,
    val imagePath: String? = null,

    val isClaimed: Boolean = false,
    val isClaimedByMe: Boolean = false
)