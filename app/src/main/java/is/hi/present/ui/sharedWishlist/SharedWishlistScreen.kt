package `is`.hi.present.ui.sharedWishlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    vm: SharedWishlistViewModel = hiltViewModel(),
    onSelectWishlists: () -> Unit,
    selectedSegmentIndex: Int = 0,
    onOpenSharedWishlists: () -> Unit
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val pullState = rememberPullToRefreshState()

    var isEditMode by remember { mutableStateOf(false) }
    var wishlistToLeave by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        vm.loadSharedWishlists()
    }

    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message = msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Shared wishlists") },
                actions = {
                    TextButton(onClick = { isEditMode = !isEditMode }) {
                        Text(if (isEditMode) "Done" else "Edit")
                    }

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

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { vm.loadSharedWishlists() },
                state = pullState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        state.isLoading && state.wishlists.isEmpty() -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }

                        state.errorMessage != null && state.wishlists.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 32.dp)
                                    .offset(y = (-40).dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.WifiOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )

                                Spacer(Modifier.height(16.dp))

                                Text(
                                    text = "Ekkert netsamband",
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = "Gakktu úr skugga um að þú sért tengd/ur neti og reyndu aftur.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(20.dp))

                                OutlinedButton(onClick = { vm.loadSharedWishlists() }) {
                                    Text("Reyna aftur")
                                }
                            }
                        }

                        state.isEmpty -> {
                            Text(
                                text = "Engum óskalistum hefur verið deilt með þér",
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset(y = (-40).dp)
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
                                        onClick = {
                                            if (!isEditMode) {
                                                onOpenWishlist(w.id)
                                            }
                                        },
                                        isEditMode = isEditMode,
                                        showLeaveButton = true,
                                        onLeaveClick = {
                                            wishlistToLeave = w.id
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (wishlistToLeave != null) {
                        AlertDialog(
                            onDismissRequest = { wishlistToLeave = null },
                            confirmButton = {
                                TextButton(onClick = {
                                    vm.leaveSharedWishlist(wishlistToLeave!!)
                                    wishlistToLeave = null
                                }) {
                                    Text("Yfirgefa")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { wishlistToLeave = null }) {
                                    Text("Hætta við")
                                }
                            },
                            title = { Text("Yfirgefa lista?") },
                            text = { Text("Ertu viss um að þú viljir yfirgefa þennan shared wishlist?") }
                        )
                    }
                }
            }
        }
    }
}