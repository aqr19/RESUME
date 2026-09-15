package com.example.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.YouTubeRepository
import com.example.model.SponsorSegmentItem
import com.example.model.VideoItem
import com.example.ui.theme.AccentFocus
import com.example.ui.theme.NuvioBorderSubtle
import com.example.ui.theme.NuvioCyan
import com.example.ui.theme.NuvioViolet
import com.example.ui.theme.SponsorGreen
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

/**
 * Centered HUD indicator providing visual feedback when seeking with the remote control.
 */
@Composable
fun TvSeekFeedbackHud(
    seekDeltaText: String?,
    currentPositionMs: Long,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = seekDeltaText != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { -it / 2 },
        modifier = modifier
    ) {
        if (seekDeltaText != null) {
            val isForward = seekDeltaText.startsWith("+")
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xF00F111B))
                    .border(1.5.dp, NuvioViolet.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp, vertical = 14.dp)
                    .testTag("tv_seek_feedback_hud"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isForward) Icons.Default.FastForward else Icons.Default.FastRewind,
                        contentDescription = "Seek Direction",
                        tint = NuvioCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = seekDeltaText,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = YouTubeRepository.formatDuration(currentPositionMs),
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Nuvio-styled auto-hiding transport overlay for the Media3 Player.
 */
