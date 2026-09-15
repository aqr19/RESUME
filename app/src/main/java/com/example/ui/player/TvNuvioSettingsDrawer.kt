package com.example.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.VideoItem
import com.example.model.VideoStreamFormat
import kotlinx.coroutines.delay
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NuvioCyan
import com.example.ui.theme.NuvioIndigo
import com.example.ui.theme.NuvioViolet
import com.example.ui.theme.SponsorGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

/**
 * Aspect Ratio display modes modeled after NuvioTV TV app.
 */
enum class TvAspectRatioOption(val titleRes: Int, val description: String) {
    FIT(R.string.aspect_fit, "16:9 Original format with letterbox"),
    ZOOM(R.string.aspect_zoom, "Nuvio immersive full screen (no black bars)"),
    FILL(R.string.aspect_fill, "Stretch video to display borders"),
    FOUR_THREE(R.string.aspect_4_3, "Classic 4:3 box ratio")
}

/**
 * Settings tab categories in the NuvioTV modal drawer.
 */
enum class NuvioPlayerSettingsTab(val labelRes: Int, val icon: ImageVector) {
    QUALITY(R.string.player_tab_quality, Icons.Default.HighQuality),
    ASPECT_RATIO(R.string.player_tab_aspect, Icons.Default.AspectRatio),
    SPEED(R.string.player_tab_speed, Icons.Default.Speed),
    SUBTITLES(R.string.player_tab_subtitles, Icons.Default.Subtitles),
    AUDIO(R.string.player_tab_audio, Icons.Default.VolumeUp),
    SLEEP_TIMER(R.string.player_tab_sleep, Icons.Default.Timer),
    SPONSORBLOCK(R.string.player_tab_sponsor, Icons.Default.Security),
    STATS(R.string.player_tab_stats, Icons.Default.Info)
}

/**
 * NuvioTV-style comprehensive player settings drawer.
 * Slides in from the side as a sleek frosted glass panel with remote D-Pad navigation.
 */
