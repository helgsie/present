package `is`.hi.present.ui.ownedwishlist.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `is`.hi.present.ui.components.AddButton
import `is`.hi.present.ui.components.Segments
import `is`.hi.present.ui.components.WishlistCard
import android.content.res.Configuration
import `is`.hi.present.ui.ownedwishlist.detail.WishlistDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistsScreen(
    modifier: Modifier = Modifier,
    ownerId: String,
    wishlistVm: WishlistsViewModel,
    detailVm: WishlistDetailViewModel,
    onLogout: () -> Unit,
    onAccountSettings: () -> Unit,
    onCreateWishlist: () -> Unit,
    onOpenSharedWishlists: () -> Unit,
    onOpenWishlist: (wishlistId: String) -> Unit,
    onSelectWishlists: () -> Unit,
    selectedSegmentIndex: Int = 0,
) {
    LaunchedEffect(ownerId) {
        wishlistVm.loadWishlists(ownerId)
    }

    val state by wishlistVm.uiState.collectAsStateWithLifecycle()
    val pullState = rememberPullToRefreshState()

    var isEditMode by remember { mutableStateOf(false) }
    var wishlistToDelete by remember { mutableStateOf<String?>(null) }

    val dismissEditMode = {
        isEditMode = false
        wishlistToDelete = null
    }
    val dimmedAlpha = if (isEditMode) 0.45f else 1f

    LaunchedEffect(state.wishlists) {
        if (state.wishlists.isEmpty() && isEditMode) {
            dismissEditMode()
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val columns = if (isLandscape) 4 else 2

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = isEditMode,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = dismissEditMode
                    )
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Óskalistar",
                            modifier = Modifier.alpha(dimmedAlpha)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    actions = {
                        IconButton(onClick = {
                            if (isEditMode) {
                                dismissEditMode()
                            } else {
                                onAccountSettings()
                            }
                        }) {
                            Icon(
                                Icons.Filled.AccountCircle,
                                contentDescription = "Account Settings",
                                modifier = Modifier.alpha(dimmedAlpha)
                            )
                        }
                        IconButton(onClick = {
                            if (isEditMode) {
                                dismissEditMode()
                            } else {
                                onLogout()
                            }
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                modifier = Modifier.alpha(dimmedAlpha)
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            Box(modifier = Modifier.alpha(dimmedAlpha)) {
                AddButton(
                    onClick = {
                        if (isEditMode) {
                            dismissEditMode()
                        } else {
                            onCreateWishlist()
                        }
                    },
                    contentDescription = "Create wishlist"
                )
            }
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
                    if (isEditMode) {
                        dismissEditMode()
                    } else {
                        when (index) {
                            0 -> onSelectWishlists()
                            1 -> onOpenSharedWishlists()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .alpha(dimmedAlpha)
            )

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = {
                    wishlistVm.refresh(ownerId)
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
                            onDismissRequest = { wishlistVm.consumeOfflineDialog() },
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
                                    TextButton(onClick = { wishlistVm.consumeOfflineDialog() }) {
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
                                        Button(onClick = { wishlistVm.refresh(ownerId) }) {
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
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable(
                                                enabled = isEditMode,
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = dismissEditMode
                                            )
                                    ) {
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
                                                    onClick = {
                                                        if (!isEditMode) {
                                                            onOpenWishlist(w.id)
                                                        }
                                                    },
                                                    isEditMode = isEditMode,
                                                    showLeaveButton = true,
                                                    onLeaveClick = { wishlistToDelete = w.id },
                                                    onLongClick = { isEditMode = true }
                                                )
                                            }
                                        }
                                    }
                                    if (wishlistToDelete != null) {
                                        AlertDialog(
                                            onDismissRequest = dismissEditMode,
                                            confirmButton = {
                                                TextButton(onClick = {
                                                    detailVm.deleteWishlist(wishlistToDelete!!)
                                                    dismissEditMode()
                                                }) {
                                                    Text("Eyða")
                                                }
                                            },
                                            dismissButton = {
                                                TextButton(onClick = dismissEditMode) {
                                                    Text("Hætta við")
                                                }
                                            },
                                            title = { Text("Ertu viss þú viljir eyða?") },
                                            text = { Text("Óskalistanum verður eytt ef þú heldur áfram.") }
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