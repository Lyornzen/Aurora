package com.aurora.ai.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================================
// Aurora AI Dimension System
// Corner Radius: Small=12dp, Medium=20dp, Large=28dp, Extra Large=32dp
// Spacing: 4, 8, 12, 16, 24, 32
// ============================================================

object AuroraDp {
    val dp0: Dp = 0.dp
    val dp0_5: Dp = 0.5.dp
    val dp1: Dp = 1.dp
    val dp2: Dp = 2.dp
    val dp3: Dp = 3.dp
    val dp4: Dp = 4.dp
    val dp6: Dp = 6.dp
    val dp8: Dp = 8.dp
    val dp10: Dp = 10.dp
    val dp12: Dp = 12.dp
    val dp14: Dp = 14.dp
    val dp16: Dp = 16.dp
    val dp18: Dp = 18.dp
    val dp20: Dp = 20.dp
    val dp22: Dp = 22.dp
    val dp24: Dp = 24.dp
    val dp28: Dp = 28.dp
    val dp32: Dp = 32.dp
    val dp36: Dp = 36.dp
    val dp40: Dp = 40.dp
    val dp44: Dp = 44.dp
    val dp48: Dp = 48.dp
    val dp52: Dp = 52.dp
    val dp56: Dp = 56.dp
    val dp64: Dp = 64.dp
    val dp72: Dp = 72.dp
    val dp80: Dp = 80.dp
    val dp88: Dp = 88.dp
    val dp96: Dp = 96.dp
    val dp100: Dp = 100.dp
    val dp120: Dp = 120.dp
    val dp140: Dp = 140.dp
    val dp180: Dp = 180.dp
    val dp200: Dp = 200.dp
    val dp256: Dp = 256.dp
    val dp999: Dp = 999.dp
}

data class AuroraSpacing(
    val space0: Dp = AuroraDp.dp0,
    val space4: Dp = AuroraDp.dp4,
    val space8: Dp = AuroraDp.dp8,
    val space12: Dp = AuroraDp.dp12,
    val space16: Dp = AuroraDp.dp16,
    val space20: Dp = AuroraDp.dp20,
    val space24: Dp = AuroraDp.dp24,
    val space28: Dp = AuroraDp.dp28,
    val space32: Dp = AuroraDp.dp32,
    val screenHorizontal: Dp = AuroraDp.dp16,
    val screenVertical: Dp = AuroraDp.dp16,
    val contentPadding: Dp = AuroraDp.dp16,
)

data class AuroraRadii(
    val small: Dp = AuroraDp.dp12,
    val medium: Dp = AuroraDp.dp20,
    val large: Dp = AuroraDp.dp28,
    val extraLarge: Dp = AuroraDp.dp32,
    val full: Dp = AuroraDp.dp999,
    val chip: Dp = AuroraDp.dp20,
    val card: Dp = AuroraDp.dp24,
    val message: Dp = AuroraDp.dp24,
)

data class AuroraSizes(
    val navigationBarHeight: Dp = AuroraDp.dp80,
    val greetingCardHeight: Dp = AuroraDp.dp180,
    val modelSelectorHeight: Dp = AuroraDp.dp56,
    val inputAreaHeight: Dp = AuroraDp.dp72,
    val chipHeight: Dp = AuroraDp.dp40,
    val taskCardHeight: Dp = AuroraDp.dp88,
    val deviceCardHeight: Dp = AuroraDp.dp80,
    val actionCardSize: Dp = AuroraDp.dp88,
    val sendButtonSize: Dp = AuroraDp.dp48,
    val iconSmall: Dp = AuroraDp.dp18,
    val iconMedium: Dp = AuroraDp.dp24,
    val iconLarge: Dp = AuroraDp.dp32,
    val avatarLarge: Dp = AuroraDp.dp80,
    val avatarMedium: Dp = AuroraDp.dp56,
)

data class AuroraElevation(
    val level0: Dp = AuroraDp.dp0,
    val level1: Dp = AuroraDp.dp1,
    val level2: Dp = AuroraDp.dp3,
)

val LocalAuroraSpacing = staticCompositionLocalOf { AuroraSpacing() }
val LocalAuroraRadii = staticCompositionLocalOf { AuroraRadii() }
val LocalAuroraSizes = staticCompositionLocalOf { AuroraSizes() }
val LocalAuroraElevation = staticCompositionLocalOf { AuroraElevation() }

// Convenience padding extension — use .padding(horizontal = LocalAuroraSpacing.current.screenHorizontal) directly
