package com.qinmu.eyecare.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Figma "Music Player Mobile App UI" Soft Neumorphism Light Blue Design Tokens
// ============================================================================

// Base Sky Blue Gradient & Background Palette
val SkyGradientStart = Color(0xFFB1D6EA)
val SkyGradientEnd = Color(0xFFD5EAF5)
val SkyBackgroundLight = Color(0xFFF0F8FA)

// Neumorphic Surface & Card Colors
val NeumorphicSurface = Color(0xFFE1F0F7)
val NeumorphicCardSurface = Color(0xFFE8F4FA)
val NeumorphicCardElevated = Color(0xFFEEF7FC)
val NeumorphicHighlight = Color(0xFFFFFFFF)
val NeumorphicShadowDark = Color(0xFF99BCCF)
val NeumorphicShadowDeep = Color(0x402368A4)
val NeumorphicInsetShadow = Color(0x3070ACD3)

// Typography & Text Colors (Deep Navy Contrast & Soft Accents)
val TextPrimaryDarkNavy = Color(0xFF1A365D)
val TextSecondaryBlue = Color(0xFF3B6285)
val TextMutedSky = Color(0xFF6FABD3)

// Accent Colors (Extracted from Figma UI)
val AccentRoyalBlue = Color(0xFF2368A4)
val AccentCoralRed = Color(0xFFFF3B30)
val AccentMintGreen = Color(0xFF34C759)
val AccentWarmOrange = Color(0xFFFF9500)
val AccentSoftSky = Color(0xFF70ACD3)

// ============================================================================
// Backwards Compatibility Mapping (Prevents any existing references from breaking)
// ============================================================================
val SpotifyGreen = AccentRoyalBlue
val SpotifyGreenDark = Color(0xFF1B5385)
val SpotifyDarkBase = SkyBackgroundLight
val SpotifyDarkSurface = NeumorphicSurface
val SpotifyDarkControl = NeumorphicCardSurface
val SpotifyDarkElevated = NeumorphicCardElevated
val SpotifyDarkCardHighlight = Color(0xFFD5EAF5)

val SpotifyTextPrimary = TextPrimaryDarkNavy
val SpotifyTextSecondary = TextSecondaryBlue
val SpotifyTextMuted = TextMutedSky

val SpotifyBorder = Color(0xFFB9DCED)
val SpotifyBorderLight = Color(0xFFCDEAFA)

val SpotifyRed = AccentCoralRed
val SpotifyOrange = AccentWarmOrange
val SpotifyBlue = AccentRoyalBlue

val GreenPrimary = AccentRoyalBlue
val GreenSecondary = SpotifyGreenDark
val GreenBackground = SkyBackgroundLight
val CardSurface = NeumorphicSurface
val WarmOrange = AccentWarmOrange
val AccentTeal = AccentRoyalBlue

val TextPrimary = TextPrimaryDarkNavy
val TextSecondary = TextSecondaryBlue
val TextSubtle = TextMutedSky
