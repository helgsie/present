package `is`.hi.present.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wishlist_items",
    foreignKeys = [
        ForeignKey(
            entity = WishlistEntity::class,
            parentColumns = ["id"],
            childColumns = ["wishlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("wishlistId"),
        Index("category")
    ]
)
data class WishlistItemEntity(
    @PrimaryKey
    val id: String,
    val wishlistId: String,
    val name: String,
    val notes: String?,
    val url: String?,
    val price: Double?,
    val imagePath: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val claimedByUserId: String?,
    val claimedAt: Long?
)