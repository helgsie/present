package `is`.hi.present.data.DTO

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WishlistItemInsert(
    @SerialName("wishlist_id") val wishlistId: String,
    val name: String,
    val notes: String? = null,
    val url: String? = null,
    val price: Double? = null,
    @SerialName("image_path") val imagePath: String? = null
)