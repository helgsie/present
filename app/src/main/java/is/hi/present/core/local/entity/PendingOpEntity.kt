package `is`.hi.present.core.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// defines a queued offline action (that needs to be synced)
@Entity(tableName = "pending_ops")
data class PendingOpEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val entityId: String,
    val parentId: String? = null,
    val payloadJson: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

object PendingOpType {
    const val WISHLIST_CREATE = "WISHLIST_CREATE"
    const val WISHLIST_UPDATE = "WISHLIST_UPDATE"
    const val WISHLIST_DELETE = "WISHLIST_DELETE"
    const val ITEM_CREATE = "ITEM_CREATE"
    const val ITEM_UPDATE = "ITEM_UPDATE"
    const val ITEM_DELETE = "ITEM_DELETE"
    const val PROFILE_UPDATE = "PROFILE_UPDATE"
}