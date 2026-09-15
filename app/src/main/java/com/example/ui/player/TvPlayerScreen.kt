package com.example.ui.player

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.R
import com.example.data.SettingsManager
import com.example.data.TvLibraryRepository
import com.example.data.YouTubeRepository
import com.example.model.VideoItem
import com.example.model.VideoPlaybackDetails
import com.example.model.VideoStreamFormat
import com.example.player.SponsorBlockController
import com.example.ui.theme.SponsorGreen
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * TV Player Screen built with Media3 (ExoPlayer & PlayerView), an auto-hiding transport bar,
 * and a simple progress indicator engineered for remote-controlled interaction.
 */
@OptIn(UnstableApi::class)
@Composable
fun TvPlayerScreen(
    video: VideoItem,
    repository: YouTubeRepository,
    settingsManager: SettingsManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    libraryRepository: TvLibraryRepository? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val libRepo = remember(context) { libraryRepository ?: TvLibraryRepository(context) }
    val sponsorSettings by settingsManager.sponsorSettings.collectAsState()
    val playbackSettings by settingsManager.playbackSettings.collectAsState()

    var currentVideoItem by remember(video.id) { mutableStateOf(video) }
    val isFavorite by libRepo.isFavorite(currentVideoItem.id).collectAsState(initial = false)
    val isSubscribed by libRepo.isSubscribed(currentVideoItem.author).collectAsState(initial = false)
    var videoDetails by remember { mutableStateOf<VideoPlaybackDetails?>(null) }
    var isLoadingDetails by remember { mutableStateOf(true) }
    var playerError by remember { mutableStateOf<String?>(null) }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(video.durationMs) }
    var bufferedPositionMs by remember { mutableLongStateOf(0L) }
    var playbackSpeed by remember { mutableFloatStateOf(playbackSettings.preferredSpeed) }

    var isControlsVisible by remember { mutableStateOf(true) }
    var controlsTimeoutKey by remember { mutableStateOf(0) }
    var seekFeedbackText by remember { mutableStateOf<String?>(null) }

    var isQualityDialogOpen by remember { mutableStateOf(false) }
    var isSpeedDialogOpen by remember { mutableStateOf(false) }
    var selectedFormatUrl by remember { mutableStateOf<String?>(null) }
    var isInfoDialogOpen by remember { mutableStateOf(false) }
    var isNuvioSettingsOpen by remember { mutableStateOf(false) }

    // Nuvio TV Features: Aspect ratio, audio boost, subtitles, sleep timer
    var currentAspectRatio by remember { mutableStateOf(TvAspectRatioOption.FIT) }
    var isSubtitlesEnabled by remember { mutableStateOf(false) }
    var subtitleLanguage by remember { mutableStateOf("ar") }
    var audioBoostLevel by remember { mutableFloatStateOf(1.0f) }
    var sleepTimerMinutes by remember { mutableIntStateOf(0) }

    val playButtonFocusRequester = remember { FocusRequester() }

    // SponsorBlock Controller
    val sponsorBlockController = remember {
        SponsorBlockController(getSettings = { sponsorSettings })
    }
    val skipEvent by sponsorBlockController.skipEvent.collectAsState()

    // Create Media3 ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            setPlaybackParameters(PlaybackParameters(playbackSpeed))
            trackSelectionParameters = trackSelectionParameters
                .buildUpon()
                .setPreferredVideoMimeType(MimeTypes.VIDEO_AV1)
                .build()
        }
    }

    // Connect Media3 Player listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        isLoadingDetails = false
                        val d = exoPlayer.duration
                        if (d > 0) durationMs = d
                    }
                    Player.STATE_BUFFERING -> {
                        bufferedPositionMs = exoPlayer.bufferedPosition
                    }
                    Player.STATE_ENDED -> {
                        isPlaying = false
                        isControlsVisible = true
                        val next = videoDetails?.nextVideo
                        if (playbackSettings.autoPlayNext && next != null) {
                            currentVideoItem = next
                        }
                    }
                    Player.STATE_IDLE -> {}
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                isLoadingDetails = false
                playerError = error.message ?: "Playback encountered an error"
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Load stream details & SponsorBlock segments
    LaunchedEffect(currentVideoItem.id) {
        isLoadingDetails = true
        playerError = null
        selectedFormatUrl = null
        currentPositionMs = 0L

        try {
            val details = repository.getVideoPlaybackDetails(currentVideoItem.id)
            videoDetails = details

            if (details.sponsorSegments.isNotEmpty()) {
                sponsorBlockController.loadSegments(details.sponsorSegments)
            } else {
                sponsorBlockController.loadSegments(emptyList())
            }

            // Select stream: Dash MPD Manifest (1080p/720p/480p), Dash URL, HLS, or direct format
            val mpdMediaItem = try {
                if (!details.dashManifestContent.isNullOrBlank()) {
                    val cacheFile = java.io.File(context.cacheDir, "dash_${details.videoId}.mpd")
                    cacheFile.writeText(details.dashManifestContent)
                    MediaItem.Builder()
                        .setUri(Uri.fromFile(cacheFile))
                        .setMimeType(MimeTypes.APPLICATION_MPD)
                        .build()
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            val mediaItem = mpdMediaItem ?: when {
                !details.dashManifestUrl.isNullOrBlank() -> {
                    MediaItem.Builder()
                        .setUri(Uri.parse(details.dashManifestUrl))
                        .setMimeType(MimeTypes.APPLICATION_MPD)
                        .build()
                }
                !details.hlsManifestUrl.isNullOrBlank() -> {
                    MediaItem.Builder()
                        .setUri(Uri.parse(details.hlsManifestUrl))
                        .setMimeType(MimeTypes.APPLICATION_M3U8)
                        .build()
                }
                details.directStreamUrls.isNotEmpty() -> {
                    val best = details.directStreamUrls.first()
                    val builder = MediaItem.Builder().setUri(Uri.parse(best.url))
                    if (!best.mimeType.isNullOrBlank()) {
                        builder.setMimeType(best.mimeType)
                    }
                    builder.build()
                }
                else -> null
            }

            if (mediaItem != null) {
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                val savedPosition = libRepo.getSavedPosition(currentVideoItem.id)
                if (savedPosition > 5000L && (currentVideoItem.durationMs <= 0 || savedPosition < (currentVideoItem.durationMs - 15000L))) {
                    exoPlayer.seekTo(savedPosition)
                }
                exoPlayer.play()
            } else {
                playerError = context.getString(R.string.player_error_default)
                isLoadingDetails = false
            }
        } catch (e: Exception) {
            playerError = e.localizedMessage ?: "Failed to load stream details"
            isLoadingDetails = false
        }
    }

    // Periodic watch history record (every 5 seconds while playing)
    LaunchedEffect(currentVideoItem.id, isPlaying) {
        while (isActive && isPlaying) {
            delay(5000)
            val pos = exoPlayer.currentPosition
            if (pos > 1000L) {
                libRepo.recordPlayback(currentVideoItem, pos)
            }
        }
    }

    // Record watch history on exit or video change
    DisposableEffect(currentVideoItem.id) {
        onDispose {
            val pos = exoPlayer.currentPosition
            if (pos > 1000L) {
                coroutineScope.launch {
                    libRepo.recordPlayback(currentVideoItem, pos)
                }
            }
        }
    }

    // Periodic position update & SponsorBlock skip ticker
    LaunchedEffect(exoPlayer) {
        while (isActive) {
            if (exoPlayer.isPlaying) {
                val pos = exoPlayer.currentPosition
                currentPositionMs = pos
                bufferedPositionMs = exoPlayer.bufferedPosition
                val d = exoPlayer.duration
                if (d > 0) durationMs = d

                // Check SponsorBlock
                val skipTo = sponsorBlockController.checkSkip(pos)
                if (skipTo != null) {
                    exoPlayer.seekTo(skipTo)
                }
            }
            delay(250)
        }
    }

    // Auto-dismiss skip notification toast after 3 seconds
    LaunchedEffect(skipEvent) {
        if (skipEvent != null) {
            delay(3000)
            sponsorBlockController.clearEvent()
        }
    }

    // Auto-clear seek feedback HUD after 1.2s
    LaunchedEffect(seekFeedbackText, controlsTimeoutKey) {
        if (seekFeedbackText != null) {
            delay(1200)
            seekFeedbackText = null
        }
    }

    // Auto-hide transport controls overlay after 4 seconds of inactivity while playing
    LaunchedEffect(isControlsVisible, controlsTimeoutKey, isPlaying) {
        if (isControlsVisible && isPlaying) {
            delay(4000)
            isControlsVisible = false
        }
    }

    // Auto-focus primary play/pause button when controls appear
    LaunchedEffect(isControlsVisible) {
        if (isControlsVisible) {
            try {
                playButtonFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    // Sleep timer auto-pause countdown
    LaunchedEffect(sleepTimerMinutes) {
        if (sleepTimerMinutes > 0) {
            val millis = sleepTimerMinutes * 60 * 1000L
            delay(millis)
            exoPlayer.pause()
            isControlsVisible = true
            sleepTimerMinutes = 0
        }
    }

    // Audio volume booster
    LaunchedEffect(audioBoostLevel) {
        exoPlayer.volume = audioBoostLevel
    }

    // Remote Back Handler
    BackHandler {
        when {
            isNuvioSettingsOpen -> isNuvioSettingsOpen = false
            isSpeedDialogOpen -> isSpeedDialogOpen = false
            isQualityDialogOpen -> isQualityDialogOpen = false
            isInfoDialogOpen -> isInfoDialogOpen = false
            isControlsVisible -> isControlsVisible = false
            else -> onBack()
        }
    }

    fun selectQualityFormat(format: VideoStreamFormat?) {
        selectedFormatUrl = format?.url
        try {
            if (format != null && format.width > 0 && format.height > 0) {
                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                    .buildUpon()
                    .setMaxVideoSize(format.width, format.height)
                    .setMinVideoSize(format.width, format.height)
                    .setPreferredVideoMimeType(MimeTypes.VIDEO_AV1)
                    .build()
            } else {
                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                    .buildUpon()
                    .clearVideoSizeConstraints()
                    .setPreferredVideoMimeType(MimeTypes.VIDEO_AV1)
                    .build()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // If playing direct stream URLs without DASH/HLS
        val hasManifest = !videoDetails?.dashManifestContent.isNullOrBlank() || !videoDetails?.dashManifestUrl.isNullOrBlank() || !videoDetails?.hlsManifestUrl.isNullOrBlank()
        if (!hasManifest) {
            val targetPos = exoPlayer.currentPosition
            val currentPlayWhenReady = exoPlayer.playWhenReady

            val newMediaItem = if (format != null) {
                val builder = MediaItem.Builder().setUri(Uri.parse(format.url))
                if (!format.mimeType.isNullOrBlank()) {
                    builder.setMimeType(format.mimeType)
                }
                builder.build()
            } else {
                videoDetails?.directStreamUrls?.firstOrNull()?.let {
                    MediaItem.Builder().setUri(Uri.parse(it.url)).build()
                }
            }

            if (newMediaItem != null) {
                exoPlayer.setMediaItem(newMediaItem)
                exoPlayer.prepare()
                if (targetPos > 0) {
                    exoPlayer.seekTo(targetPos)
                }
                exoPlayer.playWhenReady = currentPlayWhenReady
            }
        }
        controlsTimeoutKey++
    }

    fun showControls() {
        isControlsVisible = true
        controlsTimeoutKey++
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
        controlsTimeoutKey++
    }

    fun seekRelative(deltaMs: Long) {
        val current = exoPlayer.currentPosition
        val target = (current + deltaMs).coerceIn(0L, durationMs.coerceAtLeast(1L))
        exoPlayer.seekTo(target)
        currentPositionMs = target
        seekFeedbackText = if (deltaMs > 0) "+${deltaMs / 1000}s" else "${deltaMs / 1000}s"
        controlsTimeoutKey++
    }

    fun setPlaybackSpeed(newSpeed: Float) {
        playbackSpeed = newSpeed
        exoPlayer.setPlaybackParameters(PlaybackParameters(newSpeed))
        settingsManager.updatePlaybackSettings(
            settingsManager.playbackSettings.value.copy(preferredSpeed = newSpeed)
        )
        controlsTimeoutKey++
    }

    fun cyclePlaybackSpeed() {
        val speeds = PLAYBACK_SPEED_OPTIONS
        val nextIdx = (speeds.indexOf(playbackSpeed) + 1) % speeds.size
        val newSpeed = speeds[nextIdx]
        setPlaybackSpeed(newSpeed)
    }

    fun cycleAspectRatio() {
        val options = TvAspectRatioOption.values()
        val nextIdx = (options.indexOf(currentAspectRatio) + 1) % options.size
        currentAspectRatio = options[nextIdx]
    }

    val isAnyModalOpen = isNuvioSettingsOpen || isSpeedDialogOpen || isQualityDialogOpen || isInfoDialogOpen

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                            if (!isControlsVisible && !isAnyModalOpen) {
                                togglePlayPause()
                                showControls()
                                true
                            } else {
                                controlsTimeoutKey++
                                false
                            }
                        }
                        Key.MediaPlayPause -> {
                            togglePlayPause()
                            showControls()
                            true
                        }
                        Key.MediaPlay -> {
                            exoPlayer.play()
                            showControls()
                            true
                        }
                        Key.MediaPause -> {
                            exoPlayer.pause()
                            showControls()
                            true
                        }
                        Key.DirectionLeft -> {
                            if (!isControlsVisible && !isAnyModalOpen) {
                                seekRelative(-10000L)
                                showControls()
                                true
                            } else {
                                // Allow D-pad focus movement between buttons/tabs
                                controlsTimeoutKey++
                                false
                            }
                        }
                        Key.MediaRewind -> {
                            seekRelative(-10000L)
                            showControls()
                            true
                        }
                        Key.DirectionRight -> {
                            if (!isControlsVisible && !isAnyModalOpen) {
                                seekRelative(10000L)
                                showControls()
                                true
                            } else {
                                // Allow D-pad focus movement between buttons/tabs
                                controlsTimeoutKey++
                                false
                            }
                        }
                        Key.MediaFastForward -> {
                            seekRelative(10000L)
                            showControls()
                            true
                        }
                        Key.DirectionUp, Key.DirectionDown -> {
                            if (!isControlsVisible && !isAnyModalOpen) {
                                showControls()
                                true
                            } else {
                                controlsTimeoutKey++
                                false
                            }
                        }
                        Key.Back, Key.Escape -> {
                            when {
                                isNuvioSettingsOpen -> {
                                    isNuvioSettingsOpen = false
                                    true
                                }
                                isSpeedDialogOpen -> {
                                    isSpeedDialogOpen = false
                                    true
                                }
                                isQualityDialogOpen -> {
                                    isQualityDialogOpen = false
                                    true
                                }
                                isInfoDialogOpen -> {
                                    isInfoDialogOpen = false
                                    true
                                }
                                isControlsVisible -> {
                                    isControlsVisible = false
                                    true
                                }
                                else -> {
                                    onBack()
                                    true
                                }
                            }
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { showControls() }
            )
            .testTag("tv_player_container")
    ) {
        // Media3 Video Surface (PlayerView) with dynamic Aspect Ratio Mode (NuvioTV style)
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = when (currentAspectRatio) {
                        TvAspectRatioOption.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        TvAspectRatioOption.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        TvAspectRatioOption.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        TvAspectRatioOption.FOUR_THREE -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    }
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { view ->
                view.player = exoPlayer
                view.resizeMode = when (currentAspectRatio) {
                    TvAspectRatioOption.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    TvAspectRatioOption.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    TvAspectRatioOption.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    TvAspectRatioOption.FOUR_THREE -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Remote Seek Feedback HUD (Pill overlay in center of screen)
        TvSeekFeedbackHud(
            seekDeltaText = seekFeedbackText,
            currentPositionMs = currentPositionMs,
            modifier = Modifier.align(Alignment.Center)
        )

        // Loading Spinner Overlay
        if (isLoadingDetails) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = YouTubeRed,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.player_loading),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Error message overlay
        if (playerError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xEE0B0B0E)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.player_error_title),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = playerError ?: stringResource(R.string.player_error_default),
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    TvPlayerButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        text = stringResource(R.string.player_go_back),
                        onClick = onBack,
                        testTag = "player_error_back_button"
                    )
                }
            }
        }

        // SponsorBlock Toast Notification
        AnimatedVisibility(
            visible = skipEvent != null && sponsorSettings.showSkipToast,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 28.dp, end = 32.dp)
        ) {
            skipEvent?.let { ev ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xDD0D0D11))
                        .border(1.5.dp, SponsorGreen, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "SponsorBlock",
                        tint = SponsorGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(
                            R.string.player_sponsorblock_skipped,
                            ev.category.replaceFirstChar { it.uppercase() },
                            ev.durationSeconds
                        ),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Auto-Hiding Transport Controls Overlay
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            TvPlayerTransportOverlay(
                video = currentVideoItem,
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                bufferedPositionMs = bufferedPositionMs,
                playbackSpeed = playbackSpeed,
                sponsorSegments = sponsorBlockController.currentSegments,
                isSponsorBlockActive = sponsorSettings.isEnabled,
                streamQualityLabel = if (selectedFormatUrl != null) {
                    videoDetails?.directStreamUrls?.firstOrNull { it.url == selectedFormatUrl }?.qualityLabel ?: "Custom"
                } else {
                    val top = videoDetails?.directStreamUrls?.firstOrNull()
                    when {
                        top == null -> "Auto"
                        top.height >= 2160 -> if (top.isAv1) "4K • AV1" else "4K"
                        top.height >= 1440 -> if (top.isAv1) "2K • AV1" else "2K"
                        top.qualityLabel != null -> if (top.isAv1) "${top.qualityLabel} • AV1" else top.qualityLabel
                        else -> "Auto"
                    }
                },
                currentAspectRatio = currentAspectRatio,
                hasNextVideo = videoDetails?.nextVideo != null,
                relatedVideos = videoDetails?.relatedVideos ?: emptyList(),
                isFavorite = isFavorite,
                onToggleFavorite = {
                    coroutineScope.launch {
                        libRepo.toggleFavorite(currentVideoItem)
                    }
                },
                playButtonFocusRequester = playButtonFocusRequester,
                onBack = onBack,
                onTogglePlayPause = { togglePlayPause() },
                onSeekRelative = { delta -> seekRelative(delta) },
                onSeekToPosition = { pos ->
                    val target = pos.coerceIn(0L, durationMs.coerceAtLeast(1L))
                    exoPlayer.seekTo(target)
                    currentPositionMs = target
                    controlsTimeoutKey++
                },
                onCycleSpeed = { cyclePlaybackSpeed() },
                onOpenSpeedDialog = { isSpeedDialogOpen = true },
                onOpenQualityDialog = { isQualityDialogOpen = true },
                onCycleAspectRatio = { cycleAspectRatio() },
                onOpenSettings = { isNuvioSettingsOpen = true },
                onOpenInfoDialog = { isInfoDialogOpen = true },
                onPlayNextVideo = {
                    videoDetails?.nextVideo?.let { next ->
                        currentVideoItem = next
                    }
                },
                onSelectRelatedVideo = { item ->
                    currentVideoItem = item
                },
                onResetHideTimer = { controlsTimeoutKey++ }
            )
        }

        // Playback Speed Selection Dialog (0.5x to 2.0x)
        TvPlaybackSpeedDialog(
            isOpen = isSpeedDialogOpen,
            currentSpeed = playbackSpeed,
            onSelectSpeed = { speed ->
                setPlaybackSpeed(speed)
            },
            onDismiss = { isSpeedDialogOpen = false }
        )

        // Stream Quality Selection Dialog
        TvQualitySelectionDialog(
            isOpen = isQualityDialogOpen,
            availableFormats = videoDetails?.directStreamUrls ?: emptyList(),
            selectedFormatUrl = selectedFormatUrl,
            onSelectFormat = { format ->
                selectQualityFormat(format)
            },
            onDismiss = { isQualityDialogOpen = false }
        )

        // Video Info & Full Details Dialog
        TvVideoInfoDialog(
            isOpen = isInfoDialogOpen,
            details = videoDetails,
            isSubscribed = isSubscribed,
            onToggleSubscribe = {
                coroutineScope.launch {
                    if (isSubscribed) {
                        libRepo.unsubscribe(currentVideoItem.author)
                    } else {
                        libRepo.toggleSubscription(
                            channelId = currentVideoItem.author,
                            channelTitle = currentVideoItem.author,
                            avatarUrl = videoDetails?.authorImageUrl ?: "",
                            subscriberCount = videoDetails?.subscriberCount ?: ""
                        )
                    }
                }
            },
            isFavorite = isFavorite,
            onToggleFavorite = {
                coroutineScope.launch {
                    libRepo.toggleFavorite(currentVideoItem)
                }
            },
            onDismiss = { isInfoDialogOpen = false }
        )

        // Comprehensive NuvioTV Settings Drawer
        TvNuvioSettingsDrawer(
            isOpen = isNuvioSettingsOpen,
            onDismiss = { isNuvioSettingsOpen = false },
            availableFormats = videoDetails?.directStreamUrls ?: emptyList(),
            selectedFormatUrl = selectedFormatUrl,
            onSelectFormat = { format ->
                selectQualityFormat(format)
            },
            currentAspectRatio = currentAspectRatio,
            onSelectAspectRatio = { aspect ->
                currentAspectRatio = aspect
            },
            currentSpeed = playbackSpeed,
            onSelectSpeed = { speed ->
                setPlaybackSpeed(speed)
            },
            isSubtitlesEnabled = isSubtitlesEnabled,
            onToggleSubtitles = { enabled ->
                isSubtitlesEnabled = enabled
            },
            subtitleLanguage = subtitleLanguage,
            onSelectSubtitleLanguage = { lang ->
                subtitleLanguage = lang
            },
            audioBoostLevel = audioBoostLevel,
            onSelectAudioBoost = { boost ->
                audioBoostLevel = boost
            },
            sleepTimerMinutes = sleepTimerMinutes,
            onSelectSleepTimer = { minutes ->
                sleepTimerMinutes = minutes
            },
            isSponsorBlockEnabled = sponsorSettings.isEnabled,
            onToggleSponsorBlock = { enabled ->
                settingsManager.updateSponsorSettings(sponsorSettings.copy(isEnabled = enabled))
            },
            video = currentVideoItem
        )
    }
}
