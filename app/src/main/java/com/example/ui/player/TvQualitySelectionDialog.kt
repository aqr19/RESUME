package com.example.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.model.VideoStreamFormat
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import com.example.ui.theme.NuvioCyan
import com.example.ui.theme.NuvioViolet
import kotlinx.coroutines.delay

/**
 * Dialog for selecting video quality / playback stream resolution on TV.
 */
@Composable
fun TvQualitySelectionDialog(
    isOpen: Boolean,
    availableFormats: List<VideoStreamFormat>,
    selectedFormatUrl: String?, // null means "Auto"
    onSelectFormat: (VideoStreamFormat?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val firstItemFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isOpen) {
        delay(100)
        try {
            firstItemFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable(onClick = onDismiss)
            .testTag("tv_quality_selection_scrim"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(440.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xF2121422))
                .border(2.dp, NuvioViolet.copy(alpha = 0.8f), RoundedCornerShape(22.dp))
                .clickable(enabled = false) {} // Prevent click-through
                .padding(24.dp)
                .testTag("tv_quality_dialog")
        ) {
            Column {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HighQuality,
                            contentDescription = null,
                            tint = NuvioCyan,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.player_quality_select),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TvPlayerIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = stringResource(R.string.player_close),
                        onClick = onDismiss,
                        testTag = "quality_dialog_close"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    // Auto option (receives initial focus requester)
                    item {
                        TvQualityOptionItem(
                            label = stringResource(R.string.player_auto_quality),
                            resolutionInfo = stringResource(R.string.quality_auto_desc),
                            isSelected = selectedFormatUrl == null,
                            onClick = {
                                onSelectFormat(null)
                                onDismiss()
                            },
                            modifier = Modifier.focusRequester(firstItemFocusRequester),
                            testTag = "quality_option_auto"
                        )
                    }

                    // Specific format options
                    items(availableFormats) { format ->
                        val label = format.qualityLabel ?: "${format.height}p"
                        val fpsLabel = if (format.fps > 30) "${format.fps}fps" else null
                        val resLabel = if (format.width > 0 && format.height > 0) {
                            if (fpsLabel != null) "${format.width}x${format.height} • $fpsLabel" else "${format.width}x${format.height}"
                        } else fpsLabel

                        TvQualityOptionItem(
                            label = label,
                            resolutionInfo = resLabel,
                            isAv1 = format.isAv1,
                            isSelected = selectedFormatUrl == format.url,
                            onClick = {
                                onSelectFormat(format)
                                onDismiss()
                            },
                            testTag = "quality_option_${format.height}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvQualityOptionItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    resolutionInfo: String? = null,
    isAv1: Boolean = false,
    testTag: String = "quality_option"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    isFocused -> Color.White
                    isSelected -> Color(0x337C4DFF)
                    else -> Color(0x221E2038)
                }
            )
            .border(
                width = if (isFocused) 2.5.dp else if (isSelected) 1.5.dp else 1.dp,
                color = when {
                    isFocused -> NuvioCyan
                    isSelected -> NuvioViolet
                    else -> Color(0x33FFFFFF)
                },
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .testTag(testTag)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    color = if (isFocused) Color.Black else if (isSelected) NuvioCyan else Color.White,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
                )
                if (isAv1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isFocused) Color(0xFF006688) else Color(0x3300E5FF))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AV1",
                            color = if (isFocused) Color.White else NuvioCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (resolutionInfo != null) {
                Text(
                    text = resolutionInfo,
                    color = if (isFocused) Color(0xFF333333) else TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = if (isFocused) Color.Black else NuvioCyan,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
