package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TvLibraryDao {

    // --- Watch History ---
    @Query("SELECT * FROM watch_history ORDER BY lastWatchedTimestamp DESC LIMIT 100")
    fun getAllHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE videoId = :videoId")
    suspend fun deleteHistoryItem(videoId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearHistory()

    @Query("SELECT * FROM watch_history WHERE videoId = :videoId LIMIT 1")
    suspend fun getHistoryItem(videoId: String): WatchHistoryEntity?

    // --- Favorites / Bookmarks ---
    @Query("SELECT * FROM favorite_videos ORDER BY savedTimestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(item: FavoriteVideoEntity)

    @Query("DELETE FROM favorite_videos WHERE videoId = :videoId")
    suspend fun deleteFavorite(videoId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_videos WHERE videoId = :videoId)")
    fun isFavorite(videoId: String): Flow<Boolean>

    @Query("SELECT * FROM favorite_videos WHERE videoId = :videoId LIMIT 1")
    suspend fun getFavorite(videoId: String): FavoriteVideoEntity?

    // --- Subscribed Channels ---
    @Query("SELECT * FROM subscribed_channels ORDER BY subscribedTimestamp DESC")
    fun getAllSubscriptions(): Flow<List<SubscribedChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(channel: SubscribedChannelEntity)

    @Query("DELETE FROM subscribed_channels WHERE channelId = :channelId")
    suspend fun deleteSubscription(channelId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM subscribed_channels WHERE channelId = :channelId)")
    fun isSubscribed(channelId: String): Flow<Boolean>
}
