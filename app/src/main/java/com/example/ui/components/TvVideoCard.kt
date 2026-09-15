package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.model.VideoItem
import com.example.ui.theme.BadgeBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

@Composable
fun TvVideoCard(
    video: VideoItem,
    onVideoClick: (VideoItem) -> Unit,
    modifier: Modifier = Modifier,
    cardWidth: Int = 260,
    playbackProgress: Float? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1.0f,
        label = "card_scale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color(0x18FFFFFF),
        label = "card_border"
    )

    Column(
        modifier = modifier
            .width(cardWidth.dp)
            .zIndex(if (isFocused) 2f else 0f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (isFocused) 12f else 0f
            }
            .clip(RoundedCornerShape(14.dp))
            .background(if (isFocused) Color(0xFF1E2030) else Color(0xFF13141E))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onVideoClick(video) }
            )
            .focusable(interactionSource = interactionSource)
            .testTag("video_card_${video.id}")
    ) {
        // Thumbnail container with 16:9 aspect ratio
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color(0xFF14131A))
        ) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Duration badge or LIVE badge
            if (video.isLive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
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
            } else if (video.durationFormatted.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(BadgeBackground)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = video.durationFormatted,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Bottom gradient highlight for depth
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0x88000000))
                        )
                    )
            )

            // Watch history progress bar
            if (playbackProgress != null && playbackProgress > 0f) {
                val clamped = playbackProgress.coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .align(Alignment.BottomCenter)
                        .background(Color(0x66FFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(clamped)
                            .height(4.dp)
                            .background(YouTubeRed)
                    )
                }
            }
        }

        // Meta info (title, author, extra details)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = video.title,
                color = if (isFocused) Color.White else TextPrimary,
                fontSize = 13.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (video.author.isNotBlank()) {
                Text(
                    text = video.author,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (video.secondTitle.isNotBlank()) {
                Text(
                    text = video.secondTitle,
                    color = Color(0xFF888896),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
