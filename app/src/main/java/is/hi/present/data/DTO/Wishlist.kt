import kotlinx.serialization.Serializable

@Serializable
public data class WishlistInsert(
    val title: String,
    val description: String? = null,
    val owner_id: String
)