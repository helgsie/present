package `is`.hi.present.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `is`.hi.present.ui.wishlistdetail.WishlistDetailViewModel

@Composable
fun SharedWith(
    wishlistId: String,
    isLoading: Boolean,
    vm: WishlistDetailViewModel = hiltViewModel()
) {
    val state = vm.uiState.collectAsState().value
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.padding(16.dp)
    ) {
        IconButton(
            onClick = {
                vm.onSharedWith(wishlistId)
                expanded = true
            },
            enabled = !isLoading
        ) {
            Icon(
                imageVector = Icons.Default.AccountBox,
                contentDescription = "Þátttakendur"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            when {
                state.isLoading -> {
                    DropdownMenuItem(
                        text = { Text("Hleð þátttakendum...") },
                        onClick = { }
                    )
                }

                state.sharedWithEmails.isEmpty() -> {
                    DropdownMenuItem(
                        text = { Text("Engir þátttakendur enn") },
                        onClick = { expanded = false }
                    )
                }

                else -> {
                    state.sharedWithEmails.forEach { sharedUser ->
                        DropdownMenuItem(
                            text = { Text(sharedUser.email) },
                            onClick = { }
                        )
                    }
                }
            }
        }
    }
}