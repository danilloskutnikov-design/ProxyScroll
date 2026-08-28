package com.proxyscroll.app.ui.theme

import android.app.Activity
import android.app.ActivityManager
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.proxyscroll.app.domain.AppTheme
import com.proxyscroll.app.domain.InterfaceShape
import com.proxyscroll.app.domain.MaterialMotionQuality
import com.proxyscroll.app.domain.resolveFor
import com.proxyscroll.app.domain.StainPalette
import com.proxyscroll.app.domain.StainSettings
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private const val THEME_TRANSITION_MILLIS = 720

private val LiquidGlassColors = lightColorScheme(
    primary = Color(0xFF4055D9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE5FF),
    onPrimaryContainer = Color(0xFF15205D),
    secondary = Color(0xFF287784),
    onSecondary = Color.White,
    background = Color(0xFFF2F5FF),
    onBackground = Color(0xFF171A28),
    surface = Color(0xFFF9FAFF),
    onSurface = Color(0xFF171A28),
    surfaceVariant = Color(0xFFE6EAF7),
    onSurfaceVariant = Color(0xFF555B6D),
    outline = Color(0xFF858DA8),
    error = Color(0xFFBA1A1A),
)

private val RoyalGraphiteColors = darkColorScheme(
    primary = Color(0xFFA7C9D5),
    onPrimary = Color(0xFF0A222B),
    primaryContainer = Color(0xFF263C45),
    onPrimaryContainer = Color(0xFFC8E9F2),
    secondary = Color(0xFF9FACB3),
    onSecondary = Color(0xFF172126),
    background = Color(0xFF080B0D),
    onBackground = Color(0xFFF4F7F8),
    surface = Color(0xFF12171A),
    onSurface = Color(0xFFF4F7F8),
    surfaceVariant = Color(0xFF20272B),
    onSurfaceVariant = Color(0xFFD0D8DC),
    outline = Color(0xFF69767D),
    error = Color(0xFFFFB4AB),
)

private val OldScrollColors = lightColorScheme(
    primary = Color(0xFF79552F),
    onPrimary = Color(0xFFFFF7E5),
    primaryContainer = Color(0xFFE9D2A8),
    onPrimaryContainer = Color(0xFF3C2917),
    secondary = Color(0xFF866B47),
    onSecondary = Color(0xFFFFF7E5),
    background = Color(0xFFE9D9B8),
    onBackground = Color(0xFF30271C),
    surface = Color(0xFFF1E4C8),
    onSurface = Color(0xFF30271C),
    surfaceVariant = Color(0xFFE1CEA8),
    onSurfaceVariant = Color(0xFF675641),
    outline = Color(0xFF8D795C),
    error = Color(0xFF9D342E),
)

private val LiteLifeColors = darkColorScheme(
    primary = Color(0xFF3B8CFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF173C68),
    onPrimaryContainer = Color(0xFFD7E9FF),
    secondary = Color(0xFF8AA8CC),
    onSecondary = Color(0xFF10243B),
    background = Color(0xFF101115),
    onBackground = Color(0xFFF7F8FA),
    surface = Color(0xFF1A1D23),
    onSurface = Color(0xFFF7F8FA),
    surfaceVariant = Color(0xFF22262E),
    onSurfaceVariant = Color(0xFFC5CAD2),
    outline = Color(0xFF434852),
    error = Color(0xFFFF6B74),
)

private val CyberpunkColors = darkColorScheme(
    primary = Color(0xFFF4E900),
    onPrimary = Color(0xFF08090A),
    primaryContainer = Color(0xFF4A4500),
    onPrimaryContainer = Color(0xFFFFF45A),
    secondary = Color(0xFFFF3B30),
    onSecondary = Color(0xFF120200),
    secondaryContainer = Color(0xFF64120D),
    onSecondaryContainer = Color(0xFFFFDAD5),
    tertiary = Color(0xFF00E7FF),
    onTertiary = Color(0xFF001F24),
    tertiaryContainer = Color(0xFF004E58),
    onTertiaryContainer = Color(0xFF8EF2FF),
    background = Color(0xFF070809),
    onBackground = Color(0xFFF8F6E8),
    surface = Color(0xFF101113),
    onSurface = Color(0xFFF8F6E8),
    surfaceVariant = Color(0xFF1C1C17),
    onSurfaceVariant = Color(0xFFC9C7B6),
    outline = Color(0xFF777566),
    error = Color(0xFFFF453A),
)

data class ProxyVisualStyle(
    val theme: AppTheme,
    val materialTop: Color,
    val materialMiddle: Color,
    val materialBottom: Color,
    val strongTop: Color,
    val strongBottom: Color,
    val rimLight: Color,
    val rimShade: Color,
    val specular: Color,
    val shadow: Color,
    val scrim: Color,
)

private val LiquidVisualStyle = ProxyVisualStyle(
    theme = AppTheme.LIQUID_GLASS,
    materialTop = Color.White.copy(alpha = 0.095f),
    materialMiddle = Color(0xFFF8FBFF).copy(alpha = 0.022f),
    materialBottom = Color(0xFFC9D5E8).copy(alpha = 0.038f),
    strongTop = Color.White.copy(alpha = 0.16f),
    strongBottom = Color(0xFFD2DCEC).copy(alpha = 0.065f),
    rimLight = Color.White.copy(alpha = 0.92f),
    rimShade = Color(0xFF34415D).copy(alpha = 0.28f),
    specular = Color.White.copy(alpha = 0.16f),
    shadow = Color(0xFF22304D).copy(alpha = 0.16f),
    scrim = Color(0xFF172146).copy(alpha = 0.25f),
)

private val GraphiteVisualStyle = ProxyVisualStyle(
    theme = AppTheme.ROYAL_GRAPHITE,
    materialTop = Color(0xFF35434A).copy(alpha = 0.62f),
    materialMiddle = Color(0xFF182126).copy(alpha = 0.64f),
    materialBottom = Color(0xFF0B1013).copy(alpha = 0.74f),
    strongTop = Color(0xFF435159).copy(alpha = 0.78f),
    strongBottom = Color(0xFF10171B).copy(alpha = 0.84f),
    rimLight = Color(0xFFD5E8EF).copy(alpha = 0.28f),
    rimShade = Color.Black.copy(alpha = 0.62f),
    specular = Color(0xFFD7F1FA).copy(alpha = 0.16f),
    shadow = Color.Black.copy(alpha = 0.52f),
    scrim = Color.Black.copy(alpha = 0.38f),
)

private val OldScrollVisualStyle = ProxyVisualStyle(
    theme = AppTheme.OLD_SCROLL,
    materialTop = Color(0xFFFFF4D8).copy(alpha = 0.78f),
    materialMiddle = Color(0xFFEEDDB9).copy(alpha = 0.70f),
    materialBottom = Color(0xFFD5B986).copy(alpha = 0.62f),
    strongTop = Color(0xFFFFF7E4).copy(alpha = 0.92f),
    strongBottom = Color(0xFFD8BC89).copy(alpha = 0.78f),
    rimLight = Color(0xFFFFFAEC).copy(alpha = 0.72f),
    rimShade = Color(0xFF75542E).copy(alpha = 0.34f),
    specular = Color(0xFFFFF1C8).copy(alpha = 0.30f),
    shadow = Color(0xFF4E351D).copy(alpha = 0.22f),
    scrim = Color(0xFF3A2919).copy(alpha = 0.28f),
)

private val LiteLifeVisualStyle = ProxyVisualStyle(
    theme = AppTheme.LITE_LIFE,
    materialTop = Color(0xFF1A1D23),
    materialMiddle = Color(0xFF1A1D23),
    materialBottom = Color(0xFF1A1D23),
    strongTop = Color(0xFF232831),
    strongBottom = Color(0xFF232831),
    rimLight = Color.Transparent,
    rimShade = Color.Transparent,
    specular = Color.Transparent,
    shadow = Color.Transparent,
    scrim = Color(0xFF101115),
)

private val CyberpunkVisualStyle = ProxyVisualStyle(
    theme = AppTheme.CYBERPUNK,
    materialTop = Color(0xFF1B1C18).copy(alpha = 0.96f),
    materialMiddle = Color(0xFF0D0E10).copy(alpha = 0.98f),
    materialBottom = Color(0xFF050607).copy(alpha = 0.99f),
    strongTop = Color(0xFF2D2B0A).copy(alpha = 0.98f),
    strongBottom = Color(0xFF0A0B0C).copy(alpha = 0.99f),
    rimLight = Color(0xFFF4E900).copy(alpha = 0.92f),
    rimShade = Color(0xFFFF3B30).copy(alpha = 0.72f),
    specular = Color(0xFFFFF56A).copy(alpha = 0.22f),
    shadow = Color.Black.copy(alpha = 0.82f),
    scrim = Color(0xFF050607).copy(alpha = 0.78f),
)

val LocalProxyVisualStyle = staticCompositionLocalOf { LiquidVisualStyle }
val LocalProxyShape = staticCompositionLocalOf { InterfaceShape() }
val LocalStainSettings = staticCompositionLocalOf { StainSettings() }

data class StainPaletteColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val neutral: Color,
    val caustic: Color,
)

private val AuroraOpalColors = StainPaletteColors(
    primary = Color(0xFF6F7BF7),
    secondary = Color(0xFF71D9E8),
    tertiary = Color(0xFFC8A9FF),
    neutral = Color(0xFFF6F8FF),
    caustic = Color(0xFFFFEAF8),
)

private val CoralGlacierColors = StainPaletteColors(
    primary = Color(0xFFFF8F88),
    secondary = Color(0xFF80D8F3),
    tertiary = Color(0xFFA8A7FF),
    neutral = Color(0xFFF7F5FF),
    caustic = Color(0xFFFFE7D9),
)

private val NordicBloomColors = StainPaletteColors(
    primary = Color(0xFF5FD0B5),
    secondary = Color(0xFF6C91E8),
    tertiary = Color(0xFFC38FD8),
    neutral = Color(0xFFEFF6F4),
    caustic = Color(0xFFDDFDF6),
)

private val GraphiteOilColors = StainPaletteColors(
    primary = Color(0xFF6D8D98),
    secondary = Color(0xFF5B6C91),
    tertiary = Color(0xFF4D7770),
    neutral = Color(0xFF0A0F12),
    caustic = Color(0xFFB8D9E4),
)

private val OldScrollMaterialColors = StainPaletteColors(
    primary = Color(0xFF8E6638),
    secondary = Color(0xFFC69A5E),
    tertiary = Color(0xFF6E4A28),
    neutral = Color(0xFFF0E1C1),
    caustic = Color(0xFFFFF3D4),
)

private val LiteLifeMaterialColors = StainPaletteColors(
    primary = Color(0xFF3B8CFF),
    secondary = Color(0xFF64A8FF),
    tertiary = Color(0xFF7D86A8),
    neutral = Color(0xFF111217),
    caustic = Color(0xFFDCEBFF),
)

private val CyberpunkMaterialColors = StainPaletteColors(
    primary = Color(0xFFF4E900),
    secondary = Color(0xFFFF3B30),
    tertiary = Color(0xFF00E7FF),
    neutral = Color(0xFF08090A),
    caustic = Color(0xFFFFF8A8),
)

val LocalStainPaletteColors = staticCompositionLocalOf { AuroraOpalColors }

private class OpticalAtlasBrush(
    private val image: ImageBitmap,
) : ShaderBrush() {
    override fun createShader(size: Size): Shader = ImageShader(
        image = image,
        tileModeX = TileMode.Mirror,
        tileModeY = TileMode.Mirror,
    )
}

data class MaterialMicrostructure(
    val fine: Brush,
    val spectral: Brush,
)

private enum class MicrostructureLayer {
    FINE,
    SPECTRAL,
}

