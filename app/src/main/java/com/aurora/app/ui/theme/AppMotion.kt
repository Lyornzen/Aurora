package com.aurora.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Central motion system — optimized for 120fps.
 * All animations use spring or short tweens with graphicsLayer to avoid relayout.
 */
object AppMotion {

    // ─── Easing Curves ─────────────────────────────────────────
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val Legacy = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val StandardEasing = FastOutSlowInEasing
    val EnterEasing = LinearOutSlowInEasing
    val ExitEasing = FastOutLinearInEasing

    // ─── Durations (shorter for 120fps — less frames to draw) ──
    const val DURATION_INSTANT = 100
    const val DURATION_FAST = 200
    const val DURATION_NORMAL = 280

    // ─── Spring specs (preferred — GPU-friendly, adaptive to refresh rate) ──
    fun <T> springDefault() = spring<T>(dampingRatio = 0.9f, stiffness = Spring.StiffnessMedium)
    fun <T> springBouncy() = spring<T>(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium)
    fun <T> springStiff() = spring<T>(dampingRatio = 1f, stiffness = 800f)
    fun <T> springLoose() = spring<T>(dampingRatio = 0.82f, stiffness = Spring.StiffnessLow)

    // ─── Pre-built Specs ───────────────────────────────────────
    val pressDown = tween<Float>(durationMillis = DURATION_INSTANT, easing = EmphasizedAccelerate)
    val pressReturn = spring<Float>(dampingRatio = 0.82f, stiffness = Spring.StiffnessLow)
    val pressSettle = spring<Float>(dampingRatio = 1f, stiffness = 500f)

    // ─── Navigation ────────────────────────────────────────────
    val navEasing = CubicBezierEasing(0.25f, 0.10f, 0.25f, 1.0f)
    const val NAV_SLIDE = 260
    const val NAV_FADE = 100

    // ─── Visibility ────────────────────────────────────────────
    const val VISIBILITY_DURATION = 150
    const val VISIBILITY_INITIAL_SCALE = 0.85f

    // ─── Sheet ─────────────────────────────────────────────────
    const val SHEET_SLIDE = 280
    const val SHEET_FADE = 100
}
