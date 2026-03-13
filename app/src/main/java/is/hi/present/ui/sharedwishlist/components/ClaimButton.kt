package `is`.hi.present.ui.sharedwishlist.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `is`.hi.present.core.theme.*

@Composable
fun ClaimButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = RosePink,
            contentColor = TextPrimary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("Taka frá")
    }
}

@Composable
fun ReleaseClaimButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("Losa")
    }
}

@Composable
fun ClaimedBadge() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = BlushPink.copy(alpha = 0.55f)
    ) {
        Text(
            text = "Frátekið",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary
        )
    }
}