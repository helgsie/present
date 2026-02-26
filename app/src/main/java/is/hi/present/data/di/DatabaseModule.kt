package `is`.hi.present.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import `is`.hi.present.data.local.*
import `is`.hi.present.data.local.dao.WishlistDao
import `is`.hi.present.data.local.dao.WishlistItemDao

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
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideWishlistDao(db: AppDatabase): WishlistDao =
        db.wishlistDao()

    @Provides
    fun provideWishlistItemDao(db: AppDatabase): WishlistItemDao =
        db.wishlistItemDao()
}