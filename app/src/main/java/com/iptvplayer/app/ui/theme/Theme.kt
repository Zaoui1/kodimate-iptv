package com.iptvplayer.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Kodimate Brand Palette ───────────────────────────────────────────────────

val KodimateBackground   = Color(0xFF0D0D0D)
val KodimateSurface      = Color(0xFF1A1A1A)
val KodimateCard         = Color(0xFF242424)
val KodimateCardHover    = Color(0xFF2E2E2E)
val KodimateAccent       = Color(0xFF0A84FF) // Electric Blue
val KodimateAccentDark   = Color(0xFF005CC5)
val KodimateAccentGlow   = Color(0x330A84FF)
val KodimateDivider      = Color(0xFF2A2A2A)

val TextPrimary          = Color(0xFFFFFFFF)
val TextSecondary        = Color(0xFFB0B0B0)
val TextTertiary         = Color(0xFF666666)
val TextOnAccent         = Color(0xFFFFFFFF)

val SuccessGreen         = Color(0xFF30D158)
val ErrorRed             = Color(0xFFFF453A)
val WarningOrange        = Color(0xFFFF9F0A)
val StarYellow           = Color(0xFFFFD60A)

private val KodimateColorScheme = darkColorScheme(
    primary          = KodimateAccent,
    onPrimary        = TextOnAccent,
    primaryContainer = KodimateAccentDark,
    secondary        = KodimateAccentDark,
    onSecondary      = TextOnAccent,
    background       = KodimateBackground,
    onBackground     = TextPrimary,
    surface          = KodimateSurface,
    onSurface        = TextPrimary,
    surfaceVariant   = KodimateCard,
    onSurfaceVariant = TextSecondary,
    error            = ErrorRed,
    onError          = TextOnAccent,
    outline          = KodimateDivider,
    outlineVariant   = Color(0xFF3A3A3A)
)

@Composable
fun KodimateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KodimateColorScheme,
        typography = KodimateTypography,
        content = content
    )
}

val KodimateTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 0.5.sp
    )
)
