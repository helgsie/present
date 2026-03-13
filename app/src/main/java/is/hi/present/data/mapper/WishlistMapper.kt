package `is`.hi.present.data.mapper

import `is`.hi.present.data.dto.WishlistDto
import `is`.hi.present.core.local.entity.WishlistEntity
import `is`.hi.present.domain.model.Wishlist
import java.time.Instant

fun WishlistDto.toEntity(): WishlistEntity =
    WishlistEntity(
        id = id,
        ownerId = ownerId,
        title = title,
        description = description,
        iconKey = iconKey,
        createdAt = Instant.parse(createdAt).toEpochMilli(),
        updatedAt = Instant.parse(updatedAt).toEpochMilli()
    )
fun WishlistDto.toDomain(): Wishlist = Wishlist(
    id = id,
    title = title,
    description = description,
    iconKey = iconKey,
    ownerId = ownerId,
    createdAt = Instant.parse(createdAt).toEpochMilli(),
    updatedAt = Instant.parse(updatedAt).toEpochMilli()
)

fun WishlistEntity.toDomain(): Wishlist =
    Wishlist(
        id = id,
        ownerId = ownerId,
        title = title,
        description = description,
        iconKey = iconKey,
        createdAt = createdAt,
        updatedAt = updatedAt
    )