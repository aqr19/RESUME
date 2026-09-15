package com.example.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.GoogleAccountManager
import com.example.data.GoogleSignInState
import com.example.data.SettingsManager
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.SponsorGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

enum class SettingsCategory(val titleRes: Int, val icon: ImageVector) {
    ACCOUNT(R.string.settings_cat_account, Icons.Default.AccountCircle),
    LANGUAGE(R.string.settings_cat_language, Icons.Default.Language),
    SPONSORBLOCK(R.string.settings_cat_sponsorblock, Icons.Default.Block),
    PLAYBACK(R.string.settings_cat_playback, Icons.Default.Hd),
    SUBTITLES(R.string.settings_cat_subtitles, Icons.Default.Subtitles),
    DEARROW(R.string.settings_cat_dearrow, Icons.Default.Visibility),
    INTERFACE(R.string.settings_cat_interface, Icons.Default.Tv),
    STORAGE(R.string.settings_cat_storage, Icons.Default.CleaningServices),
    ABOUT(R.string.settings_cat_about, Icons.Default.Info)
}

@Composable
fun TvSettingsScreen(
    settingsManager: SettingsManager,
    accountManager: GoogleAccountManager,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(SettingsCategory.ACCOUNT) }
    val generalSettings by settingsManager.generalSettings.collectAsState()
    val isArabic = generalSettings.appLanguage == "ar"

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Left Column: Category Navigation Tabs (D-pad vertical scroll)
        Column(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
                .padding(end = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(SettingsCategory.values()) { category ->
                    val isSelected = category == selectedCategory
                    val interactionSource = remember { MutableInteractionSource() }
                    val isFocused by interactionSource.collectIsFocusedAsState()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when {
                                    isFocused -> Color.White
                                    isSelected -> DarkSurfaceElevated
                                    else -> Color.Transparent
                                }
                            )
                            .border(
                                width = if (isFocused) 2.dp else if (isSelected) 1.dp else 0.dp,
                                color = if (isFocused) YouTubeRed else DarkSurfaceElevated,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { selectedCategory = category }
                            )
                            .focusable(interactionSource = interactionSource)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("settings_tab_${category.name.lowercase()}")
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = when {
                                isFocused -> Color.Black
                                isSelected -> YouTubeRed
                                else -> TextSecondary
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(category.titleRes),
                            color = when {
                                isFocused -> Color.Black
                                isSelected -> Color.White
                                else -> TextSecondary
                            },
                            fontSize = 14.sp,
                            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Right Column: Options Pane for the Selected Category
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurface)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            when (selectedCategory) {
                SettingsCategory.ACCOUNT -> AccountSettingsPane(accountManager)
                SettingsCategory.LANGUAGE -> LanguageRegionPane(settingsManager)
                SettingsCategory.SPONSORBLOCK -> SponsorBlockPane(settingsManager)
                SettingsCategory.PLAYBACK -> PlaybackPane(settingsManager)
                SettingsCategory.SUBTITLES -> SubtitlesPane(settingsManager)
                SettingsCategory.DEARROW -> DeArrowPane(settingsManager)
                SettingsCategory.INTERFACE -> InterfacePane(settingsManager)
                SettingsCategory.STORAGE -> StoragePane(settingsManager)
                SettingsCategory.ABOUT -> AboutPane()
            }
        }
    }
}

