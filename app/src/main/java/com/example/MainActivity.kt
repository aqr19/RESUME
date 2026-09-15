package com.example

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.GoogleAccountManager
import com.example.data.SettingsManager
import com.example.data.TvLibraryRepository
import com.example.data.YouTubeRepository
import com.example.model.VideoItem
import com.example.navigation.TvRoutes
import com.example.ui.components.TvNavigationRail
import com.example.ui.home.TvHomeScreen
import com.example.ui.library.TvLibraryScreen
import com.example.ui.player.TvPlayerScreen
import com.example.ui.search.TvSearchScreen
import com.example.ui.settings.TvSettingsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TvYouTubeTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = YouTubeRepository()
        val settingsManager = SettingsManager(applicationContext)
        val accountManager = GoogleAccountManager(applicationContext)
        val libraryRepository = TvLibraryRepository(applicationContext)

        setContent {
            val generalSettings by settingsManager.generalSettings.collectAsState()
            val isArabic = generalSettings.appLanguage == "ar"

            // Dynamic locale and layout direction synchronization
            val locale = if (isArabic) Locale("ar") else Locale("en")
            val config = Configuration(LocalConfiguration.current).apply {
                setLocale(locale)
                setLayoutDirection(locale)
            }
            val localizedContext = LocalContext.current.createConfigurationContext(config)

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides config,
                LocalLayoutDirection provides (if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr)
            ) {
                TvYouTubeTheme {
                    TvYouTubeMainApp(
                        repository = repository,
                        settingsManager = settingsManager,
                        accountManager = accountManager,
                        libraryRepository = libraryRepository,
                        isArabic = isArabic
                    )
                }
            }
        }
    }
}

/**
 * Single-Activity TV YouTube Application powered by rememberNavController and NavHost.
 * Seamlessly manages transitions between Home, Search, Settings, and Player screens.
 */
@Composable
fun TvYouTubeMainApp(
    repository: YouTubeRepository,
    settingsManager: SettingsManager,
    accountManager: GoogleAccountManager,
    libraryRepository: TvLibraryRepository,
    isArabic: Boolean
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check if the user is currently on the video playback screen
    val isPlayerScreen = currentRoute?.startsWith("player") == true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // TV Side Navigation Rail (Hidden when on Player screen for immersive playback)
            if (!isPlayerScreen) {
                TvNavigationRail(
                    currentRoute = currentRoute,
                    onScreenSelected = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    accountManager = accountManager
                )
            }

            // Single-Activity Navigation Host
            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = TvRoutes.home("Home"),
                    enterTransition = { fadeIn(animationSpec = tween(250)) },
                    exitTransition = { fadeOut(animationSpec = tween(200)) },
                    popEnterTransition = { fadeIn(animationSpec = tween(250)) },
                    popExitTransition = { fadeOut(animationSpec = tween(200)) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Home Screen (Supports categories: Home, Trending, Music, Gaming, News)
                    composable(
                        route = TvRoutes.HOME,
                        arguments = listOf(
                            navArgument("category") {
                                type = NavType.StringType
                                defaultValue = "Home"
                            }
                        )
                    ) { backStackEntry ->
                        val category = backStackEntry.arguments?.getString("category") ?: "Home"
                        TvHomeScreen(
                            categoryTitle = category,
                            onPlayVideo = { video ->
                                navController.navigate(TvRoutes.player(video))
                            },
                            repository = repository
                        )
                    }

                    // Search Screen
                    composable(route = TvRoutes.SEARCH) {
                        TvSearchScreen(
                            onPlayVideo = { video ->
                                navController.navigate(TvRoutes.player(video))
                            },
                            repository = repository,
                            isArabic = isArabic
                        )
                    }

                    // Library Screen (Watch History, Favorites, Subscriptions)
                    composable(route = TvRoutes.LIBRARY) {
                        TvLibraryScreen(
                            libraryRepository = libraryRepository,
                            onPlayVideo = { video ->
                                navController.navigate(TvRoutes.player(video))
                            }
                        )
                    }

                    // Settings Screen
                    composable(route = TvRoutes.SETTINGS) {
                        TvSettingsScreen(
                            settingsManager = settingsManager,
                            accountManager = accountManager
                        )
                    }

                    // Video Player Screen
                    composable(
                        route = TvRoutes.PLAYER,
                        arguments = listOf(
                            navArgument("videoId") { type = NavType.StringType },
                            navArgument("title") {
                                type = NavType.StringType
                                defaultValue = ""
                            },
                            navArgument("author") {
                                type = NavType.StringType
                                defaultValue = ""
                            },
                            navArgument("durationMs") {
                                type = NavType.LongType
                                defaultValue = 0L
                            },
                            navArgument("thumb") {
                                type = NavType.StringType
                                defaultValue = ""
                            }
                        )
                    ) { backStackEntry ->
                        val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
                        val title = Uri.decode(backStackEntry.arguments?.getString("title") ?: "")
                        val author = Uri.decode(backStackEntry.arguments?.getString("author") ?: "")
                        val durationMs = backStackEntry.arguments?.getLong("durationMs") ?: 0L
                        val thumb = Uri.decode(backStackEntry.arguments?.getString("thumb") ?: "")

                        val video = VideoItem(
                            id = videoId,
                            title = title.ifEmpty { "Video Playback" },
                            author = author,
                            durationMs = durationMs,
                            thumbnailUrl = thumb
                        )

                        TvPlayerScreen(
                            video = video,
                            repository = repository,
                            settingsManager = settingsManager,
                            libraryRepository = libraryRepository,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