private fun createMaterialGrainBrush(
    theme: AppTheme,
    palette: StainPaletteColors,
    width: Int,
    height: Int,
    layer: MicrostructureLayer,
): Brush {
    // Two large, differently sized mirrored atlases are composited in the material.
    // Each atlas is wider than a card and their combined repeat period is much
    // larger than a phone screen. Sparse particles carry a bright face and a
    // chromatic shadow, so they behave like inclusions that refract light instead
    // of a monochrome noise overlay.
    val pixels = IntArray(width * height)
    val oldScroll = theme == AppTheme.OLD_SCROLL
    val spectrum = if (layer == MicrostructureLayer.FINE) {
        if (oldScroll) {
            arrayOf(Color(0xFF72502C), Color(0xFFA77D49), palette.caustic)
        } else {
            arrayOf(palette.neutral, palette.caustic, palette.secondary)
        }
    } else {
        if (oldScroll) {
            arrayOf(Color(0xFF5E4226), Color(0xFF987044), palette.secondary, palette.caustic)
        } else {
            arrayOf(palette.primary, palette.secondary, palette.tertiary, palette.caustic)
        }
    }
    var seed = when {
        theme == AppTheme.LIQUID_GLASS && layer == MicrostructureLayer.FINE -> 0x51F15EED
        theme == AppTheme.LIQUID_GLASS -> 0x6E624EB7
        theme == AppTheme.OLD_SCROLL && layer == MicrostructureLayer.FINE -> 0x4F1D3A67
        theme == AppTheme.OLD_SCROLL -> 0x71C28E95
        layer == MicrostructureLayer.FINE -> 0x37A9C2D1
        else -> 0x2B7E1516
    }

    fun nextNoise(): Int {
        seed = seed * 1_664_525 + 1_013_904_223
        return seed ushr 1
    }

    fun writeOpticalNeighbour(
        px: Int,
        py: Int,
        tint: Color,
        neighbourAlpha: Int,
    ) {
        if (px !in 0 until width || py !in 0 until height) return
        val target = py * width + px
        val existing = pixels[target]
        if (android.graphics.Color.alpha(existing) >= neighbourAlpha) return
        pixels[target] = android.graphics.Color.argb(
            neighbourAlpha.coerceIn(0, 255),
            (tint.red * 255f).roundToInt().coerceIn(0, 255),
            (tint.green * 255f).roundToInt().coerceIn(0, 255),
            (tint.blue * 255f).roundToInt().coerceIn(0, 255),
        )
    }

    pixels.indices.forEach { index ->
        val x = index % width
        val y = index / width
        val noise = nextNoise()
        val occupancy = noise and 0xFF
        if (oldScroll) {
            // Paper is directional: short translucent fibres align into broad,
            // slightly wavy bands, while sparse dust has its own warm/cool tint.
            // Prime-sized layers keep both fields from visibly looping together.
            val fibreBand = abs(
                sin(y * 0.47 + sin(x * 0.021) * 1.4 + cos((x + y) * 0.014) * 0.55),
            )
            val visible = if (layer == MicrostructureLayer.FINE) {
                fibreBand > 0.955 && occupancy < 62
            } else {
                occupancy < 7
            }
            if (!visible) return@forEach

            val source = spectrum[(noise ushr 9) % spectrum.size]
            val brightFibre = layer == MicrostructureLayer.FINE && ((noise ushr 19) and 0x07) == 0
            val alpha = if (layer == MicrostructureLayer.FINE) {
                if (brightFibre) 22 + ((noise ushr 22) and 0x0F) else 10 + ((noise ushr 21) and 0x0F)
            } else {
                17 + ((noise ushr 20) and 0x1F)
            }
            val lift = if (brightFibre) 0.45f else 0f
            val red = ((source.red + (1f - source.red) * lift) * 255f)
                .roundToInt().coerceIn(0, 255)
            val green = ((source.green + (1f - source.green) * lift) * 255f)
                .roundToInt().coerceIn(0, 255)
            val blue = ((source.blue + (1f - source.blue) * lift) * 255f)
                .roundToInt().coerceIn(0, 255)
            pixels[index] = android.graphics.Color.argb(alpha, red, green, blue)
            return@forEach
        }
        val warpedX = x + sin(y * 0.0087) * 31.0 + cos((x + y) * 0.0039) * 19.0
        val warpedY = y + cos(x * 0.0073) * 27.0 + sin((x - y) * 0.0047) * 17.0
        val opticalEnvelope = (
            sin(warpedX * 0.0107) +
                cos(warpedY * 0.0089) +
                sin((warpedX + warpedY) * 0.0049) +
                3.0
            ) / 6.0
        val visibleThreshold = when (layer) {
            MicrostructureLayer.FINE -> (54 + opticalEnvelope * 38).roundToInt()
            MicrostructureLayer.SPECTRAL -> (15 + opticalEnvelope * 28).roundToInt()
        }
        if (occupancy >= visibleThreshold) return@forEach

        val source = spectrum[(noise ushr 9) % spectrum.size]
        val caustic = layer == MicrostructureLayer.SPECTRAL && occupancy < 4
        val lift = if (caustic) {
            0.72f
        } else {
            val baseLift = if (layer == MicrostructureLayer.FINE) 0.18f else 0.08f
            baseLift + ((noise ushr 17) and 0x0F) / 120f
        }
        val alpha = if (caustic) {
            92 + ((noise ushr 22) and 0x1F)
        } else {
            val baseAlpha = if (layer == MicrostructureLayer.FINE) 14 else 24
            val spread = if (layer == MicrostructureLayer.FINE) 17 else 26
            baseAlpha + ((noise ushr 21) and spread)
        }
        val red = ((source.red + (1f - source.red) * lift) * 255f)
            .roundToInt().coerceIn(0, 255)
        val green = ((source.green + (1f - source.green) * lift) * 255f)
            .roundToInt().coerceIn(0, 255)
        val blue = ((source.blue + (1f - source.blue) * lift) * 255f)
            .roundToInt().coerceIn(0, 255)
        pixels[index] = android.graphics.Color.argb(alpha.coerceAtMost(255), red, green, blue)

        val refractionTint = spectrum[((noise ushr 13) + 1) % spectrum.size]
        if (layer == MicrostructureLayer.FINE) {
            if (occupancy < 15) {
                writeOpticalNeighbour(x - 1, y - 1, palette.caustic, (alpha * 0.48f).roundToInt())
                writeOpticalNeighbour(x + 1, y + 1, refractionTint, (alpha * 0.36f).roundToInt())
            }
        } else {
            val haloAlpha = (alpha * 0.28f).roundToInt()
            writeOpticalNeighbour(x - 1, y, palette.caustic, haloAlpha)
            writeOpticalNeighbour(x, y - 1, palette.caustic, haloAlpha)
            writeOpticalNeighbour(x + 1, y, refractionTint, (haloAlpha * 0.86f).roundToInt())
            writeOpticalNeighbour(x, y + 1, refractionTint, (haloAlpha * 0.72f).roundToInt())
        }
    }

    val bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    return OpticalAtlasBrush(bitmap.asImageBitmap())
}

val LocalMaterialMicrostructure = staticCompositionLocalOf {
    MaterialMicrostructure(
        fine = SolidColor(Color.Transparent),
        spectral = SolidColor(Color.Transparent),
    )
}

/**
 * A single optical clock shared by every material layer. Reading it from a draw
 * block invalidates only drawing, rather than recomposing the complete screen.
 */
val LocalMaterialBreath = staticCompositionLocalOf<() -> Float> { { 0f } }

data class MaterialMotionProfile(
    val deformation: Float,
    val opticalDrift: Float,
    val trail: Float,
    val textureAlpha: Float,
)

val LocalMaterialMotionProfile = staticCompositionLocalOf {
    MaterialMotionProfile(
        deformation = 0.82f,
        opticalDrift = 0.78f,
        trail = 0.62f,
        textureAlpha = 0.92f,
    )
}

private data class OpticalViewport(
    val size: IntSize = IntSize.Zero,
    val originInWindow: Offset = Offset.Zero,
)

private val LocalOpticalViewport = staticCompositionLocalOf { OpticalViewport() }

enum class ProxySurfaceRole {
    CARD,
    INPUT,
    BUTTON,
    OVERLAY,
}

@Composable
fun ProxyScrollTheme(
    selectedTheme: AppTheme,
    interfaceShape: InterfaceShape = InterfaceShape(),
    stainSettings: StainSettings = StainSettings(),
    motionQuiet: Boolean = false,
    content: @Composable () -> Unit,
) {
    val targetScheme = when (selectedTheme) {
        AppTheme.LIQUID_GLASS -> LiquidGlassColors
        AppTheme.ROYAL_GRAPHITE -> RoyalGraphiteColors
        AppTheme.OLD_SCROLL -> OldScrollColors
        AppTheme.LITE_LIFE -> LiteLifeColors
        AppTheme.CYBERPUNK -> CyberpunkColors
    }
    val normalizedStainSettings = stainSettings.normalized()
    val context = LocalContext.current
    val lowRamDevice = remember(context) {
        context.getSystemService(ActivityManager::class.java)?.isLowRamDevice == true
    }
    val motionProfile = remember(normalizedStainSettings.motionQuality, lowRamDevice) {
        when (normalizedStainSettings.motionQuality) {
            MaterialMotionQuality.FULL -> MaterialMotionProfile(
                deformation = 1f,
                opticalDrift = 1f,
                trail = 1f,
                textureAlpha = 1f,
            )
            MaterialMotionQuality.LITE -> MaterialMotionProfile(
                deformation = 0.42f,
                opticalDrift = 0.28f,
                trail = 0f,
                textureAlpha = 0.72f,
            )
            MaterialMotionQuality.AUTO -> if (lowRamDevice) {
                MaterialMotionProfile(
                    deformation = 0.48f,
                    opticalDrift = 0.34f,
                    trail = 0f,
                    textureAlpha = 0.76f,
                )
            } else {
                MaterialMotionProfile(
                    deformation = 0.86f,
                    opticalDrift = 0.82f,
                    trail = 0.70f,
                    textureAlpha = 0.94f,
                )
            }
        }
    }
    val effectiveMotionProfile = if (selectedTheme == AppTheme.LITE_LIFE) {
        MaterialMotionProfile(
            deformation = 0f,
            opticalDrift = 0f,
            trail = 0f,
            textureAlpha = 0f,
        )
    } else {
        motionProfile
    }
    val animatedScheme = if (
        selectedTheme == AppTheme.ROYAL_GRAPHITE || selectedTheme == AppTheme.LITE_LIFE
    ) {
        targetScheme
    } else {
        animateScheme(targetScheme)
    }
    val visualStyle = if (selectedTheme == AppTheme.LITE_LIFE) {
        LiteLifeVisualStyle
    } else {
        animateVisualStyle(selectedTheme)
    }
    val targetStainPalette = paletteFor(selectedTheme, stainSettings.palette)
    val stainPalette = if (selectedTheme == AppTheme.LITE_LIFE) {
        targetStainPalette
    } else {
        animateStainPalette(target = targetStainPalette)
    }
    val materialMicrostructure = remember(selectedTheme, stainSettings.palette) {
        if (selectedTheme == AppTheme.LITE_LIFE) {
            MaterialMicrostructure(
                fine = SolidColor(Color.Transparent),
                spectral = SolidColor(Color.Transparent),
            )
        } else {
            val palette = paletteFor(selectedTheme, stainSettings.palette)
            MaterialMicrostructure(
                fine = createMaterialGrainBrush(
                    theme = selectedTheme,
                    palette = palette,
                    width = 631,
                    height = 887,
                    layer = MicrostructureLayer.FINE,
                ),
                spectral = createMaterialGrainBrush(
                    theme = selectedTheme,
                    palette = palette,
                    width = 827,
                    height = 1091,
                    layer = MicrostructureLayer.SPECTRAL,
                ),
            )
        }
    }
    val targetTypographyProgress = when (selectedTheme) {
            AppTheme.LIQUID_GLASS -> 0f
            AppTheme.ROYAL_GRAPHITE -> 1f
            AppTheme.OLD_SCROLL -> 0.38f
            AppTheme.LITE_LIFE -> 0.72f
            AppTheme.CYBERPUNK -> 1f
    }
    val typographyProgress = if (selectedTheme == AppTheme.LITE_LIFE) {
        targetTypographyProgress
    } else {
        animateFloatAsState(
            targetValue = targetTypographyProgress,
            animationSpec = tween(THEME_TRANSITION_MILLIS),
            label = "typography-material-transition",
        ).value
    }
    val typography = animatedTypography(typographyProgress, selectedTheme)
    val materialTransition = rememberInfiniteTransition(label = "shared-material-breath")
    val materialPhase = materialTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(normalizedStainSettings.motion.cycleMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shared-optical-phase",
    )
    val materialMotionScale = animateFloatAsState(
        targetValue = if (motionQuiet) {
            0.04f
        } else {
            normalizedStainSettings.motion.amplitudeFactor * motionProfile.opticalDrift
        },
        animationSpec = tween(if (motionQuiet) 180 else 480, easing = FastOutSlowInEasing),
        label = "shared-material-motion-scale",
    )
    val materialBreathReader = remember(selectedTheme, materialPhase, materialMotionScale) {
        if (selectedTheme == AppTheme.LITE_LIFE) {
            { 0f }
        } else {
            { materialPhase.value * materialMotionScale.value }
        }
    }
    val effectiveInterfaceShape = remember(selectedTheme, interfaceShape) {
        interfaceShape.resolveFor(selectedTheme)
    }
    val view = LocalView.current
    var opticalViewport by remember { mutableStateOf(OpticalViewport()) }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            val lightIcons = selectedTheme != AppTheme.ROYAL_GRAPHITE &&
                selectedTheme != AppTheme.LITE_LIFE &&
                selectedTheme != AppTheme.CYBERPUNK
            controller.isAppearanceLightStatusBars = lightIcons
            controller.isAppearanceLightNavigationBars = lightIcons
        }
    }

    CompositionLocalProvider(
        LocalProxyVisualStyle provides visualStyle,
        LocalProxyShape provides effectiveInterfaceShape,
        LocalStainSettings provides normalizedStainSettings,
        LocalStainPaletteColors provides stainPalette,
        LocalMaterialMicrostructure provides materialMicrostructure,
        LocalMaterialBreath provides materialBreathReader,
        LocalMaterialMotionProfile provides effectiveMotionProfile,
        LocalOpticalViewport provides opticalViewport,
    ) {
        MaterialTheme(
            colorScheme = animatedScheme,
            typography = typography,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides animatedScheme.onBackground,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { coordinates ->
                            val bounds = coordinates.boundsInWindow()
                            val next = OpticalViewport(
                                size = coordinates.size,
                                originInWindow = Offset(bounds.left, bounds.top),
                            )
                            if (next != opticalViewport) opticalViewport = next
                        },
                ) {
                    content()
                }
            }
        }
    }
}

