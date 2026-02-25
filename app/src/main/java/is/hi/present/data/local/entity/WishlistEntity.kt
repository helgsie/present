package `is`.hi.present.data.local.entity

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
    val createdAt: Long,
    val updatedAt: Long,
    val isSharedWithMe: Boolean = false
)