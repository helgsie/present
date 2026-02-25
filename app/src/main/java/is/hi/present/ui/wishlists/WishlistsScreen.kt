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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import `is`.hi.present.ui.components.Segments
import `is`.hi.present.ui.components.WishlistCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistsScreen(
    modifier: Modifier = Modifier,
    vm: WishlistsViewModel,
    onLogout: () -> Unit,
    onAccountSettings: () -> Unit,
    onCreateWishlist: () -> Unit,
    onOpenSharedWishlists: () -> Unit,
    onOpenWishlist: (wishlistId: String) -> Unit,
    onSelectWishlists: () -> Unit,
    selectedSegmentIndex: Int = 0,
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
                    /*TextButton(onClick = onOpenSharedWishlists) {
                        Text("Shared")
                    }*/
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
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Segments(
                selectedIndex = selectedSegmentIndex,
                onSelectedChange = { index ->
                    when (index) {
                        0 -> onSelectWishlists()
                        1 -> onOpenSharedWishlists()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
                            Button(onClick = { vm.loadWishlists() }) {
                                Text("Retry")
                            }
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
}
