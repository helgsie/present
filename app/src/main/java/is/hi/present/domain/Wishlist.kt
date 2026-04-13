package `is`.hi.present.domain

data class Wishlist(
    val id: String,
    val ownerId: String,
    val title: String,
    val description: String?,
    val iconKey: String = "favorite",
    val isShared: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)