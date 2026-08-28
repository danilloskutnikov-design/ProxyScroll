package com.proxyscroll.app.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.LinkedHashMap
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Reader layout is intentionally small: Original or Smart Resizer.
 * Smart Resizer trims dead page margins and also splits likely scanned book spreads.
 */
internal enum class PdfLayoutMode(val label: String) {
    ORIGINAL("Оригинал"),
    SMART_CROP("Smart Resizer"),
}

internal data class NormalizedPdfRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = (right - left).coerceAtLeast(0f)
    val height: Float get() = (bottom - top).coerceAtLeast(0f)
}

internal enum class PdfRegionRole {
    FULL_WIDTH,
    LEFT_PAGE,
    RIGHT_PAGE,
}

internal data class PdfPageRegion(
    val bounds: NormalizedPdfRect,
    val role: PdfRegionRole,
)

internal data class PdfPageLayoutAnalysis(
    val contentBounds: NormalizedPdfRect,
    val spreadRegions: List<PdfPageRegion>,
    val spreadDetected: Boolean,
    val confidence: Float,
)

internal data class SmartPdfRegionImage(
    val image: ImageBitmap,
    val role: PdfRegionRole,
) {
    val aspectRatio: Float
        get() = image.width.toFloat() / image.height.toFloat().coerceAtLeast(1f)
}

internal data class PdfPageAtmosphere(
    val edgeColorArgb: Int = Color.WHITE,
    val edgeUniformity: Float = 1f,
    val useSolidColor: Boolean = true,
    val blurredBackdrop: ImageBitmap? = null,
)

internal data class SmartPdfPageRender(
    val regions: List<SmartPdfRegionImage> = emptyList(),
    val analysis: PdfPageLayoutAnalysis? = null,
    val atmosphere: PdfPageAtmosphere? = null,
    val error: String? = null,
)

internal class SmartPdfReflowCache(
    private val maxEntries: Int = 8,
) {
    private val pages = object : LinkedHashMap<String, SmartPdfPageRender>(maxEntries, 0.75f, true) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<String, SmartPdfPageRender>?,
        ): Boolean = size > maxEntries
    }

    private fun key(page: Int, mode: PdfLayoutMode): String = "$page:${mode.name}"

    fun get(page: Int, mode: PdfLayoutMode): SmartPdfPageRender? = synchronized(this) {
        pages[key(page, mode)]
    }

    fun getOrRender(
        page: Int,
        mode: PdfLayoutMode,
        render: () -> SmartPdfPageRender,
    ): SmartPdfPageRender {
        get(page, mode)?.let { return it }
        val rendered = render()
        return synchronized(this) {
            pages[key(page, mode)] ?: rendered.also { pages[key(page, mode)] = it }
        }
    }

    fun clear() = synchronized(this) { pages.clear() }
}

internal suspend fun renderSmartPdfPageAsync(
    context: Context,
    uri: Uri,
    requestedPage: Int,
    mode: PdfLayoutMode,
    cache: SmartPdfReflowCache,
): SmartPdfPageRender = withContext(Dispatchers.IO) {
    renderSmartPdfPage(context, uri, requestedPage, mode, cache)
}

internal fun renderSmartPdfPage(
    context: Context,
    uri: Uri,
    requestedPage: Int,
    mode: PdfLayoutMode,
    cache: SmartPdfReflowCache,
): SmartPdfPageRender = runCatching {
    cache.get(requestedPage, mode)?.let { return@runCatching it }
    cache.getOrRender(requestedPage, mode) {
        val descriptor = context.contentResolver.openFileDescriptor(uri, "r")
            ?: error("Файл больше недоступен")
        descriptor.use { file ->
            PdfRenderer(file).use { renderer ->
                if (renderer.pageCount <= 0) error("В документе нет страниц")
                val actualPage = requestedPage.coerceIn(0, renderer.pageCount - 1)
                renderer.openPage(actualPage).use { pdfPage ->
                    val source = renderSourceBitmap(pdfPage)
                    val atmosphere = analyzePageAtmosphere(source)
                    val analysis = analyzePdfPageBitmap(source)
                    val regions = when (mode) {
                        PdfLayoutMode.ORIGINAL -> listOf(
                            PdfPageRegion(
                                bounds = NormalizedPdfRect(0f, 0f, 1f, 1f),
                                role = PdfRegionRole.FULL_WIDTH,
                            ),
                        )
                        PdfLayoutMode.SMART_CROP -> {
                            analysis.spreadRegions.ifEmpty {
                                listOf(PdfPageRegion(analysis.contentBounds, PdfRegionRole.FULL_WIDTH))
                            }
                        }
                    }

                    SmartPdfPageRender(
                        regions = regions.map { region ->
                            val rect = region.bounds.toPixelRect(source.width, source.height)
                            val cropped = Bitmap.createBitmap(
                                source,
                                rect.left,
                                rect.top,
                                rect.width,
                                rect.height,
                            )
                            SmartPdfRegionImage(
                                image = cropped.asImageBitmap(),
                                role = region.role,
                            )
                        },
                        analysis = analysis,
                        atmosphere = atmosphere,
                    )
                }
            }
        }
    }
}.getOrElse { error ->
    SmartPdfPageRender(error = error.message ?: "Не удалось проанализировать страницу")
}

