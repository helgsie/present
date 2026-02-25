package `is`.hi.present.ui.sharedWishlist

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSharedWishlistScreen(
    onBack: () -> Unit,
    onJoined: (wishlistId: String) -> Unit,
    vm: AddSharedWishlistViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    var code by remember { mutableStateOf("") }

    LaunchedEffect(state.joinedWishlistId) {
        state.joinedWishlistId?.let { wishlistId ->
            onJoined(wishlistId)
            vm.clearJoinedState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add shared wishlist") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Invite code") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { vm.joinByToken(code) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Join")
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}