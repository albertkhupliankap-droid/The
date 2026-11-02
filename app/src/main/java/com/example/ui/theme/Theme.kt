package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ZogamDarkColorScheme = darkColorScheme(
    primary = ZogamGold,
    onPrimary = Color.Black,
    primaryContainer = ZogamNavyCard,
    onPrimaryContainer = ZogamGold,
    secondary = ZogamRoyalBlue,
    onSecondary = Color.White,
    secondaryContainer = ZogamNavyBorder,
    onSecondaryContainer = ZogamCyanAccent,
    tertiary = ZogamCrimsonRed,
    onTertiary = Color.White,
    background = ZogamNavyDark,
    onBackground = TextPrimary,
    surface = ZogamNavySurface,
    onSurface = TextPrimary,
    surfaceVariant = ZogamNavyCard,
    onSurfaceVariant = TextSecondary,
    outline = ZogamNavyBorder
)

@Composable
fun ZogamMobileTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ZogamDarkColorScheme,
        typography = Typography,
        content = content
    )
}
