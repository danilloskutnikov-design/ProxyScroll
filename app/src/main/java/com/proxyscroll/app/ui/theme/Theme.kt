package com.proxyscroll.app.ui.theme

import android.app.Activity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.proxyscroll.app.domain.AppTheme
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
    materialTop = Color.White.copy(alpha = 0.28f),
    materialMiddle = Color(0xFFF4F8FF).copy(alpha = 0.10f),
    materialBottom = Color(0xFF9FB8FF).copy(alpha = 0.13f),
    strongTop = Color.White.copy(alpha = 0.44f),
    strongBottom = Color(0xFFB8CCFF).copy(alpha = 0.22f),
    rimLight = Color.White.copy(alpha = 0.92f),
    rimShade = Color(0xFF5367B1).copy(alpha = 0.32f),
    specular = Color.White.copy(alpha = 0.26f),
    shadow = Color(0xFF30437D).copy(alpha = 0.13f),
    scrim = Color(0xFF172146).copy(alpha = 0.25f),
)

private val GraphiteVisualStyle = ProxyVisualStyle(
    theme = AppTheme.ROYAL_GRAPHITE,
    materialTop = Color(0xFF252C30).copy(alpha = 0.88f),
    materialMiddle = Color(0xFF151A1D).copy(alpha = 0.90f),
    materialBottom = Color(0xFF0D1113).copy(alpha = 0.94f),
    strongTop = Color(0xFF30393E).copy(alpha = 0.94f),
    strongBottom = Color(0xFF101518).copy(alpha = 0.97f),
    rimLight = Color(0xFFC3D2D8).copy(alpha = 0.24f),
    rimShade = Color.Black.copy(alpha = 0.72f),
    specular = Color(0xFFCAE3EC).copy(alpha = 0.11f),
    shadow = Color.Black.copy(alpha = 0.62f),
    scrim = Color.Black.copy(alpha = 0.58f),
)

val LocalProxyVisualStyle = staticCompositionLocalOf { LiquidVisualStyle }

@Composable
fun ProxyScrollTheme(
    selectedTheme: AppTheme,
    content: @Composable () -> Unit,
) {
    val targetScheme = when (selectedTheme) {
        AppTheme.LIQUID_GLASS -> LiquidGlassColors
        AppTheme.ROYAL_GRAPHITE -> RoyalGraphiteColors
    }
    val animatedScheme = animateScheme(targetScheme)
    val visualStyle = animateVisualStyle(selectedTheme)
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

    CompositionLocalProvider(LocalProxyVisualStyle provides visualStyle) {
        MaterialTheme(
            colorScheme = animatedScheme,
            typography = typography,
            content = content,
        )
    }
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
    modifier: Modifier = Modifier,
) {
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
private fun MaterialBackground(theme: AppTheme) {
    val transition = rememberInfiniteTransition(label = "ambient-material-motion")
    val drift = transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(18_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ambient-drift",
    ).value

    Canvas(Modifier.fillMaxSize()) {
        if (theme == AppTheme.LIQUID_GLASS) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF5F8FF),
                        Color(0xFFDDE5FF),
                        Color(0xFFDFF5F2),
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                ),
            )
            val blue = Offset(
                x = size.width * (0.14f + drift * 0.035f),
                y = size.height * 0.18f,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF667EFF).copy(alpha = 0.48f),
                        Color(0xFF9DB5FF).copy(alpha = 0.18f),
                        Color.Transparent,
                    ),
                    center = blue,
                    radius = size.width * 0.94f,
                ),
                center = blue,
                radius = size.width * 0.94f,
            )
            val aqua = Offset(
                x = size.width * (0.93f - drift * 0.025f),
                y = size.height * 0.58f,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        Color(0xFF3FD4D4).copy(alpha = 0.34f),
                        Color.Transparent,
                    ),
                    center = aqua,
                    radius = size.width * 0.78f,
                ),
                center = aqua,
                radius = size.width * 0.78f,
            )
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.26f),
                        Color(0xFFB270FF).copy(alpha = 0.18f),
                        Color.Transparent,
                    ),
                    start = Offset(size.width * (0.10f + drift * 0.08f), 0f),
                    end = Offset(size.width * (0.72f + drift * 0.08f), size.height),
                ),
            )
            repeat(3) { index ->
                val radius = size.width * (0.46f + index * 0.13f)
                drawCircle(
                    color = Color.White.copy(alpha = 0.09f - index * 0.018f),
                    radius = radius,
                    center = Offset(
                        size.width * (0.72f - drift * 0.025f),
                        size.height * 0.36f,
                    ),
                    style = Stroke(width = 1.4f + index * 0.5f),
                )
            }
        } else {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF11171A),
                        Color(0xFF090C0E),
                        Color(0xFF050708),
                    ),
                ),
            )
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF6F8994).copy(alpha = 0.10f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.40f),
                    ),
                    start = Offset(size.width * (0.04f + drift * 0.025f), 0f),
                    end = Offset(size.width, size.height * 0.82f),
                ),
            )
            repeat(38) { index ->
                val fraction = index / 37f
                val x = size.width * fraction + sin(index * 1.7).toFloat() * 5f
                val alpha = if (index % 7 == 0) 0.075f else 0.028f
                drawLine(
                    color = Color(0xFFC6D4D9).copy(alpha = alpha),
                    start = Offset(x, 0f),
                    end = Offset(x + drift * 5f, size.height),
                    strokeWidth = if (index % 7 == 0) 1.2f else 0.55f,
                )
            }
            repeat(12) { index ->
                val y = size.height * (index + 1) / 13f
                drawLine(
                    color = Color.Black.copy(alpha = 0.13f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y + sin(index.toFloat()) * 2f),
                    strokeWidth = 1.4f,
                )
            }
            val wetLight = Offset(size.width * 0.17f, size.height * (0.22f + drift * 0.03f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8DAAB5).copy(alpha = 0.09f),
                        Color.Transparent,
                    ),
                    center = wetLight,
                    radius = size.width * 0.64f,
                ),
                center = wetLight,
                radius = size.width * 0.64f,
            )
        }
    }
}

