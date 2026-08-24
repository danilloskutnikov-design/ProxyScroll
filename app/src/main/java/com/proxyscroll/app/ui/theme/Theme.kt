package com.proxyscroll.app.ui.theme

import android.app.Activity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.matchParentSize
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
    }
}

@Composable
fun ProxySurface(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    role: ProxySurfaceRole = ProxySurfaceRole.CARD,
    strong: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val style = LocalProxyVisualStyle.current
    val shapeSettings = LocalProxyShape.current
    val stainSettings = LocalStainSettings.current
    val palette = LocalStainPaletteColors.current
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
    val resolvedShape = shape ?: RoundedCornerShape(animatedCornerDp.dp)
    var materialPressed by remember { mutableStateOf(false) }
    var pressPosition by remember { mutableStateOf(Offset.Zero) }
    var surfaceSize by remember { mutableStateOf(IntSize.Zero) }
    val pressAmount by animateFloatAsState(
        targetValue = if (materialPressed) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (materialPressed) 90 else 420,
            easing = FastOutSlowInEasing,
        ),
        label = "stained-surface-press-${role.name.lowercase()}",
    )
    val depthFactor = stainSettings.depth.opticalFactor
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
    fun scaled(color: Color, extra: Float = 1f) = color.copy(
        alpha = (color.alpha * materialFactor * extra).coerceIn(0f, 1f),
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
    val stainAlpha = stainSettings.intensity * roleStainFactor * themeStainFactor * depthFactor
    val elevation = when {
        strong && style.theme == AppTheme.LIQUID_GLASS -> 8.dp
        style.theme == AppTheme.LIQUID_GLASS -> 5.dp
        strong -> 7.dp
        else -> 4.dp
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = resolvedShape,
                ambientColor = style.shadow,
                spotColor = style.shadow,
            )
            .clip(resolvedShape)
            .onSizeChanged { surfaceSize = it }
            .pointerInput(role) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    pressPosition = down.position
                    materialPressed = true
                    waitForUpOrCancellation()
                    materialPressed = false
                }
            },
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
                    rotationX = -normalizedY * 2.0f * pressAmount * depthFactor
                    rotationY = normalizedX * 2.0f * pressAmount * depthFactor
                    scaleX = 1f - 0.008f * pressAmount
                    scaleY = 1f - 0.008f * pressAmount
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
                        Color.White.copy(alpha = 0.045f * depthFactor),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.12f, size.height * 0.05f),
                    radius = size.width * 0.86f,
                )
                val lowerRefraction = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent,
                        palette.secondary.copy(alpha = stainAlpha * 0.55f),
                        palette.tertiary.copy(alpha = stainAlpha * 0.72f),
                    ),
                )
                val safetyFrost = Brush.radialGradient(
                    colors = if (liquid) {
                        listOf(
                            Color.White.copy(alpha = 0.075f * depthFactor),
                            Color.White.copy(alpha = 0.025f),
                            Color.Transparent,
                        )
                    } else {
                        listOf(
                            Color(0xFF182126).copy(alpha = 0.20f * depthFactor),
                            Color(0xFF0A0F12).copy(alpha = 0.06f),
                            Color.Transparent,
                        )
                    },
                    center = Offset(size.width * 0.52f, size.height * 0.50f),
                    radius = maxOf(size.width, size.height) * 0.72f,
                )
                onDrawBehind {
                    drawRect(brush = highlight)
                    drawRect(brush = lens)
                    drawRect(brush = lowerRefraction)
                    drawRect(brush = safetyFrost)
                    if (!liquid) {
                        drawLine(
                            color = palette.caustic.copy(alpha = 0.15f * depthFactor),
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
                            color = palette.caustic.copy(alpha = 0.50f * depthFactor),
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
                                palette.secondary.copy(alpha = 0.62f * depthFactor),
                                palette.tertiary.copy(alpha = 0.38f * depthFactor),
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
                        Color(0xFFF8FAFF).copy(alpha = 0.10f * amount),
                        palette.neutral.copy(alpha = 0.30f * amount),
                        Color.White.copy(alpha = 0.22f * amount),
                    ),
                ),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.24f * amount),
                        palette.primary.copy(alpha = 0.07f * stain * amount),
                        palette.secondary.copy(alpha = 0.045f * stain * amount),
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
                        Color(0xFF172126).copy(alpha = 0.24f * amount),
                        Color(0xFF080C0E).copy(alpha = 0.48f * amount),
                        Color.Black.copy(alpha = 0.54f * amount),
                    ),
                ),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.secondary.copy(alpha = 0.09f * stain * amount),
                        palette.tertiary.copy(alpha = 0.045f * stain * amount),
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
