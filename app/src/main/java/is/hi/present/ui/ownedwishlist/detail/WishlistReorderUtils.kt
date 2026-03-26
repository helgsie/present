package `is`.hi.present.ui.ownedwishlist.detail

object WishlistReorderUtils {

    fun <T> moveItem(
        items: List<T>,
        fromIndex: Int,
        toIndex: Int
    ): List<T> {
        if (fromIndex == toIndex) return items
        if (fromIndex !in items.indices || toIndex !in items.indices) return items

        val mutable = items.toMutableList()
        val item = mutable.removeAt(fromIndex)
        mutable.add(toIndex, item)
        return mutable
    }
}