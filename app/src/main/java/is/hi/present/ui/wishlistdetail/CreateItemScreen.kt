package `is`.hi.present.ui.wishlistdetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItemScreen(
    wishlistId: String,
    onBack: () -> Unit,
    onDone: () -> Unit,
    vm: WishlistDetailViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var url by rememberSaveable { mutableStateOf("") }
    var priceText by rememberSaveable { mutableStateOf("") }
    var selectedImageUri by rememberSaveable { mutableStateOf<String?>(null) }

    val trimmedTitle = title.trim()
    val trimmedDescription = description.trim().ifBlank { null }
    val trimmedUrl = url.trim().ifBlank { null }

    val parsedPrice: Double? = priceText
        .trim()
        .replace(",", ".")
        .toDoubleOrNull()

    val canSubmit = trimmedTitle.isNotBlank() && !state.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create item") },
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
            state.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

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

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = { Text("Price (ISK)") },
                modifier = Modifier.fillMaxWidth(),
                isError = priceText.isNotBlank() && parsedPrice == null,
            )

            Button(
                enabled = canSubmit,
                onClick = {
                    vm.createWishlistItem(
                        wishlistId = wishlistId,
                        title = trimmedTitle,
                        description = trimmedDescription,
                        url = trimmedUrl,
                        price = parsedPrice,
                        imagePath = selectedImageUri
                    )
                    onDone()
                },
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