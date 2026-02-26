package `is`.hi.present.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Segments(
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("Wishlists", "Shared")

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                selected = selectedIndex == index,
                onClick = { onSelectedChange(index) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                label = { Text(label) }
            )
        }
    }
}