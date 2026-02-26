package `is`.hi.present.data.repository

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
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
    suspend fun uploadItemImage(
        context: Context,
        wishlistId: String,
        selectedImageUri: Uri
    ): String {
        val inputStream = context.contentResolver.openInputStream(selectedImageUri)
            ?: throw IllegalArgumentException("Cannot open image stream")
        val bytes = inputStream.readBytes()
        val filename = "wishlist_${wishlistId}_${System.currentTimeMillis()}.jpg"

        supabase.storage.from("wishlist-images").upload(filename, bytes)

        return filename
    }
}