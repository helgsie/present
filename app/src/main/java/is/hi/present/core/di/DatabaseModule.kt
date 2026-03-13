package `is`.hi.present.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import `is`.hi.present.core.local.AppDatabase
import javax.inject.Singleton
import `is`.hi.present.core.local.dao.WishlistDao
import `is`.hi.present.core.local.dao.WishlistItemDao

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "present.db"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    fun provideWishlistDao(db: AppDatabase): WishlistDao =
        db.wishlistDao()

    @Provides
    fun provideWishlistItemDao(db: AppDatabase): WishlistItemDao =
        db.wishlistItemDao()
}