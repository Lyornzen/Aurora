package com.aurora.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class AuroraColors(
    val accent: Color,
    val accentContainer: Color,
    val onAccentContainer: Color,
    val cardBg: Color,
    val cardBgElevated: Color,
    val inputBg: Color,
    val chipBg: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val iconBg: Color,
)

val LocalAuroraColors = staticCompositionLocalOf {
    AuroraColors(
        accent = Primary,
        accentContainer = PrimaryContainer,
        onAccentContainer = OnPrimaryContainer,
        cardBg = Surface,
        cardBgElevated = Surface,
        inputBg = SurfaceVariant,
        chipBg = SurfaceVariant,
        divider = SurfaceVariant,
        textPrimary = Color(0xFF1D1B20),
        textSecondary = OnSurfaceVariant,
        iconBg = SurfaceVariant,
    )
}

private fun lightAuroraColors(accent: Color) = AuroraColors(
    accent = accent,
    accentContainer = accent.copy(alpha = 0.12f),
    onAccentContainer = accent,
    cardBg = Color(0xFFFFFBFE),
    cardBgElevated = Color(0xFFFFFFFF),
    inputBg = Color(0xFFF3EDF7),
    chipBg = Color(0xFFF3EDF7),
    divider = Color(0xFFE7E0EC),
    textPrimary = Color(0xFF1D1B20),
    textSecondary = Color(0xFF49454F),
    iconBg = Color(0xFFF3EDF7),
)

private fun darkAuroraColors(accent: Color) = AuroraColors(
    accent = accent,
    accentContainer = accent.copy(alpha = 0.2f),
    onAccentContainer = accent,
    cardBg = Color(0xFF1D1B20),
    cardBgElevated = Color(0xFF2B2930),
    inputBg = Color(0xFF36343B),
    chipBg = Color(0xFF36343B),
    divider = Color(0xFF49454F),
    textPrimary = Color(0xFFE6E1E5),
    textSecondary = Color(0xFFCAC4D0),
    iconBg = Color(0xFF36343B),
)

private fun lightScheme(accent: Color) = lightColorScheme(
    primary = accent,
    onPrimary = Color.White,
    primaryContainer = accent.copy(alpha = 0.12f),
    onPrimaryContainer = accent,
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFEF7FF),
    onBackground = Color(0xFF1D1B20),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF7F2FA),
    surfaceContainer = Color(0xFFF3EDF7),
    surfaceContainerHigh = Color(0xFFECE6F0),
    surfaceContainerHighest = Color(0xFFE6E0EC),
)

private fun darkScheme(accent: Color) = darkColorScheme(
    primary = accent,
    onPrimary = Color(0xFF1D1B20),
    primaryContainer = accent.copy(alpha = 0.2f),
    onPrimaryContainer = accent,
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF141218),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    surfaceContainerLowest = Color(0xFF0F0D13),
    surfaceContainerLow = Color(0xFF1D1B20),
    surfaceContainer = Color(0xFF211F26),
    surfaceContainerHigh = Color(0xFF2B2930),
    surfaceContainerHighest = Color(0xFF36343B),
)

@Composable
fun AuroraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: Color = Primary,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) darkScheme(accentColor) else lightScheme(accentColor)
    val auroraColors = if (darkTheme) darkAuroraColors(accentColor) else lightAuroraColors(accentColor)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalAuroraColors provides auroraColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AuroraType,
            shapes = AuroraShapes,
            content = content,
        )
    }
}