private fun renderSourceBitmap(page: PdfRenderer.Page): Bitmap {
    val widthScale = 1800f / page.width.coerceAtLeast(1)
    val heightScale = 2700f / page.height.coerceAtLeast(1)
    val scale = min(widthScale, heightScale).coerceAtLeast(0.35f)
    val targetWidth = (page.width * scale).roundToInt().coerceAtLeast(1)
    val targetHeight = (page.height * scale).roundToInt().coerceAtLeast(1)
    return Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888).also { bitmap ->
        bitmap.eraseColor(Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
    }
}

private data class EdgeStatistics(
    val color: Int,
    val uniformity: Float,
    val nearWhite: Boolean,
)

private fun analyzePageAtmosphere(source: Bitmap): PdfPageAtmosphere {
    val stats = samplePageEdge(source)
    val useSolid = stats.nearWhite || stats.uniformity >= 0.86f
    return PdfPageAtmosphere(
        edgeColorArgb = if (stats.nearWhite) Color.WHITE else stats.color,
        edgeUniformity = stats.uniformity,
        useSolidColor = useSolid,
        blurredBackdrop = if (useSolid) null else createBlurredBackdrop(source),
    )
}

private fun samplePageEdge(source: Bitmap): EdgeStatistics {
    val width = source.width.coerceAtLeast(1)
    val height = source.height.coerceAtLeast(1)
    val bandX = max(1, width / 32)
    val bandY = max(1, height / 32)
    val step = max(1, min(width, height) / 72)

    var count = 0L
    var sumR = 0L
    var sumG = 0L
    var sumB = 0L
    var sumSq = 0.0

    fun add(color: Int) {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        sumR += r
        sumG += g
        sumB += b
        sumSq += (r * r + g * g + b * b).toDouble()
        count += 1
    }

    for (y in 0 until bandY step step) {
        for (x in 0 until width step step) {
            add(source.getPixel(x, y))
            add(source.getPixel(x, height - 1 - y))
        }
    }
    for (x in 0 until bandX step step) {
        for (y in bandY until (height - bandY).coerceAtLeast(bandY + 1) step step) {
            add(source.getPixel(x, y.coerceIn(0, height - 1)))
            add(source.getPixel(width - 1 - x, y.coerceIn(0, height - 1)))
        }
    }

    if (count <= 0L) return EdgeStatistics(Color.WHITE, 1f, true)
    val meanR = (sumR / count).toInt().coerceIn(0, 255)
    val meanG = (sumG / count).toInt().coerceIn(0, 255)
    val meanB = (sumB / count).toInt().coerceIn(0, 255)
    val meanSq = (meanR * meanR + meanG * meanG + meanB * meanB).toDouble()
    val variancePerChannel = ((sumSq / count) - meanSq).coerceAtLeast(0.0) / 3.0
    val sigma = sqrt(variancePerChannel)
    val uniformity = (1.0 - sigma / 92.0).toFloat().coerceIn(0f, 1f)
    val nearWhite = meanR >= 243 && meanG >= 243 && meanB >= 243 && uniformity >= 0.72f
    return EdgeStatistics(
        color = Color.rgb(meanR, meanG, meanB),
        uniformity = uniformity,
        nearWhite = nearWhite,
    )
}

private fun createBlurredBackdrop(source: Bitmap): ImageBitmap {
    val targetWidth = 72
    val targetHeight = (
        source.height.toFloat() * targetWidth / source.width.coerceAtLeast(1)
        ).roundToInt().coerceIn(48, 128)
    val small = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
        .copy(Bitmap.Config.ARGB_8888, true)
    repeat(3) { boxBlurInPlace(small, radius = 4) }
    return small.asImageBitmap()
}

