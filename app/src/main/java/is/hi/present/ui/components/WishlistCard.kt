package `is`.hi.present.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import `is`.hi.present.R
import `is`.hi.present.ui.theme.BlushPink
import `is`.hi.present.ui.theme.MintAccent
import `is`.hi.present.ui.theme.SoftCard
import `is`.hi.present.ui.theme.SoftSurfaceVariant
import `is`.hi.present.ui.theme.TextSecondary
import `is`.hi.present.ui.wishlists.WishlistUi

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

    val cardShape = RoundedCornerShape(28.dp)

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
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val painter = if (!w.previewImageUrl.isNullOrBlank()) {
                    rememberAsyncImagePainter(w.previewImageUrl)
                } else {
                    rememberAsyncImagePainter(R.drawable.ic_item_placeholder)
                }

                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.35f)
                        .clip(RoundedCornerShape(22.dp)),
                    contentScale = ContentScale.Crop
                )

                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SoftSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = WishlistIcon.fromKey(w.iconKey).toImageVector()
                        Icon(
                            imageVector = icon,
                            contentDescription = null
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = w.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!w.description.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = w.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SoftSurfaceVariant
                    ) {
                        Text(
                            text = "${w.itemCount} item" + if (w.itemCount == 1) "" else "s",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (w.isShared) {
                            MintAccent.copy(alpha = 0.45f)
                        } else {
                            BlushPink.copy(alpha = 0.45f)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (w.isShared) Icons.Default.People else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (w.isShared) "Shared" else "Private",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        if (isEditMode && showLeaveButton && onLeaveClick != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .offset(x = shakeOffset.dp)
                    .size(30.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error,
                tonalElevation = 4.dp
            ) {
                IconButton(onClick = onLeaveClick) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Leave wishlist",
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}