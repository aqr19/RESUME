package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscribed_channels")
data class SubscribedChannelEntity(
    @PrimaryKey val channelId: String,
    val channelTitle: String,
    val channelAvatarUrl: String = "",
    val subscriberCount: String = "",
    val subscribedTimestamp: Long = System.currentTimeMillis()
)
