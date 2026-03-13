package `is`.hi.present.ui.sharedwishlist.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import `is`.hi.present.R
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedItemDetailScreen(
    itemId: String,
    onBack: () -> Unit,
    vm: SharedItemDetailViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var lastSnackbarMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(itemId) {
        vm.load(itemId)
    }

    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage
        if (!msg.isNullOrBlank() && msg != lastSnackbarMessage) {
            lastSnackbarMessage = msg
            snackbarHostState.showSnackbar(
                message = msg,
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.name.ifBlank { "Gjöf" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.errorMessage != null && state.name.isBlank() -> {
                    Text(
                        text = state.errorMessage ?: "Villa kom upp",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                else -> {
                    val iskFormatter = remember {
                        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("is-IS")).apply {
                            maximumFractionDigits = 0
                            minimumFractionDigits = 0
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val painter =
                            if (!state.imagePath.isNullOrBlank()) {
                                rememberAsyncImagePainter(state.imagePath)
                            } else {
                                painterResource(R.drawable.ic_item_placeholder)
                            }

                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )

                        Text(
                            text = state.name,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        state.price?.let { price ->
                            Text(
                                text = iskFormatter.format(price),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        if (!state.notes.isNullOrBlank()) {
                            Text(
                                text = state.notes ?: "",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        when {
                            !state.isClaimed -> {
                                Button(
                                    onClick = { vm.claim(itemId) }
                                ) {
                                    Text("Taka frá")
                                }
                            }

                            state.isClaimedByMe -> {
                                Button(
                                    onClick = { vm.release(itemId) }
                                ) {
                                    Text("Hætta við frátekningu")
                                }
                            }

                            else -> {
                                Surface(
                                    tonalElevation = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Þessi gjöf hefur þegar verið tekin frá.",
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}