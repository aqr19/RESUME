package com.example.ui.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.HistoryVideoItem
import com.example.data.TvLibraryRepository
import com.example.data.local.SubscribedChannelEntity
import com.example.model.VideoItem
import com.example.ui.components.TvVideoCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.launch

@Composable
fun TvLibraryScreen(
    libraryRepository: TvLibraryRepository,
    onPlayVideo: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }

    val historyList by libraryRepository.watchHistory.collectAsState(initial = emptyList())
    val favoritesList by libraryRepository.favorites.collectAsState(initial = emptyList())
    val subscriptionsList by libraryRepository.subscriptions.collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(top = 28.dp, start = 28.dp, end = 28.dp)
    ) {
        // Library Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(YouTubeRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = stringResource(R.string.library_title),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            // Tab Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TvLibraryTabButton(
                    title = stringResource(R.string.library_tab_history),
                    count = historyList.size,
                    isSelected = selectedTab == 0,
                    icon = Icons.Default.History,
                    onClick = { selectedTab = 0 },
                    testTag = "tab_history"
                )
                TvLibraryTabButton(
                    title = stringResource(R.string.library_tab_favorites),
                    count = favoritesList.size,
                    isSelected = selectedTab == 1,
                    icon = Icons.Default.Bookmark,
                    onClick = { selectedTab = 1 },
                    testTag = "tab_favorites"
                )
                TvLibraryTabButton(
                    title = stringResource(R.string.library_tab_subscriptions),
                    count = subscriptionsList.size,
                    isSelected = selectedTab == 2,
                    icon = Icons.Default.Subscriptions,
                    onClick = { selectedTab = 2 },
                    testTag = "tab_subscriptions"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Content Area according to selected tab
        when (selectedTab) {
            0 -> {
                // Watch History
                if (historyList.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${stringResource(R.string.library_tab_history)} (${historyList.size})",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )

                        TvActionButton(
                            icon = Icons.Default.DeleteSweep,
                            text = stringResource(R.string.library_clear_history),
                            onClick = {
                                coroutineScope.launch {
                                    libraryRepository.clearHistory()
                                }
                            },
                            testTag = "clear_history_btn"
                        )
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 250.dp),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(historyList, key = { it.video.id }) { item ->
                            val progress = if (item.video.durationMs > 0) {
                                item.playbackPositionMs.toFloat() / item.video.durationMs.toFloat()
                            } else null

                            TvVideoCard(
                                video = item.video,
                                onVideoClick = { onPlayVideo(item.video) },
                                playbackProgress = progress,
                                cardWidth = 260
                            )
                        }
                    }
                } else {
                    TvLibraryEmptyState(
                        icon = Icons.Default.History,
                        message = stringResource(R.string.library_empty_history)
                    )
                }
            }
            1 -> {
                // Favorites / Bookmarks
                if (favoritesList.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 250.dp),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(favoritesList, key = { it.id }) { video ->
                            TvVideoCard(
                                video = video,
                                onVideoClick = { onPlayVideo(video) },
                                cardWidth = 260
                            )
                        }
                    }
                } else {
                    TvLibraryEmptyState(
                        icon = Icons.Default.Bookmark,
                        message = stringResource(R.string.library_empty_favorites)
                    )
                }
            }
            2 -> {
                // Subscribed Channels
                if (subscriptionsList.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 240.dp),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(subscriptionsList, key = { it.channelId }) { channel ->
                            TvSubscribedChannelCard(
                                channel = channel,
                                onUnsubscribe = {
                                    coroutineScope.launch {
                                        libraryRepository.unsubscribe(channel.channelId)
                                    }
                                }
                            )
                        }
                    }
                } else {
                    TvLibraryEmptyState(
                        icon = Icons.Default.Subscriptions,
                        message = stringResource(R.string.library_empty_subscriptions)
                    )
                }
            }
        }
    }
}

@Composable
fun TvLibraryTabButton(
    title: String,
    count: Int,
    isSelected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> DarkSurfaceElevated
            else -> DarkSurface
        },
        label = "tab_bg"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.Black
            isSelected -> YouTubeRed
            else -> TextSecondary
        },
        label = "tab_color"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(
                width = if (isFocused) 2.dp else if (isSelected) 1.dp else 0.dp,
                color = if (isFocused) YouTubeRed else if (isSelected) YouTubeRed.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = contentColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
        )
        if (count > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isFocused) Color.Black else YouTubeRed)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TvActionButton(
    icon: ImageVector,
    text: String,
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
            .background(if (isFocused) Color.White else DarkSurfaceElevated)
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
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isFocused) Color.Black else TextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = if (isFocused) Color.Black else TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TvSubscribedChannelCard(
    channel: SubscribedChannelEntity,
    onUnsubscribe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1.0f,
        label = "channel_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) Color.White else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        if (channel.channelAvatarUrl.isNotBlank()) {
            AsyncImage(
                model = channel.channelAvatarUrl,
                contentDescription = channel.channelTitle,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(2.dp, YouTubeRed, CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(YouTubeRed),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = channel.channelTitle.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = channel.channelTitle,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (channel.subscriberCount.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = channel.subscriberCount,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TvActionButton(
            icon = Icons.Default.Subscriptions,
            text = stringResource(R.string.channel_subscribed),
            onClick = onUnsubscribe,
            testTag = "unsub_${channel.channelId}"
        )
    }
}

@Composable
fun TvLibraryEmptyState(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = TextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(360.dp)
            )
        }
    }
}