private fun boxBlurInPlace(bitmap: Bitmap, radius: Int) {
    if (radius <= 0 || bitmap.width <= 1 || bitmap.height <= 1) return
    val width = bitmap.width
    val height = bitmap.height
    val source = IntArray(width * height)
    val target = IntArray(width * height)
    bitmap.getPixels(source, 0, width, 0, 0, width, height)

    for (y in 0 until height) {
        for (x in 0 until width) {
            var count = 0
            var r = 0
            var g = 0
            var b = 0
            val left = max(0, x - radius)
            val right = min(width - 1, x + radius)
            for (sx in left..right) {
                val c = source[y * width + sx]
                r += Color.red(c)
                g += Color.green(c)
                b += Color.blue(c)
                count += 1
            }
            target[y * width + x] = Color.rgb(r / count, g / count, b / count)
        }
    }

    for (y in 0 until height) {
        for (x in 0 until width) {
            var count = 0
            var r = 0
            var g = 0
            var b = 0
            val top = max(0, y - radius)
            val bottom = min(height - 1, y + radius)
            for (sy in top..bottom) {
                val c = target[sy * width + x]
                r += Color.red(c)
                g += Color.green(c)
                b += Color.blue(c)
                count += 1
            }
            source[y * width + x] = Color.rgb(r / count, g / count, b / count)
        }
    }
    bitmap.setPixels(source, 0, width, 0, 0, width, height)
}

private data class PixelRect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    val width: Int get() = (right - left).coerceAtLeast(1)
    val height: Int get() = (bottom - top).coerceAtLeast(1)
}

private fun NormalizedPdfRect.toPixelRect(width: Int, height: Int): PixelRect {
    val safeLeft = floor(left.coerceIn(0f, 1f) * width).toInt().coerceIn(0, width - 1)
    val safeTop = floor(top.coerceIn(0f, 1f) * height).toInt().coerceIn(0, height - 1)
    val safeRight = ceil(right.coerceIn(0f, 1f) * width).toInt().coerceIn(safeLeft + 1, width)
    val safeBottom = ceil(bottom.coerceIn(0f, 1f) * height).toInt().coerceIn(safeTop + 1, height)
    return PixelRect(safeLeft, safeTop, safeRight, safeBottom)
}

private data class MaskAnalysis(
    val width: Int,
    val height: Int,
    val foreground: BooleanArray,
    val rowInk: IntArray,
    val colInk: IntArray,
)

private data class SplitResult(
    val regions: List<PdfPageRegion> = emptyList(),
    val confidence: Float = 0f,
)

private fun analyzePdfPageBitmap(source: Bitmap): PdfPageLayoutAnalysis {
    val analysisWidth = source.width.coerceAtMost(360).coerceAtLeast(1)
    val analysisHeight = (
        source.height.toFloat() * analysisWidth / source.width.coerceAtLeast(1)
        ).roundToInt().coerceAtLeast(1)
    val sample = if (source.width == analysisWidth && source.height == analysisHeight) {
        source
    } else {
        Bitmap.createScaledBitmap(source, analysisWidth, analysisHeight, true)
    }

    val mask = buildForegroundMask(sample)
    val content = findContentBounds(mask)
    val sourceAspect = source.width.toFloat() / source.height.coerceAtLeast(1)
    val split = detectSpread(mask, content, sourceAspect)
    return PdfPageLayoutAnalysis(
        contentBounds = content,
        spreadRegions = split.regions,
        spreadDetected = split.regions.isNotEmpty(),
        confidence = split.confidence,
    )
}

private fun buildForegroundMask(sample: Bitmap): MaskAnalysis {
    val width = sample.width
    val height = sample.height
    val pixels = IntArray(width * height)
    sample.getPixels(pixels, 0, width, 0, 0, width, height)

    fun luminance(color: Int): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return (r * 299 + g * 587 + b * 114) / 1000
    }

    var borderSum = 0L
    var borderSamples = 0
    val stride = max(1, min(width, height) / 72)
    for (x in 0 until width step stride) {
        borderSum += luminance(pixels[x])
        borderSum += luminance(pixels[(height - 1) * width + x])
        borderSamples += 2
    }
    for (y in 0 until height step stride) {
        borderSum += luminance(pixels[y * width])
        borderSum += luminance(pixels[y * width + width - 1])
        borderSamples += 2
    }
    val background = if (borderSamples > 0) (borderSum / borderSamples).toInt() else 255
    val foreground = BooleanArray(width * height)
    val rowInk = IntArray(height)
    val colInk = IntArray(width)

    for (y in 0 until height) {
        val base = y * width
        for (x in 0 until width) {
            val lum = luminance(pixels[base + x])
            val isInk = abs(lum - background) >= 25
            if (isInk) {
                foreground[base + x] = true
                rowInk[y] += 1
                colInk[x] += 1
            }
        }
    }
    return MaskAnalysis(width, height, foreground, rowInk, colInk)
}

