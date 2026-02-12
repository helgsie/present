package `is`.hi.present.ui.Enums

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class WishlistIcon(val key: String, val label: String) {
    FAVORITE("favorite", "Favorite"),
    GIFT("gift", "Gift"),
    SHOPPING("shopping", "Shopping"),
    STAR("star", "Star"),
    HOME("home", "Home"),
    TRAVEL("travel", "Travel"),
    FOOD("food", "Food"),
    BOOK("book", "Book"),
    MUSIC("music", "Music"),
    PET("pet", "Pet"),
    GAME("game", "Game"),
    WORK("work", "Work");

    companion object {
        fun fromKey(key: String?): WishlistIcon =
            entries.firstOrNull { it.key == key } ?: FAVORITE
    }
}

fun WishlistIcon.toImageVector(): ImageVector = when (this) {
    WishlistIcon.FAVORITE -> Icons.Default.Favorite
    WishlistIcon.GIFT -> Icons.Default.CardGiftcard
    WishlistIcon.SHOPPING -> Icons.Default.ShoppingBag
    WishlistIcon.STAR -> Icons.Default.Star
    WishlistIcon.HOME -> Icons.Default.Home
    WishlistIcon.TRAVEL -> Icons.Default.Flight
    WishlistIcon.FOOD -> Icons.Default.Restaurant
    WishlistIcon.BOOK -> Icons.AutoMirrored.Filled.MenuBook
    WishlistIcon.MUSIC -> Icons.Default.MusicNote
    WishlistIcon.PET -> Icons.Default.Pets
    WishlistIcon.GAME -> Icons.Default.SportsEsports
    WishlistIcon.WORK -> Icons.Default.Work
}
