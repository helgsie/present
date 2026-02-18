package `is`.hi.present.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlists")
data class WishlistEntity(
    @PrimaryKey
    val id: String,

    val title: String,
    val iconKey: String,
    val ownerId: String,

    val updatedAt: Long,
    val isSharedWithMe: Boolean = false
)