// -------------------------------------------------------------
// 1. Google Account Settings Pane
// -------------------------------------------------------------
@Composable
fun AccountSettingsPane(accountManager: GoogleAccountManager) {
    val signInState by accountManager.signInState.collectAsState()
    val scope = rememberCoroutineScope()

    Text(
        text = stringResource(R.string.settings_cat_account),
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    when (val state = signInState) {
        is GoogleSignInState.SignedIn -> {
            // Signed In View
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .padding(20.dp)
            ) {
                if (!state.profile.avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = state.profile.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .border(2.dp, YouTubeRed, CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(YouTubeRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.profile.name.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.profile.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.profile.email.ifEmpty { "Google Account Connected" },
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SponsorGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.account_status_signed_in),
                            color = SponsorGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TvActionButton(
                    label = stringResource(R.string.account_sign_out),
                    isDestructive = true,
                    onClick = { accountManager.signOut() }
                )
            }
        }

        is GoogleSignInState.LoadingCode -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = YouTubeRed)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Requesting Google authorization code...",
                    color = Color.White,
                    fontSize = 15.sp
                )
            }
        }

        is GoogleSignInState.AwaitingUserCode -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.account_sign_in_google),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.account_instructions_step1),
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text = state.verificationUrl,
                    color = YouTubeRed,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.account_instructions_step2),
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                // Large code display
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E1E28))
                        .border(2.dp, YouTubeRed, RoundedCornerShape(8.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.userCode,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 4.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = YouTubeRed,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.account_instructions_waiting),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TvActionButton(
                        label = stringResource(R.string.account_cancel),
                        isDestructive = false,
                        onClick = { accountManager.cancelSignIn() }
                    )
                    TvActionButton(
                        label = stringResource(R.string.account_quick_demo),
                        isDestructive = false,
                        onClick = { accountManager.loginDemoAccount() }
                    )
                }
            }
        }

        else -> {
            // Signed Out View
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.account_status_signed_out),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.account_sign_in_desc),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TvActionButton(
                        label = stringResource(R.string.account_sign_in_google),
                        isDestructive = false,
                        isPrimary = true,
                        onClick = { accountManager.startGoogleSignIn(scope) }
                    )

                    TvActionButton(
                        label = stringResource(R.string.account_quick_demo),
                        isDestructive = false,
                        onClick = { accountManager.loginDemoAccount() }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. Language & Region Settings Pane
// -------------------------------------------------------------
@Composable
fun LanguageRegionPane(settingsManager: SettingsManager) {
    val generalSettings by settingsManager.generalSettings.collectAsState()

    Text(
        text = stringResource(R.string.settings_cat_language),
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    // Language Selector
    TvChoiceRow(
        title = stringResource(R.string.settings_app_language),
        subtitle = stringResource(R.string.settings_app_language_desc),
        selected = generalSettings.appLanguage,
        options = listOf("ar" to "العربية", "en" to "English"),
        onSelect = { lang ->
            settingsManager.updateGeneralSettings(generalSettings.copy(appLanguage = lang))
        }
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Content Region Selector
    val regions = listOf(
        "SA" to "المملكة العربية السعودية (SA)",
        "AE" to "الإمارات (AE)",
        "EG" to "مصر (EG)",
        "IQ" to "العراق (IQ)",
        "MA" to "المغرب (MA)",
        "GLOBAL" to "عالمي (Global)",
        "US" to "United States (US)"
    )
    TvChoiceRow(
        title = stringResource(R.string.settings_content_region),
        subtitle = stringResource(R.string.settings_content_region_desc),
        selected = generalSettings.contentRegion,
        options = regions,
        onSelect = { r ->
            settingsManager.updateGeneralSettings(generalSettings.copy(contentRegion = r))
        }
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Restricted Mode
    val restrictedOptions = listOf(
        "off" to "إيقاف (Off)",
        "moderate" to "متوسط (Moderate)",
        "strict" to "صارم (Strict)"
    )
    TvChoiceRow(
        title = stringResource(R.string.settings_restricted_mode),
        subtitle = stringResource(R.string.settings_restricted_mode_desc),
        selected = generalSettings.restrictedMode,
        options = restrictedOptions,
        onSelect = { rm ->
            settingsManager.updateGeneralSettings(generalSettings.copy(restrictedMode = rm))
        }
    )
}

// -------------------------------------------------------------
// 3. SponsorBlock Settings Pane
// -------------------------------------------------------------
@Composable
fun SponsorBlockPane(settingsManager: SettingsManager) {
    val sponsorSettings by settingsManager.sponsorSettings.collectAsState()

    Text(
        text = stringResource(R.string.settings_cat_sponsorblock),
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    TvSettingsToggleRow(
        title = stringResource(R.string.sb_enable_title),
        subtitle = stringResource(R.string.sb_enable_desc),
        checked = sponsorSettings.isEnabled,
        onCheckedChange = { checked ->
            settingsManager.updateSponsorSettings(sponsorSettings.copy(isEnabled = checked))
        }
    )

    if (sponsorSettings.isEnabled) {
        TvSettingsToggleRow(
            title = stringResource(R.string.sb_skip_sponsors),
            subtitle = stringResource(R.string.sb_skip_sponsors_desc),
            checked = sponsorSettings.skipSponsors,
            onCheckedChange = { checked ->
                settingsManager.updateSponsorSettings(sponsorSettings.copy(skipSponsors = checked))
            }
        )

        TvSettingsToggleRow(
            title = stringResource(R.string.sb_skip_intro),
            subtitle = stringResource(R.string.sb_skip_intro_desc),
            checked = sponsorSettings.skipIntro,
            onCheckedChange = { checked ->
                settingsManager.updateSponsorSettings(sponsorSettings.copy(skipIntro = checked))
            }
        )

        TvSettingsToggleRow(
            title = stringResource(R.string.sb_skip_outro),
            subtitle = stringResource(R.string.sb_skip_outro_desc),
            checked = sponsorSettings.skipOutro,
            onCheckedChange = { checked ->
                settingsManager.updateSponsorSettings(sponsorSettings.copy(skipOutro = checked))
            }
        )

        TvSettingsToggleRow(
            title = stringResource(R.string.sb_skip_selfpromo),
            subtitle = stringResource(R.string.sb_skip_selfpromo_desc),
            checked = sponsorSettings.skipSelfPromo,
            onCheckedChange = { checked ->
                settingsManager.updateSponsorSettings(sponsorSettings.copy(skipSelfPromo = checked))
            }
        )

        TvSettingsToggleRow(
            title = stringResource(R.string.sb_skip_interaction),
            subtitle = stringResource(R.string.sb_skip_interaction_desc),
            checked = sponsorSettings.skipInteraction,
            onCheckedChange = { checked ->
                settingsManager.updateSponsorSettings(sponsorSettings.copy(skipInteraction = checked))
            }
        )

        TvSettingsToggleRow(
            title = stringResource(R.string.sb_skip_music),
            subtitle = stringResource(R.string.sb_skip_music_desc),
            checked = sponsorSettings.skipMusicOffTopic,
            onCheckedChange = { checked ->
                settingsManager.updateSponsorSettings(sponsorSettings.copy(skipMusicOffTopic = checked))
            }
        )

        TvSettingsToggleRow(
            title = stringResource(R.string.sb_show_toast),
            subtitle = stringResource(R.string.sb_show_toast_desc),
            checked = sponsorSettings.showSkipToast,
            onCheckedChange = { checked ->
                settingsManager.updateSponsorSettings(sponsorSettings.copy(showSkipToast = checked))
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        TvChoiceRow(
            title = stringResource(R.string.sb_toast_duration),
            subtitle = "Duration of skip badge on screen",
            selected = sponsorSettings.toastDurationSec.toString(),
            options = listOf("2" to "2s", "3" to "3s", "5" to "5s"),
            onSelect = { d ->
                settingsManager.updateSponsorSettings(sponsorSettings.copy(toastDurationSec = d.toInt()))
            }
        )
    }
}

// -------------------------------------------------------------
// 4. Playback Settings Pane
// -------------------------------------------------------------
@Composable
fun PlaybackPane(settingsManager: SettingsManager) {
    val playbackSettings by settingsManager.playbackSettings.collectAsState()

    Text(
        text = stringResource(R.string.settings_cat_playback),
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    val qualities = listOf(
        "Auto" to "Auto",
        "2160p" to "4K (2160p)",
        "1440p" to "1440p",
        "1080p" to "1080p Full HD",
        "720p" to "720p HD",
        "480p" to "480p"
    )
    TvChoiceRow(
        title = stringResource(R.string.play_quality_title),
        subtitle = stringResource(R.string.play_quality_desc),
        selected = playbackSettings.defaultQuality,
        options = qualities,
        onSelect = { q ->
            settingsManager.updatePlaybackSettings(playbackSettings.copy(defaultQuality = q))
        }
    )

    Spacer(modifier = Modifier.height(10.dp))

    val speeds = listOf(
        "0.75" to "0.75x",
        "1.0" to "1.0x (Normal)",
        "1.25" to "1.25x",
        "1.5" to "1.5x",
        "2.0" to "2.0x"
    )
    TvChoiceRow(
        title = stringResource(R.string.play_speed_title),
        subtitle = stringResource(R.string.play_speed_desc),
        selected = playbackSettings.preferredSpeed.toString(),
        options = speeds,
        onSelect = { sp ->
            settingsManager.updatePlaybackSettings(playbackSettings.copy(preferredSpeed = sp.toFloat()))
        }
    )

    Spacer(modifier = Modifier.height(10.dp))

    TvSettingsToggleRow(
        title = stringResource(R.string.play_autonext_title),
        subtitle = stringResource(R.string.play_autonext_desc),
        checked = playbackSettings.autoPlayNext,
        onCheckedChange = { checked ->
            settingsManager.updatePlaybackSettings(playbackSettings.copy(autoPlayNext = checked))
        }
    )

    TvSettingsToggleRow(
        title = stringResource(R.string.play_hw_codec),
        subtitle = stringResource(R.string.play_hw_codec_desc),
        checked = playbackSettings.hardwareDecoding,
        onCheckedChange = { checked ->
            settingsManager.updatePlaybackSettings(playbackSettings.copy(hardwareDecoding = checked))
        }
    )

    Spacer(modifier = Modifier.height(10.dp))

    val audioTracks = listOf(
        "Original" to "Original Track",
        "Arabic" to "العربية (Arabic Dub)",
        "English" to "English Dub"
    )
    TvChoiceRow(
        title = stringResource(R.string.play_audio_lang),
        subtitle = stringResource(R.string.play_audio_lang_desc),
        selected = playbackSettings.preferredAudioLanguage,
        options = audioTracks,
        onSelect = { track ->
            settingsManager.updatePlaybackSettings(playbackSettings.copy(preferredAudioLanguage = track))
        }
    )

    Spacer(modifier = Modifier.height(10.dp))

    val bufferProfiles = listOf(
        "Low Latency" to "Low Latency (Fast start)",
        "Balanced" to "Balanced (Default)",
        "High Buffer" to "High Buffer (Slow Wi-Fi)"
    )
    TvChoiceRow(
        title = stringResource(R.string.play_buffer_size),
        subtitle = stringResource(R.string.play_buffer_size_desc),
        selected = playbackSettings.bufferProfile,
        options = bufferProfiles,
        onSelect = { bp ->
            settingsManager.updatePlaybackSettings(playbackSettings.copy(bufferProfile = bp))
        }
    )
}

// -------------------------------------------------------------
// 5. Subtitles Settings Pane
// -------------------------------------------------------------
@Composable
fun SubtitlesPane(settingsManager: SettingsManager) {
    val subSettings by settingsManager.subtitleSettings.collectAsState()

    Text(
        text = stringResource(R.string.settings_cat_subtitles),
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    TvSettingsToggleRow(
        title = stringResource(R.string.sub_enable),
        subtitle = stringResource(R.string.sub_enable_desc),
        checked = subSettings.subtitlesEnabled,
        onCheckedChange = { checked ->
            settingsManager.updateSubtitleSettings(subSettings.copy(subtitlesEnabled = checked))
        }
    )

    if (subSettings.subtitlesEnabled) {
        Spacer(modifier = Modifier.height(10.dp))

        val subLangs = listOf(
            "ar" to "العربية (Arabic)",
            "en" to "English",
            "auto" to "Auto-Detect"
        )
        TvChoiceRow(
            title = stringResource(R.string.sub_language),
            subtitle = "Preferred subtitle stream language",
            selected = subSettings.preferredLanguage,
            options = subLangs,
            onSelect = { l ->
                settingsManager.updateSubtitleSettings(subSettings.copy(preferredLanguage = l))
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        val fontSizes = listOf(
            "Small" to "Small (14sp)",
            "Normal" to "Normal (18sp)",
            "Large" to "Large (22sp)",
            "Extra Large" to "Extra Large (26sp)"
        )
        TvChoiceRow(
            title = stringResource(R.string.sub_font_size),
            subtitle = "Text size on TV screen",
            selected = subSettings.fontSize,
            options = fontSizes,
            onSelect = { fs ->
                settingsManager.updateSubtitleSettings(subSettings.copy(fontSize = fs))
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        val bgOptions = listOf(
            "Transparent" to "Transparent",
            "Semi-Dark" to "Semi-Dark (50%)",
            "Solid Black" to "Solid Black"
        )
        TvChoiceRow(
            title = stringResource(R.string.sub_bg_opacity),
            subtitle = "Caption background style",
            selected = subSettings.backgroundOpacity,
            options = bgOptions,
            onSelect = { bg ->
                settingsManager.updateSubtitleSettings(subSettings.copy(backgroundOpacity = bg))
            }
        )
    }
}

// -------------------------------------------------------------
// 6. DeArrow Settings Pane
// -------------------------------------------------------------
@Composable
fun DeArrowPane(settingsManager: SettingsManager) {
    val dearrowSettings by settingsManager.dearrowSettings.collectAsState()

    Text(
        text = stringResource(R.string.settings_cat_dearrow),
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    TvSettingsToggleRow(
        title = stringResource(R.string.dearrow_enable),
        subtitle = stringResource(R.string.dearrow_enable_desc),
        checked = dearrowSettings.deArrowEnabled,
        onCheckedChange = { checked ->
            settingsManager.updateDeArrowSettings(dearrowSettings.copy(deArrowEnabled = checked))
        }
    )

    TvSettingsToggleRow(
        title = stringResource(R.string.dearrow_thumbnails),
        subtitle = stringResource(R.string.dearrow_thumbnails_desc),
        checked = dearrowSettings.replaceThumbnails,
        onCheckedChange = { checked ->
            settingsManager.updateDeArrowSettings(dearrowSettings.copy(replaceThumbnails = checked))
        }
    )
}

// -------------------------------------------------------------
// 7. Interface & Remote Settings Pane
// -------------------------------------------------------------
@Composable
fun InterfacePane(settingsManager: SettingsManager) {
    val ifaceSettings by settingsManager.interfaceSettings.collectAsState()

    Text(
        text = stringResource(R.string.settings_cat_interface),
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    val cardSizes = listOf(
        "Compact" to "Compact (Dense Grid)",
        "Standard" to "Standard (Balanced)",
        "Large" to "Large (Spacious)"
    )
    TvChoiceRow(
        title = stringResource(R.string.ui_card_size),
        subtitle = "Card dimensions for grid browsing",
        selected = ifaceSettings.cardSize,
        options = cardSizes,
        onSelect = { cs ->
            settingsManager.updateInterfaceSettings(ifaceSettings.copy(cardSize = cs))
        }
    )

    Spacer(modifier = Modifier.height(10.dp))

    val seekSteps = listOf(
        "5" to "5s",
        "10" to "10s",
        "15" to "15s",
        "30" to "30s"
    )
    TvChoiceRow(
        title = stringResource(R.string.ui_fast_seek),
        subtitle = "Jump duration on left/right remote click",
        selected = ifaceSettings.fastSeekSeconds.toString(),
        options = seekSteps,
        onSelect = { ss ->
            settingsManager.updateInterfaceSettings(ifaceSettings.copy(fastSeekSeconds = ss.toInt()))
        }
    )

    Spacer(modifier = Modifier.height(10.dp))

    TvSettingsToggleRow(
        title = stringResource(R.string.ui_show_clock),
        subtitle = "Display system clock in top header",
        checked = ifaceSettings.showClock,
        onCheckedChange = { checked ->
            settingsManager.updateInterfaceSettings(ifaceSettings.copy(showClock = checked))
        }
    )

    TvSettingsToggleRow(
        title = stringResource(R.string.ui_low_ram),
        subtitle = stringResource(R.string.ui_low_ram_desc),
        checked = ifaceSettings.lowRamMode,
        onCheckedChange = { checked ->
            settingsManager.updateInterfaceSettings(ifaceSettings.copy(lowRamMode = checked))
        }
    )

    Spacer(modifier = Modifier.height(10.dp))

    val sleepTimers = listOf(
        "0" to "Off",
        "15" to "15 min",
        "30" to "30 min",
        "60" to "60 min",
        "120" to "120 min"
    )
    TvChoiceRow(
        title = stringResource(R.string.ui_sleep_timer),
        subtitle = "Stop playback automatically when TV remote is untouched",
        selected = ifaceSettings.sleepTimerMinutes.toString(),
        options = sleepTimers,
        onSelect = { st ->
            settingsManager.updateInterfaceSettings(ifaceSettings.copy(sleepTimerMinutes = st.toInt()))
        }
    )
}

// -------------------------------------------------------------
// 8. Storage & Privacy Settings Pane
// -------------------------------------------------------------
@Composable
fun StoragePane(settingsManager: SettingsManager) {
    val generalSettings by settingsManager.generalSettings.collectAsState()
    var statusNotice by remember { mutableStateOf<String?>(null) }

    Text(
        text = stringResource(R.string.settings_cat_storage),
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    TvSettingsToggleRow(
        title = stringResource(R.string.storage_save_history),
        subtitle = "Store search history queries locally for fast recall",
        checked = generalSettings.saveSearchHistory,
        onCheckedChange = { checked ->
            settingsManager.updateGeneralSettings(generalSettings.copy(saveSearchHistory = checked))
        }
    )

    Spacer(modifier = Modifier.height(14.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TvActionButton(
            label = stringResource(R.string.storage_clear_history),
            isDestructive = false,
            onClick = {
                settingsManager.clearSearchHistory()
                statusNotice = "Search history cleared"
            }
        )

        TvActionButton(
            label = stringResource(R.string.storage_clear_cache),
            isDestructive = false,
            onClick = {
                settingsManager.clearCache()
                statusNotice = "Cache cleared"
            }
        )

        TvActionButton(
            label = stringResource(R.string.storage_reset_all),
            isDestructive = true,
            onClick = {
                settingsManager.resetAllSettings()
                statusNotice = "All settings reset to default"
            }
        )
    }

    statusNotice?.let { notice ->
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = notice,
            color = SponsorGreen,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// -------------------------------------------------------------
// 9. About & Diagnostics Pane
// -------------------------------------------------------------
@Composable
fun AboutPane() {
    Text(
        text = stringResource(R.string.settings_cat_about),
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF141218))
                    .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Android TV Native Client • Kotlin + Jetpack Compose",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.about_app_desc),
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        DiagnosticRow(label = stringResource(R.string.about_version), value = "v2.0.0 (TV Edition)")
        DiagnosticRow(label = stringResource(R.string.about_engine), value = stringResource(R.string.about_active))
        DiagnosticRow(label = stringResource(R.string.about_sb_status), value = stringResource(R.string.about_active))
        DiagnosticRow(label = "UI Architecture", value = "100% Jetpack Compose for TV")
        DiagnosticRow(label = "Navigation", value = "D-pad Remote Focused")
    }
}

@Composable
fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

// -------------------------------------------------------------
// Shared UI Components for TV Settings
// -------------------------------------------------------------
@Composable
fun TvSettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isFocused) Color.White else DarkSurfaceElevated)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = YouTubeRed,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isFocused) Color.Black else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = if (isFocused) Color(0xFF444444) else TextSecondary,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (checked) YouTubeRed else (if (isFocused) Color(0xFF888888) else Color(0xFF40404C))
                )
                .padding(2.dp),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

@Composable
fun TvChoiceRow(
    title: String,
    subtitle: String,
    selected: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceElevated)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = subtitle,
            color = TextSecondary,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { (key, label) ->
                val isSelected = key == selected
                val interactionSource = remember { MutableInteractionSource() }
                val isFocused by interactionSource.collectIsFocusedAsState()

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                isFocused -> Color.White
                                isSelected -> YouTubeRed
                                else -> DarkSurface
                            }
                        )
                        .border(
                            width = if (isFocused) 2.dp else 0.dp,
                            color = YouTubeRed,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onSelect(key) }
                        )
                        .focusable(interactionSource = interactionSource)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isFocused) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun TvActionButton(
    label: String,
    isDestructive: Boolean = false,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isFocused -> Color.White
                    isPrimary -> YouTubeRed
                    isDestructive -> Color(0xFF5A1C1C)
                    else -> Color(0xFF2A2A38)
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
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = when {
                isFocused -> Color.Black
                isDestructive -> Color(0xFFFF8888)
                else -> Color.White
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
