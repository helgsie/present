package `is`.hi.present.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import `is`.hi.present.core.theme.SoftCard
import `is`.hi.present.core.theme.SoftSurfaceVariant
import `is`.hi.present.ui.ownedwishlist.list.WishlistUi

@Composable
fun WishlistCard(
    w: WishlistUi,
    onClick: () -> Unit,
    isEditMode: Boolean = false,
    showLeaveButton: Boolean = false,
    onLeaveClick: (() -> Unit)? = null
) {
    val shakeTransition = rememberInfiniteTransition(label = "shake")
    val shakeOffset by shakeTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(150),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeOffset"
    )

    val cardShape = RoundedCornerShape(22.dp)
    val mediaShape = RoundedCornerShape(18.dp)
    val cardPadding = 14.dp
    val sectionSpacing = 8.dp

    Box {
        ElevatedCard(
            onClick = onClick,
            shape = cardShape,
            colors = CardDefaults.elevatedCardColors(
                containerColor = SoftCard
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(cardPadding),
                verticalArrangement = Arrangement.spacedBy(sectionSpacing)
            ) {
                WishlistCardMedia(
                    imageUrls = w.previewImageUrls,
                    iconKey = w.iconKey,
                    mediaShape = mediaShape
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    WishlistCardText(
                        title = w.title,
                        isShared = w.isShared
                    )

                    ItemCountText(itemCount = w.itemCount)
                }
            }
        }

        WishlistLeaveButton(
            isVisible = isEditMode && showLeaveButton,
            shakeOffset = shakeOffset,
            onClick = onLeaveClick
        )
    }
}

@Composable
private fun WishlistCardMedia(
    imageUrls: List<String>,
    iconKey: String?,
    mediaShape: RoundedCornerShape
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        WishlistPreviewGrid(
            imageUrls = imageUrls,
            iconKey = iconKey,
            modifier = Modifier.fillMaxWidth(),
            shape = mediaShape
        )
    }
}

@Composable
private fun ItemCountText(itemCount: Int) {
    Text(
        text = "$itemCount ${if (itemCount == 1) "Gjöf" else "Gjafir"}",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun WishlistCardText(
    title: String,
    isShared: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (!isShared) Icons.Default.Lock else Icons.Default.People,
            contentDescription = if (!isShared) "Einka" else "Deilt",
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.width(6.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BoxScope.WishlistLeaveButton(
    isVisible: Boolean,
    shakeOffset: Float,
    onClick: (() -> Unit)?
) {
    if (!isVisible || onClick == null) return

    androidx.compose.material3.Surface(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 8.dp, y = (-8).dp)
            .offset(x = shakeOffset.dp)
            .size(30.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.error,
        tonalElevation = 4.dp
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Fara úr óskalista",
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun WishlistPreviewGrid(
    imageUrls: List<String>,
    iconKey: String?,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(22.dp)
) {
    val images = imageUrls.take(4)
    val gridSpacing = 3.dp
    val hasImages = images.isNotEmpty()
    val backgroundColor = if (hasImages) MaterialTheme.colorScheme.surface else SoftSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(shape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (!hasImages) {
            val icon = WishlistIcon.fromKey(iconKey).toImageVector()
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val previewSlots = List(4) { index -> images.getOrNull(index) }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(gridSpacing)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(gridSpacing)
                ) {
                    previewSlots.take(2).forEach { imageUrl ->
                        PreviewGridSlot(
                            imageUrl = imageUrl,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(gridSpacing)
                ) {
                    previewSlots.drop(2).forEach { imageUrl ->
                        PreviewGridSlot(
                            imageUrl = imageUrl,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewGridSlot(
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    if (imageUrl == null) {
        Box(
            modifier = modifier.background(SoftSurfaceVariant)
        )
    } else {
        NetworkImage(
            imageUrl = imageUrl,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun NetworkImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale
    )
}