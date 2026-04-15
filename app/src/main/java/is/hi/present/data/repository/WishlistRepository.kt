package `is`.hi.present.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import `is`.hi.present.core.local.dao.PendingOpDao
import `is`.hi.present.data.dto.CreateShareLinkArgs
import `is`.hi.present.data.dto.JoinByTokenArgs
import `is`.hi.present.data.dto.RemoveSharedUserArgs
import `is`.hi.present.data.dto.SharedWithRow
import `is`.hi.present.data.dto.WishlistCardDto
import `is`.hi.present.data.dto.WishlistDto
import `is`.hi.present.data.dto.WishlistIdArgs
import `is`.hi.present.core.sync.SyncManager
import `is`.hi.present.core.sync.SyncScheduler
import `is`.hi.present.data.dto.WishlistShareRow
import `is`.hi.present.core.local.dao.WishlistDao
import `is`.hi.present.core.local.entity.PendingOpEntity
import `is`.hi.present.core.local.entity.WishlistEntity
import `is`.hi.present.data.dto.PendingWishlistPayload
import `is`.hi.present.data.mapper.toDomain
import `is`.hi.present.data.mapper.toEntity
import `is`.hi.present.domain.Wishlist
import `is`.hi.present.ui.components.WishlistIcon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

class WishlistRepository @Inject constructor(
    private val wishlistDao: WishlistDao,
    private val pendingOpDao: PendingOpDao,
    private val supabase: SupabaseClient,
    private val syncScheduler: SyncScheduler
){
    // ------- READS FROM ROOM ---------
    fun observeWishlists(ownerId: String): Flow<List<Wishlist>> =
        wishlistDao.observeWishlists(ownerId).map { entities -> entities.map { it.toDomain() } }

    fun observeWishlistById(wishlistId: String): Flow<Wishlist?> =
        wishlistDao.observeWishlistById(wishlistId).map { entity -> entity?.toDomain() }

    suspend fun getWishlistsLocal(ownerId: String): List<Wishlist> =
        wishlistDao.getWishlists(ownerId).map { it.toDomain() }

    suspend fun getWishlistByIdLocal(wishlistId: String): Wishlist? =
        wishlistDao.getWishlistById(wishlistId)?.toDomain()
    // ------ SYNCING ROOM WITH DATA FROM REMOTE -------
    suspend fun refreshWishlists(ownerId: String): Result<Unit> = runCatching {
        val remote = supabase.postgrest
            .rpc("get_my_wishlist_cards")
            .decodeList<WishlistCardDto>()

        val remoteEntities = remote.map { it.toEntity() }
        val localEntities = wishlistDao.getWishlists(ownerId)

        val upsertIds = pendingOpDao.getPendingWishlistUpsertIds().toSet()
        val deleteIds = pendingOpDao.getPendingWishlistDeleteIds().toSet()

        val localById = localEntities.associateBy { it.id }
        val remoteById = remoteEntities.associateBy { it.id }

        val mergedEntities = buildList {
            val allIds = (remoteById.keys + localById.keys - deleteIds)

            for (id in allIds) {
                when {
                    id in upsertIds && id in localById -> add(localById.getValue(id))
                    id in remoteById -> add(remoteById.getValue(id))
                    id in localById -> add(localById.getValue(id))
                }
            }
        }

        wishlistDao.refreshWishlists(ownerId, mergedEntities)
    }

    suspend fun refreshWishlistById(wishlistId: String): Result<Unit> = runCatching {
        val dto: WishlistDto = supabase
            .from("wishlists")
            .select {
                filter { eq("id", wishlistId) }
                limit(1)
            }
            .decodeSingle()
        wishlistDao.upsert(dto.toEntity())
    }

    // ----- REMOTE HELPERS -----
    suspend fun fetchWishlistRemoteById(wishlistId: String): Result<Wishlist> = runCatching {
        val dto: WishlistDto = supabase
            .from("wishlists")
            .select {
                filter { eq("id", wishlistId) }
                limit(1)
            }
            .decodeSingle()
        dto.toEntity().toDomain()
    }

    suspend fun fetchSharedWishlistsRemote(): Result<List<Wishlist>> = runCatching {
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not signed in")
        val shares: List<WishlistShareRow> = supabase
            .from("wishlist_shares")
            .select {
                filter { eq("shared_with", userId) }
            }
            .decodeList()
        if (shares.isEmpty()) return@runCatching emptyList()

        val sharedIds = shares.map { it.wishlistId }.distinct()
        // TODO: Change filter into in() style filter later
        val wishlists: List<WishlistDto> = supabase
            .from("wishlists")
            .select {
                filter {
                    or {
                        sharedIds.forEach { id ->
                            eq("id", id)
                        }
                    }
                }
                order("updated_at", order = Order.DESCENDING)
            }
            .decodeList()
        wishlists.map { it.toDomain() }
    }
    suspend fun fetchMyWishlistCards(): Result<List<WishlistCardDto>> = runCatching {
        supabase.postgrest
            .rpc("get_my_wishlist_cards")
            .decodeList<WishlistCardDto>()
    }

    suspend fun fetchSharedWishlistCards(): Result<List<WishlistCardDto>> = runCatching {
        val user = supabase.auth.currentUserOrNull()
        android.util.Log.d(
            "WishlistRepository",
            "currentUser=${user?.id}"
        )

        supabase.postgrest
            .rpc("get_shared_wishlist_cards")
            .decodeList<WishlistCardDto>()
    }

    // ---- WRITES ------
    suspend fun createWishlist(
        ownerId: String,
        title: String,
        description: String? = null,
        icon: WishlistIcon
    ): Result<Unit> = runCatching {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val entity = WishlistEntity(
            id = id,
            ownerId = ownerId,
            title = title,
            description = description,
            iconKey = icon.key,
            createdAt = now,
            updatedAt = now
        )

        wishlistDao.upsert(entity)

        val payload = PendingWishlistPayload(
            id = id,
            ownerId = ownerId,
            title = title,
            description = description,
            iconKey = icon.key
        )

        pendingOpDao.insert(
            PendingOpEntity(
                type = SyncManager.WISHLIST_CREATE,
                entityId = id,
                parentId = null,
                payloadJson = Json.encodeToString(payload),
                createdAt = System.currentTimeMillis()
            )
        )

        syncScheduler.enqueueOneTimeSync()
    }

    suspend fun updateWishlist(
        wishlistId: String,
        title: String,
        description: String? = null,
        icon: WishlistIcon
    ): Result<Unit> = runCatching {
        val existing = wishlistDao.getWishlistById(wishlistId) ?: error("Wishlist not found")
        val updated = existing.copy(
            title = title,
            description = description,
            iconKey = icon.key,
            updatedAt = System.currentTimeMillis()
        )

        wishlistDao.upsert(updated)

        val payload = PendingWishlistPayload(
            id = updated.id,
            ownerId = updated.ownerId,
            title = updated.title,
            description = updated.description,
            iconKey = updated.iconKey
        )

        pendingOpDao.insert(
            PendingOpEntity(
                type = SyncManager.WISHLIST_UPDATE,
                entityId = updated.id,
                parentId = null,
                payloadJson = Json.encodeToString(payload),
                createdAt = System.currentTimeMillis()
            )
        )

        syncScheduler.enqueueOneTimeSync()
    }

    suspend fun deleteWishlist(wishlistId: String): Result<Unit> = runCatching {
        val existing = wishlistDao.getWishlistById(wishlistId) ?: error("Wishlist not found")

        wishlistDao.deleteById(wishlistId)

        val payload = PendingWishlistPayload(
            id = existing.id,
            ownerId = existing.ownerId,
            title = existing.title,
            description = existing.description,
            iconKey = existing.iconKey
        )

        pendingOpDao.insert(
            PendingOpEntity(
                type = SyncManager.WISHLIST_DELETE,
                entityId = existing.id,
                parentId = null,
                payloadJson = Json.encodeToString(payload),
                createdAt = System.currentTimeMillis()
            )
        )

        syncScheduler.enqueueOneTimeSync()
    }

    // ---- SHARE LINK -----

    // Hægt að vinna með þetta þegar vitað er hvernig url virkar á milli browser og app
    /*suspend fun createShareLink(wishlistId: String): String {
        val token: String = supabase.postgrest
            .rpc(function = "create_share_link", parameters = CreateShareLinkArgs(wishlistId))
            .decodeAs()

        val encoded = URLEncoder.encode(token, "UTF-8")
        return "https://benevolent-blini-5869c9.netlify.app/join?token=$encoded"
    }*/

    suspend fun createShareCode(wishlistId: String): Result<String> = runCatching {
        supabase.postgrest
            .rpc("create_share_link", CreateShareLinkArgs(wishlistId))
            .decodeAs()
    }

    suspend fun joinByToken(token: String): Result<String> = runCatching {
        supabase.postgrest
            .rpc(function = "join_by_token", parameters = JoinByTokenArgs(token))
            .decodeAs()
    }

    suspend fun getSharedWithUsers(wishlistId: String): Result<List<SharedWithRow>> = runCatching {
        supabase.postgrest
            .rpc(
                "shared_with_emails",
                parameters = WishlistIdArgs(wishlistId)
            )
            .decodeAs<List<SharedWithRow>>()
    }

    suspend fun removeFromWishlist(
        wishlistId: String,
        userId: String
    ): Result<Unit> = runCatching {
        supabase.postgrest
            .rpc(
                "remove_shared_user",
                parameters = RemoveSharedUserArgs(
                    wishlistId = wishlistId,
                    userId = userId
                )
            )
    }

    suspend fun leaveSharedWishlist(wishlistId: String): Result<Unit> = runCatching {
        supabase.postgrest
            .rpc(
                function = "leave_wishlist",
                parameters = WishlistIdArgs(wishlistId)
            )
    }
}

