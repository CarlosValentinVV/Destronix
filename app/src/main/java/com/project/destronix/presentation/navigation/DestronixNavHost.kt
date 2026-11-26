package com.project.destronix.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.project.destronix.presentation.common.PlaceholderScreen
import com.project.destronix.presentation.detail.DetailScreen
import com.project.destronix.presentation.home.HomeScreen
import com.project.destronix.presentation.search.SearchScreen

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
            )
        }
        composable(DestronixDestination.SEARCH.route) {
            SearchScreen(
                initialGenreId = null,
                onAnimeClick = { navController.navigate(DestronixDestination.detailRoute(it)) },
            )
        }
        composable(DestronixDestination.LIBRARY.route) {
            PlaceholderScreen(title = "Biblioteca")
        }
        composable(DestronixDestination.SETTINGS.route) {
            PlaceholderScreen(title = "Ajustes")
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
            )
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
