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
 * The reader deliberately exposes only the two modes that behave predictably.
 * Smart Resizer trims dead paper margins while keeping the actual PDF geometry.
 */
internal enum class PdfLayoutMode(val label: String) {
    ORIGINAL("Оригинал"),
    SMART_CROP("Smart Resizer"),
}

/**
 * A physical PDF page may become two virtual reader pages. The decision to split
 * lives in ModernPdfReader and is based on document/page orientation; this layer
 * only renders the requested half and never tries to guess a book gutter.
 */
internal enum class PdfPageSlice {
    FULL,
    LEFT_HALF,
    RIGHT_HALF,
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

internal data class PdfPageLayoutAnalysis(
    val contentBounds: NormalizedPdfRect,
    val sourceAspect: Float,
    val slice: PdfPageSlice,
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

    private fun key(page: Int, mode: PdfLayoutMode, slice: PdfPageSlice): String =
        "$page:${mode.name}:${slice.name}"

    fun get(
        page: Int,
        mode: PdfLayoutMode,
        slice: PdfPageSlice = PdfPageSlice.FULL,
    ): SmartPdfPageRender? = synchronized(this) {
        pages[key(page, mode, slice)]
    }

    fun getOrRender(
        page: Int,
        mode: PdfLayoutMode,
        slice: PdfPageSlice,
        render: () -> SmartPdfPageRender,
    ): SmartPdfPageRender {
        get(page, mode, slice)?.let { return it }
        val rendered = render()
        return synchronized(this) {
            pages[key(page, mode, slice)] ?: rendered.also {
                pages[key(page, mode, slice)] = it
            }
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
    slice: PdfPageSlice = PdfPageSlice.FULL,
): SmartPdfPageRender = withContext(Dispatchers.IO) {
    renderSmartPdfPage(context, uri, requestedPage, mode, cache, slice)
}

internal fun renderSmartPdfPage(
    context: Context,
    uri: Uri,
    requestedPage: Int,
    mode: PdfLayoutMode,
    cache: SmartPdfReflowCache,
    slice: PdfPageSlice = PdfPageSlice.FULL,
): SmartPdfPageRender = runCatching {
    cache.get(requestedPage, mode, slice)?.let { return@runCatching it }
    cache.getOrRender(requestedPage, mode, slice) {
        val descriptor = context.contentResolver.openFileDescriptor(uri, "r")
            ?: error("Файл больше недоступен")
        descriptor.use { file ->
            PdfRenderer(file).use { renderer ->
                if (renderer.pageCount <= 0) error("В документе нет страниц")
                val actualPage = requestedPage.coerceIn(0, renderer.pageCount - 1)
                renderer.openPage(actualPage).use { pdfPage ->
                    val source = renderSourceBitmap(pdfPage)
                    val atmosphere = analyzePageAtmosphere(source)
                    val effectiveSlice = if (mode == PdfLayoutMode.ORIGINAL) {
                        PdfPageSlice.FULL
                    } else {
                        slice
                    }
                    val analysis = analyzePdfPageBitmap(source, effectiveSlice)
                    val bounds = when (mode) {
                        PdfLayoutMode.ORIGINAL -> NormalizedPdfRect(0f, 0f, 1f, 1f)
                        PdfLayoutMode.SMART_CROP -> analysis.contentBounds
                    }
                    val role = when (effectiveSlice) {
                        PdfPageSlice.FULL -> PdfRegionRole.FULL_WIDTH
                        PdfPageSlice.LEFT_HALF -> PdfRegionRole.LEFT_PAGE
                        PdfPageSlice.RIGHT_HALF -> PdfRegionRole.RIGHT_PAGE
                    }
                    val rect = bounds.toPixelRect(source.width, source.height)
                    val cropped = Bitmap.createBitmap(
                        source,
                        rect.left,
                        rect.top,
                        rect.width,
                        rect.height,
                    )
                    SmartPdfPageRender(
                        regions = listOf(
                            SmartPdfRegionImage(
                                image = cropped.asImageBitmap(),
                                role = role,
                            ),
                        ),
                        analysis = analysis.copy(contentBounds = bounds),
                        atmosphere = atmosphere,
                    )
                }
            }
        }
    }
}.getOrElse { error ->
    SmartPdfPageRender(error = error.message ?: "Не удалось отрисовать страницу")
}

private fun renderSourceBitmap(page: PdfRenderer.Page): Bitmap {
    val widthScale = 1800f / page.width.coerceAtLeast(1)
    val heightScale = 2700f / page.height.coerceAtLeast(1)
    val scale = min(widthScale, heightScale).coerceIn(0.25f, 3.2f)
    val targetWidth = (page.width * scale).roundToInt().coerceAtLeast(1)
    val targetHeight = (page.height * scale).roundToInt().coerceAtLeast(1)
    return Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888).also { bitmap ->
        bitmap.eraseColor(Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
    }
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
)

private fun analyzePdfPageBitmap(
    source: Bitmap,
    slice: PdfPageSlice,
): PdfPageLayoutAnalysis {
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
    return PdfPageLayoutAnalysis(
        contentBounds = findContentBounds(mask, slice),
        sourceAspect = source.width.toFloat() / source.height.coerceAtLeast(1),
        slice = slice,
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
    for (index in pixels.indices) {
        val lum = luminance(pixels[index])
        foreground[index] = abs(lum - background) >= 28
    }
    return MaskAnalysis(width, height, foreground)
}

private fun findContentBounds(
    mask: MaskAnalysis,
    slice: PdfPageSlice,
): NormalizedPdfRect {
    val xStart = when (slice) {
        PdfPageSlice.FULL, PdfPageSlice.LEFT_HALF -> 0
        PdfPageSlice.RIGHT_HALF -> mask.width / 2
    }
    val xEnd = when (slice) {
        PdfPageSlice.FULL, PdfPageSlice.RIGHT_HALF -> mask.width
        PdfPageSlice.LEFT_HALF -> mask.width / 2
    }.coerceAtLeast(xStart + 1)

    val rowThreshold = max(1, ((xEnd - xStart) * 0.006f).roundToInt())
    val colThreshold = max(1, (mask.height * 0.006f).roundToInt())
    var firstRow = -1
    var lastRow = -1
    var firstCol = -1
    var lastCol = -1

    for (y in 0 until mask.height) {
        var ink = 0
        val base = y * mask.width
        for (x in xStart until xEnd) {
            if (mask.foreground[base + x]) ink += 1
        }
        if (ink >= rowThreshold) {
            if (firstRow < 0) firstRow = y
            lastRow = y
        }
    }
    for (x in xStart until xEnd) {
        var ink = 0
        for (y in 0 until mask.height) {
            if (mask.foreground[y * mask.width + x]) ink += 1
        }
        if (ink >= colThreshold) {
            if (firstCol < 0) firstCol = x
            lastCol = x
        }
    }

    if (firstRow < 0 || lastRow < firstRow || firstCol < 0 || lastCol < firstCol) {
        return NormalizedPdfRect(
            left = xStart.toFloat() / mask.width,
            top = 0f,
            right = xEnd.toFloat() / mask.width,
            bottom = 1f,
        )
    }

    val padX = max(2, (mask.width * 0.012f).roundToInt())
    val padY = max(2, (mask.height * 0.012f).roundToInt())
    return NormalizedPdfRect(
        left = (firstCol - padX).coerceAtLeast(xStart).toFloat() / mask.width,
        top = (firstRow - padY).coerceAtLeast(0).toFloat() / mask.height,
        right = (lastCol + 1 + padX).coerceAtMost(xEnd).toFloat() / mask.width,
        bottom = (lastRow + 1 + padY).coerceAtMost(mask.height).toFloat() / mask.height,
    )
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
            val sy = y.coerceIn(0, height - 1)
            add(source.getPixel(x, sy))
            add(source.getPixel(width - 1 - x, sy))
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
