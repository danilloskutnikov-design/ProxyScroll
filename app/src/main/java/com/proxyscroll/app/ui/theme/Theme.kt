package com.proxyscroll.app.ui.theme

import android.app.Activity
import android.graphics.Bitmap
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.proxyscroll.app.domain.AppTheme
import com.proxyscroll.app.domain.InterfaceShape
import com.proxyscroll.app.domain.StainPalette
import com.proxyscroll.app.domain.StainSettings
import kotlin.math.roundToInt

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
    onBackground = Color(0xFFE9EEF0),
    surface = Color(0xFF12171A),
    onSurface = Color(0xFFE9EEF0),
    surfaceVariant = Color(0xFF20272B),
    onSurfaceVariant = Color(0xFFB9C3C8),
    outline = Color(0xFF69767D),
    error = Color(0xFFFFB4AB),
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
    materialTop = Color.White.copy(alpha = 0.16f),
    materialMiddle = Color(0xFFF4F8FF).copy(alpha = 0.045f),
    materialBottom = Color(0xFF9FB8FF).copy(alpha = 0.075f),
    strongTop = Color.White.copy(alpha = 0.25f),
    strongBottom = Color(0xFFB8CCFF).copy(alpha = 0.12f),
    rimLight = Color.White.copy(alpha = 0.88f),
    rimShade = Color(0xFF5367B1).copy(alpha = 0.24f),
    specular = Color.White.copy(alpha = 0.20f),
    shadow = Color(0xFF30437D).copy(alpha = 0.10f),
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

val LocalStainPaletteColors = staticCompositionLocalOf { AuroraOpalColors }

private class RepeatingGrainBrush(
    private val image: ImageBitmap,
) : ShaderBrush() {
    override fun createShader(size: Size): Shader = ImageShader(
        image = image,
        tileModeX = TileMode.Repeated,
        tileModeY = TileMode.Repeated,
    )
}

private fun createMaterialGrainBrush(
    theme: AppTheme,
    palette: StainPaletteColors,
): Brush {
    // A compact high-frequency spectral tile replaces hundreds of draw calls.
    // The RGB tint represents inclusions in the material, while rare bright
    // pixels behave like caustic crystals under grazing light.
    val edge = 128
    val pixels = IntArray(edge * edge)
    val spectrum = arrayOf(
        palette.primary,
        palette.secondary,
        palette.tertiary,
        palette.caustic,
        palette.neutral,
    )
    var seed = if (theme == AppTheme.LIQUID_GLASS) 0x51F15EED else 0x37A9C2D1

    fun nextNoise(): Int {
        seed = seed * 1_664_525 + 1_013_904_223
        return seed ushr 1
    }

    pixels.indices.forEach { index ->
        val noise = nextNoise()
        val occupancy = noise and 0xFF
        val visibleThreshold = if (theme == AppTheme.LIQUID_GLASS) 188 else 174
        if (occupancy >= visibleThreshold) return@forEach

        val source = spectrum[(noise ushr 9) % spectrum.size]
        val caustic = occupancy < 7
        val lift = if (caustic) {
            0.62f
        } else {
            0.10f + ((noise ushr 17) and 0x0F) / 100f
        }
        val alpha = if (caustic) {
            122 + ((noise ushr 22) and 0x1F)
        } else {
            30 + ((noise ushr 21) and 0x23)
        }
        val red = ((source.red + (1f - source.red) * lift) * 255f)
            .roundToInt().coerceIn(0, 255)
        val green = ((source.green + (1f - source.green) * lift) * 255f)
            .roundToInt().coerceIn(0, 255)
        val blue = ((source.blue + (1f - source.blue) * lift) * 255f)
            .roundToInt().coerceIn(0, 255)
        pixels[index] = android.graphics.Color.argb(alpha.coerceAtMost(255), red, green, blue)
    }

    val bitmap = Bitmap.createBitmap(pixels, edge, edge, Bitmap.Config.ARGB_8888)
    return RepeatingGrainBrush(bitmap.asImageBitmap())
}

