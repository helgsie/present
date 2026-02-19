package `is`.hi.present.navigation

object Routes {
    const val AUTH = "auth"
    const val ACCOUNT_SETTINGS = "account_settings"
    const val WISHLISTS = "wishlists"
    const val CREATE_WISHLIST = "create_wishlist"
    const val WISHLIST_DETAIL = "wishlist/{wishlistId}"
    fun wishlistDetail(wishlistId: String) = "wishlist/$wishlistId"
    const val CREATE_WISHLIST_ITEM = "wishlist_item_create/{wishlistId}"
    fun createWishlistItem(wishlistId: String) = "wishlist_item_create/$wishlistId"
}