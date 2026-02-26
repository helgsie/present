package `is`.hi.present.ui.wishlistdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import `is`.hi.present.ui.components.IconPickerButton
import `is`.hi.present.ui.components.WishlistIcon
import `is`.hi.present.ui.components.toImageVector
import `is`.hi.present.ui.wishlists.WishlistsViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistDetailScreen(
    wishlistId: String,
    onBack: () -> Unit,
    onCreateItem: (wishlistId: String) -> Unit,
    wishlistsVm: WishlistsViewModel,
    detailVm: WishlistDetailViewModel = viewModel()
) {
    val state = detailVm.uiState.collectAsState().value
    val listState = wishlistsVm.uiState.collectAsState().value

    var isEditing by rememberSaveable { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var iconKey by rememberSaveable { mutableStateOf("favorite") }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(wishlistId) {
        detailVm.loadAll(wishlistId)
    }


    LaunchedEffect(isEditing, state.id) {
        if (isEditing && state.id != null) {
            title = state.title
            description = state.description.orEmpty()
            iconKey = state.iconKey
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete Wishlist?") },
            text = { Text("This wishlist and all its items will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    wishlistsVm.deleteWishlist(wishlistId) {
                        onBack()
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val iconVector = WishlistIcon.fromKey(
                            if (isEditing) iconKey else state.iconKey
                        ).toImageVector()
                        Icon(imageVector = iconVector, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(state.title.ifBlank { "Wishlist" })
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(
                            enabled = !state.isLoading,
                            onClick = { isEditing = true }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit wishlist")
                        }

                        IconButton(
                            enabled = !state.isLoading,
                            onClick = { confirmDelete = true }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete wishlist")
                        }
                    } else {
                        TextButton(
                            enabled = !listState.isLoading,
                            onClick = { isEditing = false }
                        ) { Text("Cancel") }

                        TextButton(
                            enabled = title.trim().isNotBlank() && !listState.isLoading,
                            onClick = {
                                wishlistsVm.updateWishlist(
                                    wishlistId = wishlistId,
                                    title = title.trim(),
                                    description = description.trim().ifBlank { null },
                                    icon = WishlistIcon.fromKey(iconKey),
                                    onDone = {
                                        isEditing = false
                                        detailVm.loadAll(wishlistId)
                                    }
                                )
                            }
                        ) { Text("Save") }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onCreateItem(wishlistId) }) {
                Icon(Icons.Default.Add, contentDescription = "Create wishlist item")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (listState.errorMessage != null) {
                Text(
                    text = listState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                )
            }

            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.errorMessage != null -> {
                    Text(
                        text = state.errorMessage,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                state.isEmpty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isEditing) {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Title") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Description (optional)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            IconPickerButton(
                                selectedIcon = WishlistIcon.fromKey(iconKey),
                                onSelected = { iconKey = it.key },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else if (!state.description.isNullOrBlank()) {
                            Text(state.description, style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(Modifier.height(16.dp))
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("This wishlist doesn’t have any items yet.")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (isEditing) {
                                    OutlinedTextField(
                                        value = title,
                                        onValueChange = { title = it },
                                        label = { Text("Title") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = description,
                                        onValueChange = { description = it },
                                        label = { Text("Description (optional)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    IconPickerButton(
                                        selectedIcon = WishlistIcon.fromKey(iconKey),
                                        onSelected = { iconKey = it.key },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else if (!state.description.isNullOrBlank()) {
                                    Text(
                                        state.description,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        items(state.item, key = { it.id }) { w ->
                            WishlistItemCard(w = w, onClick = { })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WishlistItemCard(w: WishlistItemUi, onClick: () -> Unit) {
    val iskFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("is-IS")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
    }

    ElevatedCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(w.title, style = MaterialTheme.typography.titleMedium)
                if (!w.description.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(w.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
            w.price?.let { price ->
                Spacer(Modifier.width(12.dp))
                Text(iskFormatter.format(price), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}