package `is`.hi.present.ui.wishlistdetail

sealed interface WishlistDetailEffect {
    data class ShowShareCode(val code: String) : WishlistDetailEffect
    object NavigateBack : WishlistDetailEffect
    object WishlistSaved : WishlistDetailEffect
    data object AccessRevoked : WishlistDetailEffect
}