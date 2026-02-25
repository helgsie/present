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
import `is`.hi.present.ui.components.IconPickerButton
import `is`.hi.present.ui.components.WishlistIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWishlistScreen(
    vm: WishlistsViewModel = viewModel(),
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    val state by vm.uiState.collectAsState()

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedIcon by rememberSaveable { mutableStateOf(WishlistIcon.FAVORITE) }

    val trimmedTitle = title.trim()
    val trimmedDescription = description.trim().ifBlank { null }
    val canSubmit = trimmedTitle.isNotBlank() && !state.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create wishlist") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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

            IconPickerButton(
                selectedIcon = selectedIcon,
                onSelected = { selectedIcon = it },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    vm.createWishlist(
                        title = trimmedTitle,
                        description = trimmedDescription,
                        icon = selectedIcon,
                        onDone = onDone
                    )
                },
                enabled = canSubmit
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Text("Create")
            }
        }
    }
}