package `is`.hi.present.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import `is`.hi.present.data.dto.CreateShareLinkArgs
import `is`.hi.present.data.dto.JoinByTokenArgs
import `is`.hi.present.data.dto.WishlistDto
import `is`.hi.present.data.dto.WishlistInsert
import `is`.hi.present.data.dto.WishlistShareRow
import `is`.hi.present.data.local.dao.WishlistDao
import `is`.hi.present.data.mapper.toDomain
import `is`.hi.present.data.mapper.toEntity
import `is`.hi.present.domain.model.Wishlist
import `is`.hi.present.ui.components.WishlistIcon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WishlistRepository @Inject constructor(
    private val wishlistDao: WishlistDao,
    private val supabase: SupabaseClient
){
    // fylgjast með wishlists frá local cache (room)
    fun observeWishlists(ownerId: String): Flow<List<Wishlist>> =
        wishlistDao.observeWishlists(ownerId).map { entities -> entities.map { it.toDomain() } }

    // sækir nýjustu wishlists úr Supabase og skiptir þeim út fyrir local cache
    suspend fun refreshWishlists(ownerId: String): Result<Unit> = runCatching {
        val remote = supabase
            .from("wishlists")
            .select {
                filter { eq("owner_id", ownerId) }
                order("updated_at", order = Order.DESCENDING)
            }
            .decodeList<WishlistDto>()

        wishlistDao.replaceOwnerWishlists(ownerId, remote.map { it.toEntity() })
    }

    // nota þetta method fyrir screens sem loada einu sinni og renderast strax
    suspend fun getWishlists(ownerId: String): List<Wishlist> =
        wishlistDao.getWishlists(ownerId).map { it.toDomain() }

    fun observeWishlistById(wishlistId: String): Flow<Wishlist?> =
        wishlistDao.observeWishlistById(wishlistId)
            .map { entity -> entity?.toDomain() }

    suspend fun getWishlistByIdLocal(wishlistId: String): Wishlist? =
        wishlistDao.getWishlistById(wishlistId)?.toDomain()

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

    suspend fun getWishlistById(wishlistId: String): Result<Wishlist> {
        val refreshResult = refreshWishlistById(wishlistId)

        val local = getWishlistByIdLocal(wishlistId)
        if (local != null) return Result.success(local)

        return refreshResult
            .map { error("Óskalisti fannst ekki í cache") }
            .recoverCatching { throw it }
    }

    suspend fun createWishlist(
        ownerId: String,
        title: String,
        description: String? = null,
        icon: WishlistIcon
    ): Result<Unit> = runCatching {
        supabase.postgrest["wishlists"].insert(
            WishlistInsert(
                title = title,
                description = description,
                ownerId = ownerId,
                iconKey = icon.key
            )
        )
        refreshWishlists(ownerId).getOrThrow()
    }

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

    suspend fun getSharedWishlists(): Result<List<Wishlist>> = runCatching {
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not signed in")
        val shares: List<WishlistShareRow> = supabase
            .from("wishlist_shares")
            .select {
                filter { eq("shared_with", userId) }
            }
            .decodeList()

        if (shares.isEmpty()) return@runCatching emptyList()

        val sharedIds = shares.map { it.wishlistId }.distinct()

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

        wishlists.map { it.toEntity().toDomain() }
    }

    suspend fun joinByToken(token: String): Result<String> = runCatching {
        supabase.postgrest
            .rpc(function = "join_by_token", parameters = JoinByTokenArgs(token))
            .decodeAs()
    }
}
