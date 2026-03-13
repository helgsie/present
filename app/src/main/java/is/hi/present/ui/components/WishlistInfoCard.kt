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

@Composable
fun WishlistInfoCard(
    description: String?
) {
    if (description.isNullOrBlank()) return

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = SoftCard,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Lýsing",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
        }
    }
}