val LocalMaterialGrainBrush = staticCompositionLocalOf<Brush> {
    SolidColor(Color.Transparent)
}

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
    content: @Composable () -> Unit,
) {
    val targetScheme = when (selectedTheme) {
        AppTheme.LIQUID_GLASS -> LiquidGlassColors
        AppTheme.ROYAL_GRAPHITE -> RoyalGraphiteColors
    }
    val animatedScheme = animateScheme(targetScheme)
    val visualStyle = animateVisualStyle(selectedTheme)
    val stainPalette = animateStainPalette(
        target = paletteFor(selectedTheme, stainSettings.palette),
    )
    val materialGrainBrush = remember(selectedTheme, stainSettings.palette) {
        createMaterialGrainBrush(
            theme = selectedTheme,
            palette = paletteFor(selectedTheme, stainSettings.palette),
        )
    }
    val typographyProgress = animateFloatAsState(
        targetValue = if (selectedTheme == AppTheme.LIQUID_GLASS) 0f else 1f,
        animationSpec = tween(THEME_TRANSITION_MILLIS),
        label = "typography-material-transition",
    ).value
    val typography = animatedTypography(typographyProgress)
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            val lightIcons = selectedTheme == AppTheme.LIQUID_GLASS
            controller.isAppearanceLightStatusBars = lightIcons
            controller.isAppearanceLightNavigationBars = lightIcons
        }
    }

    CompositionLocalProvider(
        LocalProxyVisualStyle provides visualStyle,
        LocalProxyShape provides interfaceShape,
        LocalStainSettings provides stainSettings.normalized(),
        LocalStainPaletteColors provides stainPalette,
        LocalMaterialGrainBrush provides materialGrainBrush,
    ) {
        MaterialTheme(
            colorScheme = animatedScheme,
            typography = typography,
            content = content,
        )
    }
}

