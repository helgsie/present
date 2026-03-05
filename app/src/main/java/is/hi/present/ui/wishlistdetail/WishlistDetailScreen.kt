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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistDetailScreen(
    wishlistId: String,
    onBack: () -> Unit,
    onCreateItem: (wishlistId: String) -> Unit,
    cacheToRoom: Boolean = true,
    vm: WishlistDetailViewModel = hiltViewModel()
) {
    val state = vm.uiState.collectAsState().value
    LaunchedEffect(wishlistId) {
        vm.loadAll(wishlistId, cacheToRoom = cacheToRoom)
    }
    val context = LocalContext.current
    var shareCode by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    var lastSnackbarMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val isOffline = remember(state.errorMessage) {
        val msg = state.errorMessage.orEmpty()
        msg.contains("Ekkert netsamband", ignoreCase = true) ||
            msg.contains("offline", ignoreCase = true) ||
            msg.contains("Unable to resolve host", ignoreCase = true) ||
            msg.contains("Couldn't reach", ignoreCase = true)
    }

    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage
        if (!msg.isNullOrBlank() && msg != lastSnackbarMessage) {
            lastSnackbarMessage = msg
            snackbarHostState.showSnackbar(
                message = msg,
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
        }
    }

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
                            onClick = {
                                if (isOffline) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Deiling krefst netsambands",
                                            withDismissAction = true,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                } else {
                                    vm.onShareClicked(wishlistId)
                                }
                            },
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
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (isOffline && state.item.isNotEmpty()) {
                    Surface(
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Ekkert netsamband. Vistuð gögn eru birt.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when {
                        state.isLoading && state.item.isEmpty() -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }

                        state.item.isNotEmpty() -> {
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
                                        onClick = { }
                                    )
                                }
                            }
                        }

                        state.isEmpty -> {
                            Text(
                                text = "Þessi listi er tómur.",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        state.errorMessage != null -> {
                            Text(
                                text = state.errorMessage,
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.error
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
            Image(
                painter = painterResource(R.drawable.ic_item_placeholder),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
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