package `is`.hi.present.ui.ownedwishlist.create

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
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import android.Manifest
import android.content.Context
import androidx.core.net.toUri
import `is`.hi.present.ui.ownedwishlist.detail.WishlistDetailViewModel
import `is`.hi.present.ui.ownedwishlist.components.CATEGORY_ICON
import androidx.compose.material.icons.filled.KeyboardArrowDown
import `is`.hi.present.ui.ownedwishlist.components.CategoryPickerSheet
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
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var showCategorySheet by remember { mutableStateOf(false) }

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
        selectedCameraBitmap = null
    }
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            selectedCameraBitmap = it
            selectedImageUri = null
        }
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
                title = { Text("Ný gjöf") },
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
                label = { Text("Titill") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Lýsing (valkvæð)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Hlekkur (valkvæð)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = { Text("Verð (ISK)") },
                modifier = Modifier.fillMaxWidth(),
                isError = priceText.isNotBlank() && parsedPrice == null,
            )

            Box {
                OutlinedTextField(
                    value = selectedCategory ?: "",
                    onValueChange = {},
                    label = { Text("Flokkur (valkvæð)") },
                    placeholder = { Text("Enginn flokkur") },
                    readOnly = true,
                    leadingIcon = {
                        selectedCategory?.let { cat ->
                            CATEGORY_ICON[cat]?.let { icon ->
                                Icon(imageVector = icon, contentDescription = null)
                            }
                        }
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Velja flokk"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showCategorySheet = true }
                )
            }

            if (showCategorySheet) {
                CategoryPickerSheet(
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it },
                    onDismiss = { showCategorySheet = false }
                )
            }

            // Buttons for images/photos
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { galleryLauncher.launch("image/*") }) {
                    Text("Velja frá safni")
                }

                Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Taka mynd")
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
                        category = selectedCategory,
                        selectedImageUri = imageUriToUpload,
                        context = context,
                        onDone = onDone
                    )
                },
            ) {
                Text("Búa til")
            }
        }
    }
}
fun saveBitmapToFile(context: Context, bitmap: Bitmap): Uri {
    val file = File(
        context.cacheDir,
        "camera_${System.currentTimeMillis()}.jpg"
    )

    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }

    return file.toUri()
}
