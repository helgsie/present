import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WishlistInsert(
    val title: String,
    val description: String? = null,
    val owner_id: String,
    @SerialName("icon_key") val iconKey: String
)