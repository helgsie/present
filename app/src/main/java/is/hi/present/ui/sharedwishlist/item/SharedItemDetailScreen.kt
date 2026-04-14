package `is`.hi.present.ui.sharedwishlist.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
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
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Card
import androidx.compose.ui.platform.LocalUriHandler
import `is`.hi.present.core.theme.MintCream
import `is`.hi.present.core.theme.TextPrimary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import `is`.hi.present.R
import java.net.URI
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
    val uriHandler = LocalUriHandler.current
    val iskFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("is-IS")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
    }

    LaunchedEffect(itemId) { vm.load(itemId) }

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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            if (!state.imagePath.isNullOrBlank()) {
                                Image(
                                    painter = rememberAsyncImagePainter(state.imagePath),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.ic_item_placeholder),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }

                        Text(
                            text = state.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        state.price?.let { price ->
                            Text(
                                text = iskFormatter.format(price),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (!state.notes.isNullOrBlank()) {
                            Text(
                                text = state.notes!!,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!state.url.isNullOrBlank()) {
                            val displayHost = runCatching {
                                URI(state.url!!).host?.removePrefix("www.") ?: state.url!!
                            }.getOrElse { state.url!! }
                            SuggestionChip(
                                onClick = {
                                    val target = if (state.url!!.startsWith("http")) state.url!!
                                               else "https://${state.url!!}"
                                    runCatching { uriHandler.openUri(target) }
                                },
                                label = { Text(displayHost) },
                                icon = {
                                    Icon(
                                        Icons.Default.OpenInNew,
                                        contentDescription = "Opna tengil",
                                        modifier = Modifier.size(SuggestionChipDefaults.IconSize)
                                    )
                                }
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        when {
                            !state.isClaimed -> {
                                Button(
                                    onClick = { vm.claim(itemId) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Taka frá")
                                }
                            }

                            state.isClaimedByMe -> {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MintCream,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Þú hefur tekið þessa gjöf frá.",
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Button(
                                    onClick = { vm.release(itemId) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Hætta við frátekningu")
                                }
                            }

                            else -> {
                                Surface(
                                    tonalElevation = 1.dp,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (!state.claimedByName.isNullOrBlank())
                                            "Þessi gjöf hefur þegar verið tekin frá af ${state.claimedByName}."
                                        else
                                            "Þessi gjöf hefur þegar verið tekin frá.",
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
