package com.example.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

private val TvDarkColorScheme = darkColorScheme(
    primary = YouTubeRed,
    onPrimary = Color.White,
    primaryContainer = YouTubeRedDark,
    onPrimaryContainer = Color.White,
    secondary = NuvioViolet,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary
)

@Composable
fun TvYouTubeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TvDarkColorScheme,
        typography = Typography
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides TextStyle(
                fontFamily = IbmPlexSansArabic,
                color = TextPrimary
            )
        ) {
            content()
        }
    }
}