private fun findContentBounds(mask: MaskAnalysis): NormalizedPdfRect {
    val rowThreshold = max(1, (mask.width * 0.005f).roundToInt())
    val colThreshold = max(1, (mask.height * 0.005f).roundToInt())
    val firstRow = mask.rowInk.indexOfFirst { it >= rowThreshold }
    val lastRow = mask.rowInk.indexOfLast { it >= rowThreshold }
    val firstCol = mask.colInk.indexOfFirst { it >= colThreshold }
    val lastCol = mask.colInk.indexOfLast { it >= colThreshold }
    if (firstRow < 0 || lastRow < firstRow || firstCol < 0 || lastCol < firstCol) {
        return NormalizedPdfRect(0f, 0f, 1f, 1f)
    }

    val padX = max(2, (mask.width * 0.012f).roundToInt())
    val padY = max(2, (mask.height * 0.012f).roundToInt())
    return NormalizedPdfRect(
        left = (firstCol - padX).coerceAtLeast(0).toFloat() / mask.width,
        top = (firstRow - padY).coerceAtLeast(0).toFloat() / mask.height,
        right = (lastCol + 1 + padX).coerceAtMost(mask.width).toFloat() / mask.width,
        bottom = (lastRow + 1 + padY).coerceAtMost(mask.height).toFloat() / mask.height,
    )
}

/**
 * More permissive than alpha38: a clearly wide page may split even when the physical
 * spine is dark or slightly noisy. The detector still requires meaningful content on
 * both sides and searches for a local ink valley near the geometric center.
 */
private fun detectSpread(
    mask: MaskAnalysis,
    content: NormalizedPdfRect,
    sourceAspect: Float,
): SplitResult {
    if (content.width < 0.60f || content.height < 0.42f) return SplitResult()
    val contentAspect = sourceAspect * (content.width / content.height.coerceAtLeast(0.01f))
    val clearlyWide = sourceAspect >= 1.34f || contentAspect >= 1.46f
    val moderatelyWide = sourceAspect >= 1.12f || contentAspect >= 1.24f
    if (!moderatelyWide) return SplitResult()

    val x0 = (content.left * mask.width).roundToInt().coerceIn(0, mask.width - 1)
    val x1 = (content.right * mask.width).roundToInt().coerceIn(x0 + 1, mask.width)
    val y0 = (content.top * mask.height).roundToInt().coerceIn(0, mask.height - 1)
    val y1 = (content.bottom * mask.height).roundToInt().coerceIn(y0 + 1, mask.height)
    val contentWidthPx = (x1 - x0).coerceAtLeast(2)
    val centerStart = (x0 + contentWidthPx * 0.34f).roundToInt()
    val centerEnd = (x0 + contentWidthPx * 0.66f).roundToInt().coerceAtLeast(centerStart + 1)

    fun smoothedInk(x: Int): Float {
        val radius = max(1, mask.width / 180)
        var total = 0
        var count = 0
        for (sx in (x - radius).coerceAtLeast(x0)..(x + radius).coerceAtMost(x1 - 1)) {
            total += mask.colInk[sx]
            count += 1
        }
        return total.toFloat() / count.coerceAtLeast(1)
    }

    val gutterX = (centerStart until centerEnd).minByOrNull(::smoothedInk) ?: return SplitResult()
    val gutterHalf = max(2, mask.width / 95)
    val gutterLeft = (gutterX - gutterHalf).coerceAtLeast(x0)
    val gutterRight = (gutterX + gutterHalf).coerceAtMost(x1 - 1)
    val bandWidth = (gutterRight - gutterLeft + 1).coerceAtLeast(1)

    var blankRows = 0
    var inspectedRows = 0
    for (y in y0 until y1) {
        var ink = 0
        for (x in gutterLeft..gutterRight) {
            if (mask.foreground[y * mask.width + x]) ink += 1
        }
        if (ink <= max(1, (bandWidth * 0.16f).roundToInt())) blankRows += 1
        inspectedRows += 1
    }
    val blankRatio = blankRows.toFloat() / inspectedRows.coerceAtLeast(1)

    val localRadius = max(5, mask.width / 18)
    val localStart = (gutterX - localRadius).coerceAtLeast(x0)
    val localEnd = (gutterX + localRadius).coerceAtMost(x1 - 1)
    var neighborhoodInk = 0f
    var neighborhoodCount = 0
    for (x in localStart..localEnd) {
        if (x !in gutterLeft..gutterRight) {
            neighborhoodInk += smoothedInk(x)
            neighborhoodCount += 1
        }
    }
    neighborhoodInk /= neighborhoodCount.coerceAtLeast(1)
    val gutterInk = (gutterLeft..gutterRight).map { smoothedInk(it) }.average().toFloat()
    val gutterStrength = if (neighborhoodInk <= 0.5f) 0f else {
        (1f - gutterInk / neighborhoodInk).coerceIn(0f, 1f)
    }

    val leftInk = sumInk(mask, x0, gutterLeft, y0, y1)
    val rightInk = sumInk(mask, gutterRight + 1, x1, y0, y1)
    val balance = min(leftInk, rightInk).toFloat() / max(leftInk, rightInk).coerceAtLeast(1)
    val splitFraction = (gutterX - x0).toFloat() / contentWidthPx
    val centered = 1f - (abs(splitFraction - 0.5f) / 0.18f).coerceIn(0f, 1f)

    val gutterEvidence = blankRatio >= 0.44f && gutterStrength >= 0.28f && balance >= 0.18f
    val wideFallback = clearlyWide && centered >= 0.35f && balance >= 0.27f && gutterStrength >= 0.10f
    val accepted = gutterEvidence || wideFallback
    val confidence = (
        blankRatio * 0.34f +
            gutterStrength * 0.28f +
            balance * 0.22f +
            centered * 0.16f
        ).coerceIn(0f, 1f)
    if (!accepted) return SplitResult(confidence = confidence)

    val leftBounds = tightBounds(mask, x0, gutterLeft, y0, y1)
    val rightBounds = tightBounds(mask, gutterRight + 1, x1, y0, y1)
    if (leftBounds.width < 0.16f || rightBounds.width < 0.16f) {
        return SplitResult(confidence = confidence)
    }

    return SplitResult(
        regions = listOf(
            PdfPageRegion(leftBounds, PdfRegionRole.LEFT_PAGE),
            PdfPageRegion(rightBounds, PdfRegionRole.RIGHT_PAGE),
        ),
        confidence = confidence,
    )
}

