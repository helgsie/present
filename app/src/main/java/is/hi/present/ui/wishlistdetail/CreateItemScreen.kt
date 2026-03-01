package `is`.hi.present.ui.wishlistdetail

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import android.Manifest
import android.content.Context
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItemScreen(
    wishlistId: String,
    onBack: () -> Unit,
    onDone: () -> Unit,
    vm: WishlistDetailViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    var name by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var url by rememberSaveable { mutableStateOf("") }
    var priceText by rememberSaveable { mutableStateOf("") }
    var selectedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var selectedCameraBitmap by rememberSaveable { mutableStateOf<Bitmap?>(null) }

    val trimmedName = name.trim()
    val trimmedNotes = notes.trim().ifBlank { null }
    val trimmedUrl = url.trim().ifBlank { null }

    val parsedPrice: Double? = priceText
        .trim()
        .replace(",", ".")
        .toDoubleOrNull()

    val canSubmit = trimmedName.isNotBlank() && !state.isLoading

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { selectedCameraBitmap = it }
    }

    // Camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) cameraLauncher.launch()
        else Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
    }

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
                value = name,
                onValueChange = { name = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
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

            // Buttons for images/photos
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { galleryLauncher.launch("image/*") }) {
                    Text("Choose from gallery")
                }

                Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Take photo")
                }
            }

            // preview for selected image/photo that was taken
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    selectedImageUri != null -> {
                        // Show gallery image
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri.toString()),
                            contentDescription = "Selected gallery image",
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1f)
                        )
                    }
                    selectedCameraBitmap != null -> {
                        // Show camera bitmap
                        Image(
                            bitmap = selectedCameraBitmap!!.asImageBitmap(),
                            contentDescription = "Captured camera image",
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1f)
                        )
                    }
                }
            }

            Button(
                enabled = canSubmit,
                onClick = {
                    val imageUriToUpload = selectedCameraBitmap?.let {
                        saveBitmapToFile(context, it)
                    } ?: selectedImageUri

                    vm.createWishlistItem(
                        wishlistId = wishlistId,
                        name = trimmedName,
                        notes = trimmedNotes,
                        url = trimmedUrl,
                        price = parsedPrice,
                        selectedImageUri = imageUriToUpload,
                        context = context
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
private fun saveBitmapToFile(context: Context, bitmap: Bitmap): Uri {
    val file = File(
        context.cacheDir,
        "camera_${System.currentTimeMillis()}.jpg"
    )

    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }

    return file.toUri()
}