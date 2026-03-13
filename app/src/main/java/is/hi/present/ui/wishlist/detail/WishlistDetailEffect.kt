package `is`.hi.present.ui.wishlist.detail

sealed interface WishlistDetailEffect {
    data class ShowShareCode(val code: String) : WishlistDetailEffect
    object NavigateBack : WishlistDetailEffect
    object WishlistSaved : WishlistDetailEffect
    data object AccessRevoked : WishlistDetailEffect
}