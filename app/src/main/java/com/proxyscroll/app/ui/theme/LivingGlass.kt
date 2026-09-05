package com.proxyscroll.app.ui.theme

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/** A one-way recording: only a scrolling body writes, only its sibling dock reads. */
@Stable
class GlassBackdrop internal constructor(val layer: GraphicsLayer) {
    var origin by mutableStateOf(Offset.Zero)
        internal set
    var ready by mutableStateOf(false)
        internal set
}

@Composable
fun rememberGlassBackdrop(): GlassBackdrop {
    val layer = rememberGraphicsLayer()
    return remember(layer) { GlassBackdrop(layer) }
}

fun Modifier.glassBackdropSource(backdrop: GlassBackdrop): Modifier = this
    .onGloballyPositioned { backdrop.origin = it.positionInWindow() }
    .drawWithContent {
        backdrop.layer.record { this@drawWithContent.drawContent() }
        drawLayer(backdrop.layer)
        if (!backdrop.ready) backdrop.ready = true
    }

/** Thin frosted cards, thicker polished buttons, dense readable sheets. Text is a
 * separate sibling of the optical layer and is never blurred or refracted. */
@Composable
internal fun LivingGlassSurface(
    modifier: Modifier,
    shape: Shape?,
    role: ProxySurfaceRole,
    strong: Boolean,
    active: Boolean,
    interactive: Boolean,
    recessed: Boolean,
    backdrop: GlassBackdrop?,
    content: @Composable BoxScope.() -> Unit,
) {
    val corners = LocalProxyShape.current
    val settings = LocalStainSettings.current
    val palette = LocalStainPaletteColors.current
    val motion = LocalMaterialMotionProfile.current
    val breath = LocalMaterialBreath.current
    val viewport = LocalOpticalViewport.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val corner = when (role) {
        ProxySurfaceRole.CARD -> corners.resolvedCardCornerDp
        ProxySurfaceRole.INPUT -> corners.resolvedInputCornerDp
        ProxySurfaceRole.BUTTON -> corners.resolvedButtonCornerDp
        ProxySurfaceRole.OVERLAY -> (corners.globalCornerDp + 10).coerceAtMost(34)
    }
    val resolvedShape = shape ?: RoundedCornerShape(corner.dp)
    var dimensions by remember { mutableStateOf(IntSize.Zero) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    var finger by remember { mutableStateOf(Offset.Unspecified) }
    var pressed by remember { mutableStateOf(false) }
    // Read the animation only during drawing, keeping the text out of frame updates.
    val pressure = animateFloatAsState(
        if (pressed) 1f else 0f,
        spring(dampingRatio = 0.88f, stiffness = if (pressed) 850f else 340f),
        label = "glass-pressure",
    )
    val thickness = settings.depth.opticalFactor * if (recessed) 0.58f else 1f
    val blurDp = when (role) {
        ProxySurfaceRole.CARD -> 5f
        ProxySurfaceRole.INPUT -> 9f
        ProxySurfaceRole.BUTTON -> 2f
        ProxySurfaceRole.OVERLAY -> if (backdrop != null) 13f else 18f
    }
    val opticalEffect = remember(dimensions, resolvedShape, density, layoutDirection, role, thickness, motion.textureAlpha) {
        if (dimensions.width == 0 || dimensions.height == 0 || motion.textureAlpha < 0.74f) null
        else runCatching {
            val size = Size(dimensions.width.toFloat(), dimensions.height.toFloat())
            val outline = resolvedShape.createOutline(size, layoutDirection, density)
            if (Build.VERSION.SDK_INT >= 33) {
                createGlassEffect(size, outline, density.density, thickness, blurDp)
            } else if (Build.VERSION.SDK_INT >= 31) {
                RenderEffect.createBlurEffect(
                    blurDp * density.density, blurDp * density.density,
                    android.graphics.Shader.TileMode.CLAMP,
                ).asComposeRenderEffect()
            } else null
        }.getOrNull()
    }
    val elevation = when {
        recessed -> 1.dp
        role == ProxySurfaceRole.BUTTON -> 10.dp
        role == ProxySurfaceRole.OVERLAY -> 16.dp
        strong -> 8.dp
        else -> 4.dp
    }
    Box(
        modifier = modifier
            .shadow(elevation, resolvedShape, ambientColor = Color(0x17253C62), spotColor = Color(0x29253C62))
            .clip(resolvedShape)
            .onGloballyPositioned {
                dimensions = it.size
                origin = it.positionInWindow()
            }
            .then(if (interactive) Modifier.pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    finger = down.position
                    pressed = true
                    try {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed || event.changes.count { it.pressed } > 1) break
                            // Hand the gesture to scrolling immediately; observation never consumes it.
                            if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) break
                            finger = change.position
                        }
                    } finally {
                        pressed = false
                    }
                }
            } else Modifier),
    ) {
        Canvas(Modifier.matchParentSize().graphicsLayer { renderEffect = opticalEffect }) {
            drawLiquidOpticalScene(
                viewportSize = Size(viewport.size.width.toFloat(), viewport.size.height.toFloat()),
                viewportOrigin = origin - viewport.originInWindow,
                palette = palette,
                stain = settings.intensity,
                activeDrift = breath(),
                magnification = if (opticalEffect == null) 1.018f else 1f,
            )
            if (backdrop?.ready == true) {
                translate(backdrop.origin.x - origin.x, backdrop.origin.y - origin.y) {
                    drawLayer(backdrop.layer)
                }
            }
        }
        Canvas(Modifier.matchParentSize()) {
            val p = pressure.value.coerceIn(0f, 1f)
            val phase = breath()
            val contact = if (finger == Offset.Unspecified) Offset(size.width * 0.3f, size.height * 0.2f) else finger
            val outline = resolvedShape.createOutline(size, layoutDirection, this)
            val frost = when (role) {
                ProxySurfaceRole.CARD -> if (strong) 0.42f else 0.32f
                ProxySurfaceRole.INPUT -> 0.52f
                ProxySurfaceRole.BUTTON -> 0.12f
                ProxySurfaceRole.OVERLAY -> if (backdrop != null) 0.56f else 0.84f
            }
            drawRect(Color.White.copy(alpha = frost - p * 0.035f))
            if (active) drawRect(palette.primary.copy(alpha = 0.075f))
            // Broad softbox reflection: a surface reflection, not a glow behind text.
            val light = Offset(size.width * (0.16f + phase * 0.10f), -size.height * 0.3f)
            drawRect(Brush.radialGradient(
                listOf(Color.White.copy(alpha = 0.34f), Color.White.copy(alpha = 0.06f), Color.Transparent),
                light, size.width.coerceAtLeast(size.height) * 0.92f,
            ))
            drawOutline(outline, Brush.linearGradient(
                listOf(Color.White.copy(alpha = 0.96f), Color.White.copy(alpha = 0.35f), Color(0x26395070)),
                Offset.Zero, Offset(size.width * 0.8f, size.height),
            ), style = Stroke(width = (if (strong) 2.2f else 1.5f).dp.toPx()))
            // A second, recessed edge gives the bevel a visible thickness.
            drawOutline(outline, Brush.linearGradient(
                listOf(Color.White.copy(alpha = 0.04f), Color(0x0E3E5274), Color.White.copy(alpha = 0.36f)),
                Offset.Zero, Offset(size.width, size.height),
            ), style = Stroke(width = 4.5.dp.toPx()))
            if (p > 0.001f) {
                drawRect(Brush.radialGradient(
                    listOf(Color.White.copy(alpha = p * 0.32f), palette.secondary.copy(alpha = p * 0.045f), Color.Transparent),
                    contact, 100.dp.toPx(),
                ))
                drawOutline(outline, Brush.radialGradient(
                    listOf(Color.White.copy(alpha = p * 0.98f), Color.Transparent), contact, 150.dp.toPx(),
                ), style = Stroke(width = 2.2.dp.toPx()))
            }
        }
        content()
    }
}

