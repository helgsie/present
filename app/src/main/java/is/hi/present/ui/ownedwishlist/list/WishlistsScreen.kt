package `is`.hi.present.ui.ownedwishlist.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `is`.hi.present.ui.components.AddButton
import `is`.hi.present.ui.components.Segments
import `is`.hi.present.ui.components.WishlistCard
import android.content.res.Configuration

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

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val columns = if (isLandscape) 4 else 2

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Óskalistarnir mínir") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = onAccountSettings) {
                        Icon(
                            Icons.Filled.AccountCircle,
                            contentDescription = "Account Settings"
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            AddButton(
                onClick = onCreateWishlist,
                contentDescription = "Create wishlist"
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
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
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        state.offlineBanner?.let { msg ->
                            Surface(
                                tonalElevation = 1.dp,
                                color = MaterialTheme.colorScheme.surface,
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
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center)
                                    )
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
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(columns),
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
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