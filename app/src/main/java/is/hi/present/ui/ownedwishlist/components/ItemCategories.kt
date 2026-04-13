package `is`.hi.present.ui.ownedwishlist.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Toys
import androidx.compose.ui.graphics.vector.ImageVector

val ITEM_CATEGORIES = listOf(
    "Fatnaður",
    "Bækur",
    "Raftæki",
    "Leikföng",
    "Íþróttir",
    "Húsgögn",
    "Fegurð",
    "Skart",
    "Annað"
)

val CATEGORY_ICON: Map<String, ImageVector> = mapOf(
    "Fatnaður" to Icons.Default.Checkroom,
    "Bækur" to Icons.Default.AutoStories,
    "Raftæki" to Icons.Default.Devices,
    "Leikföng" to Icons.Default.Toys,
    "Íþróttir" to Icons.Default.FitnessCenter,
    "Húsgögn" to Icons.Default.Chair,
    "Fegurð" to Icons.Default.Spa,
    "Skart" to Icons.Default.Diamond,
    "Annað" to Icons.Default.Category
)