@Composable
fun TvNuvioSettingsDrawer(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    // Quality
    availableFormats: List<VideoStreamFormat>,
    selectedFormatUrl: String?,
    onSelectFormat: (VideoStreamFormat?) -> Unit,
    // Aspect Ratio
    currentAspectRatio: TvAspectRatioOption,
    onSelectAspectRatio: (TvAspectRatioOption) -> Unit,
    // Speed
    currentSpeed: Float,
    onSelectSpeed: (Float) -> Unit,
    // Subtitles
    isSubtitlesEnabled: Boolean,
    onToggleSubtitles: (Boolean) -> Unit,
    subtitleLanguage: String,
    onSelectSubtitleLanguage: (String) -> Unit,
    // Audio boost
    audioBoostLevel: Float, // 1.0f, 1.25f, 1.5f, 2.0f
    onSelectAudioBoost: (Float) -> Unit,
    // Sleep Timer
    sleepTimerMinutes: Int, // 0 = off, 15, 30, 45, 60
    onSelectSleepTimer: (Int) -> Unit,
    // SponsorBlock
    isSponsorBlockEnabled: Boolean,
    onToggleSponsorBlock: (Boolean) -> Unit,
    // Video info & stats
    video: VideoItem,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    var activeTab by remember { mutableStateOf(NuvioPlayerSettingsTab.QUALITY) }
    val initialTabFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isOpen) {
        if (isOpen) {
            delay(120)
            try {
                initialTabFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x80000000))
            .clickable(onClick = onDismiss)
            .onKeyEvent {
                if (it.type == KeyEventType.KeyDown && (it.key == Key.Back || it.key == Key.Escape)) {
                    onDismiss()
                    true
                } else false
            }
            .testTag("nuvio_settings_scrim"),
        contentAlignment = Alignment.CenterEnd
    ) {
        // Nuvio Frosted Side Drawer
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .width(620.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xF00F111B),
                            Color(0xF80B0D15)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            NuvioViolet.copy(alpha = 0.6f),
                            Color(0x22FFFFFF),
                            NuvioIndigo.copy(alpha = 0.4f)
                        )
                    ),
                    shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                )
                .clickable(enabled = false) {} // Consume clicks
                .padding(20.dp)
                .testTag("nuvio_settings_drawer")
        ) {
            // Left Column: Tab Category Navigation
            Column(
                modifier = Modifier
                    .width(180.dp)
                    .fillMaxHeight()
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(NuvioViolet)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.player_settings_title),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(NuvioPlayerSettingsTab.values()) { tab ->
                        val isSelected = activeTab == tab
                        val interactionSource = remember { MutableInteractionSource() }
                        val isFocused by interactionSource.collectIsFocusedAsState()

                        LaunchedEffect(isFocused) {
                            if (isFocused) {
                                activeTab = tab
                            }
                        }

                        val tabModifier = if (tab == NuvioPlayerSettingsTab.QUALITY) {
                            Modifier.focusRequester(initialTabFocusRequester)
                        } else Modifier

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = tabModifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isFocused -> Color.White
                                        isSelected -> Color(0x337C4DFF)
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = if (isFocused) 2.5.dp else if (isSelected) 1.dp else 0.dp,
                                    color = when {
                                        isFocused -> NuvioCyan
                                        isSelected -> NuvioViolet.copy(alpha = 0.6f)
                                        else -> Color.Transparent
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = { activeTab = tab }
                                )
                                .focusable(interactionSource = interactionSource)
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("nuvio_tab_${tab.name}")
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = when {
                                    isFocused -> Color.Black
                                    isSelected -> NuvioCyan
                                    else -> TextSecondary
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(tab.labelRes),
                                color = when {
                                    isFocused -> Color.Black
                                    isSelected -> Color.White
                                    else -> TextSecondary
                                },
                                fontSize = 13.sp,
                                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Close Button at bottom of tab list
                NuvioCloseButton(onClick = onDismiss)
            }

            // Divider line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(Color(0x22FFFFFF))
                    .padding(vertical = 12.dp)
            )

            // Right Column: Options for Selected Tab
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 18.dp)
            ) {
                when (activeTab) {
                    NuvioPlayerSettingsTab.QUALITY -> {
                        NuvioQualitySettingsPane(
                            availableFormats = availableFormats,
                            selectedFormatUrl = selectedFormatUrl,
                            onSelectFormat = onSelectFormat
                        )
                    }
                    NuvioPlayerSettingsTab.ASPECT_RATIO -> {
                        NuvioAspectRatioSettingsPane(
                            currentAspectRatio = currentAspectRatio,
                            onSelectAspectRatio = onSelectAspectRatio
                        )
                    }
                    NuvioPlayerSettingsTab.SPEED -> {
                        NuvioSpeedSettingsPane(
                            currentSpeed = currentSpeed,
                            onSelectSpeed = onSelectSpeed
                        )
                    }
                    NuvioPlayerSettingsTab.SUBTITLES -> {
                        NuvioSubtitleSettingsPane(
                            isSubtitlesEnabled = isSubtitlesEnabled,
                            onToggleSubtitles = onToggleSubtitles,
                            subtitleLanguage = subtitleLanguage,
                            onSelectSubtitleLanguage = onSelectSubtitleLanguage
                        )
                    }
                    NuvioPlayerSettingsTab.AUDIO -> {
                        NuvioAudioSettingsPane(
                            audioBoostLevel = audioBoostLevel,
                            onSelectAudioBoost = onSelectAudioBoost
                        )
                    }
                    NuvioPlayerSettingsTab.SLEEP_TIMER -> {
                        NuvioSleepTimerPane(
                            sleepTimerMinutes = sleepTimerMinutes,
                            onSelectSleepTimer = onSelectSleepTimer
                        )
                    }
                    NuvioPlayerSettingsTab.SPONSORBLOCK -> {
                        NuvioSponsorBlockPane(
                            isSponsorBlockEnabled = isSponsorBlockEnabled,
                            onToggleSponsorBlock = onToggleSponsorBlock
                        )
                    }
                    NuvioPlayerSettingsTab.STATS -> {
                        NuvioStatsPane(
                            video = video,
                            availableFormats = availableFormats,
                            selectedFormatUrl = selectedFormatUrl
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PANES FOR EACH TAB
// -------------------------------------------------------------

@Composable
private fun NuvioQualitySettingsPane(
    availableFormats: List<VideoStreamFormat>,
    selectedFormatUrl: String?,
    onSelectFormat: (VideoStreamFormat?) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.player_quality_select),
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select video streaming bitrate and resolution",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                NuvioOptionItem(
                    title = stringResource(R.string.player_auto_quality),
                    subtitle = "Adaptive HLS / DASH stream",
                    isSelected = selectedFormatUrl == null,
                    badge = "AUTO",
                    onClick = { onSelectFormat(null) }
                )
            }

            items(availableFormats) { format ->
                val label = format.qualityLabel ?: "${format.height}p"
                val resDesc = if (format.width > 0 && format.height > 0) "${format.width}x${format.height}" else ""
                val fpsDesc = if (format.fps > 0) "${format.fps}fps" else ""
                val fullDesc = listOf(resDesc, fpsDesc, format.mimeType?.substringAfter("/")).filter { !it.isNullOrBlank() }.joinToString(" • ")

                NuvioOptionItem(
                    title = label,
                    subtitle = fullDesc,
                    isSelected = selectedFormatUrl == format.url,
                    badge = if (format.height >= 1080) "HD" else null,
                    onClick = { onSelectFormat(format) }
                )
            }
        }
    }
}

