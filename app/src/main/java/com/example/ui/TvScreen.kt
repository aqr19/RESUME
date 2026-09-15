package com.example.ui

import com.example.model.VideoItem
import com.example.navigation.TvRoutes

sealed class TvScreen(val route: String) {
    object Home : TvScreen(TvRoutes.home("Home"))
    object Trending : TvScreen(TvRoutes.home("Trending"))
    object Music : TvScreen(TvRoutes.home("Music"))
    object Gaming : TvScreen(TvRoutes.home("Gaming"))
    object News : TvScreen(TvRoutes.home("News"))
    object Library : TvScreen(TvRoutes.library())
    object Search : TvScreen(TvRoutes.search())
    object Settings : TvScreen(TvRoutes.settings())
    data class Player(val video: VideoItem) : TvScreen(TvRoutes.player(video))
}
