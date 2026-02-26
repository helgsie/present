package `is`.hi.present.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WishlistItem (
    val id: String,
    @SerialName("wishlist_id") val wishlistId: String,
    val name: String,
    val notes: String? = null,
    val url: String? = null,
    val price: Double? = null,
    @SerialName("image_path") val imagePath: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)