package com.qinmu.eyecare.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    onPrimary = Color.Black,
    primaryContainer = SpotifyDarkControl,
    onPrimaryContainer = SpotifyGreen,
    secondary = SpotifyGreenDark,
    onSecondary = Color.Black,
    tertiary = SpotifyOrange,
    background = SpotifyDarkBase,
    onBackground = SpotifyTextPrimary,
    surface = SpotifyDarkSurface,
    onSurface = SpotifyTextPrimary,
    surfaceVariant = SpotifyDarkControl,
    onSurfaceVariant = SpotifyTextSecondary,
    outline = SpotifyBorder,
    outlineVariant = SpotifyBorderLight,
    error = SpotifyRed,
    onError = Color.Black
)

@Composable
fun QinMuTheme(
    darkTheme: Boolean = true, // 强制统一使用 Spotify 暗亮黑精致系统主题
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

