package `is`.hi.present.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import `is`.hi.present.data.local.entity.WishlistItemEntity

@Dao
interface WishlistItemDao {

    @Query(
        """
        SELECT * FROM wishlist_items 
        WHERE wishlistId = :wishlistId
        ORDER BY COALESCE(sortOrder, 2147483647), updatedAt DESC
        """
    )
    fun observeItemsByWishlistId(wishlistId: String): Flow<List<WishlistItemEntity>>

    @Query("SELECT * FROM wishlist_items WHERE id = :itemId LIMIT 1")
    fun observeItemById(itemId: String): Flow<WishlistItemEntity?>

    @Query(
        """
        SELECT * FROM wishlist_items
        WHERE wishlistId = :wishlistId
        ORDER BY COALESCE(sortOrder, 2147483647), updatedAt DESC
        """
    )
    suspend fun getItemsByWishlistId(wishlistId: String): List<WishlistItemEntity>

    @Query("SELECT * FROM wishlist_items WHERE id = :itemId LIMIT 1")
    suspend fun getItemById(itemId: String): WishlistItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: WishlistItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<WishlistItemEntity>)

    @Delete
    suspend fun delete(item: WishlistItemEntity)

    @Query("DELETE FROM wishlist_items WHERE id = :itemId")
    suspend fun deleteById(itemId: String): Int

    @Query("DELETE FROM wishlist_items WHERE wishlistId = :wishlistId")
    suspend fun deleteByWishlistId(wishlistId: String): Int

    @Query("DELETE FROM wishlist_items WHERE wishlistId = :wishlistId AND id NOT IN (:keepIds)")
    suspend fun deleteWishlistItemsNotIn(wishlistId: String, keepIds: List<String>): Int

    @Transaction
    suspend fun replaceWishlistItems(wishlistId: String, items: List<WishlistItemEntity>) {
        upsertAll(items)

        val keepIds = items.map { it.id }

        if (keepIds.isNotEmpty()) {
            deleteWishlistItemsNotIn(wishlistId, keepIds)
        }
    }

    @Query("DELETE FROM wishlist_items")
    suspend fun clearAll(): Int
}