package com.example.ui.player

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.VideoPlaybackDetails
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

/**
 * TV Modal Dialog displaying comprehensive video information, stats, and full description.
 */
@Composable
fun TvVideoInfoDialog(
    isOpen: Boolean,
    details: VideoPlaybackDetails?,
    onDismiss: () -> Unit,
    isSubscribed: Boolean = false,
    onToggleSubscribe: (() -> Unit)? = null,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (!isOpen || details == null) return

    val closeInteractionSource = remember { MutableInteractionSource() }
    val isCloseFocused by closeInteractionSource.collectIsFocusedAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .clickable(onClick = onDismiss)
            .testTag("tv_video_info_scrim"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(620.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF161622))
                .border(2.dp, Color(0x40FFFFFF), RoundedCornerShape(20.dp))
                .clickable(enabled = false) {}
                .padding(28.dp)
                .testTag("tv_video_info_dialog")
        ) {
            Column {
                // Top Title & Close
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = YouTubeRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.player_video_details),
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TvPlayerIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = stringResource(R.string.player_close),
                        onClick = onDismiss,
                        testTag = "info_dialog_close_icon"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Video Title
                Text(
                    text = details.title,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Channel Info Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF222233))
                        .padding(12.dp)
                ) {
                    if (details.authorImageUrl.isNotBlank()) {
                        AsyncImage(
                            model = details.authorImageUrl,
                            contentDescription = details.author,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF333348))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = details.author,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (details.subscriberCount.isNotBlank()) {
                            Text(
                                text = details.subscriberCount,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (onToggleSubscribe != null) {
                        TvDialogActionButton(
                            icon = Icons.Default.Subscriptions,
                            text = stringResource(if (isSubscribed) R.string.channel_subscribed else R.string.channel_subscribe),
                            isToggled = isSubscribed,
                            onClick = onToggleSubscribe,
                            testTag = "info_subscribe_btn"
                        )
                    }

                    if (onToggleFavorite != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TvDialogActionButton(
                            icon = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            text = stringResource(if (isFavorite) R.string.player_remove_favorite else R.string.player_save_favorite),
                            isToggled = isFavorite,
                            onClick = onToggleFavorite,
                            testTag = "info_favorite_btn"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Row: Views, Likes, Published Date
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (details.viewCount.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = details.viewCount,
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (details.likeCount.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = details.likeCount,
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (details.publishedDate.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = details.publishedDate,
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Description
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F0F17))
                        .padding(12.dp)
                ) {
                    item {
                        Text(
                            text = if (details.description.isNotBlank()) details.description else "No description provided.",
                            color = Color(0xFFDDDDDD),
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Close Action Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isCloseFocused) YouTubeRed else Color(0xFF2A2A3E))
                        .border(
                            width = if (isCloseFocused) 2.dp else 1.dp,
                            color = if (isCloseFocused) Color.White else Color(0x30FFFFFF),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = closeInteractionSource,
                            indication = null,
                            onClick = onDismiss
                        )
                        .focusable(interactionSource = closeInteractionSource)
                        .padding(vertical = 12.dp)
                        .testTag("info_dialog_close_button")
                ) {
                    Text(
                        text = stringResource(R.string.player_close),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TvDialogActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isToggled: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isFocused -> Color.White
                    isToggled -> YouTubeRed
                    else -> Color(0xFF333348)
                }
            )
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = YouTubeRed,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isFocused) Color.Black else Color.White,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = if (isFocused) Color.Black else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
