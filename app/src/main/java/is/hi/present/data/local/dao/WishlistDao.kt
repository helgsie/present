package `is`.hi.present.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import `is`.hi.present.data.local.entity.WishlistEntity

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlists WHERE ownerId = :ownerId ORDER BY updatedAt DESC")
    fun observeWishlists(ownerId: String): Flow<List<WishlistEntity>>

    @Query("SELECT * FROM wishlists WHERE id = :wishlistId LIMIT 1")
    fun observeWishlistById(wishlistId: String): Flow<WishlistEntity?>

    @Query("SELECT * FROM wishlists WHERE ownerId = :ownerId ORDER BY updatedAt DESC")
    suspend fun getWishlists(ownerId: String): List<WishlistEntity>

    @Query("SELECT * FROM wishlists WHERE id = :wishlistId LIMIT 1")
    suspend fun getWishlistById(wishlistId: String): WishlistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(wishlist: WishlistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(wishlists: List<WishlistEntity>)

    @Delete
    suspend fun delete(wishlist: WishlistEntity)

    @Query("DELETE FROM wishlists WHERE id = :wishlistId")
    suspend fun deleteById(wishlistId: String): Int

    @Query("DELETE FROM wishlists WHERE ownerId = :ownerId")
    suspend fun deleteByOwnerId(ownerId: String): Int

    // mögulega lagar my/shared wishlists að mixast, geyma for now
    //@Query("DELETE FROM wishlists WHERE ownerId = :ownerId AND id NOT IN (:keepIds)")
    //suspend fun deleteOwnerWishlistsNotIn(ownerId: String, wishlistIds: List<String>): Int

    @Transaction
    suspend fun replaceOwnerWishlists(ownerId: String, wishlists: List<WishlistEntity>) {
        upsertAll(wishlists)

        val keepIds = wishlists.map { it.id }
        if (keepIds.isEmpty()) {
            deleteByOwnerId(ownerId)
        } else {
            //deleteOwnerWishlistsNotIn(ownerId, keepIds)
        }
    }

    @Query("DELETE FROM wishlists")
    suspend fun clearAll(): Int
}