package `is`.hi.present.ui.wishlistdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistDetailScreen(
    navController: NavHostController,
    wishlistId: String,
    vm: WishlistDetailViewModel = viewModel()
) {
    val state = vm.uiState.collectAsState().value

    LaunchedEffect(wishlistId) {
        vm.loadAll(wishlistId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title.ifBlank { "Wishlist" }) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create wishlist")
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
                        Text(text = "You have no wishlist Items yet.",)
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
                                onClick = { /* nav to item detail */ }
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
            Column(Modifier.weight(1f)) {
                Text(w.title, style = MaterialTheme.typography.titleMedium)
                if (!w.description.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(w.description, style = MaterialTheme.typography.bodyMedium)
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
