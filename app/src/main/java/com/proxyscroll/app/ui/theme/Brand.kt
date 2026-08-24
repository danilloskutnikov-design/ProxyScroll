package com.proxyscroll.app.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proxyscroll.app.domain.AppTheme

private val ProxyBrandMarkPath = Path().apply {
    moveTo(166f, 421f)
    cubicTo(188f, 454f, 226f, 476f, 279f, 479f)
    cubicTo(354f, 484f, 406f, 454f, 406f, 402f)
    cubicTo(406f, 360f, 376f, 335f, 317f, 322f)
    lineTo(253f, 308f)
    cubicTo(198f, 296f, 166f, 277f, 166f, 246f)
    cubicTo(166f, 213f, 203f, 195f, 257f, 195f)
    lineTo(323f, 195f)
    cubicTo(384f, 195f, 425f, 166f, 425f, 116f)
    cubicTo(425f, 67f, 387f, 37f, 326f, 37f)
    lineTo(232f, 37f)
    cubicTo(190f, 37f, 166f, 58f, 166f, 91f)
    lineTo(166f, 194f)
}

@Composable
fun ProxyBrandLockup(
    modifier: Modifier = Modifier,
) {
    val phase = LocalMaterialBreath.current.invoke()

    Row(
        modifier = modifier.semantics { contentDescription = "ProxyScroll" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProxyBrandMark(
            modifier = Modifier.size(32.dp),
            phase = phase,
        )
        Spacer(Modifier.width(7.dp))
        ProxyBrandWordmark(phase = phase)
    }
}

@Composable
private fun ProxyBrandMark(
    phase: Float,
    modifier: Modifier = Modifier,
) {
    val style = LocalProxyVisualStyle.current
    val palette = LocalStainPaletteColors.current
    val microstructure = LocalMaterialMicrostructure.current
    val depth = LocalStainSettings.current.depth.opticalFactor
    val liquid = style.theme == AppTheme.LIQUID_GLASS
    val oldScroll = style.theme == AppTheme.OLD_SCROLL
    val bodyColors = when (style.theme) {
        AppTheme.LIQUID_GLASS -> listOf(
            palette.primary,
            palette.secondary,
            palette.caustic,
            palette.tertiary,
            palette.primary,
        )
        AppTheme.ROYAL_GRAPHITE -> listOf(
            Color(0xFF10171B),
            palette.primary,
            palette.caustic,
            Color(0xFF080C0F),
        )
        AppTheme.OLD_SCROLL -> listOf(
            Color(0xFF6A4828),
            palette.secondary,
            palette.caustic,
            Color(0xFF8B6236),
            Color(0xFF4E351F),
        )
    }

    Canvas(modifier) {
        val scaleX = size.width / 512f
        val scaleY = size.height / 512f
        withTransform({ scale(scaleX, scaleY, pivot = Offset.Zero) }) {
            val startX = -72f + phase * 84f
            val body = Brush.linearGradient(
                colors = bodyColors,
                start = Offset(startX, 18f),
                end = Offset(520f + startX, 494f),
            )
            val outerRim = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = when {
                        liquid -> 0.92f
                        oldScroll -> 0.60f
                        else -> 0.38f
                    }),
                    palette.caustic.copy(alpha = 0.88f * depth),
                    palette.tertiary.copy(alpha = 0.62f * depth),
                    Color.White.copy(alpha = when {
                        liquid -> 0.76f
                        oldScroll -> 0.42f
                        else -> 0.18f
                    }),
                ),
                start = Offset(56f + phase * 38f, 0f),
                end = Offset(436f + phase * 38f, 512f),
            )
            val innerCaustic = Brush.linearGradient(
                colorStops = arrayOf(
                    0.00f to Color.Transparent,
                    0.24f to Color.White.copy(alpha = 0.08f),
                    0.43f to palette.caustic.copy(alpha = when {
                        liquid -> 0.92f
                        oldScroll -> 0.62f
                        else -> 0.46f
                    }),
                    0.56f to Color.White.copy(alpha = when {
                        liquid -> 0.72f
                        oldScroll -> 0.34f
                        else -> 0.22f
                    }),
                    0.78f to Color.Transparent,
                ),
                start = Offset(-100f + phase * 128f, 0f),
                end = Offset(430f + phase * 128f, 512f),
            )
            val pathStroke = Stroke(
                width = 72f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            )

            withTransform({ translate(0f, 10f) }) {
                drawPath(
                    path = ProxyBrandMarkPath,
                    color = style.shadow.copy(alpha = when {
                        liquid -> 0.22f
                        oldScroll -> 0.46f
                        else -> 0.72f
                    }),
                    style = Stroke(92f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
            drawPath(
                path = ProxyBrandMarkPath,
                brush = outerRim,
                alpha = 0.74f,
                style = Stroke(84f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            drawPath(path = ProxyBrandMarkPath, brush = body, style = pathStroke)
            drawPath(
                path = ProxyBrandMarkPath,
                brush = microstructure.fine,
                alpha = when {
                    liquid -> 0.34f
                    oldScroll -> 0.48f
                    else -> 0.24f
                },
                style = Stroke(66f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            drawPath(
                path = ProxyBrandMarkPath,
                brush = microstructure.spectral,
                alpha = when {
                    liquid -> 0.24f
                    oldScroll -> 0.30f
                    else -> 0.14f
                },
                style = Stroke(62f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            drawPath(
                path = ProxyBrandMarkPath,
                brush = innerCaustic,
                style = Stroke(11f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}

@Composable
private fun ProxyBrandWordmark(
    phase: Float,
) {
    val style = LocalProxyVisualStyle.current
    val palette = LocalStainPaletteColors.current
    val microstructure = LocalMaterialMicrostructure.current
    val liquid = style.theme == AppTheme.LIQUID_GLASS
    val oldScroll = style.theme == AppTheme.OLD_SCROLL
    val textStyle = TextStyle(
        fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        letterSpacing = (-0.7).sp,
    )
    val body = Brush.linearGradient(
        colors = when (style.theme) {
            AppTheme.LIQUID_GLASS -> listOf(
                palette.primary,
                palette.secondary,
                palette.caustic,
                palette.tertiary,
            )
            AppTheme.ROYAL_GRAPHITE -> listOf(
                palette.caustic,
                palette.primary,
                Color(0xFF6D7880),
                palette.secondary,
            )
            AppTheme.OLD_SCROLL -> listOf(
                Color(0xFF5B3D22),
                palette.primary,
                palette.caustic,
                Color(0xFF79532D),
            )
        },
        start = Offset(-40f + phase * 34f, 0f),
        end = Offset(260f + phase * 34f, 52f),
    )
    val highlight = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = when {
                liquid -> 0.72f
                oldScroll -> 0.42f
                else -> 0.24f
            }),
            Color.Transparent,
            palette.caustic.copy(alpha = when {
                liquid -> 0.22f
                oldScroll -> 0.16f
                else -> 0.10f
            }),
        ),
    )

    Box(contentAlignment = Alignment.CenterStart) {
        Text(
            text = "ProxyScroll",
            modifier = Modifier.offset(y = 1.2.dp),
            style = textStyle.copy(
                color = style.shadow.copy(alpha = when {
                    liquid -> 0.42f
                    oldScroll -> 0.56f
                    else -> 0.82f
                }),
                shadow = Shadow(
                    color = palette.primary.copy(alpha = when {
                        liquid -> 0.20f
                        oldScroll -> 0.12f
                        else -> 0.10f
                    }),
                    offset = Offset(0f, 1f),
                    blurRadius = 4f,
                ),
            ),
        )
        Text(text = "ProxyScroll", style = textStyle.copy(brush = body))
        Text(
            text = "ProxyScroll",
            style = textStyle.copy(brush = microstructure.fine),
            color = Color.Unspecified,
            modifier = Modifier
                .graphicsLayer {
                    alpha = when {
                        liquid -> 0.22f
                        oldScroll -> 0.34f
                        else -> 0.16f
                    }
                },
        )
        Text(
            text = "ProxyScroll",
            style = textStyle.copy(brush = microstructure.spectral),
            color = Color.Unspecified,
            modifier = Modifier
                .graphicsLayer {
                    alpha = when {
                        liquid -> 0.14f
                        oldScroll -> 0.20f
                        else -> 0.09f
                    }
                },
        )
        Text(
            text = "ProxyScroll",
            style = textStyle.copy(brush = highlight),
        )
    }
}
