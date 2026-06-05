package com.aurora.ai.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat

/**
 * Aurora AI Theme
 * Based on Material Design 3 + Material You with Dynamic Color.
 */
@Composable
fun AuroraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    spacing: AuroraSpacing = AuroraSpacing(),
    radii: AuroraRadii = AuroraRadii(),
    sizes: AuroraSizes = AuroraSizes(),
    elevation: AuroraElevation = AuroraElevation(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> {
            val colors = AuroraDarkColors
            darkColorScheme(
                primary = colors.Primary,
                onPrimary = colors.OnPrimary,
                primaryContainer = colors.PrimaryContainer,
                onPrimaryContainer = colors.OnPrimaryContainer,
                secondary = colors.Secondary,
                onSecondary = colors.OnSecondary,
                secondaryContainer = colors.SecondaryContainer,
                onSecondaryContainer = colors.OnSecondaryContainer,
                tertiary = colors.Tertiary,
                onTertiary = colors.OnTertiary,
                tertiaryContainer = colors.TertiaryContainer,
                onTertiaryContainer = colors.OnTertiaryContainer,
                error = colors.Error,
                onError = colors.OnError,
                errorContainer = colors.ErrorContainer,
                background = colors.Background,
                onBackground = colors.OnBackground,
                surface = colors.Surface,
                onSurface = colors.OnSurface,
                surfaceVariant = colors.SurfaceVariant,
                onSurfaceVariant = colors.OnSurfaceVariant,
                surfaceContainerHigh = colors.SurfaceContainerHigh,
                surfaceContainerHighest = colors.SurfaceContainerHighest,
                outline = colors.Outline,
                outlineVariant = colors.OutlineVariant,
                inverseSurface = colors.InverseSurface,
                inverseOnSurface = colors.InverseOnSurface,
                inversePrimary = colors.InversePrimary,
            )
        }
        else -> {
            val colors = AuroraLightColors
            lightColorScheme(
                primary = colors.Primary,
                onPrimary = colors.OnPrimary,
                primaryContainer = colors.PrimaryContainer,
                onPrimaryContainer = colors.OnPrimaryContainer,
                secondary = colors.Secondary,
                onSecondary = colors.OnSecondary,
                secondaryContainer = colors.SecondaryContainer,
                onSecondaryContainer = colors.OnSecondaryContainer,
                tertiary = colors.Tertiary,
                onTertiary = colors.OnTertiary,
                tertiaryContainer = colors.TertiaryContainer,
                onTertiaryContainer = colors.OnTertiaryContainer,
                error = colors.Error,
                onError = colors.OnError,
                errorContainer = colors.ErrorContainer,
                background = colors.Background,
                onBackground = colors.OnBackground,
                surface = colors.Surface,
                onSurface = colors.OnSurface,
                surfaceVariant = colors.SurfaceVariant,
                onSurfaceVariant = colors.OnSurfaceVariant,
                surfaceContainerHigh = colors.SurfaceContainerHigh,
                surfaceContainerHighest = colors.SurfaceContainerHighest,
                outline = colors.Outline,
                outlineVariant = colors.OutlineVariant,
                inverseSurface = colors.InverseSurface,
                inverseOnSurface = colors.InverseOnSurface,
                inversePrimary = colors.InversePrimary,
            )
        }
    }

    // Status bar styling
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalAuroraSpacing provides spacing,
        LocalAuroraRadii provides radii,
        LocalAuroraSizes provides sizes,
        LocalAuroraElevation provides elevation,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AuroraTypography,
            shapes = AuroraShapes,
        ) {
            content()
        }
    }
}

private val AuroraShapes = Shapes(
    extraSmall = RoundedCornerShape(AuroraDp.dp4),
    small = RoundedCornerShape(AuroraDp.dp8),
    medium = RoundedCornerShape(AuroraDp.dp12),
    large = RoundedCornerShape(AuroraDp.dp20),
    extraLarge = RoundedCornerShape(AuroraDp.dp28),
)

private val AuroraTypography = Typography().run {
    val systemFont = FontFamily.Default
    copy(
        displayLarge = displayLarge.copy(fontFamily = systemFont),
        displayMedium = displayMedium.copy(fontFamily = systemFont),
        displaySmall = displaySmall.copy(fontFamily = systemFont),
        headlineLarge = headlineLarge.copy(fontFamily = systemFont),
        headlineMedium = headlineMedium.copy(fontFamily = systemFont),
        headlineSmall = headlineSmall.copy(fontFamily = systemFont),
        titleLarge = titleLarge.copy(fontFamily = systemFont, fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontFamily = systemFont, fontWeight = FontWeight.Medium),
        titleSmall = titleSmall.copy(fontFamily = systemFont, fontWeight = FontWeight.Medium),
        bodyLarge = bodyLarge.copy(fontFamily = systemFont),
        bodyMedium = bodyMedium.copy(fontFamily = systemFont),
        bodySmall = bodySmall.copy(fontFamily = systemFont),
        labelLarge = labelLarge.copy(fontFamily = systemFont, fontWeight = FontWeight.Medium),
        labelMedium = labelMedium.copy(fontFamily = systemFont, fontWeight = FontWeight.Medium),
        labelSmall = labelSmall.copy(fontFamily = systemFont),
    )
}
