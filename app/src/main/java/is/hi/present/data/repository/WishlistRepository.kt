package `is`.hi.present.data.repository

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import `is`.hi.present.data.supabase.SupabaseClientProvider
import `is`.hi.present.domain.model.Wishlist

class WishlistsRepository {
    suspend fun getWishlists(): List<Wishlist> {
        return SupabaseClientProvider.client
            .from("wishlists")
            .select {
                order("created_at", order = Order.DESCENDING)
            }
            .decodeList()
    }
}
