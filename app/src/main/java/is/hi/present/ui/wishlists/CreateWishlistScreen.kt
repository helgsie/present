package `is`.hi.present.ui.wishlists

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import `is`.hi.present.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWishlistScreen(
    navController: NavHostController
) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val parentEntry = remember(currentEntry) {
        navController.getBackStackEntry(Routes.WISHLISTS)
    }
    val vm: WishlistsViewModel = viewModel(parentEntry)

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create wishlist") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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


            Button(onClick = {
                vm.createWishlist(
                    title = title.trim(),
                    description = description.trim().ifBlank { null },
                    onDone = { navController.popBackStack() }
                )
            })
            {
                Text("Create")
            }
        }
    }
}
