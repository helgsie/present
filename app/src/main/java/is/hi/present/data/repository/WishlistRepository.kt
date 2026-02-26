package `is`.hi.present.data.repository

import CreateShareLinkArgs
import JoinByTokenArgs
import WishlistInsert
import WishlistShareRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import `is`.hi.present.domain.model.Wishlist
import `is`.hi.present.ui.components.WishlistIcon
import io.github.jan.supabase.postgrest.rpc
import `is`.hi.present.data.local.dao.WishlistDao
import javax.inject.Inject
import java.net.URLEncoder

class WishlistsRepository @Inject constructor(
    private val wishlistDao: WishlistDao,
    private val supabase: SupabaseClient
){
    suspend fun getWishlists(): List<Wishlist> {
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not signed in")

        return supabase
            .from("wishlists")
            .select {
                filter { eq("owner_id", userId) }
                order("created_at", order = Order.DESCENDING)
            }
            .decodeList()
    }

    suspend fun getWishlistById(wishlistId: String): Wishlist {
        return supabase.postgrest["wishlists"]
            .select {
                filter { eq("id", wishlistId) }
            }
            .decodeSingle()
    }

    suspend fun createWishlist(title: String, description: String? = null, icon: WishlistIcon) {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: error("Not signed in")

        supabase.postgrest["wishlists"].insert(
            WishlistInsert(
                title = title,
                description = description,
                owner_id = userId,
                iconKey = icon.key
            )
        )
    }

    // Hægt að vinna með þetta þegar vitað er hvernig url virkar á milli browser og app
    /*suspend fun createShareLink(wishlistId: String): String {
        val token: String = supabase.postgrest
            .rpc(function = "create_share_link", parameters = CreateShareLinkArgs(wishlistId))
            .decodeAs()

        val encoded = URLEncoder.encode(token, "UTF-8")
        return "https://benevolent-blini-5869c9.netlify.app/join?token=$encoded"
    }*/

    suspend fun createShareCode(wishlistId: String): String {
        return supabase.postgrest
            .rpc("create_share_link", CreateShareLinkArgs(wishlistId))
            .decodeAs()
    }

    suspend fun getSharedWishlists(): List<Wishlist> {
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not signed in")

        val shares: List<WishlistShareRow> = supabase
            .from("wishlist_shares")
            .select {
                filter { eq("shared_with", userId) }
            }
            .decodeList()

        if (shares.isEmpty()) return emptyList()

        return shares.mapNotNull { share ->
            runCatching { getWishlistById(share.wishlistId) }
                .onFailure { it.printStackTrace() }
                .getOrNull()
        }
    }

    suspend fun joinByToken(token: String): String {
        val wishlistId: String = supabase.postgrest
            .rpc(function = "join_by_token", parameters = JoinByTokenArgs(token))
            .decodeAs()

        return wishlistId
    }
}
