package `is`.hi.present.ui.ownedwishlist.item

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import `is`.hi.present.ui.ownedwishlist.create.saveBitmapToFile
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale

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
    var showImagePickerDialog by rememberSaveable { mutableStateOf(false) }

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
    

    if (showImagePickerDialog) {
        val hasImage =
            selectedImageUri != null ||
                    selectedCameraBitmap != null ||
                    !state.imageUrl.isNullOrBlank()

        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showImagePickerDialog = false
                            galleryLauncher.launch("image/*")
                        }
                    ) {
                        Text("Velja mynd úr myndasafni")
                    }

                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showImagePickerDialog = false
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    ) {
                        Text("Taka Mynd")
                    }

                    if (isEditing && hasImage) {
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isLoading,
                            onClick = {
                                showImagePickerDialog = false
                                vm.removeImage()
                                selectedImageUri = null
                                selectedCameraBitmap = null
                            }
                        ) {
                            Text("Fjarlægja mynd")
                        }
                    }

                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showImagePickerDialog = false }
                    ) {
                        Text("Hætta við")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
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
                                vm.load(itemId)
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
                state.errorMessage != null -> {
                    Text(
                    state.errorMessage,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }

                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.errorMessage != null) {
                        Surface(
                            tonalElevation = 1.dp,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Ekkert netsamband. Vistuð gögn eru birt.",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clickable(enabled = isEditing && !state.isLoading) {
                                showImagePickerDialog = true
                            },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                selectedImageUri != null -> {
                                    Image(
                                        painter = rememberAsyncImagePainter(selectedImageUri),
                                        contentDescription = "Selected gallery image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                selectedCameraBitmap != null -> {
                                    Image(
                                        bitmap = selectedCameraBitmap!!.asImageBitmap(),
                                        contentDescription = "Captured camera image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                !state.imageUrl.isNullOrBlank() -> {
                                    Image(
                                        painter = rememberAsyncImagePainter(state.imageUrl),
                                        contentDescription = "Current item image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                else -> {
                                    Text(
                                       text = if (isEditing) "Ýttu hér til að bæta við mynd" else "Engin mynd"
                                    )
                                }
                            }
                        }
                    }

                    if (isEditing) {
                        Text(
                            text = "Ýttu á myndina til að breyta",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }


                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.name,
                        onValueChange = vm::onNameChange,
                        label = { Text("Nafn") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = !isEditing,
                        enabled = true,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = vm::onNotesChange,
                        label = { Text("Lýsing") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = !isEditing,
                        enabled = true
                    )

                    OutlinedTextField(
                        value = state.url,
                        onValueChange = vm::onUrlChange,
                        label = { Text("Tengill") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = !isEditing,
                        enabled = true,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )

                    OutlinedTextField(
                        value = state.priceText,
                        onValueChange = vm::onPriceChange,
                        label = { Text("Verð") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = !isEditing,
                        singleLine = true,
                        enabled = true,
                    )
                }
            }
        }
    }
}


