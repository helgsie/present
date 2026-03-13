package `is`.hi.present.data.mapper

import `is`.hi.present.data.dto.WishlistItemDto
import `is`.hi.present.core.local.entity.WishlistItemEntity
import `is`.hi.present.domain.model.WishlistItem
import java.time.Instant

fun WishlistItemDto.toEntity(): WishlistItemEntity =
    WishlistItemEntity(
        id = id,
        wishlistId = wishlistId,
        name = name,
        notes = notes,
        url = url,
        price = price,
        imagePath = imagePath,
        category = category,
        sortOrder = sortOrder,
        createdAt = Instant.parse(createdAt).toEpochMilli(),
        updatedAt = Instant.parse(updatedAt).toEpochMilli()
    )

fun WishlistItemEntity.toDomain(): WishlistItem =
    WishlistItem(
        id = id,
        wishlistId = wishlistId,
        name = name,
        notes = notes,
        url = url,
        price = price,
        imagePath = imagePath,
        category = category,
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt
    )