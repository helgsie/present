package `is`.hi.present.data.repository

import `is`.hi.present.data.dto.ItemClaim
import android.content.Context
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import `is`.hi.present.data.dto.ClaimItemArgs
import `is`.hi.present.data.dto.WishlistItemDto
import `is`.hi.present.data.dto.WishlistItemInsert
import `is`.hi.present.core.local.dao.WishlistItemDao
import `is`.hi.present.data.mapper.toDomain
import `is`.hi.present.data.mapper.toEntity
import `is`.hi.present.domain.WishlistItem
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WishlistItemRepository @Inject constructor(
    private val dao: WishlistItemDao,
    private val supabase: SupabaseClient
) {
    // ------- ROOM READS ---------
    fun observeWishlistItems(wishlistId: String): Flow<List<WishlistItem>> =
        dao.observeItemsByWishlistId(wishlistId)
            .map { entities ->
                entities.map { it.toDomain() }
            }

    fun observeWishlistItem(itemId: String): Flow<WishlistItem?> =
        dao.observeItemById(itemId)
            .map { it?.toDomain() }

    suspend fun getWishlistItemsLocal(wishlistId: String): List<WishlistItem> {
        return dao.getItemsByWishlistId(wishlistId).map { it.toDomain() }
    }

    suspend fun getWishlistItemLocal(itemId: String): WishlistItem? {
        return dao.getItemById(itemId)?.toDomain()
    }

    // ------- NETWORK -> ROOM SYNC -------
    suspend fun refreshWishlistItems(wishlistId: String): Result<Unit> = runCatching {
        val remote = fetchRemoteItems(wishlistId)
        val entities = remote.map { it.toEntity() }
        dao.replaceWishlistItems(wishlistId, entities)
    }

    // ----- REMOTE-ONLY FETCHES -----
    suspend fun fetchWishlistItemsRemote(wishlistId: String): Result<List<WishlistItem>> =
        runCatching {
            fetchRemoteItems(wishlistId).map { it.toEntity().toDomain() }
        }

    suspend fun fetchWishlistItemRemoteById(itemId: String): Result<WishlistItem> = runCatching {
        val dto: WishlistItemDto = supabase
            .from("wishlist_items")
            .select {
                filter { eq("id", itemId) }
                limit(1)
            }
            .decodeSingle()
        dto.toEntity().toDomain()
    }

    // ------ ITEM WRITES ------
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

    suspend fun updateWishlistItem(
        itemId: String,
        name: String,
        notes: String?,
        price: Double?,
        imagePath: String?
    ): Result<Unit> = runCatching {
        val dto: WishlistItemDto = supabase
            .from("wishlist_items")
            .select {
                filter { eq("id", itemId) }
                limit(1)
            }
            .decodeSingle()

        val wishlistId = dto.wishlistId
        supabase
            .from("wishlist_items")
            .update(
                {
                    set("name", name)
                    set("notes", notes)
                    set("price", price)
                    set("image_path", imagePath)
                }
            ) {
                filter { eq("id", itemId) }
            }
        refreshWishlistItems(wishlistId).getOrThrow()
    }

    suspend fun updateWishlistItemOrder(
        wishlistId: String,
        orderedItemIds: List<String>
    ): Result<Unit> = runCatching {
        orderedItemIds.forEachIndexed { index, itemId ->
            supabase
                .from("wishlist_items")
                .update(
                    {
                        set("sort_order", index)
                    }
                ) {
                    filter { eq("id", itemId) }
                }
        }

        refreshWishlistItems(wishlistId).getOrThrow()
    }

    suspend fun deleteWishlistItem(itemId: String): Result<Unit> = runCatching {
        val dto: WishlistItemDto = supabase
            .from("wishlist_items")
            .select {
                filter { eq("id", itemId) }
                limit(1)
            }
            .decodeSingle()

        val wishlistId = dto.wishlistId
        supabase
            .from("wishlist_items")
            .delete {
                filter { eq("id", itemId) }
            }
        refreshWishlistItems(wishlistId).getOrThrow()
    }

    // ---- REMOTE-ONLY MEDIA -----
    suspend fun uploadItemImage(
        context: Context,
        wishlistId: String,
        selectedImageUri: Uri
    ): Result<String> = runCatching {
        val inputStream = context.contentResolver.openInputStream(selectedImageUri)
            ?: throw IllegalArgumentException("Cannot open image stream")
        val bytes = inputStream.readBytes()
        val filename = "wishlist_${wishlistId}_${System.currentTimeMillis()}.jpg"

        supabase.storage.from("wishlist-images").upload(filename, bytes)
        filename
    }
    //Ana
     fun getWishlistImage(imagePath: String?): Result<String?> = runCatching {
        if (imagePath.isNullOrBlank()) return@runCatching null

        if (imagePath.startsWith("http")) {
            imagePath
        } else {
            supabase.storage.from("wishlist-images").publicUrl(imagePath)
        }
    }


    // ----- REMOTE-ONLY CLAIMS ------
    suspend fun getClaimsForItems(itemIds: List<String>): Result<List<ItemClaim>> = runCatching {
        if (itemIds.isEmpty()) return@runCatching emptyList()

        supabase
            .from("item_claims")
            .select {
                filter {
                    isIn("item_id", itemIds)
                }
            }
            .decodeList()
    }

    suspend fun claimItem(itemId: String): Result<String> = runCatching {
        supabase.postgrest
            .rpc(
                function = "claim_item",
                parameters = ClaimItemArgs(itemId)
            )
            .decodeAs<String>()
    }

    suspend fun releaseClaim(itemId: String): Result<String> = runCatching {
        supabase.postgrest
            .rpc(
                function = "release_claim",
                parameters = ClaimItemArgs(itemId)
            )
            .decodeAs<String>()
    }

    // ----- PRIVATE HELPERS -----
    private suspend fun fetchRemoteItems(wishlistId: String): List<WishlistItemDto> {
        val result = supabase
            .from("wishlist_items")
            .select {
                filter { eq("wishlist_id", wishlistId) }
                order("sort_order", order = Order.ASCENDING)
                order("created_at", order = Order.DESCENDING)
            }
            .decodeList<WishlistItemDto>()
        return result
    }
}

