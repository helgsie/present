package `is`.hi.present.ui.wishlist.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import `is`.hi.present.R
import `is`.hi.present.ui.wishlist.detail.WishlistItemUi
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WishlistItemCard(
    w: WishlistItemUi,
    onClick: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val iskFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("is-IS")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
    }
    ElevatedCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(w.name, style = MaterialTheme.typography.titleMedium)
                if (!w.notes.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(w.notes, style = MaterialTheme.typography.bodyMedium)
                }
            }
            w.price?.let { price ->
                Spacer(Modifier.width(12.dp))
                Text(
                    text = iskFormatter.format(price),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            trailingContent?.let {
                Spacer(Modifier.width(12.dp))
                Box {
                    it()
                }
            }
        }
    }
}