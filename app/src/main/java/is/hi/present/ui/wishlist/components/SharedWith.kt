package `is`.hi.present.ui.wishlist.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `is`.hi.present.ui.wishlist.detail.WishlistDetailViewModel

@Composable
fun SharedWith(
    wishlistId: String,
    isLoading: Boolean,
    vm: WishlistDetailViewModel = hiltViewModel()
) {
    val state = vm.uiState.collectAsState().value
    var showSharedWithDialog by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            vm.onSharedWith(wishlistId)
            showSharedWithDialog = true
        },
        enabled = !isLoading
    ) {
        Icon(
            imageVector = Icons.Default.AccountBox,
            contentDescription = "Þátttakendur"
        )
    }

    if (showSharedWithDialog) {
        AlertDialog(
            onDismissRequest = { showSharedWithDialog = false },
            title = { Text("Deilt með") },
            text = {
                when {
                    state.isLoading -> {
                        Text("Hleð þátttakendum...")
                    }

                    state.sharedWithEmails.isEmpty() -> {
                        Text("Engir þátttakendur enn")
                    }

                    else -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.sharedWithEmails.forEach { sharedUser ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(sharedUser.email)

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
                TextButton(onClick = { showSharedWithDialog = false }) {
                    Text("Loka")
                }
            }
        )
    }
}