package `is`.hi.present.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import `is`.hi.present.data.local.dao.WishlistDao
import `is`.hi.present.data.local.dao.WishlistItemDao
import `is`.hi.present.data.local.entity.WishlistEntity
import `is`.hi.present.data.local.entity.WishlistItemEntity

@Database(
    entities = [
        WishlistEntity::class,
        WishlistItemEntity::class
   ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wishlistDao(): WishlistDao
    abstract fun wishlistItemDao(): WishlistItemDao
}