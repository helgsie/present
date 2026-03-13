package `is`.hi.present.domain

data class WishlistItem (
    val id: String,
    val wishlistId: String,
    val name: String,
    val notes: String?,
    val url: String?,
    val price: Double?,
    val imagePath: String?,
    val category: String?,
    val sortOrder: Int?,
    val createdAt: Long,
    val updatedAt: Long
)