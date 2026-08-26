package com.proxyscroll.app.ui.labs

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.proxyscroll.app.domain.LabsSettings
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max

@Stable
data class MotionSensorAvailability(
    val rotation: Boolean,
    val acceleration: Boolean,
)

@Stable
data class MotionCompensationFrame(
    /** Filtered, normalized high-frequency rotation compensation. */
    val shakeX: Float = 0f,
    val shakeY: Float = 0f,
    /** Filtered, normalized vehicle/device acceleration for peripheral cues. */
    val travelX: Float = 0f,
    val travelY: Float = 0f,
)

@Stable
class MotionCompensationState internal constructor(
    internal val controller: MotionSensorController,
) {
    val availability: MotionSensorAvailability = controller.availability

    var frame by mutableStateOf(MotionCompensationFrame())
        internal set
}

@Composable
fun rememberMotionCompensationState(
    sensorsEnabled: Boolean,
): MotionCompensationState {
    val context = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember(context) {
        lateinit var createdState: MotionCompensationState
        val controller = MotionSensorController(context) { frame ->
            createdState.frame = frame
        }
        createdState = MotionCompensationState(controller)
        createdState
    }

    DisposableEffect(lifecycleOwner, sensorsEnabled) {
        fun updateRegistration() {
            if (sensorsEnabled && lifecycleOwner.lifecycle.currentState.isAtLeast(
                    Lifecycle.State.STARTED,
                )
            ) {
                state.controller.start()
            } else {
                state.controller.stop()
                state.frame = MotionCompensationFrame()
            }
        }

        val observer = LifecycleEventObserver { _, _ -> updateRegistration() }
        lifecycleOwner.lifecycle.addObserver(observer)
        updateRegistration()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            state.controller.stop()
            state.frame = MotionCompensationFrame()
        }
    }

    return state
}

/**
 * Peripheral motion reference. It intentionally never intercepts touches and
 * never moves the content the user is reading or editing.
 */
@Composable
fun TravelMotionCues(
    frame: MotionCompensationFrame,
    settings: LabsSettings,
    flat: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!settings.travelCuesEnabled) return

    val strength = settings.motionStrength
    val magnitude = max(abs(frame.travelX), abs(frame.travelY))
    val visibility = (0.16f + magnitude * 0.72f).coerceIn(0.12f, 0.88f)

    Box(modifier = modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val edgeInset = 18.dp.toPx()
            val baseRadius = if (flat) 3.2.dp.toPx() else 4.0.dp.toPx()
            val xTravel = frame.travelX * 10.dp.toPx() * strength
            val yTravel = frame.travelY * 20.dp.toPx() * strength
            val cueColor = if (flat) {
                Color.White.copy(alpha = visibility * 0.62f)
            } else {
                Color(0xFFBCEBFF).copy(alpha = visibility * 0.74f)
            }
            val haloColor = Color(0xFF8F9BFF).copy(alpha = visibility * 0.22f)

            repeat(7) { index ->
                val fraction = (index + 1f) / 8f
                val verticalCurve = 1f - abs(fraction - 0.5f) * 0.55f
                val y = (size.height * fraction + yTravel * verticalCurve)
                    .coerceIn(edgeInset * 2f, size.height - edgeInset * 2f)
                val radius = baseRadius * (0.82f + verticalCurve * 0.24f)
                val left = Offset(edgeInset + xTravel, y)
                val right = Offset(size.width - edgeInset + xTravel, y)

                if (!flat) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(haloColor, Color.Transparent),
                            center = left,
                            radius = radius * 2.8f,
                        ),
                        radius = radius * 2.8f,
                        center = left,
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(haloColor, Color.Transparent),
                            center = right,
                            radius = radius * 2.8f,
                        ),
                        radius = radius * 2.8f,
                        center = right,
                    )
                }
                drawCircle(color = cueColor, radius = radius, center = left)
                drawCircle(color = cueColor, radius = radius, center = right)
                if (!flat) {
                    drawCircle(
                        color = Color.White.copy(alpha = visibility * 0.52f),
                        radius = radius,
                        center = left,
                        style = Stroke(width = 0.7.dp.toPx()),
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = visibility * 0.52f),
                        radius = radius,
                        center = right,
                        style = Stroke(width = 0.7.dp.toPx()),
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = visibility * 0.68f),
                        radius = radius * 0.27f,
                        center = left + Offset(-radius * 0.28f, -radius * 0.30f),
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = visibility * 0.68f),
                        radius = radius * 0.27f,
                        center = right + Offset(-radius * 0.28f, -radius * 0.30f),
                    )
                }
            }
        }
    }
}

