package com.example.model

data class VideoItem(
    val id: String,
    val title: String,
    val author: String = "",
    val channelId: String = "",
    val thumbnailUrl: String = "",
    val durationMs: Long = 0L,
    val durationFormatted: String = "",
    val badgeText: String = "",
    val secondTitle: String = "",
    val isLive: Boolean = false
)

data class VideoPlaybackDetails(
    val videoId: String,
    val title: String,
    val author: String,
    val channelId: String,
    val description: String,
    val isLive: Boolean,
    val dashManifestUrl: String? = null,
    val dashManifestContent: String? = null,
    val hlsManifestUrl: String? = null,
    val directStreamUrls: List<VideoStreamFormat> = emptyList(),
    val sponsorSegments: List<SponsorSegmentItem> = emptyList(),
    val viewCount: String = "",
    val likeCount: String = "",
    val publishedDate: String = "",
    val subscriberCount: String = "",
    val authorImageUrl: String = "",
    val relatedVideos: List<VideoItem> = emptyList(),
    val nextVideo: VideoItem? = null
)

data class VideoStreamFormat(
    val url: String,
    val mimeType: String?,
    val qualityLabel: String?,
    val width: Int,
    val height: Int,
    val fps: Int = 0
) {
    val isAv1: Boolean get() = mimeType?.contains("av01") == true
}

data class SponsorSegmentItem(
    val startMs: Long,
    val endMs: Long,
    val category: String,
    val action: String
)

data class GeneralAppSettings(
    val appLanguage: String = "ar", // "ar" or "en"
    val contentRegion: String = "SA", // "SA", "EG", "AE", "IQ", "MA", "US", "GLOBAL"
    val restrictedMode: String = "off", // "off", "moderate", "strict"
    val saveSearchHistory: Boolean = true
)

data class SponsorSettings(
    val isEnabled: Boolean = true,
    val skipSponsors: Boolean = true,
    val skipIntro: Boolean = true,
    val skipOutro: Boolean = true,
    val skipSelfPromo: Boolean = true,
    val skipInteraction: Boolean = true,
    val skipMusicOffTopic: Boolean = false,
    val showSkipToast: Boolean = true,
    val toastDurationSec: Int = 3
)

data class PlaybackSettings(
    val defaultQuality: String = "1080p", // "Auto", "2160p (4K)", "1440p", "1080p", "720p", "480p", "360p"
    val preferredSpeed: Float = 1.0f,
    val autoPlayNext: Boolean = true,
    val lowRamMode: Boolean = false,
    val deArrowEnabled: Boolean = true,
    val preferredAudioLanguage: String = "Original", // "Original", "Arabic", "English"
    val hardwareDecoding: Boolean = true,
    val bufferProfile: String = "Balanced" // "Low Latency", "Balanced", "High Buffer"
)

data class SubtitleSettings(
    val subtitlesEnabled: Boolean = false,
    val preferredLanguage: String = "ar", // "ar", "en", "auto"
    val fontSize: String = "Normal", // "Small", "Normal", "Large", "Extra Large"
    val backgroundOpacity: String = "Semi-Dark" // "Transparent", "Semi-Dark", "Solid Black"
)

data class DeArrowSettings(
    val deArrowEnabled: Boolean = true,
    val replaceThumbnails: Boolean = true
)

data class InterfaceSettings(
    val cardSize: String = "Standard", // "Compact", "Standard", "Large"
    val fastSeekSeconds: Int = 10, // 5, 10, 15, 30
    val showClock: Boolean = true,
    val lowRamMode: Boolean = false,
    val sleepTimerMinutes: Int = 0 // 0 = off, 15, 30, 60, 120
)
