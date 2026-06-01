package com.appylabs.nocontact.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Immutable
data class NoContactColorTokens(
    val accent: Color,
    val accentSoft: Color,
    val urgentContainer: Color,
    val success: Color,
    val softIconContainer: Color,
    val mutedIcon: Color,
    val cardBorder: Color,
    val navInactive: Color
)

@Immutable
data class NoContactOnboardingColorTokens(
    val background: Color,
    val content: Color,
    val contentMuted: Color,
    val panel: Color,
    val panelHigh: Color,
    val outline: Color
)

private val NoContactDarkTokens = NoContactColorTokens(
    accent = NoContactRedDark,
    accentSoft = NoContactRedDarkContainer,
    urgentContainer = NoContactRedDarkContainer,
    success = Color(0xFF41D889),
    softIconContainer = NoContactDarkSurfaceHigh,
    mutedIcon = NoContactDarkMute,
    cardBorder = NoContactDarkHairline,
    navInactive = NoContactDarkMute
)

private val NoContactLightTokens = NoContactColorTokens(
    accent = NoContactRed,
    accentSoft = NoContactRedSoft,
    urgentContainer = NoContactRedSoft,
    success = NoContactSuccess,
    softIconContainer = NoContactLightSurfaceHigh,  // Secondary BG #E5E5E0
    mutedIcon = NoContactLightMute,                  // Mute #62625B
    cardBorder = NoContactLightHairline,             // Hairline #DADAD3
    navInactive = NoContactLightMute                 // Mute #62625B
)

private val NoContactDarkOnboardingTokens = NoContactOnboardingColorTokens(
    background = NoContactDarkOnboardingBackground,
    content = Color.White,
    contentMuted = Color(0xFFCFC9BE),
    panel = NoContactDarkSurfaceLow,
    panelHigh = NoContactDarkSurfaceHigh,
    outline = NoContactDarkHairline
)

private val NoContactLightOnboardingTokens = NoContactOnboardingColorTokens(
    background = NoContactLightOnboardingBackground,  // Canvas #FFFFFF
    content = NoContactLightInk,                       // Ink Soft #211922
    contentMuted = NoContactLightMute,                 // Mute #62625B
    panel = NoContactLightSurfaceLow,                  // Soft Surface #FBFBF9
    panelHigh = NoContactLightSurfaceHigh,             // Secondary BG #E5E5E0
    outline = NoContactLightHairline                   // Hairline #DADAD3
)

val LocalNoContactColors = staticCompositionLocalOf { NoContactDarkTokens }
val LocalNoContactOnboardingColors = staticCompositionLocalOf { NoContactDarkOnboardingTokens }

private val NoContactDarkColorScheme = darkColorScheme(
    primary = NoContactRedDark,
    onPrimary = Color(0xFF4F000A),
    primaryContainer = NoContactRedDarkContainer,
    onPrimaryContainer = Color(0xFFFFD9DE),
    secondary = NoContactDarkInk,
    onSecondary = NoContactDarkBackground,
    secondaryContainer = NoContactDarkSurfaceHigh,
    onSecondaryContainer = NoContactDarkInk,
    tertiary = NoContactRedSoft,
    onTertiary = Color(0xFF4F000A),
    tertiaryContainer = Color(0xFF5B1A1F),
    onTertiaryContainer = Color(0xFFFFD9DE),
    background = NoContactDarkBackground,
    onBackground = NoContactDarkInk,
    surface = NoContactDarkBackground,
    onSurface = NoContactDarkInk,
    surfaceVariant = NoContactDarkSurface,
    onSurfaceVariant = NoContactDarkMute,
    surfaceContainerLowest = NoContactDarkSurfaceLowest,
    surfaceContainerLow = NoContactDarkSurfaceLow,
    surfaceContainer = NoContactDarkSurface,
    surfaceContainerHigh = NoContactDarkSurfaceHigh,
    surfaceContainerHighest = NoContactDarkSurfaceHighest,
    outline = Color(0xFF8E897F),
    outlineVariant = NoContactDarkHairline,
    error = NoContactRedDark,
    onError = Color.White,
    errorContainer = NoContactRedDarkContainer,
    onErrorContainer = Color(0xFFFFDADB),
    inverseSurface = NoContactDarkSurface,
    inverseOnSurface = Color.White,
    inversePrimary = NoContactRed,
    scrim = Color(0xFF000000)
)

private val NoContactLightColorScheme = lightColorScheme(
    primary = NoContactRed,                            // Pinterest Red #E60023
    onPrimary = Color.White,
    primaryContainer = NoContactRedSoft,               // Soft Red #FFE0E5
    onPrimaryContainer = Color(0xFF4F000A),
    secondary = NoContactLightBody,                    // Body #33332E — warm charcoal
    onSecondary = Color.White,
    secondaryContainer = NoContactLightSurfaceHigh,    // Secondary BG #E5E5E0
    onSecondaryContainer = NoContactLightInk,
    tertiary = NoContactRedPressed,                    // Red Pressed #CC001F
    onTertiary = Color.White,
    tertiaryContainer = NoContactRedSoft,
    onTertiaryContainer = Color(0xFF4F000A),
    background = NoContactLightBackground,             // Soft Surface #FBFBF9
    onBackground = NoContactLightInk,                  // Ink Soft #211922
    surface = NoContactLightBackground,                // Soft Surface #FBFBF9
    onSurface = NoContactLightInk,                     // Ink Soft #211922
    surfaceVariant = NoContactLightSurface,            // Surface Card #F6F6F3
    onSurfaceVariant = NoContactLightMute,             // Mute #62625B
    surfaceContainerLowest = NoContactLightSurfaceLowest,  // Canvas #FFFFFF
    surfaceContainerLow = NoContactLightSurfaceLow,    // Soft Surface #FBFBF9
    surfaceContainer = NoContactLightSurface,          // Surface Card #F6F6F3
    surfaceContainerHigh = NoContactLightSurfaceHigh,  // Secondary BG #E5E5E0
    surfaceContainerHighest = NoContactLightSurfaceHighest, // Hairline step #DADAD3
    outline = NoContactLightOutline,                   // Ash #91918C
    outlineVariant = NoContactLightHairline,           // Hairline #DADAD3
    error = NoContactRed,                              // Pinterest Red for destructive
    onError = Color.White,
    errorContainer = NoContactRedSoft,
    onErrorContainer = Color(0xFF4F000A),
    inverseSurface = Color(0xFF262622),                // Obsidian — SOS / snackbar surfaces
    inverseOnSurface = Color(0xFFF4F0EA),              // Dark ink on obsidian
    inversePrimary = NoContactRedDark,
    scrim = Color(0xFF000000)
)

private val NoContactShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun NoContactTheme(
    darkMode: Boolean? = null,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = darkMode ?: systemDark
    val configuration = LocalConfiguration.current
    val colorScheme = if (useDark) NoContactDarkColorScheme else NoContactLightColorScheme
    val tokens = if (useDark) NoContactDarkTokens else NoContactLightTokens
    val onboardingTokens = if (useDark) NoContactDarkOnboardingTokens else NoContactLightOnboardingTokens

    CompositionLocalProvider(
        LocalNoContactColors provides tokens,
        LocalNoContactOnboardingColors provides onboardingTokens,
        LocalNoContactDimensions provides NoContactDimensions.forScreenWidth(configuration.screenWidthDp)
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = NoContactShapes,
            content = content
        )
    }
}
