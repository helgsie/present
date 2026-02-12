package `is`.hi.present.data.repository

import WishlistInsert
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import `is`.hi.present.data.supabase.SupabaseClientProvider
import `is`.hi.present.domain.model.Wishlist
import `is`.hi.present.ui.Enums.WishlistIcon

class WishlistsRepository {
    suspend fun getWishlists(): List<Wishlist> {
        return SupabaseClientProvider.client
            .from("wishlists")
            .select {
                order("created_at", order = Order.DESCENDING)
            }
            .decodeList()
    }

    suspend fun getWishlistById(wishlistId: String): Wishlist {
        val client = SupabaseClientProvider.client

        return client.postgrest["wishlists"]
            .select {
                filter { eq("id", wishlistId) }
            }
            .decodeSingle()
    }

    suspend fun createWishlist(title: String, description: String? = null, icon: WishlistIcon) {
        val client = SupabaseClientProvider.client

        val userId = client.auth.currentUserOrNull()?.id
            ?: error("Not signed in")

        client.postgrest["wishlists"].insert(
            WishlistInsert(
                title = title,
                description = description,
                owner_id = userId,
                iconKey = icon.key
            )
        )
    }
}
