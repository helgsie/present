package `is`.hi.present.ui.sharedWishlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import `is`.hi.present.ui.components.Segments
import `is`.hi.present.ui.components.WishlistCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedWishlistScreen(
    modifier: Modifier = Modifier,
    onAddSharedWishlist: () -> Unit,
    onAccountSettings: () -> Unit,
    onLogout: () -> Unit,
    onOpenWishlist: (wishlistId: String) -> Unit,
    vm: SharedWishlistViewModel = viewModel(),
    onSelectWishlists: () -> Unit,
    selectedSegmentIndex: Int = 0,
    onOpenSharedWishlists: () -> Unit,
    ) {
    val state = vm.uiState.collectAsState().value

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.loadSharedWishlists()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shared wishlists") },
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
            FloatingActionButton(onClick = onAddSharedWishlist) {
                Icon(Icons.Default.Add, contentDescription = "Add shared wishlist")
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
            Spacer(modifier = Modifier.height(24.dp))
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
                            Button(onClick = { vm.loadSharedWishlists() }) {
                                Text("Retry")
                            }
                        }
                    }

                    state.isEmpty -> {
                        Text(
                            text = "You have no shared wishlists yet.",
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