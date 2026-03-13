package `is`.hi.present.ui.wishlist.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `is`.hi.present.ui.components.Segments
import `is`.hi.present.ui.wishlist.components.WishlistCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistsScreen(
    modifier: Modifier = Modifier,
    ownerId: String,
    vm: WishlistsViewModel,
    onLogout: () -> Unit,
    onAccountSettings: () -> Unit,
    onCreateWishlist: () -> Unit,
    onOpenSharedWishlists: () -> Unit,
    onOpenWishlist: (wishlistId: String) -> Unit,
    onSelectWishlists: () -> Unit,
    selectedSegmentIndex: Int = 0,
) {
    LaunchedEffect(ownerId) {
        vm.loadWishlists(ownerId)
    }
    val state by vm.uiState.collectAsStateWithLifecycle()
    val pullState = rememberPullToRefreshState()

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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
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

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = {
                    vm.refresh(ownerId)
                },
                state = pullState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    state.offlineDialog?.let { dialog ->
                        AlertDialog(
                            onDismissRequest = { vm.consumeOfflineDialog() },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = null
                                )
                            },
                            title = { Text(dialog.title) },
                            text = { Text(dialog.message) },
                            confirmButton = {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    TextButton(onClick = { vm.consumeOfflineDialog() }) {
                                        Text("OK")
                                    }
                                }
                            }
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        state.offlineBanner?.let { msg ->
                            Surface(
                                tonalElevation = 1.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = msg,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            when {
                                state.isLoading && state.wishlists.isEmpty() -> {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                }

                                state.errorMessage != null && state.wishlists.isEmpty() -> {
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = state.errorMessage ?: "Óþekkt villa kom upp.",
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        Button(onClick = { vm.refresh(ownerId) }) {
                                            Text("Reyna aftur")
                                        }
                                    }
                                }

                                state.isEmpty -> {
                                    Text(
                                        text = "Þú hefur ekki búið til neina óskalista.",
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }

                                else -> {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(
                                            items = state.wishlists,
                                            key = { it.id }
                                        ) { w ->
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
        }
    }
}
