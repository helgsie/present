package `is`.hi.present.ui.sharedwishlist.list

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `is`.hi.present.ui.components.AddButton
import `is`.hi.present.ui.components.WishlistCard
import `is`.hi.present.ui.components.WishlistsHeaderScreen

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
    onOpenSharedWishlists: () -> Unit,
    embeddedInHeaderScreen: Boolean = false,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val pullState = rememberPullToRefreshState()

    var isEditMode by remember { mutableStateOf(false) }
    var wishlistToLeave by remember { mutableStateOf<String?>(null) }

    val dismissEditMode = {
        isEditMode = false
        wishlistToLeave = null
    }
    val dimmedAlpha = if (isEditMode) 0.45f else 1f

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val columns = if (isLandscape) 4 else 2

    LaunchedEffect(Unit) {
        vm.loadSharedWishlists()
    }

    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message = msg)
        }
    }

    LaunchedEffect(state.wishlists) {
        if (state.wishlists.isEmpty() && isEditMode) {
            dismissEditMode()
        }
    }

    val screenContent: @Composable (PaddingValues) -> Unit = { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
        ) {
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
                                            onLeaveClick = { wishlistToLeave = w.id },
                                            onLongClick = { isEditMode = true }
                                        )
                                    }
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
                                    dismissEditMode()
                                }) {
                                    Text("Yfirgefa")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = dismissEditMode) {
                                    Text("Hætta við")
                                }
                            },
                            title = { Text("Ertu viss þú viljir yfirgefa?") },
                            text = { Text("Þú missir aðgang að þessum óskalista ef þú heldur áfram.") }
                        )
                    }
                }
            }
        }
    }

    if (embeddedInHeaderScreen) {
        screenContent(PaddingValues())
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                WishlistsHeaderScreen(
                    selectedSegmentIndex = selectedSegmentIndex,
                    onSelectedChange = { index ->
                        when (index) {
                            0 -> onSelectWishlists()
                            1 -> onOpenSharedWishlists()
                        }
                    },
                    onAccountSettings = onAccountSettings,
                    onLogout = onLogout,
                    title = "Óskalistar",
                    isEditMode = isEditMode,
                    onDismissEditMode = dismissEditMode
                )
            },
            floatingActionButton = {
                Box(modifier = Modifier.alpha(dimmedAlpha)) {
                    AddButton(
                        onClick = {
                            if (isEditMode) {
                                dismissEditMode()
                            } else {
                                onAddSharedWishlist()
                            }
                        },
                        contentDescription = "Add shared wishlist"
                    )
                }
            }
        ) { padding ->
            screenContent(padding)
        }
    }
}