private fun sumInk(
    mask: MaskAnalysis,
    xStart: Int,
    xEndExclusive: Int,
    yStart: Int,
    yEndExclusive: Int,
): Int {
    var sum = 0
    val safeStart = xStart.coerceIn(0, mask.width)
    val safeEnd = xEndExclusive.coerceIn(safeStart, mask.width)
    for (y in yStart.coerceIn(0, mask.height) until yEndExclusive.coerceIn(0, mask.height)) {
        for (x in safeStart until safeEnd) {
            if (mask.foreground[y * mask.width + x]) sum += 1
        }
    }
    return sum
}

private fun tightBounds(
    mask: MaskAnalysis,
    xStart: Int,
    xEndExclusive: Int,
    yStart: Int,
    yEndExclusive: Int,
): NormalizedPdfRect {
    val safeX0 = xStart.coerceIn(0, mask.width - 1)
    val safeX1 = xEndExclusive.coerceIn(safeX0 + 1, mask.width)
    val safeY0 = yStart.coerceIn(0, mask.height - 1)
    val safeY1 = yEndExclusive.coerceIn(safeY0 + 1, mask.height)
    var minX = safeX1
    var maxX = safeX0
    var minY = safeY1
    var maxY = safeY0
    var found = false

    for (y in safeY0 until safeY1) {
        for (x in safeX0 until safeX1) {
            if (mask.foreground[y * mask.width + x]) {
                minX = min(minX, x)
                maxX = max(maxX, x)
                minY = min(minY, y)
                maxY = max(maxY, y)
                found = true
            }
        }
    }
    if (!found) {
        return NormalizedPdfRect(
            safeX0.toFloat() / mask.width,
            safeY0.toFloat() / mask.height,
            safeX1.toFloat() / mask.width,
            safeY1.toFloat() / mask.height,
        )
    }

    val padX = max(2, (mask.width * 0.010f).roundToInt())
    val padY = max(2, (mask.height * 0.010f).roundToInt())
    return NormalizedPdfRect(
        left = (minX - padX).coerceAtLeast(safeX0).toFloat() / mask.width,
        top = (minY - padY).coerceAtLeast(safeY0).toFloat() / mask.height,
        right = (maxX + 1 + padX).coerceAtMost(safeX1).toFloat() / mask.width,
        bottom = (maxY + 1 + padY).coerceAtMost(safeY1).toFloat() / mask.height,
    )
}
