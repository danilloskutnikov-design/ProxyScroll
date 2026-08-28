package com.proxyscroll.app.ui

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.proxyscroll.app.ui.theme.ProxySurface
import com.proxyscroll.app.ui.theme.ProxySurfaceRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import java.util.LinkedHashMap
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Rendering modes are deliberately independent from OCR. SMART_CROP only removes
 * dead page margins. REFLOW additionally detects a stable vertical gutter and
 * places the left and right columns into one phone-width reading stream.
 */
internal enum class PdfLayoutMode(val label: String) {
    ORIGINAL("Оригинал"),
    SMART_CROP("Smart Crop"),
    REFLOW("Smart Reflow"),
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
    LEFT_COLUMN,
    RIGHT_COLUMN,
}

internal data class PdfPageRegion(
    val bounds: NormalizedPdfRect,
    val role: PdfRegionRole,
)

internal data class PdfPageLayoutAnalysis(
    val contentBounds: NormalizedPdfRect,
    val readingRegions: List<PdfPageRegion>,
    val columnCount: Int,
    val confidence: Float,
)

internal data class SmartPdfRegionImage(
    val image: ImageBitmap,
    val role: PdfRegionRole,
) {
    val aspectRatio: Float
        get() = image.width.toFloat() / image.height.toFloat().coerceAtLeast(1f)
}

internal data class SmartPdfPageRender(
    val regions: List<SmartPdfRegionImage> = emptyList(),
    val analysis: PdfPageLayoutAnalysis? = null,
    val error: String? = null,
)

