package `is`.hi.present.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Wishlist(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    val title: String,
    val description: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("icon_key") val iconKey: String = "favorite"
)