/** API 33 refraction bends the recorded background along the actual rounded
 * outline. Reflections live above it. The input is clamped before sampling to
 * prevent black/transparent seams, including at circular action buttons. */
@RequiresApi(33)
internal fun createGlassEffect(
    size: Size,
    outline: Outline,
    density: Float,
    thickness: Float,
    blurDp: Float,
): androidx.compose.ui.graphics.RenderEffect {
    val shader = RuntimeShader(GLASS_REFRACTION)
    shader.setFloatUniform("resolution", size.width, size.height)
    val r = (outline as? Outline.Rounded)?.roundRect
    shader.setFloatUniform("corners", r?.topLeftCornerRadius?.x ?: 0f,
        r?.topRightCornerRadius?.x ?: 0f, r?.bottomRightCornerRadius?.x ?: 0f,
        r?.bottomLeftCornerRadius?.x ?: 0f)
    shader.setFloatUniform("band", 9f * density * thickness)
    shader.setFloatUniform("bend", 5.5f * density * thickness)
    val blur = RenderEffect.createBlurEffect(blurDp * density, blurDp * density, android.graphics.Shader.TileMode.CLAMP)
    return RenderEffect.createChainEffect(RenderEffect.createRuntimeShaderEffect(shader, "scene"), blur).asComposeRenderEffect()
}

