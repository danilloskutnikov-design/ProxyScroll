package com.proxyscroll.app.ui.theme

import android.app.Activity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.proxyscroll.app.R
import com.proxyscroll.app.domain.AppTheme

private const val THEME_TRANSITION_MILLIS = 560

private val LiquidGlassColors = lightColorScheme(
    primary = Color(0xFF4655D7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE3FF),
    onPrimaryContainer = Color(0xFF17205C),
    secondary = Color(0xFF3E7584),
    onSecondary = Color.White,
    background = Color(0xFFF1F4FF),
    onBackground = Color(0xFF171A29),
    surface = Color(0xFFF8FAFF),
    onSurface = Color(0xFF171A29),
    surfaceVariant = Color(0xFFE4E8F7),
    onSurfaceVariant = Color(0xFF53586B),
    outline = Color(0xFF8C92AA),
)

private val RoyalGraphiteColors = darkColorScheme(
    primary = Color(0xFF9BC3D2),
    onPrimary = Color(0xFF0B2028),
    primaryContainer = Color(0xFF263C45),
    onPrimaryContainer = Color(0xFFC5E8F3),
    secondary = Color(0xFF9AAAB2),
    onSecondary = Color(0xFF172126),
    background = Color(0xFF090C0F),
    onBackground = Color(0xFFE7ECEF),
    surface = Color(0xFF14191D),
    onSurface = Color(0xFFE7ECEF),
    surfaceVariant = Color(0xFF22292E),
    onSurfaceVariant = Color(0xFFB6C0C6),
    outline = Color(0xFF68747B),
)

private val LiquidTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.35).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 23.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
)

private val GraphiteTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 29.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.25.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.15.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.12.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.08.sp,
    ),
)

data class ProxyVisualStyle(
    val theme: AppTheme,
    val glassTop: Color,
    val glassBottom: Color,
    val glassStrongTop: Color,
    val glassStrongBottom: Color,
    val borderTop: Color,
    val borderBottom: Color,
    val shadow: Color,
    val scrim: Color,
)

private val LiquidVisualStyle = ProxyVisualStyle(
    theme = AppTheme.LIQUID_GLASS,
    glassTop = Color.White.copy(alpha = 0.68f),
    glassBottom = Color.White.copy(alpha = 0.30f),
    glassStrongTop = Color.White.copy(alpha = 0.86f),
    glassStrongBottom = Color(0xFFC9D6FF).copy(alpha = 0.58f),
    borderTop = Color.White.copy(alpha = 0.96f),
    borderBottom = Color(0xFF7788D9).copy(alpha = 0.30f),
    shadow = Color(0xFF344C92).copy(alpha = 0.22f),
    scrim = Color(0xFF172146).copy(alpha = 0.28f),
)

private val GraphiteVisualStyle = ProxyVisualStyle(
    theme = AppTheme.ROYAL_GRAPHITE,
    glassTop = Color(0xFF222A30).copy(alpha = 0.94f),
    glassBottom = Color(0xFF101519).copy(alpha = 0.91f),
    glassStrongTop = Color(0xFF2D373E).copy(alpha = 0.97f),
    glassStrongBottom = Color(0xFF11171B).copy(alpha = 0.96f),
    borderTop = Color(0xFFB7C7D0).copy(alpha = 0.24f),
    borderBottom = Color(0xFF41505A).copy(alpha = 0.16f),
    shadow = Color.Black.copy(alpha = 0.70f),
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
    val typography = when (selectedTheme) {
        AppTheme.LIQUID_GLASS -> LiquidTypography
        AppTheme.ROYAL_GRAPHITE -> GraphiteTypography
    }
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            val useDarkIcons = selectedTheme == AppTheme.LIQUID_GLASS
            controller.isAppearanceLightStatusBars = useDarkIcons
            controller.isAppearanceLightNavigationBars = useDarkIcons
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
private fun animateScheme(target: ColorScheme): ColorScheme {
    @Composable
    fun animated(targetColor: Color, label: String): Color {
        return animateColorAsState(
            targetValue = targetColor,
            animationSpec = tween(THEME_TRANSITION_MILLIS),
            label = label,
        ).value
    }

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
    )
}

