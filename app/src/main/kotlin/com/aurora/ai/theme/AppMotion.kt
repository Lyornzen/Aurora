package com.aurora.ai.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Aurora AI Motion System
 * Based on Material Design 3 motion principles.
 */
object AuroraMotion {

    // --- Easing Curves ---
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val StandardEasing = FastOutSlowInEasing
    val EnterEasing = LinearOutSlowInEasing
    val ExitEasing = FastOutLinearInEasing

    // --- Duration Constants ---
    const val DurationInstant = 120
    const val DurationFast = 200
    const val DurationNormal = 300
    const val DurationSlow = 450
    const val DurationPageTransition = 340
    const val DurationFade = 140

    // --- Spring specs ---
    val PressDown = tween<Float>(DurationInstant, easing = EmphasizedAccelerate)
    val PressReturn = spring<Float>(
        dampingRatio = 0.82f,
        stiffness = Spring.StiffnessLow,
    )

    // Timeline animation
    const val TimelineStepDelay = 80
    const val TimelineStepDuration = 300
    val TimelineStepEasing = StandardEasing
}
