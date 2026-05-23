package com.appylabs.nocontact.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
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

private val NoContactTokens = NoContactColorTokens(
    accent = NoContactRedDark,
    accentSoft = NoContactRedDarkContainer,
    urgentContainer = NoContactRedDarkContainer,
    success = Color(0xFF41D889),
    softIconContainer = NoContactDarkSurfaceHigh,
    mutedIcon = NoContactDarkMute,
    cardBorder = NoContactDarkHairline,
    navInactive = NoContactDarkMute
)

private val OnboardingTokens = NoContactOnboardingColorTokens(
    background = NoContactDarkOnboardingBackground,
    content = Color.White,
    contentMuted = Color(0xFFCFC9BE),
    panel = NoContactDarkSurfaceLow,
    panelHigh = NoContactDarkSurfaceHigh,
    outline = NoContactDarkHairline
)

val LocalNoContactColors = staticCompositionLocalOf { NoContactTokens }
val LocalNoContactOnboardingColors = staticCompositionLocalOf { OnboardingTokens }

private val NoContactColorScheme = darkColorScheme(
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

private val NoContactShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(18.dp)
)

@Composable
fun NoContactTheme(
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current

    CompositionLocalProvider(
        LocalNoContactColors provides NoContactTokens,
        LocalNoContactOnboardingColors provides OnboardingTokens,
        LocalNoContactDimensions provides NoContactDimensions.forScreenWidth(configuration.screenWidthDp)
    ) {
        MaterialTheme(
            colorScheme = NoContactColorScheme,
            typography = Typography,
            shapes = NoContactShapes,
            content = content
        )
    }
}
