package `is`.hi.present.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import `is`.hi.present.R
import `is`.hi.present.core.theme.SoftCard
import `is`.hi.present.core.theme.SoftSurfaceVariant
import `is`.hi.present.core.theme.TextPrimary
import `is`.hi.present.core.theme.TextSecondary
import `is`.hi.present.ui.ownedwishlist.detail.WishlistItemUi
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WishlistItemCard(
    w: WishlistItemUi,
    onClick: () -> Unit,
    trailingContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    val iskFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("is-IS")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
    }

    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = SoftCard
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val painter = if (!w.imagePath.isNullOrBlank()) {
                rememberAsyncImagePainter(w.imagePath)
            } else {
                painterResource(R.drawable.ic_item_placeholder)
            }

            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
            )

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = w.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!w.notes.isNullOrBlank()) {
                    Text(
                        text = w.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                w.price?.let { price ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SoftSurfaceVariant
                    ) {
                        Text(
                            text = iskFormatter.format(price),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary
                        )
                    }
                }

                trailingContent?.invoke(this)
            }
        }
    }
}