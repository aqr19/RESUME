package com.example.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.VideoItem
import com.example.ui.components.TvVideoCard

/**
 * Interactive Related Videos / Suggestions row displayed at the bottom of the TV Player overlay.
 */
@Composable
fun TvRelatedVideosRow(
    videos: List<VideoItem>,
    onSelectVideo: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (videos.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tv_related_videos_row")
    ) {
        Text(
            text = stringResource(R.string.player_related_videos),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(videos, key = { it.id }) { item ->
                TvVideoCard(
                    video = item,
                    onVideoClick = onSelectVideo,
                    cardWidth = 190
                )
            }
        }
    }
}
