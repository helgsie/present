package `is`.hi.present.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `is`.hi.present.core.theme.SoftCard
import `is`.hi.present.core.theme.TextPrimary
import `is`.hi.present.core.theme.TextSecondary
import `is`.hi.present.ui.ownedwishlist.components.WishlistMetadataRow

@Composable
fun WishlistInfoCard(
    description: String?,
    isShared: Boolean,
    itemCount: Int
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = SoftCard,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WishlistMetadataRow(
                isShared = isShared,
                itemCount = itemCount
            )

            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
        }
    }
}