package com.project.destronix.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Violet200,
    onPrimary = Violet900,
    primaryContainer = Violet900,
    onPrimaryContainer = Violet50,
    secondary = Cyan200,
    onSecondary = Color(0xFF00343F),
    secondaryContainer = Color(0xFF084B59),
    onSecondaryContainer = Cyan200,
    tertiary = Coral,
    onTertiary = Color(0xFF3B1508),
    background = NightBackground,
    onBackground = NightOnSurface,
    surface = NightSurface,
    onSurface = NightOnSurface,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = NightOnSurfaceVariant,
    outline = Color(0xFF414A5E),
    error = ErrorRed,
    onError = Color(0xFF3B0000),
)

private val LightColorScheme = lightColorScheme(
    primary = Violet600,
    onPrimary = Color.White,
    primaryContainer = Violet50,
    onPrimaryContainer = Violet900,
    secondary = Cyan700,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC4EFF8),
    onSecondaryContainer = Color(0xFF00343F),
    tertiary = Color(0xFFB23E17),
    onTertiary = Color.White,
    background = Color(0xFFF7F8FC),
    onBackground = Color(0xFF171A21),
    surface = Color.White,
    onSurface = Color(0xFF171A21),
    surfaceVariant = Color(0xFFE9ECF4),
    onSurfaceVariant = Color(0xFF555E73),
    error = Color(0xFFB3261E),
    onError = Color.White,
)

@Composable
fun DestronixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
