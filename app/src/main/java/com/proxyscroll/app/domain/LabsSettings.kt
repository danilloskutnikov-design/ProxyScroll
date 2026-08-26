package com.proxyscroll.app.domain

const val MIN_LABS_MOTION_STRENGTH = 0.25f
const val MAX_LABS_MOTION_STRENGTH = 1.0f

/**
 * Experimental motion-comfort features. They are deliberately opt-in and kept
 * independent from visual themes so a material never silently enables sensors.
 */
data class LabsSettings(
    val microStabilizationEnabled: Boolean = false,
    val travelCuesEnabled: Boolean = false,
    val motionStrength: Float = 0.55f,
) {
    val sensorsEnabled: Boolean
        get() = microStabilizationEnabled || travelCuesEnabled

    fun normalized(): LabsSettings = copy(
        motionStrength = motionStrength.coerceIn(
            MIN_LABS_MOTION_STRENGTH,
            MAX_LABS_MOTION_STRENGTH,
        ),
    )
}
