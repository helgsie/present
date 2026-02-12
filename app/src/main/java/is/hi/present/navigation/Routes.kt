package `is`.hi.present.navigation

object Routes {
    const val WISHLISTS = "wishlists"
    const val CREATE_WISHLIST = "create_wishlist"
    const val WISHLIST_DETAIL = "wishlist/{wishlistId}"
    fun wishlistDetail(wishlistId: String) = "wishlist/$wishlistId"
}