import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WishlistInsert(
    val title: String,
    val description: String? = null,
    val owner_id: String,
    @SerialName("icon_key") val iconKey: String
)

@Serializable
data class CreateShareLinkArgs(
    @SerialName("p_wishlist_id") val wishlistId: String
)

@Serializable
data class JoinByTokenArgs(
    @SerialName("p_token") val token: String
)

@Serializable
data class WishlistShareRow(
    @SerialName("wishlist_id") val wishlistId: String,
    @SerialName("shared_with") val sharedWith: String,
    val role: String? = null
)