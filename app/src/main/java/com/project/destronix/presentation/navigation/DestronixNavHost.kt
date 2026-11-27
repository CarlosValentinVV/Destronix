package com.project.destronix.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.project.destronix.presentation.detail.DetailScreen
import com.project.destronix.presentation.home.HomeScreen
import com.project.destronix.presentation.library.LibraryScreen
import com.project.destronix.presentation.player.PlayerScreen
import com.project.destronix.presentation.search.SearchScreen
import com.project.destronix.presentation.settings.SettingsScreen

@Composable
fun DestronixNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = DestronixDestination.HOME.route,
        modifier = modifier,
    ) {
        composable(DestronixDestination.HOME.route) {
            HomeScreen(
                onAnimeClick = { navController.navigate(DestronixDestination.detailRoute(it)) },
                onGenreClick = { navController.navigate(DestronixDestination.genreRoute(it)) },
                onSearchClick = { navController.navigate(DestronixDestination.SEARCH.route) },
                onPlayClick = { navController.navigate(DestronixDestination.playerRoute(it)) },
            )
        }
        composable(DestronixDestination.SEARCH.route) {
            SearchScreen(
                initialGenreId = null,
                onAnimeClick = { navController.navigate(DestronixDestination.detailRoute(it)) },
            )
        }
        composable(DestronixDestination.LIBRARY.route) {
            LibraryScreen(
                onAnimeClick = { navController.navigate(DestronixDestination.detailRoute(it)) },
            )
        }
        composable(DestronixDestination.SETTINGS.route) {
            SettingsScreen()
        }
        composable(
            route = DestronixDestination.DETAIL.route,
            arguments = listOf(
                navArgument(DestronixDestination.ARG_ANIME_ID) { type = NavType.IntType },
            ),
        ) { entry ->
            DetailScreen(
                animeId = entry.arguments?.getInt(DestronixDestination.ARG_ANIME_ID) ?: -1,
                onAnimeClick = { navController.navigate(DestronixDestination.detailRoute(it)) },
                onPlayClick = { navController.navigate(DestronixDestination.playerRoute(it)) },
            )
        }
        composable(
            route = DestronixDestination.PLAYER.route,
            arguments = listOf(
                navArgument(DestronixDestination.ARG_ANIME_ID) { type = NavType.IntType },
            ),
        ) { entry ->
            PlayerScreen(animeId = entry.arguments?.getInt(DestronixDestination.ARG_ANIME_ID) ?: -1)
        }
        composable(
            route = DestronixDestination.SEARCH_GENRE.route,
            arguments = listOf(
                navArgument(DestronixDestination.ARG_GENRE_ID) { type = NavType.IntType },
            ),
        ) { entry ->
            SearchScreen(
                initialGenreId = entry.arguments?.getInt(DestronixDestination.ARG_GENRE_ID),
                onAnimeClick = { navController.navigate(DestronixDestination.detailRoute(it)) },
            )
        }
    }
}