private fun paletteFor(theme: AppTheme, palette: StainPalette): StainPaletteColors {
    if (theme == AppTheme.ROYAL_GRAPHITE) return GraphiteOilColors
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
private fun animatedTypography(progress: Float): Typography {
    fun between(start: Float, end: Float) = start + (end - start) * progress
    return Typography(
        headlineMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = between(30f, 29f).sp,
            lineHeight = between(36f, 37f).sp,
            letterSpacing = between(-0.35f, 0.18f).sp,
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = between(22f, 21.5f).sp,
            lineHeight = between(28f, 29f).sp,
            letterSpacing = between(-0.10f, 0.12f).sp,
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            lineHeight = between(23f, 24f).sp,
            letterSpacing = between(0f, 0.10f).sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 19.sp,
            lineHeight = between(29f, 30f).sp,
            letterSpacing = between(0f, 0.08f).sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
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
    val target = if (theme == AppTheme.LIQUID_GLASS) LiquidVisualStyle else GraphiteVisualStyle

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

@Composable
fun ProxyThemeBackground(
    selectedTheme: AppTheme,
    motionQuiet: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Crossfade(
        targetState = selectedTheme,
        modifier = modifier,
        animationSpec = tween(THEME_TRANSITION_MILLIS + 180),
        label = "material-background",
    ) { theme ->
        MaterialBackground(theme, motionQuiet)
    }
}

@Composable
private fun MaterialBackground(
    theme: AppTheme,
    motionQuiet: Boolean,
) {
    val settings = LocalStainSettings.current
    val palette = LocalStainPaletteColors.current
    val materialGrainBrush = LocalMaterialGrainBrush.current
    val transition = rememberInfiniteTransition(label = "ambient-material-motion")
    val drift = transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(settings.motion.cycleMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ambient-drift",
    )
    val motionScale = animateFloatAsState(
        targetValue = if (motionQuiet) 0.08f else settings.motion.amplitudeFactor,
        animationSpec = tween(if (motionQuiet) 180 else 420, easing = FastOutSlowInEasing),
        label = "stain-motion-scale",
    )
    val intensity = animateFloatAsState(
        targetValue = settings.intensity,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "stain-intensity",
    )

    Canvas(Modifier.fillMaxSize()) {
        val activeDrift = drift.value * motionScale.value
        val stain = intensity.value
        if (theme == AppTheme.LIQUID_GLASS) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF9FAFF),
                        palette.neutral,
                        Color(0xFFEAF2F5),
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                ),
            )
            val primaryWell = Offset(
                x = size.width * (0.12f + activeDrift * 0.045f),
                y = size.height * (0.16f - activeDrift * 0.012f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.primary.copy(alpha = 0.34f * stain),
                        palette.primary.copy(alpha = 0.13f * stain),
                        Color.Transparent,
                    ),
                    center = primaryWell,
                    radius = size.width * 0.94f,
                ),
                center = primaryWell,
                radius = size.width * 0.94f,
            )
            val secondaryWell = Offset(
                x = size.width * (0.91f - activeDrift * 0.035f),
                y = size.height * (0.55f + activeDrift * 0.016f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        palette.secondary.copy(alpha = 0.28f * stain),
                        palette.secondary.copy(alpha = 0.08f * stain),
                        Color.Transparent,
                    ),
                    center = secondaryWell,
                    radius = size.width * 0.78f,
                ),
                center = secondaryWell,
                radius = size.width * 0.78f,
            )
            val tertiaryWell = Offset(
                x = size.width * (0.47f + activeDrift * 0.06f),
                y = size.height * (0.88f - activeDrift * 0.018f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.tertiary.copy(alpha = 0.24f * stain),
                        palette.tertiary.copy(alpha = 0.065f * stain),
                        Color.Transparent,
                    ),
                    center = tertiaryWell,
                    radius = size.width * 0.86f,
                ),
                center = tertiaryWell,
                radius = size.width * 0.86f,
            )
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color.Transparent,
                        palette.caustic.copy(alpha = 0.08f * stain),
                        Color.White.copy(alpha = 0.10f),
                    ),
                    start = Offset(size.width * (0.04f + activeDrift * 0.04f), 0f),
                    end = Offset(size.width * (0.82f + activeDrift * 0.04f), size.height),
                ),
            )
        } else {
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
        }

        drawRect(
            brush = materialGrainBrush,
            alpha = if (theme == AppTheme.LIQUID_GLASS) {
                (0.62f * stain).coerceIn(0.28f, 0.78f)
            } else {
                (0.54f * stain).coerceIn(0.26f, 0.66f)
            },
        )
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
    content: @Composable BoxScope.() -> Unit,
) {
    val style = LocalProxyVisualStyle.current
    val shapeSettings = LocalProxyShape.current
    val stainSettings = LocalStainSettings.current
    val palette = LocalStainPaletteColors.current
    val materialGrainBrush = LocalMaterialGrainBrush.current
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
    val compression by animateFloatAsState(
        targetValue = if (materialPressed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = if (materialPressed) 0.82f else 0.58f,
            stiffness = if (materialPressed) 820f else 420f,
        ),
        label = "material-compression-${role.name.lowercase()}",
    )
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
    val morphCornerDp = animatedCornerDp + compression * when (role) {
        ProxySurfaceRole.BUTTON -> 4.8f
        ProxySurfaceRole.INPUT -> 3.6f
        ProxySurfaceRole.CARD -> 3.0f
        ProxySurfaceRole.OVERLAY -> 2.2f
    }
    val resolvedShape = shape ?: RoundedCornerShape(morphCornerDp.dp)
    val materialFactor = if (style.theme == AppTheme.LIQUID_GLASS) {
        when (role) {
            ProxySurfaceRole.CARD -> 0.70f
            ProxySurfaceRole.INPUT -> 0.82f
            ProxySurfaceRole.BUTTON -> 1.00f
            ProxySurfaceRole.OVERLAY -> 1.08f
        }
    } else {
        when (role) {
            ProxySurfaceRole.CARD -> 0.84f
            ProxySurfaceRole.INPUT -> 0.90f
            ProxySurfaceRole.BUTTON -> 1.00f
            ProxySurfaceRole.OVERLAY -> 1.05f
        }
    }
    val transmissionFactor = if (style.theme == AppTheme.LIQUID_GLASS) {
        1f - clarity * 0.64f
    } else {
        1f - clarity * 0.34f
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
    val themeStainFactor = if (style.theme == AppTheme.LIQUID_GLASS) 1f else 0.40f
    val stainAlpha = stainSettings.intensity * roleStainFactor * themeStainFactor * depthFactor *
        (1f + clarity * 0.62f)
    val baseElevation = when {
        strong && style.theme == AppTheme.LIQUID_GLASS -> 8.dp
        style.theme == AppTheme.LIQUID_GLASS -> 5.dp
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
                scaleX = if (deformContent) 1f + compression * 0.004f else 1f
                scaleY = if (deformContent) {
                    1f - compression * verticalCompression
                } else {
                    1f
                }
            }
            .shadow(
                elevation = elevation,
                shape = resolvedShape,
                ambientColor = style.shadow,
                spotColor = style.shadow,
            )
            .clip(resolvedShape)
            .then(
                if (interactive) {
                    Modifier
                        .onSizeChanged { surfaceSize = it }
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
                    rotationX = -normalizedY * 2.35f * compression * depthFactor
                    rotationY = normalizedX * 2.35f * compression * depthFactor
                    scaleX = 1f + 0.012f * compression
                    scaleY = 1f - 0.010f * compression
                }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(top, middle, bottom),
                    ),
                )
                .drawWithCache {
                val liquid = style.theme == AppTheme.LIQUID_GLASS
                val highlight = if (liquid) {
                    Brush.linearGradient(
                        colors = listOf(
                            palette.caustic.copy(alpha = 0.25f * depthFactor),
                            Color.Transparent,
                            palette.secondary.copy(alpha = stainAlpha),
                            palette.tertiary.copy(alpha = stainAlpha * 0.82f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height),
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            style.specular,
                            Color.Transparent,
                            style.specular.copy(alpha = style.specular.alpha * 0.32f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height),
                    )
                }
                val lens = Brush.radialGradient(
                    colors = listOf(
                        palette.primary.copy(alpha = stainAlpha * 1.15f),
                        Color.White.copy(alpha = (0.045f + clarity * 0.055f) * depthFactor),
                        Color.Transparent,
                    ),
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
                val glowTouchMix = clarity * 0.32f
                val liveGlowCenter = Offset(
                    x = size.width * 0.72f * (1f - glowTouchMix) +
                        touchCenter.x * glowTouchMix,
                    y = size.height * 0.76f * (1f - glowTouchMix) +
                        touchCenter.y * glowTouchMix,
                )
                val materialGlowFactor = if (liquid) 1f else 0.76f
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
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent,
                        palette.secondary.copy(alpha = stainAlpha * 0.55f),
                        palette.tertiary.copy(alpha = stainAlpha * 0.72f),
                    ),
                )
                val frostFactor = (1f - clarity * 0.88f).coerceIn(0.08f, 1f)
                val safetyFrost = Brush.radialGradient(
                    colors = if (liquid) {
                        listOf(
                            Color.White.copy(alpha = 0.085f * depthFactor * frostFactor),
                            Color.White.copy(alpha = 0.030f * frostFactor),
                            Color.Transparent,
                        )
                    } else {
                        listOf(
                            Color(0xFF182126).copy(alpha = 0.20f * depthFactor * frostFactor),
                            Color(0xFF0A0F12).copy(alpha = 0.06f * frostFactor),
                            Color.Transparent,
                        )
                    },
                    center = Offset(size.width * 0.52f, size.height * 0.50f),
                    radius = maxOf(size.width, size.height) * 0.72f,
                )
                val grainOpacity = (
                    (if (liquid) 0.82f else 0.66f) * depthFactor *
                        (0.98f - clarity * 0.34f) *
                        (0.72f + stainSettings.intensity * 0.36f)
                ).coerceIn(0.30f, 0.92f)
                val innerRim = Brush.linearGradient(
                    colors = if (liquid) {
                        listOf(
                            Color.White.copy(alpha = 0.72f),
                            palette.caustic.copy(alpha = 0.52f * depthFactor),
                            Color.Transparent,
                            palette.secondary.copy(alpha = 0.28f * depthFactor),
                            Color.White.copy(alpha = 0.16f),
                        )
                    } else {
                        listOf(
                            palette.caustic.copy(alpha = 0.34f * depthFactor),
                            Color.White.copy(alpha = 0.11f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.46f),
                        )
                    },
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                )
                onDrawBehind {
                    drawRect(brush = highlight)
                    drawRect(brush = subglassGlow)
                    drawRect(brush = lens)
                    drawRect(brush = lowerRefraction)
                    drawRect(brush = materialGrainBrush, alpha = grainOpacity)
                    drawRect(brush = safetyFrost)
                    if (clarity > 0.01f) {
                        drawRect(brush = chromaticTouchBloom)
                        drawRect(brush = touchSpecular)
                    }
                    drawRoundRect(
                        color = if (liquid) {
                            Color.White.copy(alpha = 0.14f + clarity * 0.22f)
                        } else {
                            palette.caustic.copy(alpha = 0.08f + clarity * 0.12f)
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
                    if (!liquid) {
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
                    } else {
                        drawLine(
                            color = palette.caustic.copy(
                                alpha = (0.50f + clarity * 0.28f) * depthFactor,
                            ),
                            start = Offset(size.width * 0.18f, 1.2f),
                            end = Offset(size.width * 0.72f, 1.2f),
                            strokeWidth = 1.1f,
                        )
                    }
                }
                }
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = if (style.theme == AppTheme.LIQUID_GLASS) {
                            listOf(
                                palette.caustic.copy(alpha = 0.82f),
                                style.rimLight,
                                palette.secondary.copy(
                                    alpha = (0.62f + clarity * 0.20f) * depthFactor,
                                ),
                                palette.tertiary.copy(
                                    alpha = (0.38f + clarity * 0.22f) * depthFactor,
                                ),
                                style.rimShade,
                            )
                        } else {
                            listOf(
                                palette.caustic.copy(alpha = 0.24f * depthFactor),
                                palette.secondary.copy(alpha = 0.14f),
                                style.rimShade,
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
    val stainSettings = LocalStainSettings.current
    val palette = LocalStainPaletteColors.current
    val materialGrainBrush = LocalMaterialGrainBrush.current
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
    val resolvedShape = shape ?: RoundedCornerShape(animatedCornerDp.dp)
    val depthFactor = stainSettings.depth.opticalFactor
    val stainAlpha = stainSettings.intensity * depthFactor
    val fillBrush = if (style.theme == AppTheme.LIQUID_GLASS) {
        Brush.linearGradient(
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
    } else {
        Brush.linearGradient(
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
    }
    val outline = if (style.theme == AppTheme.LIQUID_GLASS) {
        Color.White.copy(alpha = if (selected) 0.52f else 0.24f)
    } else {
        Color(0xFFBFD3DA).copy(alpha = if (selected) 0.24f else 0.12f)
    }

    Box(
        modifier = modifier
            .clip(resolvedShape)
            .background(fillBrush)
            .drawWithCache {
                val liquid = style.theme == AppTheme.LIQUID_GLASS
                val grainAlpha = (if (liquid) 0.68f else 0.58f) *
                    depthFactor * (if (selected) 1.12f else 1f)
                onDrawBehind {
                    drawRect(brush = materialGrainBrush, alpha = grainAlpha)
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
        if (selectedTheme == AppTheme.LIQUID_GLASS) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFF).copy(alpha = 0.24f * amount),
                        palette.neutral.copy(alpha = 0.46f * amount),
                        Color.White.copy(alpha = 0.36f * amount),
                    ),
                ),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.34f * amount),
                        palette.primary.copy(alpha = 0.11f * stain * amount),
                        palette.secondary.copy(alpha = 0.075f * stain * amount),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.50f, size.height * 0.74f),
                    radius = size.width * 0.92f,
                ),
                center = Offset(size.width * 0.50f, size.height * 0.74f),
                radius = size.width * 0.92f,
            )
        } else {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF172126).copy(alpha = 0.38f * amount),
                        Color(0xFF080C0E).copy(alpha = 0.62f * amount),
                        Color.Black.copy(alpha = 0.68f * amount),
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
    }
}
