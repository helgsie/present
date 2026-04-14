package `is`.hi.present.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistsHeaderScreen(
    selectedSegmentIndex: Int,
    onSelectedChange: (Int) -> Unit,
    onAccountSettings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Óskalistar",
    isEditMode: Boolean = false,
    onDismissEditMode: (() -> Unit)? = null
) {
    val dimmedAlpha = if (isEditMode) 0.45f else 1f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = isEditMode && onDismissEditMode != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onDismissEditMode?.invoke() }
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        modifier = Modifier.alpha(dimmedAlpha)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = {
                        if (isEditMode && onDismissEditMode != null) {
                            onDismissEditMode()
                        } else {
                            onAccountSettings()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Account Settings",
                            modifier = Modifier.alpha(dimmedAlpha)
                        )
                    }

                    IconButton(onClick = {
                        if (isEditMode && onDismissEditMode != null) {
                            onDismissEditMode()
                        } else {
                            onLogout()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            modifier = Modifier.alpha(dimmedAlpha)
                        )
                    }
                }
            )

            Segments(
                selectedIndex = selectedSegmentIndex,
                onSelectedChange = { index ->
                    if (isEditMode && onDismissEditMode != null) {
                        onDismissEditMode()
                    } else {
                        onSelectedChange(index)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .alpha(dimmedAlpha)
            )
        }
    }
}