@Composable
private fun NuvioAspectRatioSettingsPane(
    currentAspectRatio: TvAspectRatioOption,
    onSelectAspectRatio: (TvAspectRatioOption) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.player_tab_aspect),
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Adjust frame geometry to fit your TV screen",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(TvAspectRatioOption.values()) { option ->
                NuvioOptionItem(
                    title = stringResource(option.titleRes),
                    subtitle = option.description,
                    isSelected = currentAspectRatio == option,
                    badge = if (option == TvAspectRatioOption.ZOOM) "NUVIO" else null,
                    onClick = { onSelectAspectRatio(option) }
                )
            }
        }
    }
}

@Composable
private fun NuvioSpeedSettingsPane(
    currentSpeed: Float,
    onSelectSpeed: (Float) -> Unit
) {
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
    Column {
        Text(
            text = stringResource(R.string.player_speed_select),
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Adjust playback rate without pitch distortion",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(speeds) { speed ->
                val isNormal = speed == 1.0f
                NuvioOptionItem(
                    title = "${speed}x",
                    subtitle = if (isNormal) stringResource(R.string.player_speed_normal) else null,
                    isSelected = currentSpeed == speed,
                    badge = if (isNormal) "DEFAULT" else null,
                    onClick = { onSelectSpeed(speed) }
                )
            }
        }
    }
}

