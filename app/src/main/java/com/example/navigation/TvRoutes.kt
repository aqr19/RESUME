package com.example.navigation

import com.example.model.VideoItem
import java.net.URLEncoder

/**
 * Type-safe navigation routes and deep-link patterns for TV YouTube Single-Activity Navigation.
 */
object TvRoutes {
    const val HOME_BASE = "home"
    const val HOME = "home?category={category}"
    const val SEARCH = "search"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val PLAYER = "player/{videoId}?title={title}&author={author}&durationMs={durationMs}&thumb={thumb}"

    private fun safeEncode(value: String): String {
        return try {
            URLEncoder.encode(value, "UTF-8")
        } catch (e: Exception) {
            value
        }
    }

    /**
     * Constructs the Home route, optionally with a content category (e.g. "Home", "Trending", "Music", "Gaming", "News").
     */
    fun home(category: String = "Home"): String {
        val encodedCategory = safeEncode(category)
        return "home?category=$encodedCategory"
    }

    /**
     * Constructs the Search screen route.
     */
    fun search(): String = SEARCH

    /**
     * Constructs the Library screen route.
     */
    fun library(): String = LIBRARY

    /**
     * Constructs the Settings screen route.
     */
    fun settings(): String = SETTINGS

    /**
     * Constructs the Player route from a VideoItem.
     */
    fun player(video: VideoItem): String {
        val encodedTitle = safeEncode(video.title)
        val encodedAuthor = safeEncode(video.author)
        val encodedThumb = safeEncode(video.thumbnailUrl)
        return "player/${video.id}?title=$encodedTitle&author=$encodedAuthor&durationMs=${video.durationMs}&thumb=$encodedThumb"
    }

    /**
     * Constructs the Player route with explicit parameters.
     */
    fun player(
        videoId: String,
        title: String = "",
        author: String = "",
        durationMs: Long = 0L,
        thumbnailUrl: String = ""
    ): String {
        val encodedTitle = safeEncode(title)
        val encodedAuthor = safeEncode(author)
        val encodedThumb = safeEncode(thumbnailUrl)
        return "player/$videoId?title=$encodedTitle&author=$encodedAuthor&durationMs=$durationMs&thumb=$encodedThumb"
    }
}

