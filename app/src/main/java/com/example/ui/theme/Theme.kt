package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AndeanStudioColorScheme = darkColorScheme(
    primary = AndeanGold,
    onPrimary = StudioBlack,
    primaryContainer = AndeanGoldDark,
    onPrimaryContainer = AndeanGoldLight,
    secondary = AndeanTurquoise,
    onSecondary = StudioBlack,
    secondaryContainer = AndeanIndigo,
    onSecondaryContainer = AndeanTurquoiseLight,
    tertiary = AndeanRed,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF4A1017),
    onTertiaryContainer = AndeanRedLight,
    background = StudioBlack,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = StudioBorder,
    outlineVariant = Color(0xFF282531),
    error = AndeanRed,
    onError = Color.White
)

@Composable
fun HuaynoStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AndeanStudioColorScheme,
        typography = Typography,
        content = content
    )
}
