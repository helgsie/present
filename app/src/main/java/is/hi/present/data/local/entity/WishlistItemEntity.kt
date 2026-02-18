package `is`.hi.present.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist_items")
data class WishlistItemEntity(
    @PrimaryKey
    val id: String,

    val wishlistId: String,

    val title: String,
    val description: String?,
    val url: String?,
    val imageUrl: String?,

    val updatedAt: Long,

    val claimedByUserId: String?,
    val claimedAt: Long?
)