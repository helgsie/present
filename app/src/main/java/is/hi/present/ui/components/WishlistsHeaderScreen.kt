package `is`.hi.present.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.unit.DpOffset
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    var showAccountMenu by remember { mutableStateOf(false) }

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
                    Box {
                        IconButton(onClick = {
                            if (isEditMode && onDismissEditMode != null) {
                                onDismissEditMode()
                            } else {
                                showAccountMenu = true
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Aðgangsvalkostir",
                                modifier = Modifier.alpha(dimmedAlpha)
                            )
                        }
                        DropdownMenu(
                            expanded = showAccountMenu,
                            onDismissRequest = { showAccountMenu = false },
                            modifier = Modifier.widthIn(min = 220.dp),
                            offset = DpOffset(x = (-8).dp, y = 0.dp),
                            shape = RoundedCornerShape(20.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
                            tonalElevation = 2.dp,
                            shadowElevation = 14.dp,
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 2.dp)
                            ) {
                                HeaderMenuActionRow(
                                    text = "Aðgangsstillingar",
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Settings,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    textColor = MaterialTheme.colorScheme.onSurface,
                                    onClick = {
                                        showAccountMenu = false
                                        onAccountSettings()
                                    }
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                ) {
                                    androidx.compose.material3.HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }

                                HeaderMenuActionRow(
                                    text = "Útskrá",
                                    icon = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    textColor = MaterialTheme.colorScheme.error,
                                    onClick = {
                                        showAccountMenu = false
                                        onLogout()
                                    }
                                )
                            }
                        }
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

@Composable
private fun HeaderMenuActionRow(
    text: String,
    icon: @Composable () -> Unit,
    textColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = 46.dp)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()

        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
    }
}