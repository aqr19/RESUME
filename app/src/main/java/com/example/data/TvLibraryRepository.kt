package com.example.data

import android.content.Context
import com.example.data.local.FavoriteVideoEntity
import com.example.data.local.SubscribedChannelEntity
import com.example.data.local.TvDatabase
import com.example.data.local.WatchHistoryEntity
import com.example.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class HistoryVideoItem(
    val video: VideoItem,
    val playbackPositionMs: Long,
    val lastWatchedTimestamp: Long
)

class TvLibraryRepository(context: Context) {
    private val dao = TvDatabase.getInstance(context).libraryDao()

    val watchHistory: Flow<List<HistoryVideoItem>> = dao.getAllHistory().map { list ->
        list.map { entity ->
            HistoryVideoItem(
                video = VideoItem(
                    id = entity.videoId,
                    title = entity.title,
                    author = entity.author,
                    thumbnailUrl = entity.thumbnailUrl,
                    durationMs = entity.durationMs
                ),
                playbackPositionMs = entity.playbackPositionMs,
                lastWatchedTimestamp = entity.lastWatchedTimestamp
            )
        }
    }

    val favorites: Flow<List<VideoItem>> = dao.getAllFavorites().map { list ->
        list.map { entity ->
            VideoItem(
                id = entity.videoId,
                title = entity.title,
                author = entity.author,
                thumbnailUrl = entity.thumbnailUrl,
                durationMs = entity.durationMs
            )
        }
    }

    val subscriptions: Flow<List<SubscribedChannelEntity>> = dao.getAllSubscriptions()

    fun isFavorite(videoId: String): Flow<Boolean> = dao.isFavorite(videoId)

    fun isSubscribed(channelId: String): Flow<Boolean> = dao.isSubscribed(channelId)

    suspend fun recordPlayback(video: VideoItem, positionMs: Long) = withContext(Dispatchers.IO) {
        if (video.id.isBlank()) return@withContext
        dao.insertHistory(
            WatchHistoryEntity(
                videoId = video.id,
                title = video.title,
                author = video.author,
                thumbnailUrl = video.thumbnailUrl,
                durationMs = video.durationMs,
                playbackPositionMs = positionMs,
                lastWatchedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleFavorite(video: VideoItem) = withContext(Dispatchers.IO) {
        if (video.id.isBlank()) return@withContext
        val existing = dao.getFavorite(video.id)
        if (existing != null) {
            dao.deleteFavorite(video.id)
        } else {
            dao.insertFavorite(
                FavoriteVideoEntity(
                    videoId = video.id,
                    title = video.title,
                    author = video.author,
                    thumbnailUrl = video.thumbnailUrl,
                    durationMs = video.durationMs,
                    savedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun removeFavorite(videoId: String) = withContext(Dispatchers.IO) {
        dao.deleteFavorite(videoId)
    }

    suspend fun toggleSubscription(
        channelId: String,
        channelTitle: String,
        avatarUrl: String = "",
        subscriberCount: String = ""
    ) = withContext(Dispatchers.IO) {
        if (channelId.isBlank()) return@withContext
        // Check if already subscribed
        val list = dao.getAllSubscriptions()
        // Simple toggle
        dao.insertSubscription(
            SubscribedChannelEntity(
                channelId = channelId,
                channelTitle = channelTitle,
                channelAvatarUrl = avatarUrl,
                subscriberCount = subscriberCount,
                subscribedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun unsubscribe(channelId: String) = withContext(Dispatchers.IO) {
        dao.deleteSubscription(channelId)
    }

    suspend fun deleteHistoryItem(videoId: String) = withContext(Dispatchers.IO) {
        dao.deleteHistoryItem(videoId)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        dao.clearHistory()
    }

    suspend fun getSavedPosition(videoId: String): Long = withContext(Dispatchers.IO) {
        dao.getHistoryItem(videoId)?.playbackPositionMs ?: 0L
    }
}
