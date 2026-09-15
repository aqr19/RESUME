package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val author: String,
    val thumbnailUrl: String,
    val durationMs: Long = 0L,
    val playbackPositionMs: Long = 0L,
    val lastWatchedTimestamp: Long = System.currentTimeMillis()
)
