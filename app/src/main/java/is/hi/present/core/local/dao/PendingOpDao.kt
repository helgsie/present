package `is`.hi.present.core.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import `is`.hi.present.core.local.entity.PendingOpEntity

// where we store and retrieve the queued actions
@Dao
interface PendingOpDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(op: PendingOpEntity)

    @Delete
    suspend fun delete(op: PendingOpEntity)

    @Query("DELETE FROM pending_ops WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM pending_ops ORDER BY createdAt ASC")
    suspend fun getAll(): List<PendingOpEntity>

    @Query("SELECT * FROM pending_ops ORDER BY createdAt ASC")
    suspend fun getAllOrdered(): List<PendingOpEntity>

    @Query("SELECT * FROM pending_ops WHERE entityId = :entityId")
    suspend fun getByEntityId(entityId: String): List<PendingOpEntity>

    @Query("""
    SELECT entityId FROM pending_ops
    WHERE type IN ('WISHLIST_CREATE', 'WISHLIST_UPDATE', 'WISHLIST_DELETE')
    """)
    suspend fun getPendingWishlistIds(): List<String>

    @Query("""
    SELECT entityId FROM pending_ops
    WHERE type IN ('ITEM_CREATE','ITEM_UPDATE','ITEM_DELETE')
    AND parentId = :wishlistId
    """)
    suspend fun getPendingWishlistItemIds(wishlistId: String): List<String>

    @Query("DELETE FROM pending_ops WHERE entityId = :entityId")
    suspend fun deleteByEntityId(entityId: String)
}