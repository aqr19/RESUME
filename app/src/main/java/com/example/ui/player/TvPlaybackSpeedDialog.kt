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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

/**
 * Available playback speeds from 0.5x to 2.0x.
 */
val PLAYBACK_SPEED_OPTIONS = listOf(
    0.5f,
    0.75f,
    1.0f,
    1.25f,
    1.5f,
    1.75f,
    2.0f
)

/**
 * Modal dialog for selecting player playback speed (0.5x to 2.0x) on TV.
 */
@Composable
fun TvPlaybackSpeedDialog(
    isOpen: Boolean,
    currentSpeed: Float,
    onSelectSpeed: (Float) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .clickable(onClick = onDismiss)
            .testTag("tv_speed_selection_scrim"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF161622))
                .border(2.dp, Color(0x40FFFFFF), RoundedCornerShape(20.dp))
                .clickable(enabled = false) {} // Prevent scrim click-through
                .padding(24.dp)
                .testTag("tv_speed_dialog")
        ) {
            Column {
                // Dialog Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = YouTubeRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.player_speed_select),
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TvPlayerIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = stringResource(R.string.player_close),
                        onClick = onDismiss,
                        testTag = "speed_dialog_close"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List of speed options (0.5x to 2.0x)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(PLAYBACK_SPEED_OPTIONS) { speed ->
                        val isSelected = (speed == currentSpeed)
                        val label = if (speed == 1.0f) {
                            "1.0x (${stringResource(R.string.player_speed_normal)})"
                        } else {
                            "${speed}x"
                        }

                        TvSpeedOptionItem(
                            label = label,
                            isSelected = isSelected,
                            onClick = {
                                onSelectSpeed(speed)
                                onDismiss()
                            },
                            testTag = "speed_option_${speed.toString().replace('.', '_')}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvSpeedOptionItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
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
                    isFocused -> Color(0xFF2A2A3E)
                    isSelected -> Color(0xFF1E1E2C)
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = when {
                    isFocused -> YouTubeRed
                    isSelected -> Color(0x60E50914)
                    else -> Color(0x20FFFFFF)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            color = if (isFocused || isSelected) Color.White else TextPrimary,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = YouTubeRed,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