private fun paletteFor(theme: AppTheme, palette: StainPalette): StainPaletteColors {
    if (theme == AppTheme.ROYAL_GRAPHITE) return GraphiteOilColors
    if (theme == AppTheme.OLD_SCROLL) return OldScrollMaterialColors
    if (theme == AppTheme.LITE_LIFE) return LiteLifeMaterialColors
    if (theme == AppTheme.CYBERPUNK) return CyberpunkMaterialColors
    return when (palette) {
        StainPalette.AURORA_OPAL -> AuroraOpalColors
        StainPalette.CORAL_GLACIER -> CoralGlacierColors
        StainPalette.NORDIC_BLOOM -> NordicBloomColors
    }
}

@Composable
private fun animateStainPalette(target: StainPaletteColors): StainPaletteColors {
    @Composable
    fun animated(color: Color, label: String) = animateColorAsState(
        targetValue = color,
        animationSpec = tween(THEME_TRANSITION_MILLIS),
        label = label,
    ).value

    return StainPaletteColors(
        primary = animated(target.primary, "stain-primary"),
        secondary = animated(target.secondary, "stain-secondary"),
        tertiary = animated(target.tertiary, "stain-tertiary"),
        neutral = animated(target.neutral, "stain-neutral"),
        caustic = animated(target.caustic, "stain-caustic"),
    )
}

@Composable
private fun animatedTypography(progress: Float, theme: AppTheme): Typography {
    fun between(start: Float, end: Float) = start + (end - start) * progress
    val materialFont = if (theme == AppTheme.OLD_SCROLL) FontFamily.Serif else FontFamily.SansSerif
    return Typography(
        headlineMedium = TextStyle(
            fontFamily = materialFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = between(30f, 29f).sp,
            lineHeight = between(36f, 37f).sp,
            letterSpacing = between(-0.35f, 0.18f).sp,
        ),
        titleLarge = TextStyle(
            fontFamily = materialFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = between(22f, 21.5f).sp,
            lineHeight = between(28f, 29f).sp,
            letterSpacing = between(-0.10f, 0.12f).sp,
        ),
        titleMedium = TextStyle(
            fontFamily = materialFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            lineHeight = between(23f, 24f).sp,
            letterSpacing = between(0f, 0.10f).sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = materialFont,
            fontWeight = FontWeight.Normal,
            fontSize = 19.sp,
            lineHeight = between(29f, 30f).sp,
            letterSpacing = between(0f, 0.08f).sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = materialFont,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = between(22f, 23f).sp,
            letterSpacing = between(0f, 0.07f).sp,
        ),
    )
}

@Composable
private fun animateScheme(target: ColorScheme): ColorScheme {
    @Composable
    fun animated(color: Color, label: String) = animateColorAsState(
        targetValue = color,
        animationSpec = tween(THEME_TRANSITION_MILLIS),
        label = label,
    ).value

    return target.copy(
        primary = animated(target.primary, "theme-primary"),
        onPrimary = animated(target.onPrimary, "theme-on-primary"),
        primaryContainer = animated(target.primaryContainer, "theme-primary-container"),
        onPrimaryContainer = animated(target.onPrimaryContainer, "theme-on-primary-container"),
        secondary = animated(target.secondary, "theme-secondary"),
        onSecondary = animated(target.onSecondary, "theme-on-secondary"),
        background = animated(target.background, "theme-background"),
        onBackground = animated(target.onBackground, "theme-on-background"),
        surface = animated(target.surface, "theme-surface"),
        onSurface = animated(target.onSurface, "theme-on-surface"),
        surfaceVariant = animated(target.surfaceVariant, "theme-surface-variant"),
        onSurfaceVariant = animated(target.onSurfaceVariant, "theme-on-surface-variant"),
        outline = animated(target.outline, "theme-outline"),
        error = animated(target.error, "theme-error"),
    )
}

@Composable
private fun animateVisualStyle(theme: AppTheme): ProxyVisualStyle {
    val target = when (theme) {
        AppTheme.LIQUID_GLASS -> LiquidVisualStyle
        AppTheme.ROYAL_GRAPHITE -> GraphiteVisualStyle
        AppTheme.OLD_SCROLL -> OldScrollVisualStyle
        AppTheme.LITE_LIFE -> LiteLifeVisualStyle
        AppTheme.CYBERPUNK -> CyberpunkVisualStyle
    }

    @Composable
    fun animated(color: Color, label: String) = animateColorAsState(
        targetValue = color,
        animationSpec = tween(THEME_TRANSITION_MILLIS),
        label = label,
    ).value

    return target.copy(
        theme = theme,
        materialTop = animated(target.materialTop, "material-top"),
        materialMiddle = animated(target.materialMiddle, "material-middle"),
        materialBottom = animated(target.materialBottom, "material-bottom"),
        strongTop = animated(target.strongTop, "strong-top"),
        strongBottom = animated(target.strongBottom, "strong-bottom"),
        rimLight = animated(target.rimLight, "rim-light"),
        rimShade = animated(target.rimShade, "rim-shade"),
        specular = animated(target.specular, "specular"),
        shadow = animated(target.shadow, "material-shadow"),
        scrim = animated(target.scrim, "material-scrim"),
    )
}

