package `is`.hi.present.core.sync

import android.util.Log
import `is`.hi.present.core.local.dao.PendingOpDao
import `is`.hi.present.core.local.entity.PendingOpEntity
import `is`.hi.present.data.dto.PendingWishlistItemPayload
import `is`.hi.present.data.dto.PendingWishlistPayload
import `is`.hi.present.data.dto.WishlistInsert
import `is`.hi.present.data.dto.WishlistItemInsert
import `is`.hi.present.data.repository.WishlistItemRepository
import `is`.hi.present.data.repository.WishlistRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Singleton
class SyncManager @Inject constructor(
    private val pendingOpDao: PendingOpDao,
    private val wishlistRepository: WishlistRepository,
    private val wishlistItemRepository: WishlistItemRepository,
    private val supabase: SupabaseClient
) {

    suspend fun syncOwnedData(ownerId: String): Result<Unit> = runCatching {
        replayPendingOps().getOrThrow()
        wishlistRepository.refreshWishlists(ownerId).getOrThrow()

        val wishlists = wishlistRepository.getWishlistsLocal(ownerId)
        wishlists.forEach { wishlist ->
            wishlistItemRepository.refreshWishlistItems(wishlist.id).getOrThrow()
        }
    }

    suspend fun replayPendingOps(): Result<Unit> = runCatching {
        Log.d("SyncDebug", "replayPendingOps started")
        val ops = pendingOpDao.getAllOrdered()
            .sortedWith(compareBy<PendingOpEntity> { syncPriority(it.type) }.thenBy { it.createdAt })

        for (op in ops) {
            try {
                when (op.type) {
                    WISHLIST_CREATE -> replayWishlistCreate(op)
                    WISHLIST_UPDATE -> replayWishlistUpdate(op)
                    WISHLIST_DELETE -> replayWishlistDelete(op)
                    ITEM_CREATE -> replayItemCreate(op)
                    ITEM_UPDATE -> replayItemUpdate(op)
                    ITEM_DELETE -> replayItemDelete(op)
                    else -> {
                        Log.w(TAG, "Unknown pending op type: ${op.type}. Leaving it pending.")
                        continue
                    }
                }

                pendingOpDao.deleteById(op.id)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed replaying pending op id=${op.id}, type=${op.type}", t)
                throw t
            }
        }
    }

    private suspend fun replayWishlistCreate(op: PendingOpEntity) {
        val payload = decodeWishlistPayload(op)
        Log.d("SyncDebug", "Replaying WISHLIST_CREATE for ${payload.id}")

        supabase.from("wishlists").insert(
            WishlistInsert(
                id = payload.id,
                title = payload.title,
                description = payload.description,
                ownerId = payload.ownerId,
                iconKey = payload.iconKey
            )
        )
    }

    private suspend fun replayWishlistUpdate(op: PendingOpEntity) {
        val payload = decodeWishlistPayload(op)
        Log.d("SyncDebug", "Replaying WISHLIST_UPDATE for ${payload.id}")

        supabase
            .from("wishlists")
            .update(
                {
                    set("title", payload.title)
                    set("description", payload.description)
                    set("icon_key", payload.iconKey)
                }
            ) {
                filter {
                    eq("id", payload.id)
                    eq("owner_id", payload.ownerId)
                }
            }
    }

    private suspend fun replayWishlistDelete(op: PendingOpEntity) {
        val payload = decodeWishlistPayload(op)
        Log.d("SyncDebug", "Replaying WISHLIST_DELETE for ${payload.id}")

        supabase
            .from("wishlists")
            .delete {
                filter {
                    eq("id", payload.id)
                    eq("owner_id", payload.ownerId)
                }
            }
    }

    private suspend fun replayItemCreate(op: PendingOpEntity) {
        val payload = decodeWishlistItemPayload(op)
        Log.d("SyncDebug", "Replaying ITEM_CREATE for ${payload.id}")

        supabase.from("wishlist_items").insert(
            WishlistItemInsert(
                id = payload.id,
                wishlistId = payload.wishlistId,
                name = payload.name,
                notes = payload.notes,
                url = payload.url,
                price = payload.price,
                imagePath = payload.imagePath,
                category = payload.category,
                sortOrder = payload.sortOrder
            )
        )
    }

    private suspend fun replayItemUpdate(op: PendingOpEntity) {
        val payload = decodeWishlistItemPayload(op)
        Log.d("SyncDebug", "Replaying ITEM_UPDATE for ${payload.id}")

        supabase
            .from("wishlist_items")
            .update(
                {
                    set("name", payload.name)
                    set("notes", payload.notes)
                    set("url", payload.url)
                    set("price", payload.price)
                    set("image_path", payload.imagePath)
                    set("category", payload.category)
                    set("sort_order", payload.sortOrder)
                }
            ) {
                filter {
                    eq("id", payload.id)
                    eq("wishlist_id", payload.wishlistId)
                }
            }
    }

    private suspend fun replayItemDelete(op: PendingOpEntity) {
        val payload = decodeWishlistItemPayload(op)
        Log.d("SyncDebug", "Replaying ITEM_DELETE for ${payload.id}")

        supabase
            .from("wishlist_items")
            .delete {
                filter {
                    eq("id", payload.id)
                    eq("wishlist_id", payload.wishlistId)
                }
            }
    }

    private fun decodeWishlistPayload(op: PendingOpEntity): PendingWishlistPayload {
        val json = op.payloadJson ?: error("Missing payloadJson for op id=${op.id}, type=${op.type}")
        return Json.decodeFromString<PendingWishlistPayload>(json)
    }

    private fun decodeWishlistItemPayload(op: PendingOpEntity): PendingWishlistItemPayload {
        val json = op.payloadJson ?: error("Missing payloadJson for op id=${op.id}, type=${op.type}")
        return Json.decodeFromString<PendingWishlistItemPayload>(json)
    }

    private fun syncPriority(type: String): Int = when (type) {
        ITEM_DELETE -> 0
        WISHLIST_DELETE -> 1
        WISHLIST_CREATE -> 2
        WISHLIST_UPDATE -> 3
        ITEM_CREATE -> 4
        ITEM_UPDATE -> 5
        else -> 99
    }

    companion object {
        private const val TAG = "SyncManager"

        const val WISHLIST_CREATE = "WISHLIST_CREATE"
        const val WISHLIST_UPDATE = "WISHLIST_UPDATE"
        const val WISHLIST_DELETE = "WISHLIST_DELETE"
        const val ITEM_CREATE = "ITEM_CREATE"
        const val ITEM_UPDATE = "ITEM_UPDATE"
        const val ITEM_DELETE = "ITEM_DELETE"
    }
}