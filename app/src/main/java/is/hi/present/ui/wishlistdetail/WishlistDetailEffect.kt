package `is`.hi.present.ui.wishlistdetail

sealed interface WishlistDetailEffect {
    data class ShowShareCode(val code: String) : WishlistDetailEffect
}