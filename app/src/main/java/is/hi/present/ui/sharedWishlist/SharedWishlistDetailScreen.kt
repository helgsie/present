package `is`.hi.present.ui.sharedWishlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `is`.hi.present.ui.components.WishlistItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedWishlistDetailScreen(
    wishlistId: String,
    onBack: () -> Unit,
    onOpenItem: (itemId: String) -> Unit,
    vm: SharedWishlistDetailViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(wishlistId) {
        vm.load(wishlistId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title.ifBlank { "Wishlist" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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

                        items(
                            items = state.items,
                            key = { it.id }
                        ) { item ->
                            WishlistItemCard(
                                w = item,
                                onClick = { onOpenItem(item.id) },
                                trailingContent = {
                                    if (!item.isClaimed) {
                                        Button(onClick = { vm.claimItem(wishlistId, item.id) }) {
                                            Text("Taka frá")
                                        }
                                    } else if (item.isClaimedByMe) {
                                        Button(onClick = { vm.releaseClaim(wishlistId, item.id) }) {
                                            Text("Hætta við")
                                        }
                                    } else {
                                        Text("Frátekið")
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