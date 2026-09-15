package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.DeArrowSettings
import com.example.model.GeneralAppSettings
import com.example.model.InterfaceSettings
import com.example.model.PlaybackSettings
import com.example.model.SponsorSettings
import com.example.model.SubtitleSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tv_youtube_settings", Context.MODE_PRIVATE)

    private val _generalSettings = MutableStateFlow(loadGeneralSettings())
    val generalSettings: StateFlow<GeneralAppSettings> = _generalSettings.asStateFlow()

    private val _sponsorSettings = MutableStateFlow(loadSponsorSettings())
    val sponsorSettings: StateFlow<SponsorSettings> = _sponsorSettings.asStateFlow()

    private val _playbackSettings = MutableStateFlow(loadPlaybackSettings())
    val playbackSettings: StateFlow<PlaybackSettings> = _playbackSettings.asStateFlow()

    private val _subtitleSettings = MutableStateFlow(loadSubtitleSettings())
    val subtitleSettings: StateFlow<SubtitleSettings> = _subtitleSettings.asStateFlow()

    private val _dearrowSettings = MutableStateFlow(loadDeArrowSettings())
    val dearrowSettings: StateFlow<DeArrowSettings> = _dearrowSettings.asStateFlow()

    private val _interfaceSettings = MutableStateFlow(loadInterfaceSettings())
    val interfaceSettings: StateFlow<InterfaceSettings> = _interfaceSettings.asStateFlow()

    private fun loadGeneralSettings(): GeneralAppSettings {
        return GeneralAppSettings(
            appLanguage = prefs.getString("app_language", "ar") ?: "ar", // Default Arabic
            contentRegion = prefs.getString("content_region", "SA") ?: "SA",
            restrictedMode = prefs.getString("restricted_mode", "off") ?: "off",
            saveSearchHistory = prefs.getBoolean("save_search_history", true)
        )
    }

    private fun loadSponsorSettings(): SponsorSettings {
        return SponsorSettings(
            isEnabled = prefs.getBoolean("sb_enabled", true),
            skipSponsors = prefs.getBoolean("sb_sponsor", true),
            skipIntro = prefs.getBoolean("sb_intro", true),
            skipOutro = prefs.getBoolean("sb_outro", true),
            skipSelfPromo = prefs.getBoolean("sb_selfpromo", true),
            skipInteraction = prefs.getBoolean("sb_interaction", true),
            skipMusicOffTopic = prefs.getBoolean("sb_music_offtopic", false),
            showSkipToast = prefs.getBoolean("sb_show_toast", true),
            toastDurationSec = prefs.getInt("sb_toast_duration", 3)
        )
    }

    private fun loadPlaybackSettings(): PlaybackSettings {
        return PlaybackSettings(
            defaultQuality = prefs.getString("play_quality", "1080p") ?: "1080p",
            preferredSpeed = prefs.getFloat("play_speed", 1.0f),
            autoPlayNext = prefs.getBoolean("play_autonext", true),
            lowRamMode = prefs.getBoolean("low_ram_mode", false),
            deArrowEnabled = prefs.getBoolean("dearrow_enabled", true),
            preferredAudioLanguage = prefs.getString("play_audio_lang", "Original") ?: "Original",
            hardwareDecoding = prefs.getBoolean("play_hw_decoding", true),
            bufferProfile = prefs.getString("play_buffer_profile", "Balanced") ?: "Balanced"
        )
    }

    private fun loadSubtitleSettings(): SubtitleSettings {
        return SubtitleSettings(
            subtitlesEnabled = prefs.getBoolean("sub_enabled", false),
            preferredLanguage = prefs.getString("sub_language", "ar") ?: "ar",
            fontSize = prefs.getString("sub_font_size", "Normal") ?: "Normal",
            backgroundOpacity = prefs.getString("sub_bg_opacity", "Semi-Dark") ?: "Semi-Dark"
        )
    }

    private fun loadDeArrowSettings(): DeArrowSettings {
        return DeArrowSettings(
            deArrowEnabled = prefs.getBoolean("dearrow_titles", true),
            replaceThumbnails = prefs.getBoolean("dearrow_thumbs", true)
        )
    }

    private fun loadInterfaceSettings(): InterfaceSettings {
        return InterfaceSettings(
            cardSize = prefs.getString("ui_card_size", "Standard") ?: "Standard",
            fastSeekSeconds = prefs.getInt("ui_fast_seek", 10),
            showClock = prefs.getBoolean("ui_show_clock", true),
            lowRamMode = prefs.getBoolean("low_ram_mode", false),
            sleepTimerMinutes = prefs.getInt("ui_sleep_timer", 0)
        )
    }

    fun updateGeneralSettings(settings: GeneralAppSettings) {
        prefs.edit().apply {
            putString("app_language", settings.appLanguage)
            putString("content_region", settings.contentRegion)
            putString("restricted_mode", settings.restrictedMode)
            putBoolean("save_search_history", settings.saveSearchHistory)
            apply()
        }
        _generalSettings.value = settings
    }

    fun updateSponsorSettings(settings: SponsorSettings) {
        prefs.edit().apply {
            putBoolean("sb_enabled", settings.isEnabled)
            putBoolean("sb_sponsor", settings.skipSponsors)
            putBoolean("sb_intro", settings.skipIntro)
            putBoolean("sb_outro", settings.skipOutro)
            putBoolean("sb_selfpromo", settings.skipSelfPromo)
            putBoolean("sb_interaction", settings.skipInteraction)
            putBoolean("sb_music_offtopic", settings.skipMusicOffTopic)
            putBoolean("sb_show_toast", settings.showSkipToast)
            putInt("sb_toast_duration", settings.toastDurationSec)
            apply()
        }
        _sponsorSettings.value = settings
    }

    fun updatePlaybackSettings(settings: PlaybackSettings) {
        prefs.edit().apply {
            putString("play_quality", settings.defaultQuality)
            putFloat("play_speed", settings.preferredSpeed)
            putBoolean("play_autonext", settings.autoPlayNext)
            putBoolean("low_ram_mode", settings.lowRamMode)
            putBoolean("dearrow_enabled", settings.deArrowEnabled)
            putString("play_audio_lang", settings.preferredAudioLanguage)
            putBoolean("play_hw_decoding", settings.hardwareDecoding)
            putString("play_buffer_profile", settings.bufferProfile)
            apply()
        }
        _playbackSettings.value = settings
    }

    fun updateSubtitleSettings(settings: SubtitleSettings) {
        prefs.edit().apply {
            putBoolean("sub_enabled", settings.subtitlesEnabled)
            putString("sub_language", settings.preferredLanguage)
            putString("sub_font_size", settings.fontSize)
            putString("sub_bg_opacity", settings.backgroundOpacity)
            apply()
        }
        _subtitleSettings.value = settings
    }

    fun updateDeArrowSettings(settings: DeArrowSettings) {
        prefs.edit().apply {
            putBoolean("dearrow_titles", settings.deArrowEnabled)
            putBoolean("dearrow_thumbs", settings.replaceThumbnails)
            apply()
        }
        _dearrowSettings.value = settings
    }

    fun updateInterfaceSettings(settings: InterfaceSettings) {
        prefs.edit().apply {
            putString("ui_card_size", settings.cardSize)
            putInt("ui_fast_seek", settings.fastSeekSeconds)
            putBoolean("ui_show_clock", settings.showClock)
            putBoolean("low_ram_mode", settings.lowRamMode)
            putInt("ui_sleep_timer", settings.sleepTimerMinutes)
            apply()
        }
        _interfaceSettings.value = settings
    }

    fun clearCache() {
        try {
            context.cacheDir.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearSearchHistory() {
        prefs.edit().remove("search_history_items").apply()
    }

    fun resetAllSettings() {
        prefs.edit().clear().apply()
        _generalSettings.value = loadGeneralSettings()
        _sponsorSettings.value = loadSponsorSettings()
        _playbackSettings.value = loadPlaybackSettings()
        _subtitleSettings.value = loadSubtitleSettings()
        _dearrowSettings.value = loadDeArrowSettings()
        _interfaceSettings.value = loadInterfaceSettings()
    }
}
