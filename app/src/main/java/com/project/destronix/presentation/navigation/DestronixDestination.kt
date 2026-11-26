package com.project.destronix.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.project.destronix.R

enum class DestronixDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector? = null,
    val isTopLevel: Boolean = false,
) {
    HOME("home", R.string.nav_home, Icons.Rounded.Home, true),
    SEARCH("search", R.string.nav_search, Icons.Rounded.Search, true),
    LIBRARY("library", R.string.nav_library, Icons.Rounded.Bookmark, true),
    SETTINGS("settings", R.string.nav_settings, Icons.Rounded.Settings, true),
    DETAIL("anime/{animeId}", R.string.nav_home),
    SEARCH_GENRE("search_genre/{genreId}", R.string.nav_search),
    PLAYER("player/{animeId}", R.string.player_title);

    companion object {
        const val ARG_ANIME_ID = "animeId"
        const val ARG_GENRE_ID = "genreId"

        fun detailRoute(animeId: Int): String = "anime/$animeId"

        fun genreRoute(genreId: Int): String = "search_genre/$genreId"

        fun playerRoute(animeId: Int): String = "player/$animeId"

        val topLevel: List<DestronixDestination> = entries.filter { it.isTopLevel }
    }
}
