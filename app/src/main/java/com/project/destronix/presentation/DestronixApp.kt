package com.project.destronix.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.project.destronix.R
import com.project.destronix.di.LocalAppContainer
import com.project.destronix.domain.model.ThemeMode
import com.project.destronix.domain.model.ThemeSettings
import com.project.destronix.presentation.navigation.DestronixNavHost
import com.project.destronix.presentation.navigation.DestronixDestination
import com.project.destronix.ui.theme.DestronixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestronixApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentTopLevel = DestronixDestination.topLevel.firstOrNull { it.route == currentRoute }
    val container = LocalAppContainer.current
    val settings by container.observeThemeSettings().collectAsStateWithLifecycle(
        initialValue = ThemeSettings(),
    )
    val factory = remember { DestronixViewModelFactory(container) }

    DestronixTheme(
        darkTheme = when (settings.mode) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        },
        dynamicColor = settings.dynamicColor,
    ) {
    CompositionLocalProvider(LocalViewModelFactory provides factory) {
        Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when {
                            currentTopLevel != null -> stringResource(currentTopLevel.labelRes)
                            currentRoute?.startsWith("anime/") == true -> stringResource(R.string.detail_title)
                            currentRoute?.startsWith("player/") == true -> stringResource(R.string.player_title)
                            else -> stringResource(R.string.app_name)
                        },
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    if (currentTopLevel == null && navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                DestronixDestination.topLevel.forEach { destination ->
                    val selected = destination.route == currentTopLevel?.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(destination.route) {
                                    popUpTo(DestronixDestination.HOME.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = requireNotNull(destination.icon),
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(destination.labelRes)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
            DestronixNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
    }
}
