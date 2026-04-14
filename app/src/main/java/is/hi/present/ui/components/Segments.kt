package `is`.hi.present.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Segments(
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("Mínir", "Deilt")
    val selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    val selectedContentColor = MaterialTheme.colorScheme.onSurface
    val unselectedContainerColor = Color.Transparent
    val unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = selectedIndex == index

            SegmentedButton(
                selected = isSelected,
                onClick = { onSelectedChange(index) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = selectedContainerColor,
                    activeContentColor = selectedContentColor,
                    inactiveContainerColor = unselectedContainerColor,
                    inactiveContentColor = unselectedContentColor,
                    activeBorderColor = borderColor,
                    inactiveBorderColor = borderColor
                ),
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            )
        }
    }
}