package com.qinmu.eyecare.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NeumorphicLightColorScheme = lightColorScheme(
    primary = AccentRoyalBlue,
    onPrimary = Color.White,
    primaryContainer = NeumorphicCardSurface,
    onPrimaryContainer = AccentRoyalBlue,
    secondary = AccentSoftSky,
    onSecondary = Color.White,
    tertiary = AccentWarmOrange,
    background = SkyBackgroundLight,
    onBackground = TextPrimaryDarkNavy,
    surface = NeumorphicSurface,
    onSurface = TextPrimaryDarkNavy,
    surfaceVariant = NeumorphicCardSurface,
    onSurfaceVariant = TextSecondaryBlue,
    outline = SpotifyBorder,
    outlineVariant = SpotifyBorderLight,
    error = AccentCoralRed,
    onError = Color.White
)

@Composable
fun QinMuTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NeumorphicLightColorScheme,
        typography = Typography,
        content = content
    )
}