@Composable
private fun animateVisualStyle(theme: AppTheme): ProxyVisualStyle {
    val target = when (theme) {
        AppTheme.LIQUID_GLASS -> LiquidVisualStyle
        AppTheme.ROYAL_GRAPHITE -> GraphiteVisualStyle
    }

    @Composable
    fun animated(targetColor: Color, label: String): Color {
        return animateColorAsState(
            targetValue = targetColor,
            animationSpec = tween(THEME_TRANSITION_MILLIS),
            label = label,
        ).value
    }

    return target.copy(
        theme = theme,
        glassTop = animated(target.glassTop, "glass-top"),
        glassBottom = animated(target.glassBottom, "glass-bottom"),
        glassStrongTop = animated(target.glassStrongTop, "glass-strong-top"),
        glassStrongBottom = animated(target.glassStrongBottom, "glass-strong-bottom"),
        borderTop = animated(target.borderTop, "glass-border-top"),
        borderBottom = animated(target.borderBottom, "glass-border-bottom"),
        shadow = animated(target.shadow, "glass-shadow"),
        scrim = animated(target.scrim, "glass-scrim"),
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
        animationSpec = tween(THEME_TRANSITION_MILLIS + 120),
        label = "theme-background-crossfade",
    ) { theme ->
        when (theme) {
            AppTheme.LIQUID_GLASS -> LiquidGlassBackground()
            AppTheme.ROYAL_GRAPHITE -> RoyalGraphiteBackground()
        }
    }
}

@Composable
private fun LiquidGlassBackground() {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFF8FAFF),
                    Color(0xFFE9EEFF),
                    Color(0xFFF2F7F8),
                ),
                start = Offset.Zero,
                end = Offset(size.width, size.height),
            ),
        )
        val blueCenter = Offset(size.width * 0.08f, size.height * 0.13f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF829CFF).copy(alpha = 0.40f),
                    Color.Transparent,
                ),
                center = blueCenter,
                radius = size.width * 0.88f,
            ),
            radius = size.width * 0.88f,
            center = blueCenter,
        )
        val cyanCenter = Offset(size.width * 0.94f, size.height * 0.54f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF67D8DC).copy(alpha = 0.26f),
                    Color.Transparent,
                ),
                center = cyanCenter,
                radius = size.width * 0.76f,
            ),
            radius = size.width * 0.76f,
            center = cyanCenter,
        )
        val violetCenter = Offset(size.width * 0.30f, size.height * 0.94f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFC485FF).copy(alpha = 0.18f),
                    Color.Transparent,
                ),
                center = violetCenter,
                radius = size.width * 0.72f,
            ),
            radius = size.width * 0.72f,
            center = violetCenter,
        )
    }
}

@Composable
private fun RoyalGraphiteBackground() {
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.royal_graphite),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.82f,
            modifier = Modifier.fillMaxSize(),
        )
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF071014).copy(alpha = 0.76f),
                        Color(0xFF090D10).copy(alpha = 0.30f),
                        Color(0xFF020405).copy(alpha = 0.72f),
                    ),
                ),
            )
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF29414B).copy(alpha = 0.16f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.28f),
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height * 0.72f),
                ),
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
        if (style.theme == AppTheme.LIQUID_GLASS) 28.dp else 18.dp,
    )
    val top = if (strong) style.glassStrongTop else style.glassTop
    val bottom = if (strong) style.glassStrongBottom else style.glassBottom
    val elevation = if (style.theme == AppTheme.LIQUID_GLASS) 12.dp else 8.dp

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = resolvedShape,
                ambientColor = style.shadow,
                spotColor = style.shadow,
            )
            .clip(resolvedShape)
            .background(Brush.linearGradient(listOf(top, bottom)))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(style.borderTop, style.borderBottom),
                ),
                shape = resolvedShape,
            ),
        content = content,
    )
}
