package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WatchHistoryEntity::class,
        FavoriteVideoEntity::class,
        SubscribedChannelEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TvDatabase : RoomDatabase() {
    abstract fun libraryDao(): TvLibraryDao

    companion object {
        @Volatile
        private var INSTANCE: TvDatabase? = null

        fun getInstance(context: Context): TvDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TvDatabase::class.java,
                    "tv_youtube.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
