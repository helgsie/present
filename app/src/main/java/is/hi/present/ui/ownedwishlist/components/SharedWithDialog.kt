package `is`.hi.present.ui.ownedwishlist.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `is`.hi.present.ui.ownedwishlist.detail.WishlistDetailViewModel

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deilt með") },
        text = {
            when {
                state.sharedWithError != null -> {
                    Text(state.sharedWithError)
                }
                state.sharedWithUsers.isEmpty() -> {
                    Text("Engir þátttakendur enn")
                }
                else -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.sharedWithUsers.forEach { sharedUser ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(sharedUser.displayName)

                                TextButton(
                                    onClick = {
                                        vm.removeSharedUser(
                                            wishlistId = wishlistId,
                                            userId = sharedUser.userId
                                        )
                                    }
                                ) {
                                    Text("Fjarlægja")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Loka")
            }
        }
    )
}