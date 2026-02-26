package `is`.hi.present.ui.wishlistdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import `is`.hi.present.R
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistDetailScreen(
    wishlistId: String,
    onBack: () -> Unit,
    onCreateItem: (wishlistId: String) -> Unit,
    vm: WishlistDetailViewModel = hiltViewModel()
) {
    val state = vm.uiState.collectAsState().value
    LaunchedEffect(wishlistId) {
        vm.loadAll(wishlistId)
    }
    val context = LocalContext.current
    var shareCode by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        vm.effects.collectLatest { effect ->
            when (effect) {
                is WishlistDetailEffect.ShowShareCode -> {
                    shareCode = effect.code
                }
            }
        }
    }

    shareCode?.let { code ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Invite code") },
            text = {
                SelectionContainer {
                    Text(code)
                }
            },
            confirmButton = {
                TextButton(onClick = { shareCode = null }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val clipboardManager = context.getSystemService(ClipboardManager::class.java)
                        clipboardManager?.setPrimaryClip(
                            ClipData.newPlainText("invite_code", code)
                        )
                    }
                ) {
                    Text("Copy")
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
                    if (state.isOwner) {
                        IconButton(
                            onClick = { vm.onShareClicked(wishlistId) },
                            enabled = !state.isLoading
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share wishlist")
                        }
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
                            .padding(16.dp)
                    ) {
                        if (!state.description.isNullOrBlank()) {
                            Text(
                                text = state.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "You have no wishlist Items yet.")
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!state.description.isNullOrBlank()) {
                            item {
                                Text(
                                    text = state.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        items(
                            items = state.item,
                            key = { it.id }
                        ) { w ->
                            WishlistItemCard(
                                w = w,
                                onClick = { /* later: onOpenItem(w.id) */ }
                            )
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
            val painter = if (!w.imagePath.isNullOrBlank()) {
                rememberAsyncImagePainter(w.imagePath)
            } else {
                painterResource(R.drawable.ic_item_placeholder)
            }
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(w.name, style = MaterialTheme.typography.titleMedium)
                if (!w.notes.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(w.notes, style = MaterialTheme.typography.bodyMedium)
                }
            }
            w.price?.let { price ->
                Spacer(Modifier.width(12.dp))
                Text(
                    text = iskFormatter.format(price),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}