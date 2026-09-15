package com.example.ui.player

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.YouTubeRepository
import com.example.model.SponsorSegmentItem
import com.example.ui.theme.AccentFocus
import com.example.ui.theme.FocusGlow
import com.example.ui.theme.NuvioCyan
import com.example.ui.theme.NuvioViolet
import com.example.ui.theme.SponsorGreen
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

/**
 * Nuvio-inspired high-precision TV Scrubber and Progress Indicator.
 *
 * Engineered with a single-canvas drawing architecture:
 * 1. Strictly forced LTR layout direction to avoid RTL coordinate inversion.
 * 2. The played track and the scrubber ball/knob are drawn together using the identical
 *    `currentX` coordinate, eliminating any chance of inverse movement or meeting in the middle.
 * 3. Smooth D-Pad remote navigation (-10s / +10s) and touch/pointer scrubbing.
 * 4. High-visibility focus glow, sponsor segment indicators, and buffered range indicator.
 */
@Composable
fun TvRemoteProgressIndicator(
    currentPositionMs: Long,
    durationMs: Long,
    bufferedPositionMs: Long = 0L,
    sponsorSegments: List<SponsorSegmentItem> = emptyList(),
    onSeekRelative: (Long) -> Unit,
    onSeekToPosition: ((Long) -> Unit)? = null,
    onTogglePlayPause: () -> Unit,
    isLive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val progressFraction = if (durationMs > 0L) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val bufferedFraction = if (durationMs > 0L) {
        (bufferedPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val trackHeight by animateDpAsState(
        targetValue = if (isFocused) 8.dp else 5.dp,
        animationSpec = tween(180),
        label = "track_height"
    )

    // Enforce strict Left-To-Right coordinate system for video progress timeline
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .testTag("tv_remote_progress_indicator")
        ) {
            // Top Timestamps and Remote navigation cues
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                // Elapsed Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = YouTubeRepository.formatDuration(currentPositionMs),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isLive) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(YouTubeRed)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Remote D-Pad Navigation Hint when focused (NuvioTV style)
                if (isFocused) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xE6131624))
                            .border(1.dp, NuvioViolet.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "◄ -10s   |   +10s ►",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Total Duration or Remaining Time
                Text(
                    text = if (isLive) "Live Stream" else YouTubeRepository.formatDuration(durationMs),
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Interactive Progress Canvas Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onTogglePlayPause
                    )
                    .focusable(interactionSource = interactionSource)
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            when (keyEvent.key) {
                                Key.DirectionLeft -> {
                                    onSeekRelative(-10000L)
                                    true
                                }
                                Key.DirectionRight -> {
                                    onSeekRelative(10000L)
                                    true
                                }
                                Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                                    onTogglePlayPause()
                                    true
                                }
                                else -> false
                            }
                        } else {
                            false
                        }
                    }
                    .pointerInput(durationMs) {
                        detectTapGestures { offset ->
                            if (durationMs > 0L) {
                                val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                val targetMs = (fraction * durationMs).toLong()
                                if (onSeekToPosition != null) {
                                    onSeekToPosition(targetMs)
                                } else {
                                    onSeekRelative(targetMs - currentPositionMs)
                                }
                            }
                        }
                    }
                    .pointerInput(durationMs) {
                        detectDragGestures { change, _ ->
                            if (durationMs > 0L) {
                                val fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                val targetMs = (fraction * durationMs).toLong()
                                if (onSeekToPosition != null) {
                                    onSeekToPosition(targetMs)
                                } else {
                                    onSeekRelative(targetMs - currentPositionMs)
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Single unified Canvas where both the track and the thumb are drawn together
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                ) {
                    val trackHeightPx = trackHeight.toPx()
                    val centerY = size.height / 2f
                    val trackCornerRadius = CornerRadius(trackHeightPx / 2f, trackHeightPx / 2f)

                    // 1. Inactive background track
                    drawRoundRect(
                        color = Color(0x38FFFFFF),
                        topLeft = Offset(0f, centerY - (trackHeightPx / 2f)),
                        size = Size(size.width, trackHeightPx),
                        cornerRadius = trackCornerRadius
                    )

                    // 2. Buffered progress
                    if (bufferedFraction > 0f) {
                        val bufferedWidth = (size.width * bufferedFraction).coerceIn(0f, size.width)
                        drawRoundRect(
                            color = Color(0x55FFFFFF),
                            topLeft = Offset(0f, centerY - (trackHeightPx / 2f)),
                            size = Size(bufferedWidth, trackHeightPx),
                            cornerRadius = trackCornerRadius
                        )
                    }

                    // 3. SponsorBlock skipped segments
                    if (durationMs > 0L) {
                        sponsorSegments.forEach { segment ->
                            val startFrac = (segment.startMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                            val endFrac = (segment.endMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                            val startX = size.width * startFrac
                            val segWidth = (size.width * (endFrac - startFrac)).coerceAtLeast(4f)

                            drawRect(
                                color = SponsorGreen.copy(alpha = 0.95f),
                                topLeft = Offset(startX, centerY - (trackHeightPx / 2f)),
                                size = Size(segWidth, trackHeightPx)
                            )
                        }
                    }

                    // 4. Played progress bar (Strictly calculated from LTR: 0 to currentX)
                    val currentX = (size.width * progressFraction).coerceIn(0f, size.width)
                    if (currentX > 0f) {
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(NuvioViolet, YouTubeRed, AccentFocus)
                            ),
                            topLeft = Offset(0f, centerY - (trackHeightPx / 2f)),
                            size = Size(currentX, trackHeightPx),
                            cornerRadius = trackCornerRadius
                        )
                    }

                    // 5. Scrubber Ball / Thumb (Drawn with the EXACT SAME currentX coordinate)
                    val glowRadius = if (isFocused) 15.dp.toPx() else 9.dp.toPx()
                    val outerRadius = if (isFocused) 9.5.dp.toPx() else 6.5.dp.toPx()
                    val innerRadius = if (isFocused) 5.5.dp.toPx() else 3.5.dp.toPx()

                    // Glowing aura
                    drawCircle(
                        color = (if (isFocused) NuvioViolet else YouTubeRed).copy(alpha = if (isFocused) 0.65f else 0.35f),
                        radius = glowRadius,
                        center = Offset(currentX, centerY)
                    )

                    // Outer crisp white ring
                    drawCircle(
                        color = Color.White,
                        radius = outerRadius,
                        center = Offset(currentX, centerY)
                    )

                    // Inner core button
                    drawCircle(
                        color = if (isFocused) NuvioViolet else YouTubeRed,
                        radius = innerRadius,
                        center = Offset(currentX, centerY)
                    )
                }
            }
        }
    }
}
