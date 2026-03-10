package `is`.hi.present.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.unit.dp
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
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(120),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeOffset"
    )

    Box {
        ElevatedCard(onClick = onClick) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = WishlistIcon.fromKey(w.iconKey).toImageVector()
                Icon(icon, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(w.title, style = MaterialTheme.typography.titleMedium)
                    if (!w.description.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(w.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        if (isEditMode && showLeaveButton && onLeaveClick != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .offset(x = shakeOffset.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.error,
                tonalElevation = 4.dp
            ) {
                IconButton(onClick = onLeaveClick) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Leave wishlist",
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    }
}