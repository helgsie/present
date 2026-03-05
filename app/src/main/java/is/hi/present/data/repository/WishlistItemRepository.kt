package `is`.hi.present.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import `is`.hi.present.data.dto.WishlistItemDto
import `is`.hi.present.data.dto.WishlistItemInsert
import `is`.hi.present.data.local.dao.WishlistItemDao
import `is`.hi.present.data.mapper.toDomain
import `is`.hi.present.data.mapper.toEntity
import `is`.hi.present.domain.model.WishlistItem
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WishlistItemRepository @Inject constructor(
    private val dao: WishlistItemDao,
    private val supabase: SupabaseClient
) {
    fun observeWishlistItems(wishlistId: String): Flow<List<WishlistItem>> {
        return dao.observeItemsByWishlistId(wishlistId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun refreshWishlistItems(wishlistId: String): Result<Unit> {
        return runCatching {
            val remote = fetchRemoteItems(wishlistId)
            val entities = remote.map { it.toEntity() }
            dao.replaceWishlistItems(wishlistId, entities)
        }
    }

    suspend fun getWishlistItemsLocal(wishlistId: String): List<WishlistItem> {
        return dao.getItemsByWishlistId(wishlistId).map { it.toDomain() }
    }

    suspend fun fetchWishlistItemsRemote(wishlistId: String): Result<List<WishlistItem>> = runCatching {
        val remote = fetchRemoteItems(wishlistId)
        remote.map { it.toEntity().toDomain() }
    }

    suspend fun createWishlistItem(
        wishlistId: String,
        name: String,
        notes: String? = null,
        url: String? = null,
        price: Double? = null,
        imagePath: String? = null,
        category: String? = null,
        sortOrder: Int? = null
    ): Result<Unit> {
        return runCatching {
            supabase
                .from("wishlist_items")
                .insert(
                    WishlistItemInsert(
                        wishlistId = wishlistId,
                        name = name,
                        notes = notes,
                        url = url,
                        price = price,
                        imagePath = imagePath,
                        category = category,
                        sortOrder = sortOrder
                    )
                )

            refreshWishlistItems(wishlistId).getOrThrow()
        }
    }

    private suspend fun fetchRemoteItems(wishlistId: String): List<WishlistItemDto> {
        return supabase
            .from("wishlist_items")
            .select {
                filter { eq("wishlist_id", wishlistId) }
                order("sort_order", order = Order.ASCENDING)
                order("created_at", order = Order.DESCENDING)
            }
            .decodeList()
    }
}