package `is`.hi.present.ui.ownedwishlist.detail

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `is`.hi.present.ui.components.AddButton
import `is`.hi.present.ui.ownedwishlist.components.SharedWithSection
import `is`.hi.present.ui.components.WishlistItemCard
import `is`.hi.present.ui.components.WishlistInfoCard
import `is`.hi.present.ui.ownedwishlist.components.WishlistEditor
import `is`.hi.present.core.theme.SoftCard
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.itemsIndexed
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

    val pullState = rememberPullToRefreshState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var shareCode by remember { mutableStateOf<String?>(null) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var isEditing by rememberSaveable { mutableStateOf(false) }

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var iconKey by rememberSaveable { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    var lastSnackbarMessage by remember { mutableStateOf<String?>(null) }

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
                else -> {}
            }
        }
    }

    shareCode?.let { code ->
        AlertDialog(
            onDismissRequest = { shareCode = null },
            title = { Text("Aðgangskóði") },
            text = {
                SelectionContainer {
                    Text(code)
                }
            },
            confirmButton = {
                TextButton(onClick = { shareCode = null }) {
                    Text("Loka")
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
                        scope.launch {
                            snackbarHostState.showSnackbar("Aðgangskóða afritað")
                        }
                    }
                ) {
                    Text("Afrita")
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
                        vm.deleteWishlist(wishlistId)
                    }
                ) {
                    Text("Eyða")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("Hætta við")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(state.title.ifBlank { "Wishlist" }) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Til baka"
                        )
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
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Deila óskalista"
                            )
                        }

                        SharedWithSection(
                            isLoading = state.isLoading,
                            wishlistId = wishlistId
                        )

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
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Breyta óskalista"
                                )
                            }

                            IconButton(
                                enabled = !state.isLoading,
                                onClick = { confirmDelete = true }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eyða óskalista"
                                )
                            }
                        } else {
                            TextButton(
                                enabled = !state.isLoading,
                                onClick = { isEditing = false }
                            ) {
                                Text("Hætta við")
                            }

                            TextButton(
                                enabled = title.trim().isNotBlank() && !state.isLoading,
                                onClick = {
                                    vm.updateWishlist(
                                        wishlistId = wishlistId,
                                        title = title.trim(),
                                        description = description.trim().ifBlank { null },
                                        iconKey = iconKey ?: state.iconKey
                                    )
                                }
                            ) {
                                Text("Vista")
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            AddButton(
                onClick = { onCreateItem(wishlistId) },
                contentDescription = "Bæta við gjöf"
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { vm.refresh(wishlistId) },
            state = pullState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    if (isOffline && state.items.isNotEmpty()) {
                        Surface(
                            tonalElevation = 1.dp,
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(18.dp),
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
                                if (isOffline) {
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
                                        } else {
                                            WishlistInfoCard(
                                                description = state.description
                                            )
                                        }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(24.dp),
                                                color = SoftCard,
                                                tonalElevation = 1.dp
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(
                                                        horizontal = 24.dp,
                                                        vertical = 20.dp
                                                    ),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = "Engar vistaðar gjafir fundust fyrir þennan lista.",
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                    Text(
                                                        text = "Þú getur samt bætt við nýrri gjöf og hún vistast locally þar til netsamband kemur aftur.",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.padding(top = 8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = state.errorMessage,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
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
                                    } else {
                                        WishlistInfoCard(
                                            description = state.description
                                        )
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(24.dp),
                                            color = SoftCard,
                                            tonalElevation = 1.dp
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(
                                                    horizontal = 24.dp,
                                                    vertical = 20.dp
                                                ),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "Þessi listi er tómur.",
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    item {
                                        if (isEditing) {
                                            WishlistEditor(
                                                title = title,
                                                onTitleChange = { title = it },
                                                description = description,
                                                onDescriptionChange = { description = it }
                                            )
                                        } else {
                                            WishlistInfoCard(
                                                description = state.description
                                            )
                                        }
                                    }

//                                    items(
//                                        items = state.items,
//                                        key = { _, item -> item.id }
//                                    ) {item ->
//                                        WishlistItemCard(
//                                            w = item,
//                                            onClick = { onOpenItem(item.id) }
//                                        )
//                                    }

                                    itemsIndexed(
                                        items = state.items,
                                        key = { _, item -> item.id }
                                    ) { index, item ->
                                        ReorderableWishlistRow(
                                            item = item,
                                            index = index,
                                            onMove = { fromIndex, toIndex ->
                                                vm.onMoveItem(fromIndex, toIndex)
                                            },
                                            onDragEnd = {
                                                vm.persistReorderedItems()
                                            },
                                            onClick = { onOpenItem(item.id) }
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
