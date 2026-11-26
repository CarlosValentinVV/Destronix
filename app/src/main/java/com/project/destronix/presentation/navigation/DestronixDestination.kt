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
    DETAIL("anime/{animeId}", R.string.nav_home);

    companion object {
        const val ARG_ANIME_ID = "animeId"

        fun detailRoute(animeId: Int): String = "anime/$animeId"

        val topLevel: List<DestronixDestination> = entries.filter { it.isTopLevel }
    }
}
