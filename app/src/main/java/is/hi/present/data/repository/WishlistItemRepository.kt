package `is`.hi.present.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import `is`.hi.present.data.DTO.WishlistItemInsert
import `is`.hi.present.data.local.dao.WishlistItemDao
import `is`.hi.present.domain.model.WishlistItem
import javax.inject.Inject

class WishlistItemRepository @Inject constructor(
    private val wishlistItemDao: WishlistItemDao,
    private val supabase: SupabaseClient
) {
    suspend fun getWishlistItems(wishlistId: String): List<WishlistItem> {
        return supabase
            .from("wishlist_items")
            .select {
                filter { eq("wishlist_id", wishlistId) }
                order("sort_order", order = Order.ASCENDING)
                order("created_at", order = Order.DESCENDING)
            }
            .decodeList()
    }

    suspend fun createWishlistItem(
        wishlistId: String,
        name: String,
        notes: String? = null,
        url: String? = null,
        price: Double? = null,
        imagePath: String? = null
    ) {
        supabase.postgrest["wishlist_items"].insert(
            WishlistItemInsert(
                wishlistId = wishlistId,
                name = name,
                notes = notes,
                url = url,
                price = price,
                imagePath = imagePath
            )
        )
    }
}