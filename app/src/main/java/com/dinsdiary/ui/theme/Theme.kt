package com.dinsdiary.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
private val LightScheme = lightColorScheme(
    primary = DinsPrimaryDark,
    onPrimary = DinsSurface,
    primaryContainer = DinsAccent,
    onPrimaryContainer = DinsTextPrimary,
    secondary = DinsSecondary,
    tertiary = DinsPrimary,
    background = DinsBackground,
    onBackground = DinsTextPrimary,
    surface = DinsSurface,
    onSurface = DinsTextPrimary,
    onSurfaceVariant = DinsTextSecondary,
)

private val DarkScheme = darkColorScheme(
    primary = DinsPrimary,
    onPrimary = DinsTextPrimary,
    primaryContainer = DinsPrimaryDark,
    secondary = DinsSecondary,
    tertiary = DinsAccent,
    background = DinsBackgroundDark,
    onBackground = DinsTextPrimaryDark,
    surface = DinsSurfaceDark,
    onSurface = DinsTextPrimaryDark,
    onSurfaceVariant = DinsTextSecondaryDark,
)

@Composable
fun DinsDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled: PRD brand palette must stay recognizable.
    content: @Composable () -> Unit,
) {
    // Edge-to-edge bars are transparent: icon contrast must follow the app
    // theme (user can override system), not the system theme.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            content = content,
        )
    }
}
