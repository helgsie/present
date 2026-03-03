package `is`.hi.present.ui.wishlistdetail


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.saveable.rememberSaveable
import `is`.hi.present.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    wishlistId: String,
    itemId: String,
    onBack: () -> Unit,
    vm: ItemDetailViewModel = hiltViewModel()
) {
    val state = vm.uiState.collectAsState().value

    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var isEditing by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(itemId) { vm.load(itemId) }

    LaunchedEffect(Unit) {
        vm.effects.collectLatest { effect ->
            when (effect) {
                ItemDetailEffect.NavigateBack -> onBack()
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete item?") },
            text = { Text("This item will be permanently removed.") },
            confirmButton = {
                TextButton(
                    enabled = !state.isLoading,
                    onClick = {
                        confirmDelete = false
                        vm.delete(itemId)
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        enabled = !state.isLoading,
                        onClick = { confirmDelete = true }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }

                    if (!isEditing) {
                        IconButton(
                            enabled = !state.isLoading,
                            onClick = { isEditing = true }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    } else {
                        IconButton(
                            enabled = !state.isLoading,
                            onClick = { isEditing = false }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }

                        IconButton(
                            enabled = !state.isLoading && state.name.trim().isNotBlank(),
                            onClick = {
                                vm.save(itemId)
                                isEditing = false
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.errorMessage != null -> Text(
                    state.errorMessage,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = vm::onNameChange,
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isEditing,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = vm::onNotesChange,
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isEditing
                    )

                    OutlinedTextField(
                        value = state.priceText,
                        onValueChange = vm::onPriceChange,
                        label = { Text("Price") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isEditing,
                        singleLine = true
                    )
                }
            }
        }
    }
}
