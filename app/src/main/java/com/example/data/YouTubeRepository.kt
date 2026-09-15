package com.example.data

import com.example.model.SponsorSegmentItem
import com.example.model.VideoItem
import com.example.model.VideoPlaybackDetails
import com.example.model.VideoStreamFormat
import com.liskovsoft.mediaserviceinterfaces.ContentService
import com.liskovsoft.mediaserviceinterfaces.MediaItemService
import com.liskovsoft.mediaserviceinterfaces.ServiceManager
import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup
import com.liskovsoft.mediaserviceinterfaces.data.MediaItem
import com.liskovsoft.mediaserviceinterfaces.data.SponsorSegment
import com.liskovsoft.youtubeapi.service.YouTubeServiceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class YouTubeRepository {
    private val serviceManager: ServiceManager by lazy { YouTubeServiceManager.instance() }
    private val contentService: ContentService get() = serviceManager.contentService
    private val mediaItemService: MediaItemService get() = serviceManager.mediaItemService

    data class VideoSection(
        val title: String,
        val items: List<VideoItem>
    )

    suspend fun getHomeSections(): List<VideoSection> = withContext(Dispatchers.IO) {
        val sections = mutableListOf<VideoSection>()
        try {
            val groups: List<MediaGroup>? = contentService.home
            if (!groups.isNullOrEmpty()) {
                for (group in groups) {
                    val items = group.mediaItems?.mapNotNull { it.toVideoItem() } ?: emptyList()
                    if (items.isNotEmpty()) {
                        val rawTitle = group.title?.takeIf { it.isNotBlank() } ?: "Recommended"
                        sections.add(VideoSection(cleanTitle(rawTitle), items))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback or guarantee at least recommended section
        if (sections.isEmpty()) {
            try {
                val recGroup = contentService.recommended
                val recItems = recGroup?.mediaItems?.mapNotNull { it.toVideoItem() } ?: emptyList()
                if (recItems.isNotEmpty()) {
                    val rawTitle = recGroup.title ?: "Recommended"
                    sections.add(VideoSection(cleanTitle(rawTitle), recItems))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        sections
    }

    suspend fun getTrendingVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
        try {
            val groups = contentService.getSearch("Trending")
            groups?.flatMap { it.mediaItems ?: emptyList() }
                ?.mapNotNull { it.toVideoItem() }
                ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getCategoryVideos(categoryName: String): List<VideoItem> = withContext(Dispatchers.IO) {
        try {
            val groups = contentService.getSearch(categoryName)
            groups?.flatMap { it.mediaItems ?: emptyList() }
                ?.mapNotNull { it.toVideoItem() }
                ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun search(query: String): List<VideoItem> = withContext(Dispatchers.IO) {
        try {
            val groups = contentService.getSearch(query)
            groups?.flatMap { it.mediaItems ?: emptyList() }
                ?.mapNotNull { it.toVideoItem() }
                ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getSearchSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        try {
            contentService.getSearchTags(query)?.map { cleanTitle(it) }?.filter { it.isNotBlank() } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getVideoPlaybackDetails(videoId: String): VideoPlaybackDetails = withContext(Dispatchers.IO) {
        val formatInfo = mediaItemService.getFormatInfo(videoId)
            ?: throw IllegalStateException("Unable to retrieve format info for video $videoId")

        val sponsorSegments = try {
            mediaItemService.getSponsorSegments(videoId)?.map { it.toSponsorItem() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val metadata = try {
            mediaItemService.getMetadata(videoId)
        } catch (e: Exception) {
            null
        }

        val suggestions = mutableListOf<VideoItem>()
        if (metadata != null) {
            try {
                metadata.suggestions?.forEach { group ->
                    group.mediaItems?.mapNotNull { it.toVideoItem() }?.let { suggestions.addAll(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback for related videos if metadata suggestions are empty
        if (suggestions.isEmpty()) {
            try {
                val searchFallback = contentService.getSearch(formatInfo.author ?: "Trending")
                searchFallback?.flatMap { it.mediaItems ?: emptyList() }
                    ?.mapNotNull { it.toVideoItem() }
                    ?.filter { it.id != videoId }
                    ?.let { suggestions.addAll(it.take(15)) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val nextVideo = try {
            metadata?.nextVideo?.toVideoItem() ?: suggestions.firstOrNull { it.id != videoId }
        } catch (e: Exception) {
            suggestions.firstOrNull { it.id != videoId }
        }

        // 1. Try to generate local DASH MPD manifest via YouTubeMPDBuilder prioritizing AV1 codec
        val mpdContent: String? = try {
            val hasAv1 = formatInfo.adaptiveFormats?.any { it.mimeType?.contains("av01") == true } == true
            if (hasAv1) {
                val mpdBuilder = com.liskovsoft.youtubeapi.formatbuilders.mpdbuilder.YouTubeMPDBuilder.from(formatInfo)
                mpdBuilder.limitVideoCodec("av01")
                if (!mpdBuilder.isEmpty) {
                    mpdBuilder.build()?.bufferedReader()?.use { it.readText() }
                } else {
                    formatInfo.createMpdStream()?.bufferedReader()?.use { it.readText() }
                }
            } else {
                formatInfo.createMpdStream()?.bufferedReader()?.use { it.readText() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        // 2. Extract video stream formats: Include high-res adaptive video streams (4K, 2K, 1080p, 720p, etc.)
        // as well as muxed urlFormats, prioritizing AV1 codec
        val formatsMap = LinkedHashMap<String, VideoStreamFormat>()

        // Process adaptive formats (where real 4K, 2K, 1080p, 720p exist)
        // Sort so AV1 formats are processed first to guarantee AV1 is selected for each resolution
        val sortedAdaptive = formatInfo.adaptiveFormats?.sortedWith(
            compareByDescending<com.liskovsoft.mediaserviceinterfaces.data.MediaFormat> {
                it.mimeType?.contains("av01") == true
            }.thenByDescending { it.height }
             .thenByDescending { it.fps?.toIntOrNull() ?: 0 }
        )

        sortedAdaptive?.forEach { fmt ->
            val url = fmt.url
            val height = fmt.height
            var quality = fmt.qualityLabel ?: fmt.quality
            if (!url.isNullOrBlank() && height > 0) {
                if (quality.isNullOrBlank()) {
                    quality = "${height}p"
                }
                val fps = fmt.fps?.toIntOrNull() ?: if (quality.contains("60")) 60 else if (quality.contains("50")) 50 else 30
                val displayLabel = when {
                    height >= 2160 -> if (fps >= 50) "4K 60fps" else "4K"
                    height >= 1440 -> if (fps >= 50) "2K 60fps" else "2K"
                    else -> quality
                }
                val isAv1 = fmt.mimeType?.contains("av01") == true
                val existing = formatsMap[displayLabel]

                if (existing == null || (!existing.isAv1 && isAv1)) {
                    formatsMap[displayLabel] = VideoStreamFormat(
                        url = url,
                        mimeType = fmt.mimeType,
                        qualityLabel = displayLabel,
                        width = fmt.width.takeIf { it > 0 } ?: (height * 16 / 9),
                        height = height,
                        fps = fps
                    )
                }
            }
        }

        // Also process legacy muxed urlFormats (360p, 720p fallback)
        formatInfo.urlFormats?.forEach { fmt ->
            val url = fmt.url
            val quality = fmt.qualityLabel ?: fmt.quality
            if (!url.isNullOrBlank() && !quality.isNullOrBlank()) {
                val fps = if (quality.contains("60")) 60 else 30
                val height = fmt.height.takeIf { it > 0 } ?: when {
                    quality.contains("2160") -> 2160
                    quality.contains("1440") -> 1440
                    quality.contains("1080") -> 1080
                    quality.contains("720") -> 720
                    quality.contains("480") -> 480
                    quality.contains("360") -> 360
                    quality.contains("240") -> 240
                    quality.contains("144") -> 144
                    else -> 360
                }
                val width = fmt.width.takeIf { it > 0 } ?: (height * 16 / 9)
                val displayLabel = when {
                    height >= 2160 -> "4K"
                    height >= 1440 -> "2K"
                    else -> quality
                }
                if (!formatsMap.containsKey(displayLabel)) {
                    formatsMap[displayLabel] = VideoStreamFormat(
                        url = url,
                        mimeType = fmt.mimeType,
                        qualityLabel = displayLabel,
                        width = width,
                        height = height,
                        fps = fps
                    )
                }
            }
        }

        // Sort descending: 2160p (4K), 1440p (2K), 1080p, 720p, 480p, etc.
        val directFormats = formatsMap.values.sortedWith(
            compareByDescending<VideoStreamFormat> { it.height }
                .thenByDescending { it.fps }
                .thenByDescending { it.isAv1 }
        )

        val rawTitle = formatInfo.title ?: metadata?.title ?: "YouTube Video"
        val rawAuthor = formatInfo.author ?: metadata?.author ?: ""
        val rawDesc = formatInfo.description ?: metadata?.description ?: ""

        VideoPlaybackDetails(
            videoId = videoId,
            title = cleanTitle(rawTitle),
            author = cleanTitle(rawAuthor),
            channelId = formatInfo.channelId ?: metadata?.channelId ?: "",
            description = cleanTitle(rawDesc),
            isLive = formatInfo.isLive || formatInfo.isLiveContent || (metadata?.isLive ?: false),
            dashManifestUrl = formatInfo.dashManifestUrl,
            dashManifestContent = mpdContent,
            hlsManifestUrl = formatInfo.hlsManifestUrl,
            directStreamUrls = directFormats,
            sponsorSegments = sponsorSegments,
            viewCount = metadata?.viewCount ?: "",
            likeCount = metadata?.likeCount ?: "",
            publishedDate = metadata?.publishedDate ?: "",
            subscriberCount = metadata?.subscriberCount ?: "",
            authorImageUrl = metadata?.authorImageUrl ?: "",
            relatedVideos = suggestions.filter { it.id != videoId }.distinctBy { it.id },
            nextVideo = nextVideo
        )
    }

    private fun MediaItem.toVideoItem(): VideoItem? {
        val vId = videoId ?: return null
        val durationFormatted = formatDuration(durationMs)
        val rawTitle = title ?: "Video"
        val rawAuthor = author ?: ""
        val rawSecTitle = secondTitle?.toString() ?: ""
        return VideoItem(
            id = vId,
            title = cleanTitle(rawTitle),
            author = cleanTitle(rawAuthor),
            channelId = channelId ?: "",
            thumbnailUrl = cardImageUrl ?: "",
            durationMs = durationMs,
            durationFormatted = durationFormatted,
            badgeText = badgeText ?: "",
            secondTitle = cleanTitle(rawSecTitle),
            isLive = isLive
        )
    }

    private fun SponsorSegment.toSponsorItem(): SponsorSegmentItem {
        return SponsorSegmentItem(
            startMs = startMs,
            endMs = endMs,
            category = getCategory() ?: "sponsor",
            action = getAction() ?: "skip"
        )
    }

    companion object {
        /**
         * Cleans video titles, authors, and channel names:
         * - Decodes URL encoded strings (%20, %D8, etc.)
         * - Replaces '+' with spaces (common in YouTube raw endpoints / URL query params)
         * - Decodes HTML entities (&quot;, &amp;, &#39;, &gt;, &lt;)
         * - Normalizes spaces
         */
        fun cleanTitle(raw: String?): String {
            if (raw.isNullOrBlank()) return ""
            var result = raw.trim()

            // 1. Decode HTML entities (e.g. &quot;, &#39;, &amp;)
            try {
                result = android.text.Html.fromHtml(result, android.text.Html.FROM_HTML_MODE_LEGACY).toString()
            } catch (_: Exception) {}

            // 2. Decode URL encoding if present
            if (result.contains('%')) {
                try {
                    result = java.net.URLDecoder.decode(result, "UTF-8")
                } catch (_: Exception) {}
            }

            // 3. In URL query encoding, spaces are represented as '+'
            // If the string contains '+' where words are joined like "كلمة+كلمة"
            if (result.contains('+')) {
                result = result.replace('+', ' ')
            }

            // 4. Normalize multiple whitespaces into a single space
            return result.replace(Regex("\\s+"), " ").trim()
        }

        fun formatDuration(durationMs: Long): String {
            if (durationMs <= 0) return ""
            val totalSeconds = durationMs / 1000
            val seconds = totalSeconds % 60
            val minutes = (totalSeconds / 60) % 60
            val hours = totalSeconds / 3600
            return if (hours > 0) {
                String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.US, "%d:%02d", minutes, seconds)
            }
        }
    }
}
