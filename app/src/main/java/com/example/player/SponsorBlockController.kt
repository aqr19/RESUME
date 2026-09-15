package com.example.player

import com.example.model.SponsorSegmentItem
import com.example.model.SponsorSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SponsorBlockController(
    private val getSettings: () -> SponsorSettings
) {
    data class SkipEvent(
        val category: String,
        val durationSeconds: Long,
        val timestamp: Long = System.currentTimeMillis()
    )

    private var segments: List<SponsorSegmentItem> = emptyList()
    val currentSegments: List<SponsorSegmentItem> get() = segments
    private var lastSkippedSegment: SponsorSegmentItem? = null

    private val _skipEvent = MutableStateFlow<SkipEvent?>(null)
    val skipEvent: StateFlow<SkipEvent?> = _skipEvent.asStateFlow()

    fun setSegments(newSegments: List<SponsorSegmentItem>) {
        segments = newSegments
        lastSkippedSegment = null
        _skipEvent.value = null
    }

    fun loadSegments(newSegments: List<SponsorSegmentItem>) {
        setSegments(newSegments)
    }

    /**
     * Checks if current position falls into any skip segment.
     * Returns the target seek position in ms if skipping should occur, or null if no skip.
     */
    fun checkSkip(currentPositionMs: Long): Long? {
        val settings = getSettings()
        if (!settings.isEnabled || segments.isEmpty()) {
            return null
        }

        for (segment in segments) {
            // Buffer of 500ms to avoid skipping at the very edge of the end
            if (currentPositionMs >= segment.startMs && currentPositionMs < (segment.endMs - 300)) {
                if (segment == lastSkippedSegment) {
                    continue
                }

                if (isCategoryEnabled(segment.category, settings)) {
                    lastSkippedSegment = segment
                    val durationSec = ((segment.endMs - segment.startMs) / 1000).coerceAtLeast(1)
                    _skipEvent.value = SkipEvent(
                        category = segment.category,
                        durationSeconds = durationSec
                    )
                    return segment.endMs
                }
            }
        }
        return null
    }

    private fun isCategoryEnabled(category: String, settings: SponsorSettings): Boolean {
        return when (category.lowercase()) {
            "sponsor" -> settings.skipSponsors
            "intro" -> settings.skipIntro
            "outro" -> settings.skipOutro
            "selfpromo" -> settings.skipSelfPromo
            "interaction" -> settings.skipInteraction
            "music_offtopic" -> settings.skipMusicOffTopic
            else -> true
        }
    }

    fun clearEvent() {
        _skipEvent.value = null
    }
}
