//package `is`.hi.present.ui.ownedwishlist.detail
//
//import `is`.hi.present.ui.ownedwishlist.item.ItemDetailUiState
//
//fun <T> List<T>.move(fromIndex: Int, toIndex: Int): List<T> {
//    if (fromIndex == toIndex) return this
//    val mutable = toMutableList()
//    val item = mutable.removeAt(fromIndex)
//    mutable.add(toIndex, item)
//    return mutable
//}
//
//fun List<ItemDetailUiState>.withUpdatedSortOrder(): List<WishlistItemUiState> {
//    return mapIndexed { index, item ->
//        item.copy(sortOrder = index)
//    }
//}