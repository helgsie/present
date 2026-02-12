import `is`.hi.present.ui.Enums.WishlistIcon
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class WishlistInsert(
    val title: String,
    val description: String? = null,
    val owner_id: String,
    @SerialName("icon_key") val iconKey: String
)