/** Keeps only a few analyzed pages: cropped regions are approximately one source page each. */
internal class SmartPdfReflowCache(
    private val maxEntries: Int = 3,
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

@Composable
internal fun SmartPdfReflowPage(
    documentUri: String,
    page: Int,
    mode: PdfLayoutMode,
    readingProfile: PdfReadingProfile,
    cache: SmartPdfReflowCache,
    isCurrent: Boolean,
    onInteractionChanged: (Boolean) -> Unit,
    onCenterTap: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var viewportWidth by remember(page, mode) { mutableIntStateOf(1) }
    val cached = remember(documentUri, page, mode) { cache.get(page, mode) }
    val render by produceState(
        initialValue = cached ?: SmartPdfPageRender(),
        key1 = documentUri,
        key2 = page,
        key3 = mode,
    ) {
        if (value.regions.isEmpty() && value.error == null) {
            value = withContext(Dispatchers.IO) {
                renderSmartPdfPage(
                    context = context,
                    uri = Uri.parse(documentUri),
                    requestedPage = page,
                    mode = mode,
                    cache = cache,
                )
            }
        }
    }

    LaunchedEffect(listState, isCurrent) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collectLatest { scrolling ->
                onInteractionChanged(isCurrent && scrolling)
            }
    }
    DisposableEffect(isCurrent) {
        onDispose {
            if (isCurrent) onInteractionChanged(false)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { viewportWidth = it.size.width.coerceAtLeast(1) }
            .pointerInput(page, mode, viewportWidth) {
                detectTapGestures { position ->
                    when {
                        position.x < viewportWidth * 0.18f -> onPreviousPage()
                        position.x > viewportWidth * 0.82f -> onNextPage()
                        else -> onCenterTap()
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        when {
            render.error != null -> Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    render.error.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            render.regions.isEmpty() -> CircularProgressIndicator()

            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = 86.dp,
                    bottom = 142.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    val analysis = render.analysis
                    ProxySurface(
                        modifier = Modifier.fillMaxWidth(),
                        role = ProxySurfaceRole.OVERLAY,
                        strong = false,
                        interactive = false,
                        deformContent = false,
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = if (analysis?.columnCount == 2 && mode == PdfLayoutMode.REFLOW) {
                                    "Smart Reflow · 2 колонки"
                                } else {
                                    "Smart Crop · поля удалены"
                                },
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Text(
                                text = "Уверенность ${(analysis?.confidence?.times(100f) ?: 0f).roundToInt()}% · без OCR",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                itemsIndexed(render.regions) { index, region ->
                    ProxySurface(
                        modifier = Modifier.fillMaxWidth(),
                        role = ProxySurfaceRole.CARD,
                        strong = true,
                        interactive = false,
                        deformContent = false,
                    ) {
                        Image(
                            bitmap = region.image,
                            contentDescription = "Страница ${page + 1}, фрагмент ${index + 1}",
                            contentScale = ContentScale.FillWidth,
                            colorFilter = pdfReadingColorFilter(readingProfile),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(region.aspectRatio),
                        )
                    }
                }

                item { Spacer(Modifier.height(6.dp)) }
            }
        }
    }
}

private fun renderSmartPdfPage(
    context: android.content.Context,
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
                    val targetWidth = 1800
                    val targetHeight = (targetWidth.toFloat() * pdfPage.height / pdfPage.width)
                        .roundToInt()
                        .coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(
                        targetWidth,
                        targetHeight,
                        Bitmap.Config.ARGB_8888,
                    )
                    bitmap.eraseColor(Color.WHITE)
                    pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    val analysis = analyzePdfPageBitmap(bitmap)
                    val chosenRegions = when (mode) {
                        PdfLayoutMode.ORIGINAL -> listOf(
                            PdfPageRegion(
                                NormalizedPdfRect(0f, 0f, 1f, 1f),
                                PdfRegionRole.FULL_WIDTH,
                            ),
                        )
                        PdfLayoutMode.SMART_CROP -> listOf(
                            PdfPageRegion(analysis.contentBounds, PdfRegionRole.FULL_WIDTH),
                        )
                        PdfLayoutMode.REFLOW -> analysis.readingRegions
                    }
                    val regionImages = chosenRegions.map { region ->
                        val pixelRect = region.bounds.toPixelRect(bitmap.width, bitmap.height)
                        val cropped = Bitmap.createBitmap(
                            bitmap,
                            pixelRect.left,
                            pixelRect.top,
                            pixelRect.width,
                            pixelRect.height,
                        )
                        SmartPdfRegionImage(cropped.asImageBitmap(), region.role)
                    }
                    SmartPdfPageRender(
                        regions = regionImages,
                        analysis = analysis.copy(readingRegions = chosenRegions),
                    )
                }
            }
        }
    }
}.getOrElse { error ->
    SmartPdfPageRender(error = error.message ?: "Не удалось проанализировать страницу")
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

/**
 * OCR-free page geometry analysis.
 *
 * The page is downsampled to a small luminance mask, which keeps this cheap enough
 * for on-device use. Content bounds are found from horizontal/vertical ink
 * projections. A two-column page is accepted only when a central low-ink gutter
 * persists for a large vertical part of the content; otherwise the result safely
 * falls back to a single Smart Crop region.
 */
private fun analyzePdfPageBitmap(source: Bitmap): PdfPageLayoutAnalysis {
    val analysisWidth = source.width.coerceAtMost(320).coerceAtLeast(1)
    val analysisHeight = (
        source.height.toFloat() * analysisWidth / source.width.coerceAtLeast(1)
        ).roundToInt().coerceAtLeast(1)
    val sample = if (source.width == analysisWidth && source.height == analysisHeight) {
        source
    } else {
        Bitmap.createScaledBitmap(source, analysisWidth, analysisHeight, true)
    }

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
    val stride = max(1, minOf(width, height) / 80)
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
    val darkBackground = background < 128
    val lightThreshold = (background - 30).coerceIn(145, 238)
    val darkThreshold = (background + 30).coerceIn(24, 205)

    val foreground = BooleanArray(width * height)
    val rowInk = IntArray(height)
    val colInk = IntArray(width)
    for (y in 0 until height) {
        val rowBase = y * width
        for (x in 0 until width) {
            val lum = luminance(pixels[rowBase + x])
            val isInk = if (darkBackground) lum > darkThreshold else lum < lightThreshold
            if (isInk) {
                foreground[rowBase + x] = true
                rowInk[y] += 1
                colInk[x] += 1
            }
        }
    }

    val minRowInk = max(2, (width * 0.004f).roundToInt())
    val minColInk = max(2, (height * 0.003f).roundToInt())
    var contentTop = rowInk.indexOfFirst { it >= minRowInk }
    var contentBottom = rowInk.indexOfLast { it >= minRowInk }
    var contentLeft = colInk.indexOfFirst { it >= minColInk }
    var contentRight = colInk.indexOfLast { it >= minColInk }

    if (contentTop < 0 || contentBottom < contentTop || contentLeft < 0 || contentRight < contentLeft) {
        if (sample !== source) sample.recycle()
        val full = NormalizedPdfRect(0f, 0f, 1f, 1f)
        return PdfPageLayoutAnalysis(
            contentBounds = full,
            readingRegions = listOf(PdfPageRegion(full, PdfRegionRole.FULL_WIDTH)),
            columnCount = 1,
            confidence = 0.35f,
        )
    }

    val rawContentWidth = (contentRight - contentLeft + 1).coerceAtLeast(1)
    val rawContentHeight = (contentBottom - contentTop + 1).coerceAtLeast(1)
    val padX = max(2, (rawContentWidth * 0.018f).roundToInt())
    val padY = max(2, (rawContentHeight * 0.012f).roundToInt())
    contentLeft = (contentLeft - padX).coerceAtLeast(0)
    contentRight = (contentRight + padX).coerceAtMost(width - 1)
    contentTop = (contentTop - padY).coerceAtLeast(0)
    contentBottom = (contentBottom + padY).coerceAtMost(height - 1)

    val contentWidth = (contentRight - contentLeft + 1).coerceAtLeast(1)
    val contentHeight = (contentBottom - contentTop + 1).coerceAtLeast(1)
    val contentRect = NormalizedPdfRect(
        left = contentLeft.toFloat() / width,
        top = contentTop.toFloat() / height,
        right = (contentRight + 1).toFloat() / width,
        bottom = (contentBottom + 1).toFloat() / height,
    )

    val scanTop = contentTop + (contentHeight * 0.10f).roundToInt()
    val scanBottom = contentBottom - (contentHeight * 0.06f).roundToInt()
    val centerSearchLeft = contentLeft + (contentWidth * 0.27f).roundToInt()
    val centerSearchRight = contentLeft + (contentWidth * 0.73f).roundToInt()
    val scanHeight = (scanBottom - scanTop + 1).coerceAtLeast(1)

    val lowInkColumn = BooleanArray(width)
    for (x in centerSearchLeft.coerceAtLeast(0)..centerSearchRight.coerceAtMost(width - 1)) {
        var ink = 0
        for (y in scanTop.coerceAtLeast(0)..scanBottom.coerceAtMost(height - 1)) {
            if (foreground[y * width + x]) ink += 1
        }
        lowInkColumn[x] = ink.toFloat() / scanHeight < 0.025f
    }

    data class Run(val start: Int, val end: Int) {
        val length: Int get() = end - start + 1
        val center: Float get() = (start + end) / 2f
    }

    val gutterRuns = mutableListOf<Run>()
    var runStart = -1
    for (x in centerSearchLeft..centerSearchRight + 1) {
        val isBlank = x <= centerSearchRight && x in lowInkColumn.indices && lowInkColumn[x]
        if (isBlank && runStart < 0) runStart = x
        if (!isBlank && runStart >= 0) {
            gutterRuns += Run(runStart, x - 1)
            runStart = -1
        }
    }

    val minGutterWidth = max(3, (contentWidth * 0.022f).roundToInt())
    val pageCenter = (contentLeft + contentRight) / 2f
    val gutter = gutterRuns
        .filter { it.length >= minGutterWidth }
        .maxByOrNull { run ->
            val widthScore = run.length.toFloat() / contentWidth
            val centerPenalty = kotlin.math.abs(run.center - pageCenter) / contentWidth
            widthScore * 2.4f - centerPenalty
        }

    if (gutter == null) {
        if (sample !== source) sample.recycle()
        return PdfPageLayoutAnalysis(
            contentBounds = contentRect,
            readingRegions = listOf(PdfPageRegion(contentRect, PdfRegionRole.FULL_WIDTH)),
            columnCount = 1,
            confidence = 0.78f,
        )
    }

    val leftWidth = gutter.start - contentLeft
    val rightWidth = contentRight - gutter.end
    if (leftWidth < contentWidth * 0.26f || rightWidth < contentWidth * 0.26f) {
        if (sample !== source) sample.recycle()
        return PdfPageLayoutAnalysis(
            contentBounds = contentRect,
            readingRegions = listOf(PdfPageRegion(contentRect, PdfRegionRole.FULL_WIDTH)),
            columnCount = 1,
            confidence = 0.74f,
        )
    }

    val gutterWidth = gutter.length.coerceAtLeast(1)
    val blankThroughGutter = BooleanArray(height)
    for (y in contentTop..contentBottom) {
        var ink = 0
        for (x in gutter.start..gutter.end) {
            if (foreground[y * width + x]) ink += 1
        }
        blankThroughGutter[y] = ink <= max(1, (gutterWidth * 0.03f).roundToInt())
    }

    var bestStart = contentTop
    var bestEnd = contentTop - 1
    var currentStart = -1
    for (y in contentTop..contentBottom + 1) {
        val blank = y <= contentBottom && y in blankThroughGutter.indices && blankThroughGutter[y]
        if (blank && currentStart < 0) currentStart = y
        if (!blank && currentStart >= 0) {
            if (y - currentStart > bestEnd - bestStart + 1) {
                bestStart = currentStart
                bestEnd = y - 1
            }
            currentStart = -1
        }
    }

    val bodyHeight = (bestEnd - bestStart + 1).coerceAtLeast(0)
    val bodyRatio = bodyHeight.toFloat() / contentHeight
    if (bodyRatio < 0.44f) {
        if (sample !== source) sample.recycle()
        return PdfPageLayoutAnalysis(
            contentBounds = contentRect,
            readingRegions = listOf(PdfPageRegion(contentRect, PdfRegionRole.FULL_WIDTH)),
            columnCount = 1,
            confidence = 0.70f,
        )
    }

    fun normalizedRect(left: Int, top: Int, rightInclusive: Int, bottomInclusive: Int) =
        NormalizedPdfRect(
            left = left.coerceIn(0, width - 1).toFloat() / width,
            top = top.coerceIn(0, height - 1).toFloat() / height,
            right = (rightInclusive + 1).coerceIn(1, width).toFloat() / width,
            bottom = (bottomInclusive + 1).coerceIn(1, height).toFloat() / height,
        )

    val regions = mutableListOf<PdfPageRegion>()
    val minBandHeight = max(6, (contentHeight * 0.035f).roundToInt())
    if (bestStart - contentTop >= minBandHeight) {
        regions += PdfPageRegion(
            normalizedRect(contentLeft, contentTop, contentRight, bestStart - 1),
            PdfRegionRole.FULL_WIDTH,
        )
    }
    regions += PdfPageRegion(
        normalizedRect(contentLeft, bestStart, gutter.start - 1, bestEnd),
        PdfRegionRole.LEFT_COLUMN,
    )
    regions += PdfPageRegion(
        normalizedRect(gutter.end + 1, bestStart, contentRight, bestEnd),
        PdfRegionRole.RIGHT_COLUMN,
    )
    if (contentBottom - bestEnd >= minBandHeight) {
        regions += PdfPageRegion(
            normalizedRect(contentLeft, bestEnd + 1, contentRight, contentBottom),
            PdfRegionRole.FULL_WIDTH,
        )
    }

    val centerOffset = kotlin.math.abs(gutter.center - pageCenter) / contentWidth
    val gutterRatio = gutter.length.toFloat() / contentWidth
    val confidence = (
        0.58f + bodyRatio * 0.28f + gutterRatio * 1.6f - centerOffset * 0.25f
        ).coerceIn(0.55f, 0.97f)

    if (sample !== source) sample.recycle()
    return PdfPageLayoutAnalysis(
        contentBounds = contentRect,
        readingRegions = regions,
        columnCount = 2,
        confidence = confidence,
    )
}
