package `is`.hi.present.ui.wishlists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `is`.hi.present.ui.components.WishlistIcon
import `is`.hi.present.ui.components.toImageVector
import androidx.compose.material.icons.automirrored.filled.ExitToApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistsScreen(
    modifier: Modifier = Modifier,
    vm: WishlistsViewModel,
    onLogout: () -> Unit,
    onAccountSettings: () -> Unit,
    onCreateWishlist: () -> Unit,
    onOpenWishlist: (wishlistId: String) -> Unit,
) {
    val state = vm.uiState.collectAsState().value

    if (state.needsAuth) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Not logged in, this will direct to the login page.")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My wishlists") },
                actions = {
                    IconButton(onClick = onAccountSettings) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "Account Settings")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateWishlist) {
                Icon(Icons.Default.Add, contentDescription = "Create wishlist")
            }
        }
    ) { padding ->
        Box(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { vm.loadWishlists() }) { Text("Retry") }
                    }
                }

                state.isEmpty -> {
                    Text(
                        text = "You have no wishlists yet.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.wishlists, key = { it.id }) { w ->
                            WishlistCard(
                                w = w,
                                onClick = { onOpenWishlist(w.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WishlistCard(w: WishlistUi, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = WishlistIcon.fromKey(w.iconKey).toImageVector()
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(w.title, style = MaterialTheme.typography.titleMedium)
                if (!w.description.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(w.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}