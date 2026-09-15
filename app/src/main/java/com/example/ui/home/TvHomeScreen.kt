package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.YouTubeRepository
import com.example.model.VideoItem
import com.example.ui.components.TvVideoCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

@Composable
fun TvHomeScreen(
    categoryTitle: String,
    onPlayVideo: (VideoItem) -> Unit,
    repository: YouTubeRepository,
    modifier: Modifier = Modifier
) {
    var isLoading by remember(categoryTitle) { mutableStateOf(true) }
    var errorMessage by remember(categoryTitle) { mutableStateOf<String?>(null) }
    var sections by remember(categoryTitle) { mutableStateOf<List<YouTubeRepository.VideoSection>>(emptyList()) }
    var retryTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(categoryTitle, retryTrigger) {
        isLoading = true
        errorMessage = null
        try {
            sections = when (categoryTitle) {
                "Home" -> {
                    val sec = repository.getHomeSections()
                    if (sec.isEmpty()) {
                        val trending = repository.getTrendingVideos()
                        if (trending.isNotEmpty()) {
                            listOf(YouTubeRepository.VideoSection("Trending", trending))
                        } else {
                            emptyList()
                        }
                    } else sec
                }
                "Trending" -> {
                    val trending = repository.getTrendingVideos()
                    listOf(YouTubeRepository.VideoSection("Trending Now", trending))
                }
                else -> {
                    val items = repository.getCategoryVideos(categoryTitle)
                    listOf(YouTubeRepository.VideoSection(categoryTitle, items))
                }
            }
            if (sections.isEmpty()) {
                errorMessage = "No videos found. Check network connection."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = e.localizedMessage ?: "Failed to load content."
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 12.dp)
    ) {
        when {
            isLoading -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = YouTubeRed,
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading $categoryTitle...",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            errorMessage != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage ?: "Unable to load videos",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val interactionSource = remember { MutableInteractionSource() }
                    val isFocused by interactionSource.collectIsFocusedAsState()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
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
                                onClick = { retryTrigger++ }
                            )
                            .focusable(interactionSource = interactionSource)
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                            .testTag("retry_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = if (isFocused) Color.Black else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Retry",
                            color = if (isFocused) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
                ) {
                    itemsIndexed(
                        items = sections,
                        key = { index, section -> "${section.title}_$index" }
                    ) { _, section ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = section.title,
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                itemsIndexed(section.items, key = { _, video -> video.id }) { _, video ->
                                    TvVideoCard(
                                        video = video,
                                        onVideoClick = onPlayVideo
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
