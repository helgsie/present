package `is`.hi.present.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
fun IconPickerButton(
    selectedIcon: WishlistIcon,
    onSelected: (WishlistIcon) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = selectedIcon.toImageVector(),
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text("Icon: ${selectedIcon.label}")
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Open"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            WishlistIcon.entries.forEach { icon ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon.toImageVector(), contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text(icon.label)
                        }
                    },
                    onClick = {
                        onSelected(icon)
                        expanded = false
                    }
                )
            }
        }
    }
}
