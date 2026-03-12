package `is`.hi.present.ui.wishlistdetail

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.collectLatest
import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    wishlistId: String,
    itemId: String,
    onBack: () -> Unit,
    vm: ItemDetailViewModel = hiltViewModel()
) {
    val state = vm.uiState.collectAsState().value
    val context = LocalContext.current


    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var isEditing by rememberSaveable { mutableStateOf(false) }

    var selectedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var selectedCameraBitmap by rememberSaveable { mutableStateOf<Bitmap?>(null) }


    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        selectedCameraBitmap = null
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            selectedCameraBitmap = it
            selectedImageUri = null
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) cameraLauncher.launch(null)
        else Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
    }

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
            title = { Text("Eyða gjöf?") },
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
                TextButton(onClick = { confirmDelete = false }) { Text("Hætta við") }
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gjöf") },
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
                            onClick = {
                                isEditing = false
                                selectedImageUri = null
                                selectedCameraBitmap = null
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }

                        IconButton(
                            enabled = !state.isLoading && state.name.trim().isNotBlank(),
                            onClick = {
                                val imageUriToUpload: Uri? =
                                    selectedCameraBitmap?.let { saveBitmapToFile(context, it) }
                                        ?: selectedImageUri
                                vm.save(itemId, wishlistId, context, imageUriToUpload)
                                isEditing = false
                                selectedImageUri = null
                                selectedCameraBitmap = null
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
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = vm::onNameChange,
                        label = { Text("Nafn") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isEditing,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = vm::onNotesChange,
                        label = { Text("Nótur") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isEditing
                    )

                    OutlinedTextField(
                        value = state.priceText,
                        onValueChange = vm::onPriceChange,
                        label = { Text("Verð") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isEditing,
                        singleLine = true
                    )
                    if (isEditing) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                enabled = !state.isLoading,
                                onClick = { galleryLauncher.launch("image/*") }
                            ) {
                                Text("Velja frá safni")
                            }

                            Button(
                                enabled = !state.isLoading,
                                onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                            ) {
                                Text("Taka mynd")
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(top = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            selectedImageUri != null -> {
                                Image(
                                    painter = rememberAsyncImagePainter(selectedImageUri.toString()),
                                    contentDescription = "Selected gallery image",
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(1f)
                                )
                            }

                            selectedCameraBitmap != null -> {
                                Image(
                                    bitmap = selectedCameraBitmap!!.asImageBitmap(),
                                    contentDescription = "Captured camera image",
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(1f)
                                )
                            }

                            // NOTE: if state.imageUri is a filename, you may need to convert it to a public URL.
                            // If it's already a URL, this will display it.
                            !state.imageUrl.isNullOrBlank() -> {
                                Image(
                                    painter = rememberAsyncImagePainter(state.imageUrl),
                                    contentDescription = "Current item image",
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(1f)
                                )
                            }

                            else -> {
                                Text("Engin mynd")
                            }
                        }
                    }
                }
            }
        }
    }
}


