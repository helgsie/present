package `is`.hi.present.ui.wishlistdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import `is`.hi.present.R
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import coil.compose.rememberAsyncImagePainter
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.runtime.saveable.rememberSaveable
import `is`.hi.present.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistDetailScreen(
    wishlistId: String,
    onBack: () -> Unit,
    onCreateItem: (wishlistId: String) -> Unit,
    onOpenItem: (itemId: String) -> Unit,
    vm: WishlistDetailViewModel = hiltViewModel()
) {
    val state = vm.uiState.collectAsState().value
    LaunchedEffect(wishlistId) {
        vm.loadAll(wishlistId)
    }
    val context = LocalContext.current
    var shareCode by remember { mutableStateOf<String?>(null) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var isEditing by rememberSaveable { mutableStateOf(false) }

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var iconKey by rememberSaveable { mutableStateOf<String?>(null) }

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

                WishlistDetailEffect.NavigateBack -> onBack()
                WishlistDetailEffect.WishlistSaved -> isEditing = false
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
                        val clipboardManager =
                            context.getSystemService(ClipboardManager::class.java)
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

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Eyða óskalista?") },
            text = { Text("Þessum óskalista og öllum gjöfum hans verður eytt til frambúðar.") },
            confirmButton = {
                TextButton(
                    enabled = !state.isLoading,
                    onClick = {
                        confirmDelete = false
                        vm.deleteWishlist(wishlistId) { onBack() }
                    }
                ) { Text("Eyða") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Hætta við") }
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
                                            message = "Til að deila óskalista þarf netsamband",
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
                            Icon(Icons.Default.Share, contentDescription = "Deila óskalista")
                        }
                    }

                    if (state.isOwner) {
                        if (!isEditing) {
                            IconButton(
                                enabled = !state.isLoading,
                                onClick = {
                                    title = state.title
                                    description = state.description.orEmpty()
                                    iconKey = state.iconKey
                                    isEditing = true
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Breyta óskalista")
                            }

                            IconButton(
                                enabled = !state.isLoading,
                                onClick = { confirmDelete = true }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Eyða óskalista")
                            }
                        } else {
                            TextButton(
                                enabled = !state.isLoading,
                                onClick = { isEditing = false }
                            ) { Text("Hætta við") }

                            TextButton(
                                enabled = title.trim().isNotBlank() && !state.isLoading,
                                onClick = {
                                    vm.updateWishlist(
                                        wishlistId = wishlistId,
                                        title = title.trim(),
                                        description = description.trim().ifBlank { null },
                                        iconKey = iconKey ?: state.iconKey,
                                        onDone = {
                                            isEditing = false
                                            vm.loadAll(wishlistId)
                                        }
                                    )
                                }
                            ) { Text("Vista") }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.isOwner) {
                FloatingActionButton(onClick = { onCreateItem(wishlistId) }) {
                    Icon(Icons.Default.Add, contentDescription = "Bæta við gjöf")
                }
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
                modifier = Modifier.fillMaxSize()
            ) {
                if (isOffline && state.items.isNotEmpty()) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when {
                        state.isLoading && state.items.isEmpty() -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        state.errorMessage != null && state.items.isEmpty() -> {
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
                                if (isEditing) {
                                    WishlistEditor(
                                        title = title,
                                        onTitleChange = { title = it },
                                        description = description,
                                        onDescriptionChange = { description = it }
                                    )
                                } else if (!state.description.isNullOrBlank()) {
                                    Text(
                                        text = state.description,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Þessi listi er tómur.")
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
                                    if (isEditing) {
                                        WishlistEditor(
                                            title = title,
                                            onTitleChange = { title = it },
                                            description = description,
                                            onDescriptionChange = { description = it }
                                        )
                                    } else if (!state.description.isNullOrBlank()) {
                                        Text(
                                            text = state.description,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                items(
                                    items = state.items,
                                    key = { it.id }
                                ) { item ->
                                    WishlistItemCard(
                                        w = item,
                                        onClick = { onOpenItem(item.id) },
                                        isOwner = state.isOwner,
                                        onClaim = { vm.claimItem(wishlistId, item.id) },
                                        onRelease = { vm.releaseClaim(wishlistId, item.id) }
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

@Composable
private fun WishlistEditor(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
    }
}

@Composable
private fun WishlistItemCard(w: WishlistItemUi, onClick: () -> Unit, onClaim: () -> Unit, isOwner: Boolean, onRelease: () -> Unit) {

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
                Text(
                    text = w.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!w.notes.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(w.notes, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.width(12.dp))
            w.price?.let { price ->
                Spacer(Modifier.width(12.dp))
                Text(
                    text = iskFormatter.format(price),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (!isOwner) {
                Spacer(Modifier.width(12.dp))
                if (!w.isClaimed) {
                    Button(
                        onClick = onClaim,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NewMint,
                            contentColor = Purple40
                        )
                    ) {
                        Text("Taka frá")
                    }
                }
                if (w.isClaimedByMe) {
                    Button(
                        onClick = onRelease,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurpleGrey80,
                            contentColor = Black
                        )
                    ) {
                        Text("Losa gjöf")
                    }
                }
                if (w.isClaimed && !w.isClaimedByMe) {
                    Text(
                        text = "Frátekið",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}