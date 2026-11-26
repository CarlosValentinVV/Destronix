package com.project.destronix.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.project.destronix.presentation.common.PlaceholderScreen
import com.project.destronix.presentation.home.HomeScreen

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
            PlaceholderScreen(title = "Buscar")
        }
        composable(DestronixDestination.LIBRARY.route) {
            PlaceholderScreen(title = "Biblioteca")
        }
        composable(DestronixDestination.SETTINGS.route) {
            PlaceholderScreen(title = "Ajustes")
        }
        composable(DestronixDestination.DETAIL.route) {
            PlaceholderScreen(title = "Detalle de anime")
        }
        composable(DestronixDestination.SEARCH_GENRE.route) {
            PlaceholderScreen(title = "Búsqueda por género")
        }
    }
}
