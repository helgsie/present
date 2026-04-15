package `is`.hi.present.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WishlistDto(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    val title: String,
    val description: String?,
    @SerialName("icon_key") val iconKey: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class WishlistInsert(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerialName("owner_id") val ownerId: String,
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
data class WishlistIdArgs(
    @SerialName("wishlist_id")
    val wishlistId: String
)

@Serializable
data class WishlistDetailArgs(
    @SerialName("p_wishlist_id")
    val wishlistId: String
)

@Serializable
data class SharedWithRow(
    @SerialName("user_id")
    val userId: String,
    @SerialName("email")
    val displayName: String,
    val role: String? = null
)

@Serializable
data class RemoveSharedUserArgs(
    @SerialName("p_wishlist_id")
    val wishlistId: String,
    @SerialName("p_user_id")
    val userId: String
)

@Serializable
data class WishlistShareRow(
    @SerialName("wishlist_id") val wishlistId: String,
    @SerialName("shared_with") val sharedWith: String,
    val role: String? = null
)

@Serializable
data class WishlistCardDto(
    val id: String,
    @SerialName("owner_id")
    val ownerId: String,
    @SerialName("owner_display_name")
    val ownerDisplayName: String? = null,
    val title: String,
    val description: String? = null,
    @SerialName("icon_key")
    val iconKey: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("item_count")
    val itemCount: Int,
    @SerialName("is_shared")
    val isShared: Boolean,
    @SerialName("preview_image_urls")
    val previewImageUrls: List<String> = emptyList()
)

@Serializable
data class WishlistDetailDto(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    val title: String,
    val description: String?,
    @SerialName("icon_key") val iconKey: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_shared") val isShared: Boolean
)