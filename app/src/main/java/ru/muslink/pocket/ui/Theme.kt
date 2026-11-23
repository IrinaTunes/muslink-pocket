package ru.muslink.pocket.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4E5B7B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE1F9),
    onPrimaryContainer = Color(0xFF081B3D),
    secondary = Color(0xFF7A6A9B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEDE3FF),
    onSecondaryContainer = Color(0xFF26163F),
    tertiary = Color(0xFF9C5B3F),
    tertiaryContainer = Color(0xFFFFDBCB),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFF1EFF7),
    onSurfaceVariant = Color(0xFF47464F),
    outline = Color(0xFF787680),
    error = Color(0xFFB3261E)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBCC4E6),
    onPrimary = Color(0xFF1E2C4A),
    primaryContainer = Color(0xFF354462),
    onPrimaryContainer = Color(0xFFDCE1F9),
    secondary = Color(0xFFD4C2F5),
    background = Color(0xFF131318),
    onBackground = Color(0xFFE5E1E9),
    surface = Color(0xFF131318),
    onSurface = Color(0xFFE5E1E9),
    surfaceVariant = Color(0xFF2A2A31),
    onSurfaceVariant = Color(0xFFC9C5D0),
    error = Color(0xFFF2B8B5)
)

@Composable
fun MuslinkTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
