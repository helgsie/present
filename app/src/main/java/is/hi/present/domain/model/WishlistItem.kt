package `is`.hi.present.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WishlistItem (
    val id: String,
    @SerialName("wishlist_id") val wishlistId: String,
    val title: String,
    val description: String? = null,
    val url: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)