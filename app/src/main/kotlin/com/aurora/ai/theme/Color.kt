package com.aurora.ai.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// Aurora AI Color System
// Primary: #7C4DFF (Purple)
// Secondary: #B39DDB
// Tertiary: #F8BBD0
// Error: #EF5350
// Success: #4CAF50
// Background: #FAF8FD
// Surface Container: #F1EDF7
// ============================================================

// --- Light Theme ---
object AuroraLightColors {
    val Primary = Color(0xFF7C4DFF)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFEDE7FF)
    val OnPrimaryContainer = Color(0xFF2D0060)
    val PrimaryVariant = Color(0xFF6938D6)

    val Secondary = Color(0xFFB39DDB)
    val OnSecondary = Color(0xFF1A1A2E)
    val SecondaryContainer = Color(0xFFEDE0FF)
    val OnSecondaryContainer = Color(0xFF2B1550)

    val Tertiary = Color(0xFFF8BBD0)
    val OnTertiary = Color(0xFF3E1030)
    val TertiaryContainer = Color(0xFFFFD8E6)
    val OnTertiaryContainer = Color(0xFF3E1030)

    val Error = Color(0xFFEF5350)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFFDAD6)

    val Success = Color(0xFF4CAF50)
    val OnSuccess = Color(0xFFFFFFFF)

    val Background = Color(0xFFFAF8FD)
    val OnBackground = Color(0xFF1C1B1F)

    val Surface = Color(0xFFFAF8FD)
    val OnSurface = Color(0xFF1C1B1F)
    val SurfaceVariant = Color(0xFFE7E0EB)
    val OnSurfaceVariant = Color(0xFF49454E)

    val SurfaceContainer = Color(0xFFF1EDF7)
    val SurfaceContainerHigh = Color(0xFFEDE7F3)
    val SurfaceContainerHighest = Color(0xFFE6E0EE)

    val Outline = Color(0xFF79747E)
    val OutlineVariant = Color(0xFFCAC4CF)

    val InverseSurface = Color(0xFF313033)
    val InverseOnSurface = Color(0xFFF4EFF4)
    val InversePrimary = Color(0xFFCDBDFF)

    // Custom
    val CardBackground = Color(0xFFFFFFFF)
    val Divider = Color(0xFFE0E0E0)
    val ChipBackground = Color(0xFFF5F1FF)

    // Chat gradient colors
    val GradientStart = Color(0xFFC7AFFF)
    val GradientEnd = Color(0xFFF3C5E7)

    // Status colors
    val Running = Primary
    val Completed = Success
    val Paused = Color(0xFFF8BBD0)
    val Failed = Error
}

// --- Dark Theme ---
object AuroraDarkColors {
    val Primary = Color(0xFFCDBDFF)
    val OnPrimary = Color(0xFF3A009C)
    val PrimaryContainer = Color(0xFF5217C5)
    val OnPrimaryContainer = Color(0xFFEDE7FF)

    val Secondary = Color(0xFFD0BBFF)
    val OnSecondary = Color(0xFF38237C)
    val SecondaryContainer = Color(0xFF4E3C93)
    val OnSecondaryContainer = Color(0xFFEDE0FF)

    val Tertiary = Color(0xFFFFB4CB)
    val OnTertiary = Color(0xFF5D113C)
    val TertiaryContainer = Color(0xFF792853)
    val OnTertiaryContainer = Color(0xFFFFD8E6)

    val Error = Color(0xFFEF5350)
    val OnError = Color(0xFF690005)
    val ErrorContainer = Color(0xFF93000A)

    val Success = Color(0xFF4CAF50)
    val OnSuccess = Color(0xFF002200)

    val Background = Color(0xFF141218)
    val OnBackground = Color(0xFFE6E1E5)

    val Surface = Color(0xFF141218)
    val OnSurface = Color(0xFFE6E1E5)
    val SurfaceVariant = Color(0xFF48454E)
    val OnSurfaceVariant = Color(0xFFCAC4CF)

    val SurfaceContainer = Color(0xFF211F26)
    val SurfaceContainerHigh = Color(0xFF2C2931)
    val SurfaceContainerHighest = Color(0xFF37343C)

    val Outline = Color(0xFF948F99)
    val OutlineVariant = Color(0xFF48454E)

    val InverseSurface = Color(0xFFE6E1E5)
    val InverseOnSurface = Color(0xFF313033)
    val InversePrimary = Color(0xFF7C4DFF)

    // Custom
    val CardBackground = Color(0xFF1E1C24)
    val Divider = Color(0xFF2D2B33)
    val ChipBackground = Color(0xFF2A2540)

    val GradientStart = Color(0xFF4A148C)
    val GradientEnd = Color(0xFF880E4F)

    val Running = Primary
    val Completed = Success
    val Paused = Tertiary
    val Failed = Error
}
