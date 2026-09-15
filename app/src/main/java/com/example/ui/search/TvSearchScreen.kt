package com.example.ui.search

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.YouTubeRepository
import com.example.model.VideoItem
import com.example.ui.components.TvVideoCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NuvioCyan
import com.example.ui.theme.NuvioViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

private val QuickTagsEnglish = listOf(
    "Trending", "Music", "Gaming", "News", "Documentary",
    "Science", "Tech", "Movie Trailers", "Comedy", "Lo-Fi Beats"
)

private val QuickTagsArabic = listOf(
    "المحتوى الرائج", "تلاوات خاشعة", "أخبار", "وثائقيات",
    "ألعاب فيديو", "تقنية", "أفلام ومسلسلات", "بودكاست", "رياضة"
)

// 6 Columns Ergonomic Leanback TV Keyboard (English)
private val EnglishKeyboardRows = listOf(
    listOf("A", "B", "C", "D", "E", "F"),
    listOf("G", "H", "I", "J", "K", "L"),
    listOf("M", "N", "O", "P", "Q", "R"),
    listOf("S", "T", "U", "V", "W", "X"),
    listOf("Y", "Z", "1", "2", "3", "4"),
    listOf("5", "6", "7", "8", "9", "0")
)

// 6 Columns Ergonomic Leanback TV Keyboard (Arabic)
private val ArabicKeyboardRows = listOf(
    listOf("ا", "ب", "ت", "ث", "ج", "ح"),
    listOf("خ", "د", "ذ", "ر", "ز", "س"),
    listOf("ش", "ص", "ض", "ط", "ظ", "ع"),
    listOf("غ", "ف", "ق", "ك", "ل", "م"),
    listOf("ن", "هـ", "و", "ي", "ة", "ى"),
    listOf("1", "2", "3", "4", "5", "6"),
    listOf("7", "8", "9", "0", "؟", "،")
)

/**
 * TV Search Screen with spacious Leanback keyboard, live suggestions, and system IME support.
 */
