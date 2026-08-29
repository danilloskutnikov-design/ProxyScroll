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
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * PDF rendering has intentionally been reduced to two predictable modes.
 * ORIGINAL keeps the physical PDF page. SMART_CROP creates a real cropped bitmap
 * before Compose sees it, so page positioning can never masquerade as cropping.
 */
internal enum class PdfLayoutMode(val label: String) {
    ORIGINAL("Оригинал"),
    SMART_CROP("Smart Resizer"),
}

/**
 * Landscape spreads are split by the reader into virtual pages first. Each half
 * then goes through the exact same crop pipeline independently.
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
    private val maxEntries: Int = 10,
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
                    val sliced = createPhysicalSlice(source, effectiveSlice)
                    val bounds = if (mode == PdfLayoutMode.SMART_CROP) {
                        findRobustContentBounds(sliced)
                    } else {
                        NormalizedPdfRect(0f, 0f, 1f, 1f)
                    }
                    val cropRect = bounds.toPixelRect(sliced.width, sliced.height)
                    val cropped = Bitmap.createBitmap(
                        sliced,
                        cropRect.left,
                        cropRect.top,
                        cropRect.width,
                        cropRect.height,
                    )
                    val role = when (effectiveSlice) {
                        PdfPageSlice.FULL -> PdfRegionRole.FULL_WIDTH
                        PdfPageSlice.LEFT_HALF -> PdfRegionRole.LEFT_PAGE
                        PdfPageSlice.RIGHT_HALF -> PdfRegionRole.RIGHT_PAGE
                    }
                    SmartPdfPageRender(
                        regions = listOf(
                            SmartPdfRegionImage(
                                image = cropped.asImageBitmap(),
                                role = role,
                            ),
                        ),
                        analysis = PdfPageLayoutAnalysis(
                            contentBounds = bounds,
                            sourceAspect = sliced.width.toFloat() / sliced.height.coerceAtLeast(1),
                            slice = effectiveSlice,
                        ),
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
    val widthScale = 1900f / page.width.coerceAtLeast(1)
    val heightScale = 2800f / page.height.coerceAtLeast(1)
    val scale = min(3.0f, min(widthScale, heightScale)).coerceAtLeast(0.02f)
    val targetWidth = (page.width * scale).roundToInt().coerceAtLeast(1)
    val targetHeight = (page.height * scale).roundToInt().coerceAtLeast(1)
    return Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888).also { bitmap ->
        bitmap.eraseColor(Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
    }
}

private fun createPhysicalSlice(source: Bitmap, slice: PdfPageSlice): Bitmap {
    if (slice == PdfPageSlice.FULL) return source
    val split = source.width / 2
    val left = if (slice == PdfPageSlice.LEFT_HALF) 0 else split
    val right = if (slice == PdfPageSlice.LEFT_HALF) split else source.width
    return Bitmap.createBitmap(
        source,
        left.coerceIn(0, source.width - 1),
        0,
        (right - left).coerceAtLeast(1),
        source.height.coerceAtLeast(1),
    )
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
    val safeWidth = width.coerceAtLeast(1)
    val safeHeight = height.coerceAtLeast(1)
    val safeLeft = floor(left.coerceIn(0f, 1f) * safeWidth).toInt().coerceIn(0, safeWidth - 1)
    val safeTop = floor(top.coerceIn(0f, 1f) * safeHeight).toInt().coerceIn(0, safeHeight - 1)
    val safeRight = ceil(right.coerceIn(0f, 1f) * safeWidth).toInt().coerceIn(safeLeft + 1, safeWidth)
    val safeBottom = ceil(bottom.coerceIn(0f, 1f) * safeHeight).toInt().coerceIn(safeTop + 1, safeHeight)
    return PixelRect(safeLeft, safeTop, safeRight, safeBottom)
}

/**
 * Finds the printed/scanned content on an already sliced page. This intentionally
 * does not infer a gutter. It estimates the lightest paper tone using a high
 * luminance percentile, treats sufficiently darker or chromatic pixels as ink,
 * removes solid scanner borders, then uses smoothed row/column projections.
 */