/**
 * One screen-space scene is shared by the Liquid Glass background and every
 * optical surface. Re-projecting the same landmarks inside a clipped surface
 * makes the material transmit, magnify, and displace its environment instead
 * of painting an unrelated translucent gradient over it.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLiquidOpticalScene(
    viewportSize: Size,
    viewportOrigin: Offset,
    palette: StainPaletteColors,
    stain: Float,
    activeDrift: Float,
    magnification: Float = 1f,
    displacement: Offset = Offset.Zero,
) {
    val sceneSize = if (viewportSize.width > 0f && viewportSize.height > 0f) {
        viewportSize
    } else {
        size
    }
    val localCenter = Offset(size.width * 0.5f, size.height * 0.5f)
    val opticalCenterInScene = viewportOrigin + localCenter
    fun project(point: Offset): Offset = localCenter +
        (point - opticalCenterInScene - displacement) * magnification
    fun radius(value: Float): Float = value * magnification

    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFBFCFF),
                palette.neutral,
                Color(0xFFE8F0F5),
            ),
            start = project(Offset.Zero),
            end = project(Offset(sceneSize.width, sceneSize.height)),
        ),
    )

    val primaryWellInScene = Offset(
        x = sceneSize.width * (0.12f + activeDrift * 0.045f),
        y = sceneSize.height * (0.16f - activeDrift * 0.012f),
    )
    val primaryWell = project(primaryWellInScene)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                palette.primary.copy(alpha = 0.30f * stain),
                palette.primary.copy(alpha = 0.105f * stain),
                Color.Transparent,
            ),
            center = primaryWell,
            radius = radius(sceneSize.width * 0.94f),
        ),
        center = primaryWell,
        radius = radius(sceneSize.width * 0.94f),
    )

    val secondaryWellInScene = Offset(
        x = sceneSize.width * (0.91f - activeDrift * 0.035f),
        y = sceneSize.height * (0.55f + activeDrift * 0.016f),
    )
    val secondaryWell = project(secondaryWellInScene)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                palette.secondary.copy(alpha = 0.25f * stain),
                palette.secondary.copy(alpha = 0.065f * stain),
                Color.Transparent,
            ),
            center = secondaryWell,
            radius = radius(sceneSize.width * 0.78f),
        ),
        center = secondaryWell,
        radius = radius(sceneSize.width * 0.78f),
    )

    val tertiaryWellInScene = Offset(
        x = sceneSize.width * (0.47f + activeDrift * 0.06f),
        y = sceneSize.height * (0.88f - activeDrift * 0.018f),
    )
    val tertiaryWell = project(tertiaryWellInScene)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                palette.tertiary.copy(alpha = 0.21f * stain),
                palette.tertiary.copy(alpha = 0.052f * stain),
                Color.Transparent,
            ),
            center = tertiaryWell,
            radius = radius(sceneSize.width * 0.86f),
        ),
        center = tertiaryWell,
        radius = radius(sceneSize.width * 0.86f),
    )

    // Broad environmental lights are deliberately recognisable. Their small
    // discontinuity at a glass edge is what makes refraction readable.
    val ribbonStart = project(
        Offset(
            sceneSize.width * (-0.16f + activeDrift * 0.025f),
            sceneSize.height * 0.08f,
        ),
    )
    val ribbonEnd = project(
        Offset(
            sceneSize.width * (0.94f + activeDrift * 0.025f),
            sceneSize.height * 0.70f,
        ),
    )
    drawLine(
        color = Color.White.copy(alpha = 0.038f),
        start = ribbonStart,
        end = ribbonEnd,
        strokeWidth = radius(34.dp.toPx()),
        cap = StrokeCap.Round,
    )
    drawLine(
        color = palette.caustic.copy(alpha = 0.10f * stain),
        start = ribbonStart,
        end = ribbonEnd,
        strokeWidth = radius(1.15.dp.toPx()),
        cap = StrokeCap.Round,
    )

    val lowerRibbonStart = project(
        Offset(sceneSize.width * 0.04f, sceneSize.height * 0.79f),
    )
    val lowerRibbonEnd = project(
        Offset(sceneSize.width * 1.02f, sceneSize.height * 0.56f),
    )
    drawLine(
        color = palette.secondary.copy(alpha = 0.048f * stain),
        start = lowerRibbonStart,
        end = lowerRibbonEnd,
        strokeWidth = radius(18.dp.toPx()),
        cap = StrokeCap.Round,
    )
    drawLine(
        color = Color.White.copy(alpha = 0.085f),
        start = lowerRibbonStart,
        end = lowerRibbonEnd,
        strokeWidth = radius(0.8.dp.toPx()),
        cap = StrokeCap.Round,
    )

    repeat(4) { index ->
        val normalizedCenter = when (index) {
            0 -> Offset(0.20f, 0.35f)
            1 -> Offset(0.76f, 0.21f)
            2 -> Offset(0.63f, 0.73f)
            else -> Offset(0.28f, 0.90f)
        }
        val orbCenter = project(
            Offset(
                sceneSize.width * (normalizedCenter.x + activeDrift * 0.006f * (index - 1)),
                sceneSize.height * (normalizedCenter.y - activeDrift * 0.004f * index),
            ),
        )
        val orbRadius = radius(sceneSize.width * (0.020f + index * 0.004f))
        drawCircle(
            color = Color.White.copy(alpha = 0.038f + index * 0.006f),
            radius = orbRadius,
            center = orbCenter,
        )
        drawCircle(
            color = palette.caustic.copy(alpha = 0.12f * stain),
            radius = orbRadius,
            center = orbCenter,
            style = Stroke(width = radius(0.7.dp.toPx())),
        )
    }

    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.15f),
                Color.Transparent,
                palette.caustic.copy(alpha = 0.06f * stain),
                Color.White.copy(alpha = 0.075f),
            ),
            start = project(
                Offset(sceneSize.width * (0.04f + activeDrift * 0.04f), 0f),
            ),
            end = project(
                Offset(sceneSize.width * (0.82f + activeDrift * 0.04f), sceneSize.height),
            ),
        ),
    )
}

@Composable
fun ProxyThemeBackground(
    selectedTheme: AppTheme,
    modifier: Modifier = Modifier,
) {
    if (selectedTheme == AppTheme.LITE_LIFE) {
        MaterialBackground(selectedTheme)
        return
    }
    Crossfade(
        targetState = selectedTheme,
        modifier = modifier,
        animationSpec = tween(THEME_TRANSITION_MILLIS + 180),
        label = "material-background",
    ) { theme ->
        MaterialBackground(theme)
    }
}

@Composable
private fun MaterialBackground(
    theme: AppTheme,
) {
    val settings = LocalStainSettings.current
    val palette = LocalStainPaletteColors.current
    val microstructure = LocalMaterialMicrostructure.current
    val materialBreath = LocalMaterialBreath.current
    val motionProfile = LocalMaterialMotionProfile.current
    val intensity = animateFloatAsState(
        targetValue = settings.intensity,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "stain-intensity",
    )

    Canvas(Modifier.fillMaxSize()) {
        val activeDrift = materialBreath()
        val stain = intensity.value
        when (theme) {
            AppTheme.LIQUID_GLASS -> {
                drawLiquidOpticalScene(
                    viewportSize = size,
                    viewportOrigin = Offset.Zero,
                    palette = palette,
                    stain = stain,
                    activeDrift = activeDrift,
                )
            }
            AppTheme.ROYAL_GRAPHITE -> {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF151D21),
                        Color(0xFF090E11),
                        Color(0xFF040607),
                    ),
                ),
            )
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        palette.primary.copy(alpha = 0.14f * stain),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.34f),
                    ),
                    start = Offset(size.width * (-0.18f + activeDrift * 0.03f), 0f),
                    end = Offset(size.width * 0.92f, size.height),
                ),
            )
            val wetLight = Offset(
                size.width * (0.17f + activeDrift * 0.025f),
                size.height * (0.22f + activeDrift * 0.025f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.secondary.copy(alpha = 0.16f * stain),
                        palette.primary.copy(alpha = 0.055f * stain),
                        Color.Transparent,
                    ),
                    center = wetLight,
                    radius = size.width * 0.64f,
                ),
                center = wetLight,
                radius = size.width * 0.64f,
            )
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.tertiary.copy(alpha = 0.13f * stain),
                        palette.secondary.copy(alpha = 0.035f * stain),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.62f, size.height * 0.91f),
                    radius = size.width * 0.72f,
                ),
                topLeft = Offset(-size.width * 0.1f, size.height * 0.82f),
                size = androidx.compose.ui.geometry.Size(
                    size.width * 1.2f,
                    size.height * 0.16f,
                ),
            )
            // A narrow wet reflection travels more slowly than the broad light
            // well, so graphite reads as layered oil rather than a flat gradient.
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        palette.caustic.copy(alpha = 0.075f * stain),
                        Color.White.copy(alpha = 0.035f * stain),
                        Color.Transparent,
                    ),
                    start = Offset(size.width * (-0.55f + activeDrift * 0.18f), 0f),
                    end = Offset(size.width * (0.20f + activeDrift * 0.18f), size.height),
                ),
            )
            }
            AppTheme.OLD_SCROLL -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF6EACD),
                            palette.neutral,
                            Color(0xFFE0C99D),
                            Color(0xFFD4B985),
                        ),
                    ),
                )
                // The ruling lives inside the paper substrate: it receives the
                // same warm light, edge ageing and grain as the sheet itself.
                val ruleSpacing = 30.dp.toPx()
                var ruleY = ruleSpacing * 2.15f
                while (ruleY < size.height + ruleSpacing) {
                    drawLine(
                        color = Color(0xFF6F89A0).copy(alpha = 0.115f),
                        start = Offset(0f, ruleY),
                        end = Offset(size.width, ruleY),
                        strokeWidth = 0.72.dp.toPx(),
                    )
                    drawLine(
                        color = palette.caustic.copy(alpha = 0.075f),
                        start = Offset(0f, ruleY + 1.15.dp.toPx()),
                        end = Offset(size.width, ruleY + 1.15.dp.toPx()),
                        strokeWidth = 0.42.dp.toPx(),
                    )
                    ruleY += ruleSpacing
                }
                drawLine(
                    color = Color(0xFFB46B5D).copy(alpha = 0.12f),
                    start = Offset(44.dp.toPx(), 0f),
                    end = Offset(44.dp.toPx(), size.height),
                    strokeWidth = 0.85.dp.toPx(),
                )
                val warmLight = Offset(
                    x = size.width * (0.36f + activeDrift * 0.035f),
                    y = size.height * (0.22f - activeDrift * 0.012f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            palette.caustic.copy(alpha = 0.42f * stain),
                            Color(0xFFFFE9B9).copy(alpha = 0.16f * stain),
                            Color.Transparent,
                        ),
                        center = warmLight,
                        radius = size.width * 0.92f,
                    ),
                    center = warmLight,
                    radius = size.width * 0.92f,
                )
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF72502C).copy(alpha = 0.17f),
                            Color.Transparent,
                            Color.Transparent,
                            Color(0xFF684625).copy(alpha = 0.15f),
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width, 0f),
                    ),
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color(0xFF6A4829).copy(alpha = 0.18f),
                        ),
                        center = Offset(size.width * 0.52f, size.height * 0.44f),
                        radius = maxOf(size.width, size.height) * 0.76f,
                    ),
                )
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            palette.caustic.copy(alpha = 0.095f * stain),
                            Color.Transparent,
                        ),
                        start = Offset(size.width * (-0.40f + activeDrift * 0.12f), 0f),
                        end = Offset(size.width * (0.42f + activeDrift * 0.12f), size.height),
                    ),
                )
            }
            AppTheme.CYBERPUNK -> {
                val signalYellow = Color(0xFFF4E900)
                val emergencyRed = Color(0xFFFF3B30)
                val splitCyan = Color(0xFF00E7FF)
                val glitchPulse = abs(
                    sin((activeDrift * 31f + 1.37f).toDouble()),
                ).toFloat()

                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF11120F),
                            Color(0xFF070809),
                            Color(0xFF030405),
                        ),
                    ),
                )

                // Asymmetric signal panels echo industrial circuit boards while
                // leaving the central reading column calm and high-contrast.
                val upperSignal = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width * 0.43f, 0f)
                    lineTo(size.width * 0.35f, size.height * 0.055f)
                    lineTo(size.width * 0.25f, size.height * 0.055f)
                    lineTo(size.width * 0.19f, size.height * 0.11f)
                    lineTo(size.width * 0.06f, size.height * 0.11f)
                    lineTo(0f, size.height * 0.15f)
                    close()
                }
                drawPath(
                    path = upperSignal,
                    color = signalYellow.copy(alpha = (0.82f * stain).coerceAtMost(0.96f)),
                )
                val rightSignal = Path().apply {
                    moveTo(size.width, size.height * 0.14f)
                    lineTo(size.width * 0.91f, size.height * 0.18f)
                    lineTo(size.width * 0.91f, size.height * 0.36f)
                    lineTo(size.width * 0.84f, size.height * 0.40f)
                    lineTo(size.width * 0.84f, size.height * 0.58f)
                    lineTo(size.width * 0.92f, size.height * 0.62f)
                    lineTo(size.width * 0.92f, size.height * 0.82f)
                    lineTo(size.width, size.height * 0.88f)
                    close()
                }
                drawPath(
                    path = rightSignal,
                    color = signalYellow.copy(alpha = (0.17f + 0.17f * stain)),
                )
                val lowerSignal = Path().apply {
                    moveTo(0f, size.height * 0.78f)
                    lineTo(size.width * 0.07f, size.height * 0.75f)
                    lineTo(size.width * 0.17f, size.height * 0.75f)
                    lineTo(size.width * 0.22f, size.height * 0.81f)
                    lineTo(size.width * 0.38f, size.height * 0.81f)
                    lineTo(size.width * 0.46f, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(
                    path = lowerSignal,
                    color = signalYellow.copy(alpha = (0.10f + 0.08f * stain)),
                )

                // Sparse circuit traces and contact points are intentionally
                // concentrated at the edges so long-form text never glitches.
                val circuitStroke = 1.05.dp.toPx()
                repeat(4) { index ->
                    val y = size.height * (0.19f + index * 0.115f)
                    val xEnd = size.width * (0.10f + (index % 2) * 0.045f)
                    val trace = Path().apply {
                        moveTo(0f, y)
                        lineTo(xEnd * 0.42f, y)
                        lineTo(xEnd * 0.62f, y + 9.dp.toPx())
                        lineTo(xEnd, y + 9.dp.toPx())
                    }
                    drawPath(
                        path = trace,
                        color = signalYellow.copy(alpha = 0.26f * stain),
                        style = Stroke(width = circuitStroke, cap = StrokeCap.Square),
                    )
                    drawCircle(
                        color = if (index == 2) emergencyRed else signalYellow,
                        radius = if (index == 2) 2.2.dp.toPx() else 1.55.dp.toPx(),
                        center = Offset(xEnd, y + 9.dp.toPx()),
                        alpha = (0.40f + index * 0.07f) * stain,
                    )
                }

                val scanStep = 8.dp.toPx()
                var scanY = scanStep
                while (scanY < size.height) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.012f + 0.008f * stain),
                        start = Offset(0f, scanY),
                        end = Offset(size.width, scanY),
                        strokeWidth = 0.45.dp.toPx(),
                    )
                    scanY += scanStep
                }

                // RGB split is shown as short displaced fragments, not a blur.
                val burstOffset = (activeDrift * 22.dp.toPx()).coerceIn(
                    -12.dp.toPx(),
                    12.dp.toPx(),
                )
                val glitchRows = floatArrayOf(0.17f, 0.34f, 0.57f, 0.73f, 0.91f)
                glitchRows.forEachIndexed { index, row ->
                    val fragmentWidth = size.width * (0.12f + (index % 3) * 0.065f)
                    val baseX = if (index % 2 == 0) {
                        size.width * (0.03f + index * 0.07f)
                    } else {
                        size.width - fragmentWidth - size.width * 0.045f
                    }
                    val barHeight = (if (index == 2) 3.0f else 1.35f).dp.toPx()
                    val alpha = (0.12f + glitchPulse * 0.22f) * stain
                    drawRect(
                        color = emergencyRed.copy(alpha = alpha),
                        topLeft = Offset(baseX - burstOffset - 2.dp.toPx(), size.height * row),
                        size = Size(fragmentWidth, barHeight),
                    )
                    drawRect(
                        color = splitCyan.copy(alpha = alpha * 0.78f),
                        topLeft = Offset(baseX + burstOffset + 2.dp.toPx(), size.height * row + barHeight),
                        size = Size(fragmentWidth * 0.72f, 0.85.dp.toPx()),
                    )
                    drawRect(
                        color = signalYellow.copy(alpha = alpha * 1.12f),
                        topLeft = Offset(baseX, size.height * row + 0.4.dp.toPx()),
                        size = Size(fragmentWidth * 0.88f, 0.75.dp.toPx()),
                    )
                }

                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            emergencyRed.copy(alpha = 0.055f * glitchPulse * stain),
                            Color.Transparent,
                            splitCyan.copy(alpha = 0.04f * glitchPulse * stain),
                            Color.Transparent,
                        ),
                        start = Offset(size.width * (-0.3f + activeDrift * 0.15f), 0f),
                        end = Offset(size.width * (0.7f + activeDrift * 0.15f), size.height),
                    ),
                )
            }
            AppTheme.LITE_LIFE -> {
                drawRect(color = Color(0xFF101115))
            }
        }

        if (
            motionProfile.trail > 0.01f &&
            (
                theme == AppTheme.LIQUID_GLASS ||
                    theme == AppTheme.ROYAL_GRAPHITE ||
                    theme == AppTheme.CYBERPUNK
                )
        ) {
            val trailColor = when (theme) {
                AppTheme.LIQUID_GLASS -> palette.caustic
                AppTheme.ROYAL_GRAPHITE -> palette.secondary
                AppTheme.OLD_SCROLL -> palette.caustic
                AppTheme.LITE_LIFE -> palette.primary
                AppTheme.CYBERPUNK -> palette.tertiary
            }
            repeat(2) { index ->
                val step = index + 1f
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            trailColor.copy(
                                alpha = (0.030f / step) * stain * motionProfile.trail,
                            ),
                            Color.Transparent,
                        ),
                        start = Offset(
                            size.width * (-0.34f + activeDrift * 0.16f - step * 0.045f),
                            0f,
                        ),
                        end = Offset(
                            size.width * (0.28f + activeDrift * 0.16f - step * 0.045f),
                            size.height,
                        ),
                    ),
                )
            }
        }

        val fineAlpha = when (theme) {
            AppTheme.LIQUID_GLASS -> (0.075f * stain).coerceIn(0.025f, 0.09f)
            AppTheme.ROYAL_GRAPHITE -> (0.23f * stain).coerceIn(0.09f, 0.29f)
            AppTheme.OLD_SCROLL -> (0.46f * stain).coerceIn(0.16f, 0.52f)
            AppTheme.LITE_LIFE -> 0f
            AppTheme.CYBERPUNK -> (0.18f * stain).coerceIn(0.06f, 0.24f)
        } * motionProfile.textureAlpha
        val spectralAlpha = when (theme) {
            AppTheme.LIQUID_GLASS -> (0.035f * stain).coerceIn(0.012f, 0.055f)
            AppTheme.ROYAL_GRAPHITE -> (0.12f * stain).coerceIn(0.04f, 0.16f)
            AppTheme.OLD_SCROLL -> (0.32f * stain).coerceIn(0.10f, 0.38f)
            AppTheme.LITE_LIFE -> 0f
            AppTheme.CYBERPUNK -> (0.16f * stain).coerceIn(0.05f, 0.22f)
        } * motionProfile.textureAlpha
        val overscan = 28f
        withTransform({
            val motion = if (theme == AppTheme.OLD_SCROLL) 2.5f else 11f
            translate(activeDrift * motion, activeDrift * -motion * 0.64f)
        }) {
            drawRect(
                brush = microstructure.fine,
                topLeft = Offset(-overscan, -overscan),
                size = Size(size.width + overscan * 2f, size.height + overscan * 2f),
                alpha = fineAlpha,
            )
        }
        withTransform({
            val motion = if (theme == AppTheme.OLD_SCROLL) 5f else 17f
            translate(activeDrift * -motion, activeDrift * motion * 0.53f)
        }) {
            drawRect(
                brush = microstructure.spectral,
                topLeft = Offset(-overscan, -overscan),
                size = Size(size.width + overscan * 2f, size.height + overscan * 2f),
                alpha = spectralAlpha,
            )
        }
    }
}

@Composable
fun ProxySurface(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    role: ProxySurfaceRole = ProxySurfaceRole.CARD,
    strong: Boolean = false,
    active: Boolean = false,
    deformContent: Boolean = true,
    interactive: Boolean = true,
    recessed: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val style = LocalProxyVisualStyle.current
    val shapeSettings = LocalProxyShape.current
    if (style.theme == AppTheme.LITE_LIFE) {
        val fill = when {
            strong -> Color(0xFF232831)
            role == ProxySurfaceRole.INPUT -> Color(0xFF1D2026)
            role == ProxySurfaceRole.OVERLAY -> Color(0xFF171A20)
            else -> Color(0xFF1A1D23)
        }
        val outline = if (active) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.34f)
        }
        Box(
            modifier = modifier
                .clip(RectangleShape)
                .background(fill)
                .border(1.dp, outline, RectangleShape),
        ) {
            content()
        }
        return
    }
    val stainSettings = LocalStainSettings.current
    val palette = LocalStainPaletteColors.current
    val microstructure = LocalMaterialMicrostructure.current
    val materialBreath = LocalMaterialBreath.current
    val motionProfile = LocalMaterialMotionProfile.current
    val opticalViewport = LocalOpticalViewport.current
    val cornerDp = when (role) {
        ProxySurfaceRole.CARD -> shapeSettings.resolvedCardCornerDp
        ProxySurfaceRole.INPUT -> shapeSettings.resolvedInputCornerDp
        ProxySurfaceRole.BUTTON -> shapeSettings.resolvedButtonCornerDp
        ProxySurfaceRole.OVERLAY -> (shapeSettings.globalCornerDp + 6).coerceAtMost(30)
    }
    val animatedCornerDp = animateFloatAsState(
        targetValue = cornerDp.toFloat(),
        animationSpec = tween(220),
        label = "surface-corner-${role.name.lowercase()}",
    ).value
    var materialPressed by remember { mutableStateOf(false) }
    var pressPosition by remember { mutableStateOf(Offset.Zero) }
    var surfaceSize by remember { mutableStateOf(IntSize.Zero) }
    var surfaceOrigin by remember { mutableStateOf(Offset.Zero) }
    val pressSpring = when (style.theme) {
        AppTheme.LIQUID_GLASS -> if (materialPressed) 900f else 620f
        AppTheme.ROYAL_GRAPHITE -> if (materialPressed) 920f else 560f
        AppTheme.OLD_SCROLL -> if (materialPressed) 1_160f else 760f
        AppTheme.LITE_LIFE -> if (materialPressed) 1_180f else 820f
        AppTheme.CYBERPUNK -> if (materialPressed) 1_260f else 690f
    }
    val releaseDamping = when (style.theme) {
        AppTheme.LIQUID_GLASS -> if (materialPressed) 0.90f else 0.84f
        AppTheme.ROYAL_GRAPHITE -> if (materialPressed) 0.88f else 0.72f
        AppTheme.OLD_SCROLL -> 0.92f
        AppTheme.LITE_LIFE -> 0.94f
        AppTheme.CYBERPUNK -> if (materialPressed) 0.94f else 0.78f
    }
    val compression by animateFloatAsState(
        targetValue = if (materialPressed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = releaseDamping,
            stiffness = pressSpring,
        ),
        label = "material-compression-${role.name.lowercase()}",
    )
    val materialCompression = compression * motionProfile.deformation * when (style.theme) {
        AppTheme.LIQUID_GLASS -> 1f
        AppTheme.ROYAL_GRAPHITE -> 0.72f
        AppTheme.OLD_SCROLL -> 0.34f
        AppTheme.LITE_LIFE -> 0.22f
        AppTheme.CYBERPUNK -> 0.46f
    }
    val clarity by animateFloatAsState(
        targetValue = when {
            materialPressed -> 1f
            active -> 0.72f
            else -> 0f
        },
        animationSpec = tween(
            durationMillis = if (materialPressed || active) 130 else 520,
            easing = FastOutSlowInEasing,
        ),
        label = "material-clarity-${role.name.lowercase()}",
    )
    val depthFactor = stainSettings.depth.opticalFactor
    val geometryCompression = if (style.theme == AppTheme.LIQUID_GLASS) {
        0f
    } else {
        materialCompression
    }
    val morphCornerDp = animatedCornerDp + geometryCompression * when (role) {
        ProxySurfaceRole.BUTTON -> 4.8f
        ProxySurfaceRole.INPUT -> 3.6f
        ProxySurfaceRole.CARD -> 3.0f
        ProxySurfaceRole.OVERLAY -> 2.2f
    }
    val resolvedShape = shape ?: if (style.theme == AppTheme.CYBERPUNK) {
        CutCornerShape(
            topStart = (morphCornerDp * 0.28f).dp,
            topEnd = (morphCornerDp + 8f).dp,
            bottomEnd = (morphCornerDp * 0.22f).dp,
            bottomStart = (morphCornerDp + 4f).dp,
        )
    } else {
        RoundedCornerShape(morphCornerDp.dp)
    }
    val materialFactor = when (style.theme) {
        AppTheme.LIQUID_GLASS -> when (role) {
            ProxySurfaceRole.CARD -> 0.70f
            ProxySurfaceRole.INPUT -> 0.82f
            ProxySurfaceRole.BUTTON -> 1.00f
            ProxySurfaceRole.OVERLAY -> 1.08f
        }
        AppTheme.ROYAL_GRAPHITE -> when (role) {
            ProxySurfaceRole.CARD -> 0.84f
            ProxySurfaceRole.INPUT -> 0.90f
            ProxySurfaceRole.BUTTON -> 1.00f
            ProxySurfaceRole.OVERLAY -> 1.05f
        }
        AppTheme.OLD_SCROLL -> when (role) {
            ProxySurfaceRole.CARD -> 0.92f
            ProxySurfaceRole.INPUT -> 0.96f
            ProxySurfaceRole.BUTTON -> 1.00f
            ProxySurfaceRole.OVERLAY -> 1.02f
        }
        AppTheme.LITE_LIFE -> when (role) {
            ProxySurfaceRole.CARD -> 0.96f
            ProxySurfaceRole.INPUT -> 0.98f
            ProxySurfaceRole.BUTTON -> 1.00f
            ProxySurfaceRole.OVERLAY -> 1.00f
        }
        AppTheme.CYBERPUNK -> when (role) {
            ProxySurfaceRole.CARD -> 0.94f
            ProxySurfaceRole.INPUT -> 0.97f
            ProxySurfaceRole.BUTTON -> 1.00f
            ProxySurfaceRole.OVERLAY -> 1.04f
        }
    }
    val transmissionFactor = when (style.theme) {
        AppTheme.LIQUID_GLASS -> 1f - clarity * 0.64f
        AppTheme.ROYAL_GRAPHITE -> 1f - clarity * 0.34f
        AppTheme.OLD_SCROLL -> 1f - clarity * 0.14f
        AppTheme.LITE_LIFE -> 1f
        AppTheme.CYBERPUNK -> 1f - clarity * 0.10f
    }
    fun scaled(color: Color, extra: Float = 1f) = color.copy(
        alpha = (color.alpha * materialFactor * extra * transmissionFactor)
            .coerceIn(0f, 1f),
    )
    val top = scaled(if (strong) style.strongTop else style.materialTop)
    val middle = scaled(style.materialMiddle, if (strong) 1.20f else 1f)
    val bottom = scaled(if (strong) style.strongBottom else style.materialBottom)
    val roleStainFactor = when (role) {
        ProxySurfaceRole.CARD -> 0.20f
        ProxySurfaceRole.INPUT -> 0.28f
        ProxySurfaceRole.BUTTON -> 0.48f
        ProxySurfaceRole.OVERLAY -> 0.32f
    }
    val themeStainFactor = when (style.theme) {
        AppTheme.LIQUID_GLASS -> 1f
        AppTheme.ROYAL_GRAPHITE -> 0.40f
        AppTheme.OLD_SCROLL -> 0.56f
        AppTheme.LITE_LIFE -> 0.08f
        AppTheme.CYBERPUNK -> 1.10f
    }
    val stainAlpha = stainSettings.intensity * roleStainFactor * themeStainFactor * depthFactor *
        (1f + clarity * 0.62f)
    val baseElevation = when {
        recessed -> 0.5.dp
        strong && style.theme == AppTheme.LIQUID_GLASS -> 8.dp
        style.theme == AppTheme.LIQUID_GLASS -> 5.dp
        strong && style.theme == AppTheme.OLD_SCROLL -> 5.dp
        style.theme == AppTheme.OLD_SCROLL -> 2.5.dp
        strong && style.theme == AppTheme.LITE_LIFE -> 2.dp
        style.theme == AppTheme.LITE_LIFE -> 1.dp
        strong -> 7.dp
        else -> 4.dp
    }
    val elevation by animateDpAsState(
        targetValue = baseElevation + (2.4f * clarity).dp,
        animationSpec = tween(240, easing = FastOutSlowInEasing),
        label = "material-elevation-${role.name.lowercase()}",
    )
    val verticalCompression = when (role) {
        ProxySurfaceRole.BUTTON -> 0.038f
        ProxySurfaceRole.INPUT -> 0.024f
        ProxySurfaceRole.CARD -> 0.018f
        ProxySurfaceRole.OVERLAY -> 0.012f
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                val deformGeometry = deformContent && style.theme != AppTheme.LIQUID_GLASS
                scaleX = if (deformGeometry) 1f + materialCompression * 0.006f else 1f
                scaleY = if (deformGeometry) {
                    1f - materialCompression * verticalCompression
                } else {
                    1f
                }
                translationY = if (deformGeometry) {
                    materialCompression * 1.15.dp.toPx()
                } else {
                    0f
                }
            }
            .shadow(
                elevation = elevation,
                shape = resolvedShape,
                ambientColor = style.shadow,
                spotColor = style.shadow,
            )
            .clip(resolvedShape)
            .onSizeChanged { surfaceSize = it }
            .onGloballyPositioned { coordinates ->
                val bounds = coordinates.boundsInWindow()
                surfaceOrigin = Offset(
                    x = bounds.left - opticalViewport.originInWindow.x,
                    y = bounds.top - opticalViewport.originInWindow.y,
                )
            }
            .then(
                if (interactive) {
                    Modifier
                        .pointerInput(role) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                pressPosition = down.position
                                materialPressed = true
                                try {
                                    var pointerPressed = true
                                    while (pointerPressed) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == down.id }
                                        if (change == null) {
                                            pointerPressed = false
                                        } else {
                                            pressPosition = change.position
                                            pointerPressed = change.pressed
                                        }
                                    }
                                } finally {
                                    materialPressed = false
                                }
                            }
                        }
                } else {
                    Modifier
                },
            ),
    ) {
        if (
            style.theme == AppTheme.LIQUID_GLASS &&
            opticalViewport.size.width > 0 &&
            opticalViewport.size.height > 0
        ) {
            val baseBlurDp = when (role) {
                ProxySurfaceRole.CARD -> 6.5f
                ProxySurfaceRole.INPUT -> 10.5f
                ProxySurfaceRole.BUTTON -> 4.0f
                ProxySurfaceRole.OVERLAY -> 14.0f
            }
            val resolvedBlur = (
                baseBlurDp *
                    (if (strong) 1.16f else 1f) *
                    (if (recessed) 0.68f else 1f) *
                    (1f - clarity * 0.52f)
                ).coerceAtLeast(2.2f)
            val hardwareBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                motionProfile.textureAlpha > 0.74f
            val backdropBlur = if (hardwareBlur) resolvedBlur.dp else 0.dp
            val baseMagnification = when (role) {
                ProxySurfaceRole.CARD -> 1.018f
                ProxySurfaceRole.INPUT -> 1.024f
                ProxySurfaceRole.BUTTON -> 1.040f
                ProxySurfaceRole.OVERLAY -> 1.030f
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(
                        radius = backdropBlur,
                        edgeTreatment = BlurredEdgeTreatment.Unbounded,
                    )
                    .drawWithCache {
                        val viewportSize = Size(
                            opticalViewport.size.width.toFloat(),
                            opticalViewport.size.height.toFloat(),
                        )
                        onDrawBehind {
                            val touchCenter = if (
                                surfaceSize.width > 0 && surfaceSize.height > 0 &&
                                pressPosition != Offset.Zero
                            ) {
                                pressPosition
                            } else {
                                Offset(size.width * 0.5f, size.height * 0.5f)
                            }
                            val normalizedTouch = Offset(
                                x = (touchCenter.x / size.width.coerceAtLeast(1f) - 0.5f)
                                    .coerceIn(-0.5f, 0.5f),
                                y = (touchCenter.y / size.height.coerceAtLeast(1f) - 0.5f)
                                    .coerceIn(-0.5f, 0.5f),
                            )
                            val phase = materialBreath()
                            val touchDisplacement = Offset(
                                x = normalizedTouch.x *
                                    (2.4.dp.toPx() + materialCompression * 3.8.dp.toPx()),
                                y = normalizedTouch.y *
                                    (1.8.dp.toPx() + materialCompression * 3.0.dp.toPx()),
                            )
                            val ambientDisplacement = Offset(
                                x = phase * 1.4.dp.toPx() * motionProfile.opticalDrift,
                                y = -phase * 0.8.dp.toPx() * motionProfile.opticalDrift,
                            )
                            drawLiquidOpticalScene(
                                viewportSize = viewportSize,
                                viewportOrigin = surfaceOrigin,
                                palette = palette,
                                stain = stainSettings.intensity,
                                activeDrift = phase,
                                magnification = baseMagnification +
                                    (if (strong) 0.006f else 0f) +
                                    (if (recessed) -0.006f else 0f) +
                                    materialCompression * 0.010f,
                                displacement = touchDisplacement + ambientDisplacement,
                            )
                            if (!hardwareBlur) {
                                drawRect(
                                    color = Color.White.copy(
                                        alpha = when (role) {
                                            ProxySurfaceRole.CARD -> 0.055f
                                            ProxySurfaceRole.INPUT -> 0.085f
                                            ProxySurfaceRole.BUTTON -> 0.038f
                                            ProxySurfaceRole.OVERLAY -> 0.12f
                                        },
                                    ),
                                )
                            }
                        }
                    },
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    val normalizedX = if (surfaceSize.width > 0) {
                        (pressPosition.x / surfaceSize.width.toFloat() - 0.5f)
                            .coerceIn(-0.5f, 0.5f)
                    } else {
                        0f
                    }
                    val normalizedY = if (surfaceSize.height > 0) {
                        (pressPosition.y / surfaceSize.height.toFloat() - 0.5f)
                            .coerceIn(-0.5f, 0.5f)
                    } else {
                        0f
                    }
                    val opticalGlass = style.theme == AppTheme.LIQUID_GLASS
                    rotationX = if (opticalGlass) {
                        0f
                    } else {
                        -normalizedY * 2.65f * materialCompression * depthFactor
                    }
                    rotationY = if (opticalGlass) {
                        0f
                    } else {
                        normalizedX * 2.65f * materialCompression * depthFactor
                    }
                    rotationZ = 0f
                    scaleX = if (opticalGlass) 1f else 1f + 0.014f * materialCompression
                    scaleY = if (opticalGlass) 1f else 1f - 0.011f * materialCompression
                }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(top, middle, bottom),
                    ),
                )
                .drawWithCache {
                val liquid = style.theme == AppTheme.LIQUID_GLASS
                val cyberpunk = style.theme == AppTheme.CYBERPUNK
                val highlight = when (style.theme) {
                    AppTheme.LIQUID_GLASS -> Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f * depthFactor),
                            Color.Transparent,
                            palette.secondary.copy(alpha = stainAlpha * 0.18f),
                            palette.tertiary.copy(alpha = stainAlpha * 0.10f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height),
                    )
                    AppTheme.ROYAL_GRAPHITE -> Brush.linearGradient(
                        colors = listOf(
                            style.specular,
                            Color.Transparent,
                            style.specular.copy(alpha = style.specular.alpha * 0.32f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height),
                    )
                    AppTheme.OLD_SCROLL -> Brush.linearGradient(
                        colors = listOf(
                            palette.caustic.copy(alpha = 0.32f * depthFactor),
                            Color.Transparent,
                            palette.secondary.copy(alpha = stainAlpha * 0.42f),
                            palette.tertiary.copy(alpha = stainAlpha * 0.20f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height),
                    )
                    AppTheme.LITE_LIFE -> Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.028f),
                            Color.Transparent,
                            palette.primary.copy(alpha = if (active) 0.045f else 0.012f),
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height),
                    )
                    AppTheme.CYBERPUNK -> Brush.linearGradient(
                        colors = listOf(
                            palette.primary.copy(alpha = if (strong) 0.20f else 0.11f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.22f),
                            palette.secondary.copy(alpha = 0.075f + clarity * 0.08f),
                            palette.tertiary.copy(alpha = 0.035f + clarity * 0.05f),
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height),
                    )
                }
                val lens = Brush.radialGradient(
                    colors = if (liquid) {
                        listOf(
                            Color.White.copy(alpha = (0.045f + clarity * 0.045f) * depthFactor),
                            palette.caustic.copy(alpha = 0.025f * depthFactor),
                            Color.Transparent,
                        )
                    } else {
                        listOf(
                            palette.primary.copy(alpha = stainAlpha * 1.15f),
                            Color.White.copy(
                                alpha = (0.045f + clarity * 0.055f) * depthFactor,
                            ),
                            Color.Transparent,
                        )
                    },
                    center = Offset(size.width * 0.12f, size.height * 0.05f),
                    radius = size.width * 0.86f,
                )
                val touchCenter = if (surfaceSize.width > 0 && surfaceSize.height > 0) {
                    Offset(
                        x = pressPosition.x.coerceIn(0f, size.width),
                        y = pressPosition.y.coerceIn(0f, size.height),
                    )
                } else {
                    Offset(size.width * 0.22f, size.height * 0.16f)
                }
                val touchSpecular = Brush.radialGradient(
                    colors = listOf(
                        palette.caustic.copy(alpha = clarity * 0.30f * depthFactor),
                        Color.White.copy(alpha = clarity * 0.10f),
                        Color.Transparent,
                    ),
                    center = touchCenter,
                    radius = maxOf(size.width, size.height) * 0.48f,
                )
                val touchPressure = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to Color.Transparent,
                        0.44f to Color.Transparent,
                        0.70f to when (style.theme) {
                            AppTheme.LIQUID_GLASS -> palette.secondary.copy(
                                alpha = clarity * 0.055f * depthFactor,
                            )
                            AppTheme.ROYAL_GRAPHITE -> Color.Black.copy(alpha = clarity * 0.16f)
                            AppTheme.OLD_SCROLL -> palette.tertiary.copy(alpha = clarity * 0.065f)
                            AppTheme.LITE_LIFE -> palette.primary.copy(alpha = clarity * 0.035f)
                            AppTheme.CYBERPUNK -> palette.secondary.copy(alpha = clarity * 0.11f)
                        },
                        1.00f to Color.Transparent,
                    ),
                    center = touchCenter,
                    radius = maxOf(size.width, size.height) * 0.42f,
                )
                val glowTouchMix = clarity * 0.32f
                val liveGlowCenter = Offset(
                    x = size.width * 0.72f * (1f - glowTouchMix) +
                        touchCenter.x * glowTouchMix,
                    y = size.height * 0.76f * (1f - glowTouchMix) +
                        touchCenter.y * glowTouchMix,
                )
                val materialGlowFactor = when (style.theme) {
                    AppTheme.LIQUID_GLASS -> 0.16f
                    AppTheme.ROYAL_GRAPHITE -> 0.76f
                    AppTheme.OLD_SCROLL -> 0.34f
                    AppTheme.LITE_LIFE -> 0.08f
                    AppTheme.CYBERPUNK -> 0.72f
                }
                val subglassGlow = Brush.radialGradient(
                    colors = listOf(
                        palette.secondary.copy(
                            alpha = stainAlpha * (0.90f + clarity * 0.30f) * materialGlowFactor,
                        ),
                        palette.primary.copy(
                            alpha = stainAlpha * 0.54f * materialGlowFactor,
                        ),
                        palette.tertiary.copy(
                            alpha = stainAlpha * 0.22f * materialGlowFactor,
                        ),
                        Color.Transparent,
                    ),
                    center = liveGlowCenter,
                    radius = maxOf(size.width, size.height) * 0.76f,
                )
                val chromaticTouchBloom = Brush.radialGradient(
                    colors = listOf(
                        palette.caustic.copy(alpha = clarity * 0.14f * depthFactor),
                        palette.secondary.copy(alpha = clarity * 0.10f * depthFactor),
                        palette.tertiary.copy(alpha = clarity * 0.055f * depthFactor),
                        Color.Transparent,
                    ),
                    center = touchCenter,
                    radius = maxOf(size.width, size.height) * 0.62f,
                )
                val lowerRefraction = Brush.verticalGradient(
                    colors = if (liquid) {
                        listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.White.copy(alpha = 0.018f * depthFactor),
                            palette.caustic.copy(alpha = 0.026f * depthFactor),
                        )
                    } else {
                        listOf(
                            Color.Transparent,
                            Color.Transparent,
                            palette.secondary.copy(alpha = stainAlpha * 0.55f),
                            palette.tertiary.copy(alpha = stainAlpha * 0.72f),
                        )
                    },
                )
                val frostFactor = (1f - clarity * 0.88f).coerceIn(0.08f, 1f)
                val safetyFrost = Brush.radialGradient(
                    colors = when (style.theme) {
                        AppTheme.LIQUID_GLASS -> listOf(
                            Color.White.copy(alpha = 0.085f * depthFactor * frostFactor),
                            Color.White.copy(alpha = 0.030f * frostFactor),
                            Color.Transparent,
                        )
                        AppTheme.ROYAL_GRAPHITE -> listOf(
                            Color(0xFF182126).copy(alpha = 0.20f * depthFactor * frostFactor),
                            Color(0xFF0A0F12).copy(alpha = 0.06f * frostFactor),
                            Color.Transparent,
                        )
                        AppTheme.OLD_SCROLL -> listOf(
                            palette.caustic.copy(alpha = 0.14f * depthFactor * frostFactor),
                            Color(0xFFE4CDA3).copy(alpha = 0.055f * frostFactor),
                            Color.Transparent,
                        )
                        AppTheme.LITE_LIFE -> listOf(
                            Color(0xFF252932).copy(alpha = 0.10f * frostFactor),
                            Color.Transparent,
                        )
                        AppTheme.CYBERPUNK -> listOf(
                            Color(0xFF25240B).copy(alpha = 0.20f * depthFactor * frostFactor),
                            Color(0xFF08090A).copy(alpha = 0.10f * frostFactor),
                            Color.Transparent,
                        )
                    },
                    center = Offset(size.width * 0.52f, size.height * 0.50f),
                    radius = maxOf(size.width, size.height) * 0.72f,
                )
                val fineGrainOpacity = if (liquid) 0f else (
                    (when (style.theme) {
                        AppTheme.LIQUID_GLASS -> 0f
                        AppTheme.ROYAL_GRAPHITE -> 0.22f
                        AppTheme.OLD_SCROLL -> 0.38f
                        AppTheme.LITE_LIFE -> 0f
                        AppTheme.CYBERPUNK -> 0.20f
                    }) * depthFactor *
                        (0.94f - clarity * 0.30f) *
                        (0.76f + stainSettings.intensity * 0.22f) *
                        motionProfile.textureAlpha
                ).coerceIn(0.08f, 0.34f)
                val spectralGrainOpacity = if (liquid) 0f else (
                    (when (style.theme) {
                        AppTheme.LIQUID_GLASS -> 0f
                        AppTheme.ROYAL_GRAPHITE -> 0.10f
                        AppTheme.OLD_SCROLL -> 0.22f
                        AppTheme.LITE_LIFE -> 0f
                        AppTheme.CYBERPUNK -> 0.17f
                    }) * depthFactor *
                        (0.76f + clarity * 0.24f) *
                        stainSettings.intensity *
                        motionProfile.textureAlpha
                ).coerceIn(0.035f, 0.21f)
                val innerRim = Brush.linearGradient(
                    colors = when (style.theme) {
                        AppTheme.LIQUID_GLASS -> listOf(
                            Color.White.copy(alpha = 0.76f),
                            palette.caustic.copy(alpha = 0.24f * depthFactor),
                            Color.Transparent,
                            palette.secondary.copy(alpha = 0.12f * depthFactor),
                            Color(0xFF26344F).copy(alpha = 0.15f),
                        )
                        AppTheme.ROYAL_GRAPHITE -> listOf(
                            palette.caustic.copy(alpha = 0.34f * depthFactor),
                            Color.White.copy(alpha = 0.11f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.46f),
                        )
                        AppTheme.OLD_SCROLL -> listOf(
                            palette.caustic.copy(alpha = 0.54f * depthFactor),
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent,
                            palette.tertiary.copy(alpha = 0.24f),
                            Color(0xFF604321).copy(alpha = 0.22f),
                        )
                        AppTheme.LITE_LIFE -> listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.16f),
                        )
                        AppTheme.CYBERPUNK -> listOf(
                            palette.primary.copy(alpha = 0.88f),
                            palette.caustic.copy(alpha = 0.30f),
                            Color.Transparent,
                            palette.secondary.copy(alpha = 0.44f + clarity * 0.18f),
                            palette.tertiary.copy(alpha = 0.22f + clarity * 0.16f),
                        )
                    },
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                )
                onDrawBehind {
                    val ambientPhase = materialBreath()
                    drawRect(brush = highlight)
                    if (motionProfile.trail > 0.01f) {
                        repeat(2) { index ->
                            val step = index + 1f
                            withTransform({
                                translate(
                                    left = -ambientPhase * 7.5f * step * motionProfile.trail,
                                    top = ambientPhase * 4.2f * step * motionProfile.trail,
                                )
                            }) {
                                drawRect(
                                    brush = subglassGlow,
                                    alpha = (0.16f / step) * motionProfile.trail,
                                )
                            }
                        }
                    }
                    drawRect(brush = subglassGlow)
                    drawRect(brush = lens)
                    drawRect(brush = lowerRefraction)
                    withTransform({
                        translate(
                            left = ambientPhase * 0.75f * motionProfile.opticalDrift,
                            top = -ambientPhase * 0.42f * motionProfile.opticalDrift,
                        )
                    }) {
                        drawRect(brush = microstructure.fine, alpha = fineGrainOpacity)
                    }
                    withTransform({
                        translate(
                            left = -ambientPhase * 1.18f * motionProfile.opticalDrift,
                            top = ambientPhase * 0.66f * motionProfile.opticalDrift,
                        )
                    }) {
                        drawRect(brush = microstructure.spectral, alpha = spectralGrainOpacity)
                    }
                    drawRect(brush = safetyFrost)
                    if (clarity > 0.01f) {
                        drawRect(brush = touchPressure)
                        drawRect(brush = chromaticTouchBloom)
                        drawRect(brush = touchSpecular)
                    }
                    drawRoundRect(
                        color = when (style.theme) {
                            AppTheme.LIQUID_GLASS -> Color.White.copy(alpha = 0.14f + clarity * 0.22f)
                            AppTheme.ROYAL_GRAPHITE -> palette.caustic.copy(alpha = 0.08f + clarity * 0.12f)
                            AppTheme.OLD_SCROLL -> palette.caustic.copy(alpha = 0.16f + clarity * 0.10f)
                            AppTheme.LITE_LIFE -> Color.White.copy(alpha = 0.045f + clarity * 0.025f)
                            AppTheme.CYBERPUNK -> palette.primary.copy(alpha = 0.22f + clarity * 0.38f)
                        },
                        cornerRadius = CornerRadius(morphCornerDp.dp.toPx()),
                        style = Stroke(width = (0.65f + clarity * 0.85f).dp.toPx()),
                    )
                    val rimInset = 1.35.dp.toPx()
                    drawRoundRect(
                        brush = innerRim,
                        topLeft = Offset(rimInset, rimInset),
                        size = Size(
                            width = (size.width - rimInset * 2f).coerceAtLeast(0f),
                            height = (size.height - rimInset * 2f).coerceAtLeast(0f),
                        ),
                        cornerRadius = CornerRadius(
                            (morphCornerDp.dp.toPx() - rimInset).coerceAtLeast(0f),
                        ),
                        style = Stroke(width = 0.72.dp.toPx()),
                    )
                    if (liquid) {
                        val edgeThickness = when (role) {
                            ProxySurfaceRole.CARD -> 1.8.dp.toPx()
                            ProxySurfaceRole.INPUT -> 2.1.dp.toPx()
                            ProxySurfaceRole.BUTTON -> 2.8.dp.toPx()
                            ProxySurfaceRole.OVERLAY -> 2.4.dp.toPx()
                        } * if (recessed) 0.62f else 1f
                        val edgeRadius = morphCornerDp.dp.toPx().coerceAtLeast(edgeThickness)
                        drawLine(
                            color = Color.White.copy(alpha = 0.54f + clarity * 0.18f),
                            start = Offset(edgeRadius * 0.70f, edgeThickness * 0.48f),
                            end = Offset(size.width - edgeRadius * 0.66f, edgeThickness * 0.48f),
                            strokeWidth = edgeThickness * 0.62f,
                            cap = StrokeCap.Round,
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.30f + clarity * 0.12f),
                            start = Offset(edgeThickness * 0.48f, edgeRadius * 0.72f),
                            end = Offset(edgeThickness * 0.48f, size.height - edgeRadius * 0.74f),
                            strokeWidth = edgeThickness * 0.48f,
                            cap = StrokeCap.Round,
                        )
                        drawLine(
                            color = Color(0xFF283650).copy(alpha = 0.19f + clarity * 0.05f),
                            start = Offset(edgeRadius * 0.68f, size.height - edgeThickness * 0.52f),
                            end = Offset(
                                size.width - edgeRadius * 0.70f,
                                size.height - edgeThickness * 0.52f,
                            ),
                            strokeWidth = edgeThickness * 0.72f,
                            cap = StrokeCap.Round,
                        )
                        drawLine(
                            color = Color(0xFF23314A).copy(alpha = 0.16f),
                            start = Offset(size.width - edgeThickness * 0.52f, edgeRadius * 0.70f),
                            end = Offset(
                                size.width - edgeThickness * 0.52f,
                                size.height - edgeRadius * 0.72f,
                            ),
                            strokeWidth = edgeThickness * 0.54f,
                            cap = StrokeCap.Round,
                        )
                        // A sub-pixel spectral pair lives only in the bevel,
                        // never across text or the transmitted scene.
                        drawLine(
                            color = palette.secondary.copy(alpha = 0.13f * depthFactor),
                            start = Offset(size.width * 0.12f, edgeThickness * 1.12f),
                            end = Offset(size.width * 0.43f, edgeThickness * 1.12f),
                            strokeWidth = 0.48.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                        drawLine(
                            color = palette.tertiary.copy(alpha = 0.09f * depthFactor),
                            start = Offset(size.width * 0.58f, size.height - edgeThickness * 1.04f),
                            end = Offset(size.width * 0.86f, size.height - edgeThickness * 1.04f),
                            strokeWidth = 0.42.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                    if (style.theme == AppTheme.ROYAL_GRAPHITE) {
                        drawLine(
                            color = palette.caustic.copy(
                                alpha = (0.15f + clarity * 0.18f) * depthFactor,
                            ),
                            start = Offset(size.width * 0.10f, 1.1f),
                            end = Offset(size.width * 0.58f, 1.1f),
                            strokeWidth = 1.0f,
                        )
                        drawLine(
                            color = Color.Black.copy(alpha = 0.28f),
                            start = Offset(size.width * 0.42f, size.height - 1.1f),
                            end = Offset(size.width * 0.92f, size.height - 1.1f),
                            strokeWidth = 1.0f,
                        )
                    } else if (liquid) {
                        drawLine(
                            color = Color.White.copy(
                                alpha = (0.52f + clarity * 0.20f) * depthFactor,
                            ),
                            start = Offset(size.width * 0.18f, 1.2f),
                            end = Offset(size.width * 0.72f, 1.2f),
                            strokeWidth = 1.1f,
                        )
                    } else if (style.theme == AppTheme.OLD_SCROLL) {
                        drawLine(
                            color = palette.caustic.copy(
                                alpha = (0.32f + clarity * 0.12f) * depthFactor,
                            ),
                            start = Offset(size.width * 0.08f, 1.0f),
                            end = Offset(size.width * 0.52f, 1.0f),
                            strokeWidth = 0.9f,
                        )
                        drawLine(
                            color = palette.tertiary.copy(alpha = 0.20f),
                            start = Offset(size.width * 0.52f, size.height - 1.0f),
                            end = Offset(size.width * 0.94f, size.height - 1.0f),
                            strokeWidth = 0.8f,
                        )
                    } else if (cyberpunk) {
                        val glitchGate = abs(
                            sin((ambientPhase * 37f + 0.83f).toDouble()),
                        ).toFloat()
                        val split = ambientPhase * 10.dp.toPx()
                        drawRect(
                            color = palette.primary.copy(alpha = 0.72f + clarity * 0.18f),
                            topLeft = Offset(size.width * 0.07f, 0f),
                            size = Size(size.width * 0.46f, 1.6.dp.toPx()),
                        )
                        drawRect(
                            color = palette.secondary.copy(
                                alpha = (0.20f + glitchGate * 0.28f) * depthFactor,
                            ),
                            topLeft = Offset(
                                size.width * 0.55f - split,
                                size.height - 2.1.dp.toPx(),
                            ),
                            size = Size(size.width * 0.34f, 1.05.dp.toPx()),
                        )
                        drawRect(
                            color = palette.tertiary.copy(
                                alpha = (0.13f + glitchGate * 0.22f) * depthFactor,
                            ),
                            topLeft = Offset(
                                size.width * 0.62f + split,
                                size.height - 0.9.dp.toPx(),
                            ),
                            size = Size(size.width * 0.23f, 0.75.dp.toPx()),
                        )
                        repeat(3) { index ->
                            val y = size.height * (0.24f + index * 0.22f)
                            drawRect(
                                color = when (index) {
                                    0 -> palette.primary
                                    1 -> palette.secondary
                                    else -> palette.tertiary
                                }.copy(alpha = 0.12f + glitchGate * 0.10f),
                                topLeft = Offset(
                                    if (index % 2 == 0) 0f else size.width * 0.86f,
                                    y,
                                ),
                                size = Size(size.width * 0.14f, (0.65f + index * 0.25f).dp.toPx()),
                            )
                        }
                    }
                    when (style.theme) {
                        AppTheme.LIQUID_GLASS -> drawLine(
                            color = palette.caustic.copy(alpha = 0.10f * depthFactor),
                            start = Offset(size.width * (0.05f + ambientPhase * 0.12f), 0f),
                            end = Offset(size.width * (0.58f + ambientPhase * 0.12f), size.height),
                            strokeWidth = 1.35.dp.toPx(),
                        )
                        AppTheme.ROYAL_GRAPHITE -> drawLine(
                            color = palette.caustic.copy(alpha = 0.075f * depthFactor),
                            start = Offset(size.width * (0.10f + ambientPhase * 0.08f), 0f),
                            end = Offset(size.width * (0.42f + ambientPhase * 0.08f), size.height),
                            strokeWidth = 0.75.dp.toPx(),
                        )
                        AppTheme.OLD_SCROLL -> drawCircle(
                            color = palette.caustic.copy(alpha = 0.026f * depthFactor),
                            radius = maxOf(size.width, size.height) * 0.62f,
                            center = Offset(
                                size.width * (0.28f + ambientPhase * 0.05f),
                                size.height * 0.20f,
                            ),
                        )
                        AppTheme.LITE_LIFE -> Unit
                        AppTheme.CYBERPUNK -> {
                            drawLine(
                                color = palette.primary.copy(alpha = 0.16f * depthFactor),
                                start = Offset(size.width * 0.03f, size.height * 0.68f),
                                end = Offset(size.width * 0.18f, size.height * 0.68f),
                                strokeWidth = 0.8.dp.toPx(),
                                cap = StrokeCap.Square,
                            )
                            drawCircle(
                                color = palette.secondary.copy(alpha = 0.44f),
                                radius = 1.4.dp.toPx(),
                                center = Offset(size.width * 0.18f, size.height * 0.68f),
                            )
                        }
                    }
                }
                }
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = when (style.theme) {
                            AppTheme.LIQUID_GLASS -> listOf(
                                Color.White.copy(alpha = 0.92f),
                                style.rimLight,
                                palette.secondary.copy(
                                    alpha = (0.16f + clarity * 0.10f) * depthFactor,
                                ),
                                palette.tertiary.copy(
                                    alpha = (0.08f + clarity * 0.08f) * depthFactor,
                                ),
                                style.rimShade,
                            )
                            AppTheme.ROYAL_GRAPHITE -> listOf(
                                palette.caustic.copy(alpha = 0.24f * depthFactor),
                                palette.secondary.copy(alpha = 0.14f),
                                style.rimShade,
                            )
                            AppTheme.OLD_SCROLL -> listOf(
                                palette.caustic.copy(alpha = 0.62f),
                                style.rimLight,
                                palette.secondary.copy(alpha = 0.28f * depthFactor),
                                style.rimShade,
                            )
                            AppTheme.LITE_LIFE -> listOf(
                                Color.White.copy(alpha = if (strong) 0.10f else 0.055f),
                                palette.primary.copy(alpha = if (active) 0.18f else 0.025f),
                                Color.Black.copy(alpha = 0.16f),
                            )
                            AppTheme.CYBERPUNK -> listOf(
                                palette.primary.copy(alpha = if (strong || active) 0.98f else 0.72f),
                                palette.caustic.copy(alpha = 0.24f),
                                Color.Black.copy(alpha = 0.76f),
                                palette.secondary.copy(alpha = 0.70f + clarity * 0.18f),
                                palette.tertiary.copy(alpha = 0.34f + clarity * 0.20f),
                            )
                        },
                    ),
                    shape = resolvedShape,
                ),
        )
        content()
    }
}

@Composable
fun ProxyInsetSurface(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    role: ProxySurfaceRole = ProxySurfaceRole.CARD,
    selected: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val style = LocalProxyVisualStyle.current
    val shapeSettings = LocalProxyShape.current
    if (style.theme == AppTheme.LITE_LIFE) {
        val fill = if (selected) Color(0xFF252B34) else Color(0xFF1B1E24)
        val outline = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.78f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)
        }
        Box(
            modifier = modifier
                .clip(RectangleShape)
                .background(fill)
                .border(1.dp, outline, RectangleShape),
            content = content,
        )
        return
    }
    val stainSettings = LocalStainSettings.current
    val palette = LocalStainPaletteColors.current
    val microstructure = LocalMaterialMicrostructure.current
    val cornerDp = when (role) {
        ProxySurfaceRole.CARD -> shapeSettings.resolvedCardCornerDp
        ProxySurfaceRole.INPUT -> shapeSettings.resolvedInputCornerDp
        ProxySurfaceRole.BUTTON -> shapeSettings.resolvedButtonCornerDp
        ProxySurfaceRole.OVERLAY -> (shapeSettings.globalCornerDp + 6).coerceAtMost(30)
    }
    val animatedCornerDp = animateFloatAsState(
        targetValue = cornerDp.toFloat(),
        animationSpec = tween(220),
        label = "inset-corner-${role.name.lowercase()}",
    ).value
    val resolvedShape = shape ?: if (style.theme == AppTheme.CYBERPUNK) {
        CutCornerShape(
            topStart = 0.dp,
            topEnd = (animatedCornerDp + 7f).dp,
            bottomEnd = 0.dp,
            bottomStart = (animatedCornerDp + 3f).dp,
        )
    } else {
        RoundedCornerShape(animatedCornerDp.dp)
    }
    if (style.theme == AppTheme.LIQUID_GLASS) {
        ProxySurface(
            modifier = modifier,
            shape = resolvedShape,
            role = role,
            strong = selected,
            active = selected,
            deformContent = false,
            interactive = false,
            recessed = true,
            content = content,
        )
        return
    }
    val depthFactor = stainSettings.depth.opticalFactor
    val stainAlpha = stainSettings.intensity * depthFactor
    val fillBrush = when (style.theme) {
        AppTheme.LIQUID_GLASS -> Brush.linearGradient(
            colors = if (selected) {
                listOf(
                    palette.primary.copy(alpha = 0.22f * stainAlpha),
                    Color.White.copy(alpha = 0.20f),
                    palette.secondary.copy(alpha = 0.14f * stainAlpha),
                )
            } else {
                listOf(
                    Color.White.copy(alpha = 0.15f),
                    palette.primary.copy(alpha = 0.055f * stainAlpha),
                    palette.tertiary.copy(alpha = 0.045f * stainAlpha),
                )
            },
        )
        AppTheme.ROYAL_GRAPHITE -> Brush.linearGradient(
            colors = if (selected) {
                listOf(
                    Color(0xFF304047).copy(alpha = 0.74f),
                    palette.secondary.copy(alpha = 0.12f * stainAlpha),
                    Color(0xFF0D1417).copy(alpha = 0.80f),
                )
            } else {
                listOf(
                    Color(0xFF172126).copy(alpha = 0.66f),
                    palette.primary.copy(alpha = 0.07f * stainAlpha),
                    Color(0xFF090E10).copy(alpha = 0.72f),
                )
            },
        )
        AppTheme.OLD_SCROLL -> Brush.linearGradient(
            colors = if (selected) {
                listOf(
                    Color(0xFFFFF2D2).copy(alpha = 0.88f),
                    palette.secondary.copy(alpha = 0.16f * stainAlpha),
                    Color(0xFFDABF8E).copy(alpha = 0.80f),
                )
            } else {
                listOf(
                    Color(0xFFF5E5C4).copy(alpha = 0.80f),
                    palette.primary.copy(alpha = 0.07f * stainAlpha),
                    Color(0xFFD5B982).copy(alpha = 0.70f),
                )
            },
        )
        AppTheme.LITE_LIFE -> Brush.linearGradient(
            colors = if (selected) {
                listOf(
                    Color(0xFF252B34),
                    palette.primary.copy(alpha = 0.075f),
                    Color(0xFF1C2027),
                )
            } else {
                listOf(Color(0xFF20232A), Color(0xFF191C21))
            },
        )
        AppTheme.CYBERPUNK -> Brush.linearGradient(
            colors = if (selected) {
                listOf(
                    Color(0xFF393608),
                    Color(0xFF17170E),
                    palette.secondary.copy(alpha = 0.13f * stainAlpha),
                    Color(0xFF090A0B),
                )
            } else {
                listOf(
                    Color(0xFF191A16),
                    Color(0xFF0D0E0F),
                    palette.tertiary.copy(alpha = 0.045f * stainAlpha),
                    Color(0xFF070809),
                )
            },
        )
    }
    val outline = when (style.theme) {
        AppTheme.LIQUID_GLASS -> Color.White.copy(alpha = if (selected) 0.52f else 0.24f)
        AppTheme.ROYAL_GRAPHITE -> Color(0xFFBFD3DA).copy(alpha = if (selected) 0.24f else 0.12f)
        AppTheme.OLD_SCROLL -> Color(0xFF74512E).copy(alpha = if (selected) 0.34f else 0.20f)
        AppTheme.LITE_LIFE -> Color.White.copy(alpha = if (selected) 0.12f else 0.055f)
        AppTheme.CYBERPUNK -> if (selected) {
            palette.primary.copy(alpha = 0.92f)
        } else {
            palette.primary.copy(alpha = 0.34f)
        }
    }

    Box(
        modifier = modifier
            .clip(resolvedShape)
            .background(fillBrush)
            .drawWithCache {
                val fineBase = when (style.theme) {
                    AppTheme.LIQUID_GLASS -> 0.24f
                    AppTheme.ROYAL_GRAPHITE -> 0.19f
                    AppTheme.OLD_SCROLL -> 0.34f
                    AppTheme.LITE_LIFE -> 0f
                    AppTheme.CYBERPUNK -> 0.18f
                }
                val spectralBase = when (style.theme) {
                    AppTheme.LIQUID_GLASS -> 0.12f
                    AppTheme.ROYAL_GRAPHITE -> 0.075f
                    AppTheme.OLD_SCROLL -> 0.19f
                    AppTheme.LITE_LIFE -> 0f
                    AppTheme.CYBERPUNK -> 0.14f
                }
                val fineAlpha = fineBase *
                    depthFactor * (if (selected) 1.10f else 1f)
                val spectralAlpha = spectralBase *
                    stainSettings.intensity * depthFactor * (if (selected) 1.14f else 1f)
                onDrawBehind {
                    drawRect(brush = microstructure.fine, alpha = fineAlpha)
                    drawRect(brush = microstructure.spectral, alpha = spectralAlpha)
                    if (style.theme == AppTheme.CYBERPUNK) {
                        drawRect(
                            color = palette.primary.copy(alpha = if (selected) 0.82f else 0.36f),
                            topLeft = Offset.Zero,
                            size = Size(size.width * if (selected) 0.38f else 0.20f, 1.25.dp.toPx()),
                        )
                        drawRect(
                            color = palette.secondary.copy(alpha = if (selected) 0.54f else 0.24f),
                            topLeft = Offset(size.width * 0.66f, size.height - 0.85.dp.toPx()),
                            size = Size(size.width * 0.25f, 0.85.dp.toPx()),
                        )
                        drawRect(
                            color = palette.tertiary.copy(alpha = 0.20f),
                            topLeft = Offset(size.width * 0.72f, size.height - 1.7.dp.toPx()),
                            size = Size(size.width * 0.16f, 0.55.dp.toPx()),
                        )
                    }
                }
            }
            .border(0.7.dp, outline, resolvedShape),
        content = content,
    )
}

@Composable
fun ProxySettingsFog(
    selectedTheme: AppTheme,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val stainSettings = LocalStainSettings.current
    val palette = LocalStainPaletteColors.current
    Canvas(modifier = modifier) {
        val amount = progress.coerceIn(0f, 1f)
        if (amount <= 0f) return@Canvas
        val stain = stainSettings.intensity * stainSettings.depth.opticalFactor
        when (selectedTheme) {
            AppTheme.LIQUID_GLASS -> {
            // The underlying screen is strongly blurred by the host. This veil
            // keeps only its light and colour silhouette, like privacy glass,
            // instead of replacing it with an unrelated opaque sheet.
            drawRect(
                color = Color(0xFFF3F6FD).copy(alpha = 0.58f * amount),
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFF).copy(alpha = 0.22f * amount),
                        palette.neutral.copy(alpha = 0.30f * amount),
                        Color.White.copy(alpha = 0.24f * amount),
                    ),
                ),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.20f * amount),
                        palette.primary.copy(alpha = 0.065f * stain * amount),
                        palette.secondary.copy(alpha = 0.045f * stain * amount),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.50f, size.height * 0.74f),
                    radius = size.width * 0.92f,
                ),
                center = Offset(size.width * 0.50f, size.height * 0.74f),
                radius = size.width * 0.92f,
            )
            }
            AppTheme.ROYAL_GRAPHITE -> {
            drawRect(
                color = Color(0xFF080C0F).copy(alpha = amount),
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF172126).copy(alpha = 0.52f * amount),
                        Color(0xFF080C0E).copy(alpha = 0.72f * amount),
                        Color.Black.copy(alpha = 0.78f * amount),
                    ),
                ),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.secondary.copy(alpha = 0.13f * stain * amount),
                        palette.tertiary.copy(alpha = 0.07f * stain * amount),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.24f, size.height * 0.62f),
                    radius = size.width * 0.78f,
                ),
                center = Offset(size.width * 0.24f, size.height * 0.62f),
                radius = size.width * 0.78f,
            )
            }
            AppTheme.OLD_SCROLL -> {
                drawRect(color = Color(0xFFE9D9B8).copy(alpha = amount))
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF7EBCD).copy(alpha = 0.76f * amount),
                            palette.neutral.copy(alpha = 0.82f * amount),
                            Color(0xFFD8BD8B).copy(alpha = 0.88f * amount),
                        ),
                    ),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            palette.caustic.copy(alpha = 0.22f * stain * amount),
                            Color.Transparent,
                            palette.tertiary.copy(alpha = 0.09f * amount),
                        ),
                        center = Offset(size.width * 0.42f, size.height * 0.32f),
                        radius = size.width * 0.96f,
                    ),
                    center = Offset(size.width * 0.42f, size.height * 0.32f),
                    radius = size.width * 0.96f,
                )
            }
            AppTheme.LITE_LIFE -> {
                drawRect(color = Color(0xFF101115).copy(alpha = amount))
            }
            AppTheme.CYBERPUNK -> {
                drawRect(color = Color(0xFF050607).copy(alpha = amount))
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF181910).copy(alpha = 0.82f * amount),
                            Color(0xFF08090A).copy(alpha = 0.94f * amount),
                            Color.Black.copy(alpha = 0.98f * amount),
                        ),
                    ),
                )
                val warning = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width * 0.42f, 0f)
                    lineTo(size.width * 0.30f, size.height * 0.06f)
                    lineTo(0f, size.height * 0.06f)
                    close()
                }
                drawPath(
                    path = warning,
                    color = palette.primary.copy(alpha = 0.74f * stain * amount),
                )
                drawRect(
                    color = palette.secondary.copy(alpha = 0.34f * amount),
                    topLeft = Offset(size.width * 0.56f, size.height * 0.15f),
                    size = Size(size.width * 0.31f, 1.2.dp.toPx()),
                )
                drawRect(
                    color = palette.tertiary.copy(alpha = 0.20f * amount),
                    topLeft = Offset(size.width * 0.63f, size.height * 0.15f + 1.5.dp.toPx()),
                    size = Size(size.width * 0.20f, 0.7.dp.toPx()),
                )
            }
        }
    }
}
