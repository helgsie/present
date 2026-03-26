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
import `is`.hi.present.core.local.dao.WishlistItemDao
import `is`.hi.present.data.mapper.toDomain
import `is`.hi.present.data.mapper.toEntity
import `is`.hi.present.domain.WishlistItem
import `is`.hi.present.core.local.dao.PendingOpDao
import `is`.hi.present.core.local.entity.PendingOpEntity
import `is`.hi.present.core.local.entity.WishlistItemEntity
import `is`.hi.present.core.sync.SyncManager
import `is`.hi.present.core.sync.SyncScheduler
import `is`.hi.present.data.dto.PendingWishlistItemPayload
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WishlistItemRepository @Inject constructor(
    private val dao: WishlistItemDao,
    private val pendingOpDao: PendingOpDao,
    private val supabase: SupabaseClient,
    private val syncScheduler: SyncScheduler
) {
    // ------- ROOM READS ---------
    fun observeWishlistItems(wishlistId: String): Flow<List<WishlistItem>> =
        dao.observeItemsByWishlistId(wishlistId)
            .map { entities ->
                entities.map { it.toDomain() }
            }

    fun observeWishlistItemById(itemId: String): Flow<WishlistItem?> =
        dao.observeItemById(itemId)
            .map { it?.toDomain() }

    suspend fun getWishlistItemsLocal(wishlistId: String): List<WishlistItem> {
        return dao.getItemsByWishlistId(wishlistId).map { it.toDomain() }
    }

    suspend fun getWishlistItemByIdLocal(itemId: String): WishlistItem? {
        return dao.getItemById(itemId)?.toDomain()
    }

    // ------- NETWORK -> ROOM SYNC -------
    suspend fun refreshWishlistItems(wishlistId: String): Result<Unit> = runCatching {
        val remote = fetchRemoteItems(wishlistId)
        val remoteEntities = remote.map { it.toEntity() }
        val localEntities = dao.getItemsByWishlistId(wishlistId)

        val upsertIds = pendingOpDao.getPendingWishlistItemUpsertIds(wishlistId).toSet()
        val deleteIds = pendingOpDao.getPendingWishlistItemDeleteIds(wishlistId).toSet()

        val localById = localEntities.associateBy { it.id }
        val remoteById = remoteEntities.associateBy { it.id }

        val merged = buildList {
            val allIds = (remoteById.keys + localById.keys - deleteIds)

            for (id in allIds) {
                when (id) {
                    in upsertIds if id in localById -> add(localById.getValue(id))
                    in remoteById -> add(remoteById.getValue(id))
                    in localById -> add(localById.getValue(id))
                }
            }
        }

        dao.replaceWishlistItems(wishlistId, merged)
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
            val id = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val entity = WishlistItemEntity(
                id = id,
                wishlistId = wishlistId,
                name = name,
                notes = notes,
                url = url,
                price = price,
                imagePath = imagePath,
                category = category,
                sortOrder = sortOrder,
                createdAt = now,
                updatedAt = now
            )

            dao.upsert(entity)

            val payload = PendingWishlistItemPayload(
                id = id,
                wishlistId = wishlistId,
                name = name,
                notes = notes,
                url = url,
                price = price,
                imagePath = imagePath,
                category = category,
                sortOrder = sortOrder
            )

            pendingOpDao.insert(
                PendingOpEntity(
                    type = SyncManager.ITEM_CREATE,
                    entityId = id,
                    parentId = wishlistId,
                    payloadJson = Json.encodeToString(payload),
                    createdAt = now
                )
            )

            syncScheduler.enqueueOneTimeSync()
        }
    }

    suspend fun updateWishlistItem(
        itemId: String,
        name: String,
        notes: String?,
        price: Double?,
        imagePath: String?
    ): Result<Unit> = runCatching {
        val existing = dao.getItemById(itemId) ?: error("Item not found")

        val updated = existing.copy(
            name = name,
            notes = notes,
            price = price,
            imagePath = imagePath,
            updatedAt = System.currentTimeMillis()
        )

        dao.upsert(updated)

        val payload = PendingWishlistItemPayload(
            id = updated.id,
            wishlistId = updated.wishlistId,
            name = updated.name,
            notes = updated.notes,
            url = updated.url,
            price = updated.price,
            imagePath = updated.imagePath,
            category = updated.category,
            sortOrder = updated.sortOrder
        )

        pendingOpDao.insert(
            PendingOpEntity(
                type = SyncManager.ITEM_UPDATE,
                entityId = updated.id,
                parentId = updated.wishlistId,
                payloadJson = Json.encodeToString(payload),
                createdAt = System.currentTimeMillis()
            )
        )

        syncScheduler.enqueueOneTimeSync()
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

        val existing = dao.getItemById(itemId) ?: error("Item not found")


        dao.deleteById(itemId)

        val payload = PendingWishlistItemPayload(
            id = existing.id,
            wishlistId = existing.wishlistId,
            name = existing.name,
            notes = existing.notes,
            url = existing.url,
            price = existing.price,
            imagePath = existing.imagePath,
            category = existing.category,
            sortOrder = existing.sortOrder
        )

        pendingOpDao.insert(
            PendingOpEntity(
                type = SyncManager.ITEM_DELETE,
                entityId = existing.id,
                parentId = existing.wishlistId,
                payloadJson = Json.encodeToString(payload),
                createdAt = System.currentTimeMillis()
            )
        )

        syncScheduler.enqueueOneTimeSync()
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

