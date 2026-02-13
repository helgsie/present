package `is`.hi.present.ui.wishlists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import `is`.hi.present.navigation.Routes
import `is`.hi.present.ui.Enums.WishlistIcon
import `is`.hi.present.ui.Enums.toImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    vm: WishlistsViewModel = viewModel(),
    onLogout: (() -> Unit)? = null
) {
    val state = vm.uiState.collectAsState().value
    if (state.needsAuth) {
        // navController.navigate("login")
        // for now
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Not logged in, this will direct to the login page.")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My wishlists") },
                actions = {
                    if (onLogout != null){
                        IconButton(onClick = {onLogout()}) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                        }
                    }
                }
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.CREATE_WISHLIST) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create wishlist")
            }
        }
    ) { padding ->
        Box(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { vm.loadWishlists() }) { Text("Retry") }
                    }
                }

                state.isEmpty -> {
                    Text(
                        text = "You have no wishlists yet.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Button(onClick = { navController.navigate(Routes.CREATE_WISHLIST) }) {
                        Text("Create a wishlist")
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.wishlists, key = { it.id }) { w ->
                            WishlistCard(w = w, onClick = { navController.navigate(Routes.wishlistDetail(w.id)) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WishlistCard(w: WishlistUi, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = WishlistIcon.fromKey(w.iconKey).toImageVector()
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(w.title, style = MaterialTheme.typography.titleMedium)
                if (!w.description.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(w.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
