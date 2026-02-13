package `is`.hi.present.data.repository

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import `is`.hi.present.data.supabase.SupabaseClientProvider
import `is`.hi.present.domain.model.WishlistItem

class WishlistItemRepository {
    suspend fun getWishlistItems(wishlistId: String): List<WishlistItem> {
        return SupabaseClientProvider.client
            .from("wishlist_items")
            .select {
                filter { eq("wishlist_id", wishlistId) }
                order("sort_order", order = Order.ASCENDING)
                order("created_at", order = Order.DESCENDING)
            }
            .decodeList()
    }
}