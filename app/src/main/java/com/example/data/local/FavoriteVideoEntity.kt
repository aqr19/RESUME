package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_videos")
data class FavoriteVideoEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val author: String,
    val thumbnailUrl: String,
    val durationMs: Long = 0L,
    val savedTimestamp: Long = System.currentTimeMillis()
)
