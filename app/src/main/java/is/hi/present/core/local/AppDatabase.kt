package `is`.hi.present.core.local

import androidx.room.Database
import androidx.room.RoomDatabase
import `is`.hi.present.core.local.dao.PendingOpDao
import `is`.hi.present.core.local.dao.WishlistDao
import `is`.hi.present.core.local.dao.WishlistItemDao
import `is`.hi.present.core.local.entity.WishlistEntity
import `is`.hi.present.core.local.entity.WishlistItemEntity
import `is`.hi.present.core.local.entity.PendingOpEntity

@Database(
    entities = [
        WishlistEntity::class,
        WishlistItemEntity::class,
        PendingOpEntity::class
   ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wishlistDao(): WishlistDao
    abstract fun wishlistItemDao(): WishlistItemDao
    abstract fun pendingOpDao(): PendingOpDao
}