package `is`.hi.present.ui.wishlists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CreateTokenScreen(
    token: String,
    onJoined: (wishlistId: String) -> Unit,
    vm: CreateTokenViewModel = hiltViewModel()
) {
    val state = vm.uiState.collectAsState().value

    LaunchedEffect(token) { vm.join(token) }

    LaunchedEffect(state.wishlistId) {
        state.wishlistId?.let(onJoined)
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            state.isLoading -> CircularProgressIndicator()
            state.error != null -> Text(state.error)
        }
    }
}