@Composable
fun TvPlayerTransportOverlay(
    video: VideoItem,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    bufferedPositionMs: Long,
    playbackSpeed: Float,
    sponsorSegments: List<SponsorSegmentItem>,
    isSponsorBlockActive: Boolean,
    streamQualityLabel: String?,
    currentAspectRatio: TvAspectRatioOption = TvAspectRatioOption.FIT,
    hasNextVideo: Boolean = false,
    relatedVideos: List<VideoItem> = emptyList(),
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    playButtonFocusRequester: FocusRequester,
    onBack: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekRelative: (Long) -> Unit,
    onSeekToPosition: ((Long) -> Unit)? = null,
    onCycleSpeed: () -> Unit,
    onOpenSpeedDialog: () -> Unit = onCycleSpeed,
    onOpenQualityDialog: () -> Unit = {},
    onCycleAspectRatio: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenInfoDialog: () -> Unit = {},
    onPlayNextVideo: () -> Unit = {},
    onSelectRelatedVideo: (VideoItem) -> Unit = {},
    onResetHideTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xE6080A10),
                        Color(0x30000000),
                        Color.Transparent,
                        Color(0xB3080A10),
                        Color(0xF506070B)
                    )
                )
            )
            .padding(horizontal = 36.dp, vertical = 24.dp)
            .testTag("tv_player_transport_overlay")
    ) {
        // TOP HEADER: Back button, Video Title, Author, Info/Stats & Favorite Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
        ) {
            TvPlayerIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.player_go_back),
                onClick = onBack,
                testTag = "player_back_button"
            )

            Spacer(modifier = Modifier.width(18.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (video.author.isNotBlank()) {
                    Text(
                        text = video.author,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Video Details / Stats Dialog Button
            TvPlayerIconButton(
                icon = Icons.Default.Info,
                contentDescription = stringResource(R.string.player_video_details),
                onClick = {
                    onOpenInfoDialog()
                    onResetHideTimer()
                },
                testTag = "player_info_button"
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Bookmark / Favorite Button
            TvPlayerIconButton(
                icon = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = stringResource(if (isFavorite) R.string.player_remove_favorite else R.string.player_save_favorite),
                onClick = {
                    onToggleFavorite()
                    onResetHideTimer()
                },
                testTag = "player_favorite_button"
            )

            // SponsorBlock indicator in header (Keeps bottom dock strictly centered)
            if (isSponsorBlockActive) {
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x3310B981))
                        .border(1.dp, SponsorGreen, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "SponsorBlock Active",
                            tint = SponsorGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SponsorBlock",
                            color = SponsorGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // BOTTOM TRANSPORT BAR: Progress indicator, remote action buttons & Related Videos
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            // Dedicated Remote Progress Indicator (Unified LTR canvas, zero coordinate inversion)
            TvRemoteProgressIndicator(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                bufferedPositionMs = bufferedPositionMs,
                sponsorSegments = sponsorSegments,
                onSeekRelative = { delta ->
                    onSeekRelative(delta)
                    onResetHideTimer()
                },
                onSeekToPosition = { pos ->
                    if (onSeekToPosition != null) {
                        onSeekToPosition(pos)
                    } else {
                        onSeekRelative(pos - currentPositionMs)
                    }
                    onResetHideTimer()
                },
                onTogglePlayPause = {
                    onTogglePlayPause()
                    onResetHideTimer()
                },
                isLive = video.isLive
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Transport Control Buttons Row (NuvioTV Floating Dock - Centered and Symmetrical)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xDD0D0F1B))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(32.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    // 1. Full Nuvio Settings Drawer Button
                    TvPlayerIconButton(
                        icon = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.player_settings_title),
                        onClick = {
                            onOpenSettings()
                            onResetHideTimer()
                        },
                        testTag = "player_bottom_settings_button"
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // 2. Video Quality Selector Button (HD / 1080p / Auto)
                    TvPlayerTextButton(
                        text = streamQualityLabel ?: "Auto",
                        icon = Icons.Default.HighQuality,
                        onClick = {
                            onOpenQualityDialog()
                            onResetHideTimer()
                        },
                        testTag = "player_quality_button"
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // 3. Rewind 10 Seconds
                    TvPlayerIconButton(
                        icon = Icons.Default.Replay10,
                        contentDescription = stringResource(R.string.player_rewind),
                        onClick = {
                            onSeekRelative(-10000L)
                            onResetHideTimer()
                        },
                        testTag = "player_rewind_button"
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // 4. Play / Pause Primary Hero Button (Dead-center heart of player)
                    TvPlayerPlayPauseButton(
                        isPlaying = isPlaying,
                        onClick = {
                            onTogglePlayPause()
                            onResetHideTimer()
                        },
                        modifier = Modifier.focusRequester(playButtonFocusRequester)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // 5. Fast Forward 10 Seconds
                    TvPlayerIconButton(
                        icon = Icons.Default.Forward10,
                        contentDescription = stringResource(R.string.player_forward),
                        onClick = {
                            onSeekRelative(10000L)
                            onResetHideTimer()
                        },
                        testTag = "player_forward_button"
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // 6. Playback Speed Selector Pill
                    TvPlayerTextButton(
                        text = "${playbackSpeed}x",
                        icon = Icons.Default.Speed,
                        onClick = {
                            onOpenSpeedDialog()
                            onResetHideTimer()
                        },
                        testTag = "player_speed_button"
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // 7. Next Video (or Aspect Ratio Toggle)
                    if (hasNextVideo) {
                        TvPlayerIconButton(
                            icon = Icons.Default.SkipNext,
                            contentDescription = stringResource(R.string.player_play_next),
                            onClick = {
                                onPlayNextVideo()
                                onResetHideTimer()
                            },
                            testTag = "player_next_button"
                        )
                    } else {
                        TvPlayerIconButton(
                            icon = Icons.Default.AspectRatio,
                            contentDescription = stringResource(currentAspectRatio.titleRes),
                            onClick = {
                                onCycleAspectRatio()
                                onResetHideTimer()
                            },
                            testTag = "player_aspect_button"
                        )
                    }
                }
            }

            // In-Player Related Videos Horizontal Carousel
            if (relatedVideos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                TvRelatedVideosRow(
                    videos = relatedVideos,
                    onSelectVideo = { item ->
                        onSelectRelatedVideo(item)
                        onResetHideTimer()
                    }
                )
            }
        }
    }
}

/**
 * Standard remote-focusable circular icon button with NuvioTV glow (48dp target).
 */
@Composable
fun TvPlayerIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "player_icon_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isFocused) Color.White else Color(0x66161928))
            .border(
                width = if (isFocused) 2.5.dp else 1.dp,
                color = if (isFocused) NuvioCyan else Color(0x33FFFFFF),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isFocused) Color.Black else Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Primary Play/Pause action button designed with NuvioTV prominent glowing focus ring.
 */
@Composable
fun TvPlayerPlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(62.dp)
            .clip(CircleShape)
            .background(
                when {
                    isFocused -> Color.White
                    isPlaying -> YouTubeRed
                    else -> NuvioViolet
                }
            )
            .border(
                width = if (isFocused) 3.dp else 1.dp,
                color = if (isFocused) NuvioCyan else Color(0x55FFFFFF),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .testTag("player_play_pause_button")
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) stringResource(R.string.player_pause) else stringResource(R.string.player_play),
            tint = if (isFocused) Color.Black else Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}

/**
 * Text pill button for secondary actions such as playback speed and aspect ratio (48dp target).
 */
@Composable
fun TvPlayerTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String = "player_text_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(23.dp))
            .background(if (isFocused) Color.White else Color(0x66161928))
            .border(
                width = if (isFocused) 2.5.dp else 1.dp,
                color = if (isFocused) NuvioCyan else Color(0x33FFFFFF),
                shape = RoundedCornerShape(23.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 16.dp)
            .testTag(testTag)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) Color.Black else NuvioCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = if (isFocused) Color.Black else Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Standard rectangular action button.
 */
@Composable
fun TvPlayerButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "player_action_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isFocused) Color.White else YouTubeRed)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) NuvioViolet else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = if (isFocused) Color.Black else Color.White,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = if (isFocused) Color.Black else Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