@Composable
fun ProxySurface(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    strong: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val style = LocalProxyVisualStyle.current
    val resolvedShape = shape ?: RoundedCornerShape(
        if (style.theme == AppTheme.LIQUID_GLASS) 28.dp else 20.dp,
    )
    val top = if (strong) style.strongTop else style.materialTop
    val bottom = if (strong) style.strongBottom else style.materialBottom
    val elevation = if (style.theme == AppTheme.LIQUID_GLASS) 9.dp else 6.dp

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = resolvedShape,
                ambientColor = style.shadow,
                spotColor = style.shadow,
            )
            .clip(resolvedShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(top, style.materialMiddle, bottom),
                ),
            )
            .drawWithCache {
                val liquid = style.theme == AppTheme.LIQUID_GLASS
                val highlight = if (liquid) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.Transparent,
                            Color(0xFF8EEAF2).copy(alpha = 0.07f),
                            Color(0xFFD9A8FF).copy(alpha = 0.06f),
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
                        Color.White.copy(alpha = 0.20f),
                        Color.White.copy(alpha = 0.035f),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.16f, 0f),
                    radius = size.width * 0.78f,
                )
                val lowerRefraction = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent,
                        Color(0xFF75DDE8).copy(alpha = 0.055f),
                        Color(0xFF9B7CFF).copy(alpha = 0.075f),
                    ),
                )
                onDrawWithContent {
                    drawRoundRect(
                        brush = highlight,
                        cornerRadius = CornerRadius(size.minDimension * 0.20f),
                    )
                    if (liquid) {
                        drawRoundRect(
                            brush = lens,
                            cornerRadius = CornerRadius(size.minDimension * 0.20f),
                        )
                        drawRoundRect(
                            brush = lowerRefraction,
                            cornerRadius = CornerRadius(size.minDimension * 0.20f),
                        )
                    }
                    drawContent()
                    if (!liquid) {
                        repeat(9) { index ->
                            val y = size.height * (index + 1) / 10f
                            drawLine(
                                color = Color.White.copy(alpha = 0.012f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y - 1.5f),
                                strokeWidth = 0.7f,
                            )
                        }
                    } else {
                        drawLine(
                            color = Color.White.copy(alpha = 0.48f),
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
                            Color(0xFFFFF0FA).copy(alpha = 0.78f),
                            style.rimLight,
                            Color(0xFF9FF4F3).copy(alpha = 0.62f),
                            Color(0xFFAC8BFF).copy(alpha = 0.34f),
                            style.rimShade,
                        )
                    } else {
                        listOf(
                            style.rimLight,
                            style.rimLight.copy(alpha = style.rimLight.alpha * 0.25f),
                            style.rimShade,
                        )
                    },
                ),
                shape = resolvedShape,
            ),
        content = content,
    )
}