private fun findRobustContentBounds(source: Bitmap): NormalizedPdfRect {
    if (source.width < 8 || source.height < 8) return NormalizedPdfRect(0f, 0f, 1f, 1f)

    val scale = min(
        1f,
        min(
            440f / source.width.coerceAtLeast(1),
            660f / source.height.coerceAtLeast(1),
        ),
    )
    val sampleWidth = (source.width * scale).roundToInt().coerceAtLeast(1)
    val sampleHeight = (source.height * scale).roundToInt().coerceAtLeast(1)
    val sample = if (sampleWidth == source.width && sampleHeight == source.height) {
        source
    } else {
        Bitmap.createScaledBitmap(source, sampleWidth, sampleHeight, true)
    }

    val pixels = IntArray(sampleWidth * sampleHeight)
    sample.getPixels(pixels, 0, sampleWidth, 0, 0, sampleWidth, sampleHeight)
    val luminances = IntArray(pixels.size)
    val histogram = IntArray(256)
    pixels.forEachIndexed { index, color ->
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val lum = (r * 299 + g * 587 + b * 114) / 1000
        luminances[index] = lum
        histogram[lum] += 1
    }

    val paperLum = percentileFromHistogram(histogram, pixels.size, 0.88f)
    val contrastDrop = when {
        paperLum >= 242 -> 24
        paperLum >= 220 -> 22
        paperLum >= 195 -> 19
        else -> 16
    }
    val inkThreshold = (paperLum - contrastDrop).coerceIn(118, 236)
    val foreground = BooleanArray(pixels.size)
    pixels.forEachIndexed { index, color ->
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val chroma = max(r, max(g, b)) - min(r, min(g, b))
        val lum = luminances[index]
        foreground[index] = lum <= inkThreshold ||
            (chroma >= 34 && lum <= (paperLum + 6).coerceAtMost(245))
    }

    val rawRowInk = IntArray(sampleHeight)
    val rawColInk = IntArray(sampleWidth)
    for (y in 0 until sampleHeight) {
        val base = y * sampleWidth
        for (x in 0 until sampleWidth) {
            if (foreground[base + x]) {
                rawRowInk[y] += 1
                rawColInk[x] += 1
            }
        }
    }

    var safeTop = 0
    var safeBottom = sampleHeight - 1
    var safeLeft = 0
    var safeRight = sampleWidth - 1
    val maxTrimY = max(1, (sampleHeight * 0.07f).roundToInt())
    val maxTrimX = max(1, (sampleWidth * 0.07f).roundToInt())
    while (
        safeTop < maxTrimY &&
        rawRowInk[safeTop] >= sampleWidth * 0.56f
    ) safeTop += 1
    while (
        sampleHeight - 1 - safeBottom < maxTrimY &&
        rawRowInk[safeBottom] >= sampleWidth * 0.56f
    ) safeBottom -= 1
    while (
        safeLeft < maxTrimX &&
        rawColInk[safeLeft] >= sampleHeight * 0.56f
    ) safeLeft += 1
    while (
        sampleWidth - 1 - safeRight < maxTrimX &&
        rawColInk[safeRight] >= sampleHeight * 0.56f
    ) safeRight -= 1

    if (safeLeft >= safeRight || safeTop >= safeBottom) {
        return NormalizedPdfRect(0f, 0f, 1f, 1f)
    }

    val rowInk = IntArray(sampleHeight)
    val colInk = IntArray(sampleWidth)
    for (y in safeTop..safeBottom) {
        val base = y * sampleWidth
        for (x in safeLeft..safeRight) {
            if (foreground[base + x]) {
                rowInk[y] += 1
                colInk[x] += 1
            }
        }
    }

    val effectiveWidth = (safeRight - safeLeft + 1).coerceAtLeast(1)
    val effectiveHeight = (safeBottom - safeTop + 1).coerceAtLeast(1)
    val rowThreshold = max(2, (effectiveWidth * 0.0045f).roundToInt())
    val colThreshold = max(2, (effectiveHeight * 0.0045f).roundToInt())
    val smoothRows = smoothProjection(rowInk, 2)
    val smoothCols = smoothProjection(colInk, 2)
    val firstRow = findStableStart(smoothRows, safeTop, safeBottom, rowThreshold)
    val lastRow = findStableEnd(smoothRows, safeTop, safeBottom, rowThreshold)
    val firstCol = findStableStart(smoothCols, safeLeft, safeRight, colThreshold)
    val lastCol = findStableEnd(smoothCols, safeLeft, safeRight, colThreshold)

    if (firstRow < 0 || lastRow < firstRow || firstCol < 0 || lastCol < firstCol) {
        return NormalizedPdfRect(0f, 0f, 1f, 1f)
    }

    val detectedWidth = lastCol - firstCol + 1
    val detectedHeight = lastRow - firstRow + 1
    if (
        detectedWidth < sampleWidth * 0.16f ||
        detectedHeight < sampleHeight * 0.14f
    ) {
        return NormalizedPdfRect(0f, 0f, 1f, 1f)
    }

    val padX = max(3, (sampleWidth * 0.018f).roundToInt())
    val padY = max(3, (sampleHeight * 0.016f).roundToInt())
    val left = (firstCol - padX).coerceAtLeast(0)
    val top = (firstRow - padY).coerceAtLeast(0)
    val right = (lastCol + 1 + padX).coerceAtMost(sampleWidth)
    val bottom = (lastRow + 1 + padY).coerceAtMost(sampleHeight)

    // Ignore sub-pixel/noise-only trims. Real Smart Resizer crops must remove a
    // visible amount of dead paper; otherwise keep the physical slice intact.
    val trimLeft = left.toFloat() / sampleWidth
    val trimTop = top.toFloat() / sampleHeight
    val trimRight = 1f - right.toFloat() / sampleWidth
    val trimBottom = 1f - bottom.toFloat() / sampleHeight
    val meaningful = max(max(trimLeft, trimRight), max(trimTop, trimBottom)) >= 0.012f
    if (!meaningful) return NormalizedPdfRect(0f, 0f, 1f, 1f)

    return NormalizedPdfRect(
        left = left.toFloat() / sampleWidth,
        top = top.toFloat() / sampleHeight,
        right = right.toFloat() / sampleWidth,
        bottom = bottom.toFloat() / sampleHeight,
    )
}

