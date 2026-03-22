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

    @Query("SELECT * FROM pending_ops WHERE entityId = :entityId")
    suspend fun getByEntityId(entityId: String): List<PendingOpEntity>

    @Query("DELETE FROM pending_ops WHERE entityId = :entityId")
    suspend fun deleteByEntityId(entityId: String)
}