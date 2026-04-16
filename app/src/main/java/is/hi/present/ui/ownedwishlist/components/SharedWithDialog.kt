package `is`.hi.present.ui.ownedwishlist.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `is`.hi.present.ui.ownedwishlist.detail.WishlistDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedWithDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    wishlistId: String,
    isLoading: Boolean,
    vm: WishlistDetailViewModel = hiltViewModel()
) {
    if (!visible) return
    val state = vm.uiState.collectAsState().value

    BasicAlertDialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .widthIn(min = 280.dp, max = 360.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 4.dp,
            shadowElevation = 12.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(modifier = Modifier.height(2.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Þátttakendur",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Þú hefur deilt óskalistanum með þessum notendum.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                when {
                    state.sharedWithError != null -> {
                        Text(
                            text = state.sharedWithError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    state.sharedWithUsers.isEmpty() -> {
                        Text(
                            text = "Engir þátttakendur enn",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    else -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            state.sharedWithUsers.forEachIndexed { index, sharedUser ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sharedUser.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )

                                    TextButton(
                                        modifier = Modifier.wrapContentWidth(Alignment.End),
                                        contentPadding = PaddingValues(0.dp),
                                        onClick = {
                                            vm.removeSharedUser(
                                                wishlistId = wishlistId,
                                                userId = sharedUser.userId
                                            )
                                        }
                                    ) {
                                        Text(
                                            text = "Fjarlægja",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                if (index != state.sharedWithUsers.lastIndex) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Loka")
                        }
                    }
                }
            }
        }
    }
}