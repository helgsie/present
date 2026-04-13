package `is`.hi.present.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class PendingWishlistPayload(
    val id: String,
    val ownerId: String,
    val title: String,
    val description: String?,
    val iconKey: String
)

@Serializable
data class PendingWishlistItemPayload(
    val id: String,
    val wishlistId: String,
    val name: String,
    val notes: String?,
    val url: String?,
    val price: Double?,
    val imagePath: String?,
    val category: String?,
    val sortOrder: Int?
)

@Serializable
data class PendingProfilePayload(
    val userId: String,
    val displayName: String
)
