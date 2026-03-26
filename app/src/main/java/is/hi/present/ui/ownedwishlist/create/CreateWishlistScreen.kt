package `is`.hi.present.ui.ownedwishlist.create

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `is`.hi.present.ui.ownedwishlist.components.IconPickerButton
import `is`.hi.present.ui.components.WishlistIcon
import `is`.hi.present.ui.ownedwishlist.list.WishlistsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWishlistScreen(
    ownerId: String,
    vm: WishlistsViewModel,
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
                title = { Text("Búa til óskalista") },
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
                label = { Text("Titill") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Lýsing(valkvætt)") },
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
                        ownerId = ownerId,
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
                Text("Búa til")
                state.errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}