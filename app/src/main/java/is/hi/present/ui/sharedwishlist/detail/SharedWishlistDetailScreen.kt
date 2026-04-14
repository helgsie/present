package `is`.hi.present.ui.sharedwishlist.detail

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `is`.hi.present.ui.components.WishlistItemCard
import `is`.hi.present.ui.sharedwishlist.components.ClaimButton
import `is`.hi.present.ui.sharedwishlist.components.ReleaseClaimButton
import `is`.hi.present.ui.sharedwishlist.components.ClaimedBadge
import `is`.hi.present.ui.sharedwishlist.components.ClaimedByMeBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedWishlistDetailScreen(
    wishlistId: String,
    onBack: () -> Unit,
    onOpenItem: (itemId: String) -> Unit,
    vm: SharedWishlistDetailViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    var showMenu by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showOnlyMyClaims by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(wishlistId) {
        vm.load(wishlistId)
    }

    LaunchedEffect(state.didLeaveWishlist) {
        if (state.didLeaveWishlist) {
            onBack()
            vm.consumeLeaveSuccess()
        }
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Ertu viss þú viljir yfirgefa?") },
            text = {
                Text("Þú missir aðgang að þessum óskalista ef þú heldur áfram.")
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Hætta við")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveDialog = false
                        vm.leaveSharedWishlist(wishlistId)
                    }
                ) {
                    Text("Yfirgefa")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title.ifBlank { "Wishlist" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Meira")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Yfirgefa óskalista") },
                            onClick = {
                                showMenu = false
                                showLeaveDialog = true
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.errorMessage != null -> {
                    Text(
                        text = state.errorMessage ?: "Óþekkt villa kom upp.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = (-40).dp)
                    )
                }

                state.isEmpty -> {
                    Text(
                        text = "Þessi listi er tómur.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = (-40).dp)
                    )
                }

                else -> {
                    val categories = state.items
                        .mapNotNull { it.category }
                        .distinct()
                        .sorted()

                    val visibleItems = state.items
                        .let { if (showOnlyMyClaims) it.filter { item -> item.isClaimedByMe } else it }
                        .let { if (selectedCategoryFilter != null) it.filter { item -> item.category == selectedCategoryFilter } else it }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!state.description.isNullOrBlank()) {
                            item {
                                Column {
                                    Text(
                                        text = state.description ?: "",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(Modifier.height(4.dp))
                                }
                            }
                        }

                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    FilterChip(
                                        selected = showOnlyMyClaims,
                                        onClick = { showOnlyMyClaims = !showOnlyMyClaims },
                                        label = { Text("Frátekið af mér") }
                                    )
                                }
                                if (categories.isNotEmpty()) {
                                    items(categories) { cat ->
                                        FilterChip(
                                            selected = selectedCategoryFilter == cat,
                                            onClick = {
                                                selectedCategoryFilter =
                                                    if (selectedCategoryFilter == cat) null else cat
                                            },
                                            label = { Text(cat) }
                                        )
                                    }
                                }
                            }
                        }

                        if (visibleItems.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Engar gjafir passa við valinn flokk.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(
                                items = visibleItems,
                                key = { it.id }
                            ) { item ->
                                WishlistItemCard(
                                    w = item,
                                    onClick = { onOpenItem(item.id) },
                                    trailingContent = {
                                        when {
                                            !item.isClaimed -> {
                                                ClaimButton(
                                                    onClick = { vm.claimItem(wishlistId, item.id) }
                                                )
                                            }

                                            item.isClaimedByMe -> {
                                                ClaimedByMeBadge()
                                                ReleaseClaimButton(
                                                    onClick = { vm.releaseClaim(wishlistId, item.id) }
                                                )
                                            }

                                            else -> {
                                                ClaimedBadge(claimerName = item.claimedByUserName)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}