private fun percentileFromHistogram(
    histogram: IntArray,
    total: Int,
    percentile: Float,
): Int {
    if (total <= 0) return 255
    val target = (total * percentile.coerceIn(0f, 1f)).roundToInt()
    var accumulated = 0
    histogram.forEachIndexed { value, count ->
        accumulated += count
        if (accumulated >= target) return value
    }
    return 255
}

private fun smoothProjection(values: IntArray, radius: Int): IntArray {
    if (values.isEmpty()) return values
    return IntArray(values.size) { index ->
        var sum = 0
        var count = 0
        for (i in (index - radius).coerceAtLeast(0)..(index + radius).coerceAtMost(values.lastIndex)) {
            sum += values[i]
            count += 1
        }
        if (count == 0) 0 else (sum.toFloat() / count).roundToInt()
    }
}

private fun findStableStart(
    values: IntArray,
    start: Int,
    end: Int,
    threshold: Int,
): Int {
    for (index in start..end) {
        var hits = 0
        val windowEnd = (index + 5).coerceAtMost(end)
        for (i in index..windowEnd) if (values[i] >= threshold) hits += 1
        if (hits >= 2) return index
    }
    return -1
}

private fun findStableEnd(
    values: IntArray,
    start: Int,
    end: Int,
    threshold: Int,
): Int {
    for (index in end downTo start) {
        var hits = 0
        val windowStart = (index - 5).coerceAtLeast(start)
        for (i in windowStart..index) if (values[i] >= threshold) hits += 1
        if (hits >= 2) return index
    }
    return -1
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
        blurredBackdrop = if (useSolid) null else createSoftBackdrop(source),
    )
}

private fun samplePageEdge(source: Bitmap): EdgeStatistics {
    val width = source.width.coerceAtLeast(1)
    val height = source.height.coerceAtLeast(1)
    val bandX = max(1, width / 36)
    val bandY = max(1, height / 36)
    val step = max(1, min(width, height) / 80)
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
            val safeY = y.coerceIn(0, height - 1)
            add(source.getPixel(x, safeY))
            add(source.getPixel(width - 1 - x, safeY))
        }
    }

    if (count <= 0L) return EdgeStatistics(Color.WHITE, 1f, true)
    val meanR = (sumR / count).toInt().coerceIn(0, 255)
    val meanG = (sumG / count).toInt().coerceIn(0, 255)
    val meanB = (sumB / count).toInt().coerceIn(0, 255)
    val meanSq = (meanR * meanR + meanG * meanG + meanB * meanB).toDouble()
    val variance = ((sumSq / count) - meanSq).coerceAtLeast(0.0) / 3.0
    val sigma = sqrt(variance)
    val uniformity = (1.0 - sigma / 92.0).coerceIn(0.0, 1.0).toFloat()
    val nearWhite = meanR >= 242 && meanG >= 242 && meanB >= 242 && uniformity >= 0.68f
    return EdgeStatistics(
        color = Color.rgb(meanR, meanG, meanB),
        uniformity = uniformity,
        nearWhite = nearWhite,
    )
}

private fun createSoftBackdrop(source: Bitmap): ImageBitmap {
    val targetWidth = 48
    val targetHeight = (
        source.height.toFloat() * targetWidth / source.width.coerceAtLeast(1)
        ).roundToInt().coerceIn(32, 96)
    // Upscaling this deliberately tiny bilinear image in Compose produces a
    // cheap, stable background blur without RenderEffect or per-frame work.
    return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true).asImageBitmap()
}
