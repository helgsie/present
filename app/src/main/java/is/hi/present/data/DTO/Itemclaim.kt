import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemClaim(
    val id: String,
    @SerialName("item_id") val itemId: String,
    @SerialName("claimed_by") val claimedBy: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("released_at") val releasedAt: String? = null
)

@Serializable
data class ItemClaimInsert(
    @SerialName("item_id") val itemId: String,
    @SerialName("claimed_by") val claimedBy: String
)