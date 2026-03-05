package `is`.hi.present.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WishlistItemDto(
    val id: String,
    @SerialName("wishlist_id") val wishlistId: String,
    val name: String,
    val notes: String? = null,
    val url: String? = null,
    val price: Double? = null,
    @SerialName("image_path") val imagePath: String? = null,
    val category: String? = null,
    @SerialName("sort_order") val sortOrder: Int? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class WishlistItemInsert(
    @SerialName("wishlist_id") val wishlistId: String,
    val name: String,
    val notes: String? = null,
    val url: String? = null,
    val price: Double? = null,
    @SerialName("image_path") val imagePath: String? = null,
    val category: String? = null,
    @SerialName("sort_order") val sortOrder: Int? = null
)