internal class MotionSensorController(
    context: Context,
    private val onFrame: (MotionCompensationFrame) -> Unit,
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val linearAccelerationSensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val fallbackAccelerationSensor =
        linearAccelerationSensor ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val availability = MotionSensorAvailability(
        rotation = rotationSensor != null,
        acceleration = fallbackAccelerationSensor != null,
    )

    private var running = false
    private var lastPublishedNanos = 0L
    private var baselinePitch: Float? = null
    private var baselineRoll: Float? = null
    private var filteredShakeX = 0f
    private var filteredShakeY = 0f
    private var filteredTravelX = 0f
    private var filteredTravelY = 0f
    private val gravity = FloatArray(3)
    private var hasGravitySample = false
    private var frame = MotionCompensationFrame()

    fun start() {
        if (running) return
        running = true
        baselinePitch = null
        baselineRoll = null
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        fallbackAccelerationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        if (!running) return
        running = false
        sensorManager.unregisterListener(this)
        baselinePitch = null
        baselineRoll = null
        filteredShakeX = 0f
        filteredShakeY = 0f
        filteredTravelX = 0f
        filteredTravelY = 0f
        hasGravitySample = false
        frame = MotionCompensationFrame()
        lastPublishedNanos = 0L
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_GAME_ROTATION_VECTOR,
            Sensor.TYPE_ROTATION_VECTOR,
            -> updateRotation(event.values)

            Sensor.TYPE_LINEAR_ACCELERATION -> updateAcceleration(
                x = event.values[0],
                y = event.values[1],
            )

            Sensor.TYPE_ACCELEROMETER -> {
                if (!hasGravitySample) {
                    repeat(3) { axis -> gravity[axis] = event.values[axis] }
                    hasGravitySample = true
                    return
                }
                val alpha = 0.82f
                repeat(3) { axis ->
                    gravity[axis] = alpha * gravity[axis] + (1f - alpha) * event.values[axis]
                }
                updateAcceleration(
                    x = event.values[0] - gravity[0],
                    y = event.values[1] - gravity[1],
                )
            }
        }

        // Sensor streams can exceed the display rate. Publishing at about 50 Hz
        // keeps Compose work bounded while retaining a low-latency response.
        if (event.timestamp - lastPublishedNanos >= 20_000_000L) {
            lastPublishedNanos = event.timestamp
            onFrame(frame)
        }
    }

    private fun updateRotation(values: FloatArray) {
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
        SensorManager.getOrientation(rotationMatrix, orientation)
        val pitch = orientation[1]
        val roll = orientation[2]

        val previousPitch = baselinePitch
        val previousRoll = baselineRoll
        if (previousPitch == null || previousRoll == null) {
            baselinePitch = pitch
            baselineRoll = roll
            return
        }

        val pitchDelta = shortestAngle(pitch - previousPitch)
        val rollDelta = shortestAngle(roll - previousRoll)
        val baselineFollow = 0.035f
        baselinePitch = previousPitch + pitchDelta * baselineFollow
        baselineRoll = previousRoll + rollDelta * baselineFollow

        val targetX = (-rollDelta / 0.075f).coerceIn(-1f, 1f)
        val targetY = (pitchDelta / 0.085f).coerceIn(-1f, 1f)
        filteredShakeX += (targetX - filteredShakeX) * 0.28f
        filteredShakeY += (targetY - filteredShakeY) * 0.28f
        frame = frame.copy(shakeX = filteredShakeX, shakeY = filteredShakeY)
    }

    private fun updateAcceleration(x: Float, y: Float) {
        val targetX = (-x / 3.2f).coerceIn(-1f, 1f)
        val targetY = (y / 3.6f).coerceIn(-1f, 1f)
        filteredTravelX += (targetX - filteredTravelX) * 0.16f
        filteredTravelY += (targetY - filteredTravelY) * 0.16f
        frame = frame.copy(travelX = filteredTravelX, travelY = filteredTravelY)
    }

    private fun shortestAngle(angle: Float): Float {
        var wrapped = angle
        val pi = PI.toFloat()
        while (wrapped > pi) wrapped -= 2f * pi
        while (wrapped < -pi) wrapped += 2f * pi
        return wrapped
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