@Composable
fun TvSearchScreen(
    onPlayVideo: (VideoItem) -> Unit,
    repository: YouTubeRepository,
    isArabic: Boolean = false,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isArabicKeyboard by remember { mutableStateOf(isArabic) }
    var searchResults by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var dynamicSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    val inputFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Fetch dynamic suggestions when query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            dynamicSuggestions = repository.getSearchSuggestions(searchQuery)
        } else {
            dynamicSuggestions = emptyList()
        }
    }

    LaunchedEffect(hasSearched, searchQuery) {
        if (hasSearched && searchQuery.isNotBlank()) {
            isSearching = true
            try {
                searchResults = repository.search(searchQuery)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isSearching = false
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        // Left Column: Spacious TV Keyboard & Search Field (Width 430dp)
        Column(
            modifier = Modifier
                .width(430.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF0121422),
                            Color(0xF00A0C16)
                        )
                    )
                )
                .border(1.5.dp, NuvioViolet.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            // Interactive Search Input Display (Directly opens Android TV IME / System Keyboard)
            val searchBoxInteraction = remember { MutableInteractionSource() }
            val isSearchBoxFocused by searchBoxInteraction.collectIsFocusedAsState()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSearchBoxFocused) Color(0xFF222538) else DarkSurfaceElevated)
                    .border(
                        width = if (isSearchBoxFocused) 2.dp else 1.dp,
                        color = if (isSearchBoxFocused) NuvioCyan else Color(0x33FFFFFF),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = searchBoxInteraction,
                        indication = null,
                        onClick = {
                            try {
                                inputFocusRequester.requestFocus()
                                keyboardController?.show()
                            } catch (_: Exception) {}
                        }
                    )
                    .focusable(interactionSource = searchBoxInteraction)
                    .padding(horizontal = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = NuvioCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_hint),
                            color = TextSecondary,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        cursorBrush = SolidColor(NuvioCyan),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (searchQuery.isNotBlank()) {
                                    hasSearched = true
                                    keyboardController?.hide()
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(inputFocusRequester)
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                searchQuery = ""
                                hasSearched = false
                                searchResults = emptyList()
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TV System Keyboard Trigger Banner (YouTube TV Style - Opens Native Android TV IME)
            val imeBannerInteraction = remember { MutableInteractionSource() }
            val isImeBannerFocused by imeBannerInteraction.collectIsFocusedAsState()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isImeBannerFocused) Color.White else Color(0x2B7C4DFF))
                    .border(
                        width = if (isImeBannerFocused) 2.dp else 1.dp,
                        color = if (isImeBannerFocused) NuvioCyan else NuvioViolet.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable(
                        interactionSource = imeBannerInteraction,
                        indication = null,
                        onClick = {
                            try {
                                inputFocusRequester.requestFocus()
                                keyboardController?.show()
                            } catch (_: Exception) {}
                        }
                    )
                    .focusable(interactionSource = imeBannerInteraction)
                    .padding(horizontal = 12.dp)
                    .testTag("search_open_system_ime_banner")
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = stringResource(R.string.key_open_system_keyboard),
                    tint = if (isImeBannerFocused) Color.Black else NuvioCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.key_open_system_keyboard),
                    color = if (isImeBannerFocused) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Bar: Space, Backspace, Clear, Language (عربي / EN), and Submit Search
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TvKeyButton(
                    text = stringResource(R.string.key_space),
                    modifier = Modifier.weight(1.3f),
                    height = 42.dp,
                    onClick = { searchQuery += " " }
                )
                TvKeyButton(
                    text = stringResource(R.string.key_backspace),
                    icon = Icons.Default.Backspace,
                    modifier = Modifier.weight(1.1f),
                    height = 42.dp,
                    onClick = {
                        if (searchQuery.isNotEmpty()) {
                            searchQuery = searchQuery.dropLast(1)
                        }
                    }
                )
                TvKeyButton(
                    text = stringResource(R.string.key_clear),
                    icon = Icons.Default.Clear,
                    modifier = Modifier.weight(1.0f),
                    height = 42.dp,
                    onClick = {
                        searchQuery = ""
                        hasSearched = false
                        searchResults = emptyList()
                    }
                )
                TvKeyButton(
                    text = if (isArabicKeyboard) "EN" else "عربي",
                    icon = Icons.Default.Language,
                    modifier = Modifier.weight(1.1f),
                    height = 42.dp,
                    isHighlighted = true,
                    onClick = { isArabicKeyboard = !isArabicKeyboard }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6-Column Keyboard Letters Grid
            val currentRows = if (isArabicKeyboard) ArabicKeyboardRows else EnglishKeyboardRows
            currentRows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    row.forEach { char ->
                        TvKeyButton(
                            text = char,
                            modifier = Modifier.weight(1f),
                            height = 44.dp,
                            fontSize = 16.sp,
                            onClick = { searchQuery += char }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Big Search Trigger / IME Button
            val searchInteractionSource = remember { MutableInteractionSource() }
            val isSearchFocused by searchInteractionSource.collectIsFocusedAsState()

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Submit Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSearchFocused) Color.White else YouTubeRed)
                        .border(
                            width = if (isSearchFocused) 2.dp else 0.dp,
                            color = NuvioCyan,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = searchInteractionSource,
                            indication = null,
                            onClick = {
                                if (searchQuery.isNotBlank()) {
                                    hasSearched = true
                                }
                            }
                        )
                        .focusable(interactionSource = searchInteractionSource)
                        .testTag("search_submit_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Submit Search",
                            tint = if (isSearchFocused) Color.Black else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.nav_search),
                            color = if (isSearchFocused) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Keyboard IME button
                val imeInteraction = remember { MutableInteractionSource() }
                val isImeFocused by imeInteraction.collectIsFocusedAsState()

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isImeFocused) Color.White else Color(0x337C4DFF))
                        .border(
                            width = if (isImeFocused) 2.dp else 1.dp,
                            color = if (isImeFocused) NuvioCyan else NuvioViolet,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = imeInteraction,
                            indication = null,
                            onClick = {
                                try {
                                    inputFocusRequester.requestFocus()
                                    keyboardController?.show()
                                } catch (_: Exception) {}
                            }
                        )
                        .focusable(interactionSource = imeInteraction)
                        .padding(horizontal = 14.dp)
                        .testTag("search_open_ime")
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = stringResource(R.string.key_open_system_keyboard),
                        tint = if (isImeFocused) Color.Black else NuvioCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Suggestions List
            if (dynamicSuggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.search_suggestions),
                    color = NuvioCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                dynamicSuggestions.take(3).forEach { suggestion ->
                    TvSuggestionRow(
                        text = suggestion,
                        onClick = {
                            searchQuery = suggestion
                            hasSearched = true
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(22.dp))

        // Right Column: Search Results Grid or Topic Chips
        Column(modifier = Modifier.weight(1f)) {
            // Quick Topic Tags (Localized)
            val quickTags = if (isArabic) QuickTagsArabic else QuickTagsEnglish
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickTags) { tag ->
                    TvQuickTagChip(
                        tag = tag,
                        onClick = {
                            searchQuery = tag
                            hasSearched = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Results / State Area
            when {
                isSearching -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = NuvioCyan,
                            strokeWidth = 3.dp
                        )
                    }
                }

                searchResults.isNotEmpty() -> {
                    Text(
                        text = stringResource(R.string.search_results_title) + " ($searchQuery)",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 220.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(searchResults) { video ->
                            TvVideoCard(
                                video = video,
                                onVideoClick = { onPlayVideo(video) }
                            )
                        }
                    }
                }

                hasSearched -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.search_no_results),
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.search_hint),
                                color = TextSecondary,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * TV Keyboard key button with bold focus feedback, comfortable touch/remote targets.
 */
@Composable
fun TvKeyButton(
    text: String,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 44.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 15.sp,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    isFocused -> Color.White
                    isHighlighted -> Color(0x337C4DFF)
                    else -> Color(0x331E2235)
                }
            )
            .border(
                width = if (isFocused) 2.5.dp else 1.dp,
                color = when {
                    isFocused -> NuvioCyan
                    isHighlighted -> NuvioViolet
                    else -> Color(0x22FFFFFF)
                },
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .testTag("key_$text")
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (isFocused) Color.Black else if (isHighlighted) NuvioCyan else Color.White,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = text,
                color = if (isFocused) Color.Black else if (isHighlighted) NuvioCyan else Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TvQuickTagChip(
    tag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isFocused) Color.White else Color(0x40161928))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) NuvioCyan else Color(0x33FFFFFF),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("tag_$tag")
    ) {
        Text(
            text = tag,
            color = if (isFocused) Color.Black else Color.White,
            fontSize = 13.sp,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun TvSuggestionRow(
    text: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isFocused) Color.White else Color(0x1AFFFFFF))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = if (isFocused) Color.Black else NuvioCyan,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = if (isFocused) Color.Black else Color.White,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