@Composable
private fun NuvioSubtitleSettingsPane(
    isSubtitlesEnabled: Boolean,
    onToggleSubtitles: (Boolean) -> Unit,
    subtitleLanguage: String,
    onSelectSubtitleLanguage: (String) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.player_tab_subtitles),
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Manage closed captions and subtitle track",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        // Toggle Switch Item
        NuvioToggleItem(
            title = stringResource(R.string.sub_enable),
            subtitle = stringResource(R.string.sub_enable_desc),
            isChecked = isSubtitlesEnabled,
            onCheckedChange = onToggleSubtitles
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (isSubtitlesEnabled) {
            Text(
                text = stringResource(R.string.sub_language),
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val languages = listOf("ar" to "العربية (Arabic)", "en" to "English", "auto" to "Auto-generated")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(languages) { (code, name) ->
                    NuvioOptionItem(
                        title = name,
                        subtitle = code.uppercase(),
                        isSelected = subtitleLanguage == code,
                        onClick = { onSelectSubtitleLanguage(code) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NuvioAudioSettingsPane(
    audioBoostLevel: Float,
    onSelectAudioBoost: (Float) -> Unit
) {
    val boostOptions = listOf(
        1.0f to R.string.audio_boost_normal,
        1.25f to R.string.audio_boost_medium,
        1.5f to R.string.audio_boost_high,
        2.0f to R.string.audio_boost_max
    )

    Column {
        Text(
            text = stringResource(R.string.player_tab_audio),
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Nuvio audio amplifier & volume leveling for TV speakers",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(boostOptions) { (factor, labelRes) ->
                NuvioOptionItem(
                    title = stringResource(labelRes),
                    subtitle = "ExoPlayer Master Gain ${(factor * 100).toInt()}%",
                    isSelected = audioBoostLevel == factor,
                    badge = if (factor > 1.0f) "BOOST" else null,
                    onClick = { onSelectAudioBoost(factor) }
                )
            }
        }
    }
}

@Composable
private fun NuvioSleepTimerPane(
    sleepTimerMinutes: Int,
    onSelectSleepTimer: (Int) -> Unit
) {
    val timerOptions = listOf(
        0 to R.string.sleep_timer_off,
        15 to R.string.sleep_timer_15,
        30 to R.string.sleep_timer_30,
        45 to R.string.sleep_timer_45,
        60 to R.string.sleep_timer_60,
        -1 to R.string.sleep_timer_end
    )

    Column {
        Text(
            text = stringResource(R.string.player_tab_sleep),
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Automatically pause and dim screen when idle",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(timerOptions) { (minutes, labelRes) ->
                NuvioOptionItem(
                    title = stringResource(labelRes),
                    subtitle = if (minutes > 0) "Pauses playback in $minutes minutes" else null,
                    isSelected = sleepTimerMinutes == minutes,
                    onClick = { onSelectSleepTimer(minutes) }
                )
            }
        }
    }
}

@Composable
private fun NuvioSponsorBlockPane(
    isSponsorBlockEnabled: Boolean,
    onToggleSponsorBlock: (Boolean) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.player_tab_sponsor),
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Community-driven skip of sponsors, intros, and self-promotion",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        NuvioToggleItem(
            title = stringResource(R.string.sb_enable_title),
            subtitle = stringResource(R.string.sb_enable_desc),
            isChecked = isSponsorBlockEnabled,
            onCheckedChange = onToggleSponsorBlock
        )
    }
}

@Composable
private fun NuvioStatsPane(
    video: VideoItem,
    availableFormats: List<VideoStreamFormat>,
    selectedFormatUrl: String?
) {
    val currentFormat = availableFormats.firstOrNull { it.url == selectedFormatUrl } ?: availableFormats.firstOrNull()

    Column {
        Text(
            text = "Stats for Nerds",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Real-time stream and hardware decoder diagnostics",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item { NuvioStatRow("Video ID", video.id) }
            item { NuvioStatRow("Resolution", currentFormat?.let { "${it.width}x${it.height}" } ?: "1920x1080 (Adaptive)") }
            item { NuvioStatRow("Frame Rate", currentFormat?.let { "${it.fps} fps" } ?: "60 fps") }
            item { NuvioStatRow("Video Codec", currentFormat?.mimeType ?: "video/mp4; codecs=avc1.640028") }
            item { NuvioStatRow("Audio Codec", "audio/mp4; codecs=mp4a.40.2 (Stereo)") }
            item { NuvioStatRow("Player Engine", "Media3 ExoPlayer 1.4+ (HW Accelerated)") }
            item { NuvioStatRow("Live Stream", if (video.isLive) "Yes (Low Latency)" else "No (VOD)") }
        }
    }
}

// -------------------------------------------------------------
// SHARED REUSABLE COMPONENTS
// -------------------------------------------------------------

@Composable
private fun NuvioOptionItem(
    title: String,
    subtitle: String? = null,
    isSelected: Boolean,
    badge: String? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isFocused -> Color.White
                    isSelected -> Color(0x337C4DFF)
                    else -> Color(0x22181A2A)
                }
            )
            .border(
                width = if (isFocused) 2.dp else if (isSelected) 1.5.dp else 1.dp,
                color = when {
                    isFocused -> NuvioViolet
                    isSelected -> NuvioViolet.copy(alpha = 0.8f)
                    else -> Color(0x18FFFFFF)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = if (isFocused) Color.Black else Color.White,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
                )
                if (badge != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isFocused) NuvioViolet else Color(0x447C4DFF))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = if (isFocused) Color(0xFF444455) else TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = if (isFocused) NuvioViolet else NuvioCyan,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun NuvioToggleItem(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) Color.White else Color(0x22181A2A))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) NuvioViolet else Color(0x18FFFFFF),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCheckedChange(!isChecked) }
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isFocused) Color.Black else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = if (isFocused) Color(0xFF444455) else TextSecondary,
                fontSize = 11.sp
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NuvioViolet,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = Color(0x44FFFFFF)
            )
        )
    }
}

@Composable
private fun NuvioStatRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x18FFFFFF))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun NuvioCloseButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) Color.White else Color(0x22FFFFFF))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) YouTubeRed else Color(0x22FFFFFF),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .padding(vertical = 10.dp)
            .testTag("nuvio_settings_close")
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = if (isFocused) Color.Black else Color.White,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.player_close),
            color = if (isFocused) Color.Black else Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
