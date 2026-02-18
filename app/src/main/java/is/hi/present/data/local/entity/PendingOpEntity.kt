package `is`.hi.present.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// defines a queued offline action (that needs to be synced)
@Entity(tableName = "pending_operations")
data class PendingOpEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val type: String, // CREATE_WISHLIST, CLAIM_ITEM, etc.
    val entityId: String,
    val payload: String?,
    val createdAt: Long
)