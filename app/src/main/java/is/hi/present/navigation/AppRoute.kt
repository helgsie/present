package `is`.hi.present.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable data object SignIn : AppRoute
    @Serializable data object SignUp : AppRoute

    @Serializable data object Wishlists : AppRoute
    @Serializable data object CreateWishlist : AppRoute

    @Serializable data class WishlistDetail(val wishlistId: String) : AppRoute
    @Serializable data class CreateWishlistItem(val wishlistId: String) : AppRoute
    @Serializable data class JoinWishlist(val token: String) : AppRoute
    @Serializable data object SharedWishlists : AppRoute
    @Serializable data object AddSharedWishlist : AppRoute
    @Serializable data object AccountSettings : AppRoute
}