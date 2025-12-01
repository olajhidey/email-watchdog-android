package app.web.jiniyede.email_watchdog.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Navy80,
    onPrimary = Navy10,
    primaryContainer = Navy30,
    onPrimaryContainer = Navy90,
    secondary = Amber80,
    onSecondary = Amber10,
    secondaryContainer = Amber30,
    onSecondaryContainer = Amber90,
    tertiary = Amber80,
    onTertiary = Amber10,
    background = SurfaceDark,
    onBackground = Neutral90,
    surface = SurfaceDark,
    onSurface = Neutral90,
    surfaceVariant = SurfaceContainerDark,
    onSurfaceVariant = Neutral90,
    surfaceContainerLow = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    error = ErrorDark,
    onError = ErrorContainerDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = ErrorDark
)

private val LightColorScheme = lightColorScheme(
    primary = Navy30,
    onPrimary = Color.White,
    primaryContainer = Navy90,
    onPrimaryContainer = Navy10,
    secondary = Amber40,
    onSecondary = Color.White,
    secondaryContainer = Amber90,
    onSecondaryContainer = Amber10,
    tertiary = Amber40,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = Neutral10,
    surface = SurfaceLight,
    onSurface = Neutral10,
    surfaceVariant = SurfaceContainerLight,
    onSurfaceVariant = Neutral20,
    surfaceContainerLow = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ErrorLight
)

@Composable
fun EmailwatchdogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
