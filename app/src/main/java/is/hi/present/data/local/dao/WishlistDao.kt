package `is`.hi.present.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import `is`.hi.present.data.local.entity.WishlistEntity

@Dao
interface WishlistDao {

    @Query("SELECT * FROM wishlists WHERE ownerId = :ownerId ORDER BY updatedAt DESC")
    fun observeWishlists(ownerId: String): Flow<List<WishlistEntity>>

    @Query("SELECT * FROM wishlists WHERE id = :wishlistId LIMIT 1")
    suspend fun getById(wishlistId: String): WishlistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(wishlist: WishlistEntity)

    @Delete
    suspend fun delete(wishlist: WishlistEntity)

    @Query("DELETE FROM wishlists WHERE id = :wishlistId")
    suspend fun deleteById(wishlistId: String)
}