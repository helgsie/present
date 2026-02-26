package `is`.hi.present.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import `is`.hi.present.data.local.entity.WishlistItemEntity

@Dao
interface WishlistItemDao {

    @Query("""
        SELECT * FROM wishlist_items 
        WHERE wishlistId = :wishlistId
        ORDER BY COALESCE(sortOrder, 2147483647), updatedAt DESC
    """)
    fun observeItems(wishlistId: String): Flow<List<WishlistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: WishlistItemEntity)

    @Delete
    suspend fun delete(item: WishlistItemEntity)

    @Query("DELETE FROM wishlist_items WHERE id = :itemId")
    suspend fun deleteById(itemId: String)
}