internal const val GLASS_REFRACTION = """
uniform shader scene;
uniform float2 resolution;
uniform float4 corners;
uniform float band;
uniform float bend;
float distanceToEdge(float2 p) {
    float2 c = p - resolution * 0.5;
    float r = c.y < 0.0 ? (c.x < 0.0 ? corners.x : corners.y) : (c.x < 0.0 ? corners.w : corners.z);
    r = min(r, min(resolution.x, resolution.y) * 0.5);
    float2 q = abs(c) - resolution * 0.5 + r;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}
half4 main(float2 p) {
    float d = distanceToEdge(p);
    float2 gradient = float2(distanceToEdge(p + float2(0.5, 0.0)) - distanceToEdge(p - float2(0.5, 0.0)),
        distanceToEdge(p + float2(0.0, 0.5)) - distanceToEdge(p - float2(0.0, 0.5)));
    float2 n = gradient / max(length(gradient), 0.001);
    float bevel = 1.0 - smoothstep(0.0, band, -d);
    float2 uv = clamp(p - n * bend * bevel * bevel, float2(0.5), resolution - 0.5);
    half4 transmitted = scene.eval(uv);
    // Dispersion stays below one pixel at the bevel and never touches the text layer.
    float2 split = n * bevel * 0.65;
    transmitted.r = scene.eval(clamp(uv + split, float2(0.5), resolution - 0.5)).r;
    transmitted.b = scene.eval(clamp(uv - split, float2(0.5), resolution - 0.5)).b;
    float fresnel = pow(bevel, 3.0);
    float lighting = dot(n, normalize(float2(-0.65, -0.8)));
    transmitted.rgb += half3(fresnel * lighting * 0.065);
    return half4(clamp(transmitted.rgb, 0.0, transmitted.a), transmitted.a);
}
"""

/** Sparse suspended dust catches the same light as the glass; never overlays text. */
internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSuspendedLight(
    sceneSize: Size,
    project: (Offset) -> Offset,
    phase: Float,
    intensity: Float,
) {
    repeat(16) { index ->
        val seed = index * 2.3999632f
        val x = (0.5f + cos(seed) * (0.24f + (index % 3) * 0.13f))
        val y = ((index * 0.618034f) % 1f)
        val center = project(Offset(
            sceneSize.width * (x + sin(seed + phase) * 0.012f),
            sceneSize.height * (y + cos(seed + phase) * 0.008f),
        ))
        val light = (0.08f + 0.12f * (0.5f + sin(seed + phase) * 0.5f)) * intensity
        drawCircle(Color.White.copy(alpha = light), radius = (0.75f + index % 3 * 0.45f).dp.toPx(), center = center)
    }
}
