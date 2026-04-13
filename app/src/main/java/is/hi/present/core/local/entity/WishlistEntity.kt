package `is`.hi.present.core.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wishlists",
    indices = [Index("ownerId")]
)
data class WishlistEntity(
    @PrimaryKey
    val id: String,
    val ownerId: String,
    val title: String,
    val description: String?,
    val iconKey: String,
    val isShared: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)