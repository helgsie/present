package `is`.hi.present.ui.ownedwishlist.detail

import `is`.hi.present.data.dto.SharedWithRow

data class WishlistItemUi(
    val id: String,
    val name: String,
    val notes: String? = null,
    val price: Double? = null,
    val imagePath: String? = null,
    val isClaimed: Boolean = false,
    val isClaimedByMe: Boolean = false,
    val claimedByUserEmail: String? = null,
    val claimedByUserName: String? = null
)
data class WishlistDetailUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val offlineBanner: String? = null,
    val sharedWithUsers: List<SharedWithRow> = emptyList(),
    val sharedWithError: String? = null,
    val title: String = "",
    val description: String? = null,
    val items: List<WishlistItemUi> = emptyList(),
    val isOwner: Boolean = false,
    val iconKey: String = "favorite",
    val isSavingOrder: Boolean = false
) {
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && items.isEmpty()
}