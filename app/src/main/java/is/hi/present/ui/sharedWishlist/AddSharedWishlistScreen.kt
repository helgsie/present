package `is`.hi.present.ui.sharedWishlist

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import `is`.hi.present.ui.components.ErrorMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSharedWishlistScreen(
    onBack: () -> Unit,
    onJoined: (wishlistId: String) -> Unit,
    vm: AddSharedWishlistViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    var code by remember { mutableStateOf("") }

    LaunchedEffect(state.joinedWishlistId) {
        state.joinedWishlistId?.let { wishlistId ->
            onJoined(wishlistId)
            vm.clearJoinedState()
        }
    }
    LaunchedEffect(state.error) {
        if (state.error != null) {
            kotlinx.coroutines.delay(2500)
            vm.clearError()
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
            state.error?.let { message ->
                ErrorMessage(
                    message = message,
                    modifier = Modifier
                        .padding(top = 10.dp, end = 50.dp)
                )
            }
        }
    }
}