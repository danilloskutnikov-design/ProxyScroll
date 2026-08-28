package com.proxyscroll.app.ui

import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.proxyscroll.app.domain.LibraryDocument
import com.proxyscroll.app.ui.theme.ProxySurface
import com.proxyscroll.app.ui.theme.ProxySurfaceRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt

private data class PdfSourcePageInfo(
    val index: Int,
    val width: Int,
    val height: Int,
) {
    val aspectRatio: Float
        get() = width.toFloat() / height.coerceAtLeast(1)
}

private data class ModernPdfDocumentInfo(
    val sourcePages: List<PdfSourcePageInfo> = emptyList(),
    val portraitDominant: Boolean = false,
    val error: String? = null,
) {
    val pageCount: Int get() = sourcePages.size
}

private data class ReaderVirtualPage(
    val sourcePage: Int,
    val slice: PdfPageSlice,
    val expectedAspectRatio: Float,
)

internal enum class PdfReadingProfile(val label: String) {
    ORIGINAL("Оригинал"),
    SEPIA("Сепия"),
    NIGHT("Ночь"),
    WARM("Тёплый"),
    CONTRAST("Контраст"),
}

private enum class PdfNavigationMode(val label: String) {
    CONTINUOUS("Прокрутка"),
    PAGED("Листание"),
}

private data class ReaderSavedSettings(
    val layoutMode: PdfLayoutMode,
    val navigationMode: PdfNavigationMode,
    val readingProfile: PdfReadingProfile,
    val pageWidth: Float,
    val brightness: Float,
)

private class ReaderSettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("proxyscroll_pdf_reader", Context.MODE_PRIVATE)

    fun load(documentId: String): ReaderSavedSettings {
        val prefix = "doc_${documentId}_"
        val layout = enumValueOrDefault(
            prefs.getString(prefix + "layout", prefs.getString("last_layout", null)),
            PdfLayoutMode.SMART_CROP,
        )
        val navigation = enumValueOrDefault(
            prefs.getString(prefix + "navigation", prefs.getString("last_navigation", null)),
            PdfNavigationMode.CONTINUOUS,
        )
        val profile = enumValueOrDefault(
            prefs.getString(prefix + "profile", prefs.getString("last_profile", null)),
            PdfReadingProfile.ORIGINAL,
        )
        val pageWidth = prefs.getFloat(
            prefix + "page_width",
            prefs.getFloat("last_page_width", 0.985f),
        ).coerceIn(0.84f, 1f)
        val brightness = prefs.getFloat(
            prefix + "brightness",
            prefs.getFloat("last_brightness", 1f),
        ).coerceIn(0.25f, 1f)
        return ReaderSavedSettings(
            layoutMode = if (layout == PdfLayoutMode.ORIGINAL) {
                PdfLayoutMode.ORIGINAL
            } else {
                PdfLayoutMode.SMART_CROP
            },
            navigationMode = navigation,
            readingProfile = profile,
            pageWidth = pageWidth,
            brightness = brightness,
        )
    }

    fun save(documentId: String, settings: ReaderSavedSettings) {
        val prefix = "doc_${documentId}_"
        prefs.edit()
            .putString(prefix + "layout", settings.layoutMode.name)
            .putString(prefix + "navigation", settings.navigationMode.name)
            .putString(prefix + "profile", settings.readingProfile.name)
            .putFloat(prefix + "page_width", settings.pageWidth)
            .putFloat(prefix + "brightness", settings.brightness)
            .putString("last_layout", settings.layoutMode.name)
            .putString("last_navigation", settings.navigationMode.name)
            .putString("last_profile", settings.readingProfile.name)
            .putFloat("last_page_width", settings.pageWidth)
            .putFloat("last_brightness", settings.brightness)
            .apply()
    }

    fun loadLastVirtualPage(documentId: String, layoutMode: PdfLayoutMode): Int? {
        val key = "doc_${documentId}_last_${layoutMode.name.lowercase()}"
        val value = prefs.getInt(key, -1)
        return value.takeIf { it >= 0 }
    }

    fun saveLastVirtualPage(documentId: String, layoutMode: PdfLayoutMode, page: Int) {
        prefs.edit()
            .putInt("doc_${documentId}_last_${layoutMode.name.lowercase()}", page.coerceAtLeast(0))
            .apply()
    }

    fun loadBookmarks(documentId: String): Set<Int> =
        prefs.getStringSet("doc_${documentId}_bookmarks", emptySet())
            .orEmpty()
            .mapNotNull { it.toIntOrNull() }
            .filter { it >= 0 }
            .toSet()

    fun saveBookmarks(documentId: String, bookmarks: Set<Int>) {
        prefs.edit()
            .putStringSet(
                "doc_${documentId}_bookmarks",
                bookmarks.mapTo(mutableSetOf()) { it.toString() },
            )
            .apply()
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, fallback: T): T {
        if (value.isNullOrBlank()) return fallback
        return runCatching { enumValueOf<T>(value) }.getOrDefault(fallback)
    }
}

@Composable
internal fun ModernPdfReaderScreen(
    document: LibraryDocument,
    quoteCount: Int,
    onBack: () -> Unit,
    onProgressChanged: (Int, Int) -> Unit,
    onSaveQuote: (Int, String, String) -> Unit,
    onScrollQuietChanged: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val documentInfo by produceState(
        initialValue = ModernPdfDocumentInfo(),
        key1 = document.uri,
    ) {
        value = withContext(Dispatchers.IO) {
            readModernPdfDocumentInfo(context, Uri.parse(document.uri))
        }
    }
    val latestScrollQuietChanged by rememberUpdatedState(onScrollQuietChanged)

    DisposableEffect(Unit) {
        onDispose { latestScrollQuietChanged(false) }
    }
    BackHandler(onBack = onBack)

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            documentInfo.error != null -> PdfReaderError(
                message = documentInfo.error.orEmpty(),
                onBack = onBack,
            )
            documentInfo.pageCount <= 0 -> CircularProgressIndicator()
            else -> ModernPdfReaderReady(
                document = document,
                documentInfo = documentInfo,
                quoteCount = quoteCount,
                onBack = onBack,
                onProgressChanged = onProgressChanged,
                onSaveQuote = onSaveQuote,
                onScrollQuietChanged = onScrollQuietChanged,
            )
        }
    }
}

@Composable
private fun PdfReaderError(
    message: String,
    onBack: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp),
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "В библиотеку")
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.height(12.dp))
            Text("Не удалось открыть PDF", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ModernPdfReaderReady(
    document: LibraryDocument,
    documentInfo: ModernPdfDocumentInfo,
    quoteCount: Int,
    onBack: () -> Unit,
    onProgressChanged: (Int, Int) -> Unit,
    onSaveQuote: (Int, String, String) -> Unit,
    onScrollQuietChanged: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uri = remember(document.uri) { Uri.parse(document.uri) }
    val renderCache = remember(document.uri) { SmartPdfReflowCache(maxEntries = 14) }
    val settingsStore = remember(context) { ReaderSettingsStore(context.applicationContext) }
    val saved = remember(document.id) { settingsStore.load(document.id) }
    val initialReaderPages = remember(documentInfo, saved.layoutMode) {
        buildReaderPages(documentInfo, saved.layoutMode)
    }
    val initialPage = remember(document.id, initialReaderPages, saved.layoutMode) {
        val storedVirtual = settingsStore.loadLastVirtualPage(document.id, saved.layoutMode)
        when {
            storedVirtual != null && storedVirtual in initialReaderPages.indices -> storedVirtual
            else -> initialReaderPages.indexOfFirst { it.sourcePage == document.lastPage }
                .takeIf { it >= 0 } ?: 0
        }
    }

    var controlsVisible by remember(document.id) { mutableStateOf(true) }
    var menuExpanded by remember { mutableStateOf(false) }
    var pageJumpVisible by remember { mutableStateOf(false) }
    var bookmarksVisible by remember { mutableStateOf(false) }
    var readingProfile by remember(document.id) { mutableStateOf(saved.readingProfile) }
    var layoutMode by remember(document.id) { mutableStateOf(saved.layoutMode) }
    var navigationMode by remember(document.id) { mutableStateOf(saved.navigationMode) }
    var pageWidth by remember(document.id) { mutableFloatStateOf(saved.pageWidth) }
    var brightness by remember(document.id) { mutableFloatStateOf(saved.brightness) }
    var bookmarks by remember(document.id) { mutableStateOf(settingsStore.loadBookmarks(document.id)) }
    var pendingSourceAfterLayout by remember(document.id) { mutableStateOf<Int?>(null) }

    val readerPages = remember(documentInfo, layoutMode) {
        buildReaderPages(documentInfo, layoutMode)
    }
    val pageCount = readerPages.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(initialPage = initialPage.coerceIn(0, pageCount - 1)) {
        readerPages.size.coerceAtLeast(1)
    }
    val continuousState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialPage.coerceIn(0, pageCount - 1),
    )

    var currentPage by remember(document.id) {
        mutableIntStateOf(initialPage.coerceIn(0, pageCount - 1))
    }
    var scrubPage by remember(document.id) {
        mutableFloatStateOf(initialPage.coerceIn(0, pageCount - 1).toFloat())
    }
    var isScrubbing by remember { mutableStateOf(false) }
    var activeAtmosphere by remember(document.id) { mutableStateOf<PdfPageAtmosphere?>(null) }
    var resetZoomToken by remember(document.id) { mutableIntStateOf(0) }
    var currentScale by remember(document.id) { mutableFloatStateOf(1f) }
    var navigationInteractionActive by remember { mutableStateOf(false) }
    var pageInteractionActive by remember { mutableStateOf(false) }
    var quoteDialogSourcePage by remember(document.id) { mutableStateOf<Int?>(null) }
    val latestScrollQuietChanged by rememberUpdatedState(onScrollQuietChanged)

    val currentVirtualPage = readerPages.getOrNull(currentPage)
        ?: readerPages.firstOrNull()
        ?: ReaderVirtualPage(0, PdfPageSlice.FULL, 0.72f)
    val currentSourcePage = currentVirtualPage.sourcePage
    val currentBookmarked = currentSourcePage in bookmarks

    fun resetZoom() {
        resetZoomToken += 1
        currentScale = 1f
    }

    fun goToPage(page: Int, animate: Boolean = true) {
        val target = page.coerceIn(0, pageCount - 1)
        resetZoom()
        scope.launch {
            when (navigationMode) {
                PdfNavigationMode.PAGED -> {
                    if (animate) pagerState.animateScrollToPage(target) else pagerState.scrollToPage(target)
                }
                PdfNavigationMode.CONTINUOUS -> {
                    if (animate) continuousState.animateScrollToItem(target) else continuousState.scrollToItem(target)
                }
            }
        }
    }

    fun goToSourcePage(sourcePage: Int) {
        val target = readerPages.indexOfFirst { it.sourcePage == sourcePage }
            .takeIf { it >= 0 } ?: return
        goToPage(target)
    }

    fun setLayoutMode(next: PdfLayoutMode) {
        if (next == layoutMode) return
        pendingSourceAfterLayout = currentSourcePage
        layoutMode = next
    }

    fun toggleCurrentBookmark() {
        val next = bookmarks.toMutableSet()
        if (!next.add(currentSourcePage)) next.remove(currentSourcePage)
        bookmarks = next
        settingsStore.saveBookmarks(document.id, next)
    }

    LaunchedEffect(layoutMode, navigationMode, readingProfile, pageWidth, brightness, document.id) {
        settingsStore.save(
            document.id,
            ReaderSavedSettings(
                layoutMode = layoutMode,
                navigationMode = navigationMode,
                readingProfile = readingProfile,
                pageWidth = pageWidth,
                brightness = brightness,
            ),
        )
    }

    LaunchedEffect(layoutMode, readerPages.size) {
        if (readerPages.isEmpty()) return@LaunchedEffect
        val pendingSource = pendingSourceAfterLayout
        val stored = settingsStore.loadLastVirtualPage(document.id, layoutMode)
        val target = when {
            pendingSource != null -> readerPages.indexOfFirst { it.sourcePage == pendingSource }
                .takeIf { it >= 0 } ?: 0
            stored != null && stored in readerPages.indices -> stored
            else -> readerPages.indexOfFirst { it.sourcePage == document.lastPage }
                .takeIf { it >= 0 } ?: 0
        }
        pendingSourceAfterLayout = null
        currentPage = target
        scrubPage = target.toFloat()
        resetZoom()
        pagerState.scrollToPage(target)
        continuousState.scrollToItem(target)
        activeAtmosphere = renderCache.get(
            readerPages[target].sourcePage,
            layoutMode,
            readerPages[target].slice,
        )?.atmosphere
    }

    LaunchedEffect(navigationMode) {
        resetZoom()
        val safe = currentPage.coerceIn(0, pageCount - 1)
        when (navigationMode) {
            PdfNavigationMode.PAGED -> pagerState.scrollToPage(safe)
            PdfNavigationMode.CONTINUOUS -> continuousState.scrollToItem(safe)
        }
    }

    LaunchedEffect(controlsVisible, menuExpanded, quoteDialogSourcePage, pageJumpVisible, bookmarksVisible, isScrubbing) {
        if (
            controlsVisible &&
            !menuExpanded &&
            quoteDialogSourcePage == null &&
            !pageJumpVisible &&
            !bookmarksVisible &&
            !isScrubbing
        ) {
            kotlinx.coroutines.delay(3800)
            controlsVisible = false
        }
    }

    LaunchedEffect(navigationMode, pagerState) {
        if (navigationMode != PdfNavigationMode.PAGED) return@LaunchedEffect
        snapshotFlow { pagerState.isScrollInProgress }
            .distinctUntilChanged()
            .collectLatest { navigationInteractionActive = it }
    }

    LaunchedEffect(navigationMode, continuousState) {
        if (navigationMode != PdfNavigationMode.CONTINUOUS) return@LaunchedEffect
        snapshotFlow { continuousState.isScrollInProgress }
            .distinctUntilChanged()
            .collectLatest { navigationInteractionActive = it }
    }

    LaunchedEffect(navigationInteractionActive, pageInteractionActive) {
        latestScrollQuietChanged(navigationInteractionActive || pageInteractionActive)
    }

    LaunchedEffect(navigationMode, pagerState, pageCount) {
        if (navigationMode != PdfNavigationMode.PAGED) return@LaunchedEffect
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collectLatest { page -> currentPage = page.coerceIn(0, pageCount - 1) }
    }

    LaunchedEffect(navigationMode, continuousState, pageCount) {
        if (navigationMode != PdfNavigationMode.CONTINUOUS) return@LaunchedEffect
        snapshotFlow {
            val info = continuousState.layoutInfo
            val center = (info.viewportStartOffset + info.viewportEndOffset) / 2
            info.visibleItemsInfo.minByOrNull { item ->
                abs((item.offset + item.size / 2) - center)
            }?.index ?: currentPage
        }
            .distinctUntilChanged()
            .collectLatest { page -> currentPage = page.coerceIn(0, pageCount - 1) }
    }

    LaunchedEffect(currentPage, pageCount, layoutMode, readerPages) {
        if (readerPages.isEmpty()) return@LaunchedEffect
        val safe = currentPage.coerceIn(0, readerPages.lastIndex)
        val virtual = readerPages[safe]
        if (!isScrubbing) scrubPage = safe.toFloat()
        settingsStore.saveLastVirtualPage(document.id, layoutMode, safe)
        onProgressChanged(virtual.sourcePage, documentInfo.pageCount)
        renderCache.get(virtual.sourcePage, layoutMode, virtual.slice)?.atmosphere?.let {
            activeAtmosphere = it
        }
    }

    LaunchedEffect(currentPage, layoutMode, document.uri, readerPages) {
        if (readerPages.isEmpty()) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            (currentPage - 3..currentPage + 3)
                .filter { it in readerPages.indices }
                .forEach { virtualIndex ->
                    val virtual = readerPages[virtualIndex]
                    renderSmartPdfPage(
                        context = context,
                        uri = uri,
                        requestedPage = virtual.sourcePage,
                        mode = layoutMode,
                        cache = renderCache,
                        slice = virtual.slice,
                    )
                }
        }
        val safe = currentPage.coerceIn(0, readerPages.lastIndex)
        val virtual = readerPages[safe]
        renderCache.get(virtual.sourcePage, layoutMode, virtual.slice)?.atmosphere?.let {
            activeAtmosphere = it
        }
    }

    DisposableEffect(renderCache) {
        onDispose {
            renderCache.clear()
            latestScrollQuietChanged(false)
        }
    }

    quoteDialogSourcePage?.let { sourcePage ->
        PdfQuoteDialog(
            documentTitle = document.title,
            page = sourcePage,
            onDismiss = { quoteDialogSourcePage = null },
            onSave = { excerpt, note ->
                onSaveQuote(sourcePage, excerpt, note)
                quoteDialogSourcePage = null
            },
        )
    }

    if (pageJumpVisible) {
        PageJumpDialog(
            currentPage = currentPage,
            pageCount = pageCount,
            onDismiss = { pageJumpVisible = false },
            onGo = { page ->
                pageJumpVisible = false
                controlsVisible = true
                goToPage(page)
            },
        )
    }

    if (bookmarksVisible) {
        BookmarksDialog(
            bookmarks = bookmarks,
            onDismiss = { bookmarksVisible = false },
            onOpenPage = { sourcePage ->
                bookmarksVisible = false
                controlsVisible = true
                goToSourcePage(sourcePage)
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds(),
    ) {
        PdfAtmosphereBackdrop(
            atmosphere = activeAtmosphere,
            readingProfile = readingProfile,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 4.dp, vertical = 3.dp),
        ) {
            when (navigationMode) {
                PdfNavigationMode.PAGED -> HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = layoutMode != PdfLayoutMode.ORIGINAL || currentScale <= 1.01f,
                    beyondViewportPageCount = 1,
                    pageSpacing = 8.dp,
                ) { virtualIndex ->
                    val virtual = readerPages.getOrNull(virtualIndex) ?: return@HorizontalPager
                    PagedPdfPage(
                        documentUri = document.uri,
                        virtualPage = virtual,
                        virtualIndex = virtualIndex,
                        mode = layoutMode,
                        readingProfile = readingProfile,
                        pageWidth = pageWidth,
                        cache = renderCache,
                        isCurrent = virtualIndex == currentPage,
                        resetZoomToken = resetZoomToken,
                        onScaleChanged = { scale ->
                            if (virtualIndex == currentPage) currentScale = scale
                        },
                        onInteractionChanged = { active ->
                            if (virtualIndex == currentPage) pageInteractionActive = active
                        },
                        onAtmosphere = { atmosphere ->
                            if (virtualIndex == currentPage) activeAtmosphere = atmosphere
                        },
                        onTap = { xFraction ->
                            when {
                                xFraction < 0.16f && currentPage > 0 -> goToPage(currentPage - 1)
                                xFraction > 0.84f && currentPage < pageCount - 1 -> goToPage(currentPage + 1)
                                else -> controlsVisible = !controlsVisible
                            }
                        },
                    )
                }

                PdfNavigationMode.CONTINUOUS -> LazyColumn(
                    state = continuousState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 76.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    itemsIndexed(
                        items = readerPages,
                        key = { index, page -> "$index:${page.sourcePage}:${page.slice.name}" },
                    ) { virtualIndex, virtual ->
                        ContinuousPdfPage(
                            documentUri = document.uri,
                            virtualPage = virtual,
                            virtualIndex = virtualIndex,
                            mode = layoutMode,
                            readingProfile = readingProfile,
                            pageWidth = pageWidth,
                            cache = renderCache,
                            isCurrent = virtualIndex == currentPage,
                            onAtmosphere = { atmosphere ->
                                if (virtualIndex == currentPage) activeAtmosphere = atmosphere
                            },
                            onCenterTap = { controlsVisible = !controlsVisible },
                        )
                    }
                }
            }
        }

        if (brightness < 0.995f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = ((1f - brightness) * 0.72f).coerceIn(0f, 0.56f))),
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(5f),
            enter = fadeIn(tween(160)) + slideInVertically(tween(220)) { -it },
            exit = fadeOut(tween(130)) + slideOutVertically(tween(190)) { -it },
        ) {
            ReaderTopBar(
                title = document.title,
                quoteCount = quoteCount,
                bookmarked = currentBookmarked,
                menuExpanded = menuExpanded,
                layoutMode = layoutMode,
                navigationMode = navigationMode,
                readingProfile = readingProfile,
                pageWidth = pageWidth,
                brightness = brightness,
                bookmarkCount = bookmarks.size,
                onBack = onBack,
                onQuote = { quoteDialogSourcePage = currentSourcePage },
                onToggleBookmark = ::toggleCurrentBookmark,
                onOpenBookmarks = {
                    menuExpanded = false
                    bookmarksVisible = true
                },
                onMenuExpanded = { menuExpanded = it },
                onLayoutMode = ::setLayoutMode,
                onNavigationMode = { navigationMode = it },
                onReadingProfile = { readingProfile = it },
                onPageWidth = { pageWidth = it.coerceIn(0.84f, 1f) },
                onBrightness = { brightness = it.coerceIn(0.25f, 1f) },
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(6f),
            enter = fadeIn(tween(160)) + slideInVertically(tween(230)) { it },
            exit = fadeOut(tween(130)) + slideOutVertically(tween(190)) { it },
        ) {
            ReaderNavigationDock(
                currentPage = currentPage,
                pageCount = pageCount,
                scrubPage = scrubPage,
                isScrubbing = isScrubbing,
                onScrub = { value ->
                    isScrubbing = true
                    scrubPage = value.coerceIn(0f, (pageCount - 1).coerceAtLeast(0).toFloat())
                },
                onScrubFinished = {
                    isScrubbing = false
                    goToPage(scrubPage.roundToInt())
                },
                onPrevious = { if (currentPage > 0) goToPage(currentPage - 1) },
                onNext = { if (currentPage < pageCount - 1) goToPage(currentPage + 1) },
                onPageJump = { pageJumpVisible = true },
            )
        }

        AnimatedVisibility(
            visible = !controlsVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(6f),
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(110)),
        ) {
            ReaderPassivePagePill(
                currentPage = currentPage,
                pageCount = pageCount,
                onClick = {
                    controlsVisible = true
                    pageJumpVisible = true
                },
            )
        }
    }
}

@Composable
private fun ReaderTopBar(
    title: String,
    quoteCount: Int,
    bookmarked: Boolean,
    menuExpanded: Boolean,
    layoutMode: PdfLayoutMode,
    navigationMode: PdfNavigationMode,
    readingProfile: PdfReadingProfile,
    pageWidth: Float,
    brightness: Float,
    bookmarkCount: Int,
    onBack: () -> Unit,
    onQuote: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onMenuExpanded: (Boolean) -> Unit,
    onLayoutMode: (PdfLayoutMode) -> Unit,
    onNavigationMode: (PdfNavigationMode) -> Unit,
    onReadingProfile: (PdfReadingProfile) -> Unit,
    onPageWidth: (Float) -> Unit,
    onBrightness: (Float) -> Unit,
) {
    ProxySurface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        role = ProxySurfaceRole.OVERLAY,
        strong = true,
        interactive = false,
        deformContent = false,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "В библиотеку")
            }
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${layoutMode.label} · ${navigationMode.label}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onToggleBookmark) {
                Icon(
                    imageVector = if (bookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = if (bookmarked) "Убрать закладку" else "Добавить закладку",
                    tint = if (bookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = onQuote) {
                Icon(
                    Icons.Default.FormatQuote,
                    contentDescription = if (quoteCount > 0) {
                        "Сохранить цитату или заметку, уже $quoteCount"
                    } else {
                        "Сохранить цитату или заметку"
                    },
                )
            }
            Box {
                IconButton(onClick = { onMenuExpanded(true) }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Настройки чтения")
                }
                ReaderSettingsMenu(
                    expanded = menuExpanded,
                    layoutMode = layoutMode,
                    navigationMode = navigationMode,
                    readingProfile = readingProfile,
                    pageWidth = pageWidth,
                    brightness = brightness,
                    bookmarkCount = bookmarkCount,
                    onDismiss = { onMenuExpanded(false) },
                    onLayoutMode = onLayoutMode,
                    onNavigationMode = onNavigationMode,
                    onReadingProfile = onReadingProfile,
                    onPageWidth = onPageWidth,
                    onBrightness = onBrightness,
                    onOpenBookmarks = onOpenBookmarks,
                )
            }
        }
    }
}

@Composable
private fun ReaderNavigationDock(
    currentPage: Int,
    pageCount: Int,
    scrubPage: Float,
    isScrubbing: Boolean,
    onScrub: (Float) -> Unit,
    onScrubFinished: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPageJump: () -> Unit,
) {
    val previewPage = scrubPage.roundToInt().coerceIn(0, (pageCount - 1).coerceAtLeast(0))
    ProxySurface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        role = ProxySurfaceRole.OVERLAY,
        strong = true,
        interactive = false,
        deformContent = false,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(enabled = currentPage > 0, onClick = onPrevious) {
                    Icon(Icons.Default.NavigateBefore, contentDescription = "Предыдущая страница")
                }
                TextButton(onClick = onPageJump) {
                    Text(
                        if (isScrubbing) {
                            "${previewPage + 1} / $pageCount"
                        } else {
                            "${currentPage + 1} / $pageCount"
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                IconButton(enabled = currentPage < pageCount - 1, onClick = onNext) {
                    Icon(Icons.Default.NavigateNext, contentDescription = "Следующая страница")
                }
            }
            Slider(
                value = scrubPage.coerceIn(0f, (pageCount - 1).coerceAtLeast(0).toFloat()),
                onValueChange = onScrub,
                onValueChangeFinished = onScrubFinished,
                valueRange = 0f..(pageCount - 1).coerceAtLeast(1).toFloat(),
                steps = 0,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "1",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    if (isScrubbing) "Страница ${previewPage + 1}" else "Нажмите номер для перехода",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    pageCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ReaderPassivePagePill(
    currentPage: Int,
    pageCount: Int,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .navigationBarsPadding()
            .padding(bottom = 8.dp),
    ) {
        ProxySurface(
            shape = RoundedCornerShape(18.dp),
            role = ProxySurfaceRole.OVERLAY,
            strong = true,
            interactive = false,
            deformContent = false,
        ) {
            Text(
                "${currentPage + 1} / $pageCount",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun ReaderSettingsMenu(
    expanded: Boolean,
    layoutMode: PdfLayoutMode,
    navigationMode: PdfNavigationMode,
    readingProfile: PdfReadingProfile,
    pageWidth: Float,
    brightness: Float,
    bookmarkCount: Int,
    onDismiss: () -> Unit,
    onLayoutMode: (PdfLayoutMode) -> Unit,
    onNavigationMode: (PdfNavigationMode) -> Unit,
    onReadingProfile: (PdfReadingProfile) -> Unit,
    onPageWidth: (Float) -> Unit,
    onBrightness: (Float) -> Unit,
    onOpenBookmarks: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.width(286.dp),
    ) {
        Text(
            "Страница",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        PdfLayoutMode.entries.forEach { mode ->
            DropdownMenuItem(
                text = { Text(mode.label) },
                leadingIcon = if (mode == layoutMode) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null,
                onClick = { onLayoutMode(mode) },
            )
        }
        HorizontalDivider()
        Text(
            "Навигация",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        PdfNavigationMode.entries.forEach { mode ->
            DropdownMenuItem(
                text = { Text(mode.label) },
                leadingIcon = if (mode == navigationMode) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null,
                onClick = { onNavigationMode(mode) },
            )
        }
        HorizontalDivider()
        Text(
            "Экран",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Column(Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
            Text(
                "Ширина страницы · ${(pageWidth * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Slider(
                value = pageWidth,
                onValueChange = onPageWidth,
                valueRange = 0.84f..1f,
                steps = 0,
            )
            Text(
                "Яркость · ${(brightness * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Slider(
                value = brightness,
                onValueChange = onBrightness,
                valueRange = 0.25f..1f,
                steps = 0,
            )
        }
        HorizontalDivider()
        Text(
            "Фильтр",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        PdfReadingProfile.entries.forEach { profile ->
            DropdownMenuItem(
                text = { Text(profile.label) },
                leadingIcon = if (profile == readingProfile) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null,
                onClick = { onReadingProfile(profile) },
            )
        }
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("Закладки · $bookmarkCount") },
            leadingIcon = { Icon(Icons.Default.Bookmarks, contentDescription = null) },
            onClick = onOpenBookmarks,
        )
    }
}

@Composable
private fun PageJumpDialog(
    currentPage: Int,
    pageCount: Int,
    onDismiss: () -> Unit,
    onGo: (Int) -> Unit,
) {
    var value by remember { mutableStateOf((currentPage + 1).toString()) }
    val parsed = value.toIntOrNull()
    val valid = parsed != null && parsed in 1..pageCount
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Перейти к странице") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { next -> value = next.filter { it.isDigit() }.take(7) },
                label = { Text("1–$pageCount") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { parsed?.let { onGo(it - 1) } },
            ) { Text("Перейти") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    )
}

@Composable
private fun BookmarksDialog(
    bookmarks: Set<Int>,
    onDismiss: () -> Unit,
    onOpenPage: (Int) -> Unit,
) {
    val sorted = remember(bookmarks) { bookmarks.sorted() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Закладки") },
        text = {
            if (sorted.isEmpty()) {
                Text("Закладок пока нет. Нажмите значок закладки в верхней панели на нужной странице.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(sorted) { sourcePage ->
                        TextButton(
                            onClick = { onOpenPage(sourcePage) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                "PDF · страница ${sourcePage + 1}",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Готово") }
        },
    )
}

@Composable
private fun PagedPdfPage(
    documentUri: String,
    virtualPage: ReaderVirtualPage,
    virtualIndex: Int,
    mode: PdfLayoutMode,
    readingProfile: PdfReadingProfile,
    pageWidth: Float,
    cache: SmartPdfReflowCache,
    isCurrent: Boolean,
    resetZoomToken: Int,
    onScaleChanged: (Float) -> Unit,
    onInteractionChanged: (Boolean) -> Unit,
    onAtmosphere: (PdfPageAtmosphere) -> Unit,
    onTap: (Float) -> Unit,
) {
    val context = LocalContext.current
    var viewportWidth by remember(virtualIndex, mode) { mutableIntStateOf(1) }
    val cached = remember(documentUri, virtualPage.sourcePage, virtualPage.slice, mode) {
        cache.get(virtualPage.sourcePage, mode, virtualPage.slice)
    }
    val render by produceState(
        initialValue = cached ?: SmartPdfPageRender(),
        key1 = documentUri,
        key2 = virtualPage,
        key3 = mode,
    ) {
        if (value.regions.isEmpty() && value.error == null) {
            value = renderSmartPdfPageAsync(
                context = context,
                uri = Uri.parse(documentUri),
                requestedPage = virtualPage.sourcePage,
                mode = mode,
                cache = cache,
                slice = virtualPage.slice,
            )
        }
    }

    LaunchedEffect(isCurrent, render.atmosphere) {
        if (isCurrent) render.atmosphere?.let(onAtmosphere)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { viewportWidth = it.size.width.coerceAtLeast(1) }
            .pointerInput(virtualIndex, mode, viewportWidth) {
                detectTapGestures { position ->
                    onTap((position.x / viewportWidth.coerceAtLeast(1)).coerceIn(0f, 1f))
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        when {
            render.error != null -> PdfPageError(render.error.orEmpty())
            render.regions.isEmpty() -> CircularProgressIndicator()
            mode == PdfLayoutMode.ORIGINAL -> ZoomablePdfRegion(
                region = render.regions.first(),
                readingProfile = readingProfile,
                pageWidth = pageWidth,
                resetZoomToken = resetZoomToken,
                onScaleChanged = onScaleChanged,
                onInteractionChanged = onInteractionChanged,
            )
            else -> SmartPagedRegions(
                virtualIndex = virtualIndex,
                regions = render.regions,
                readingProfile = readingProfile,
                pageWidth = pageWidth,
                onInteractionChanged = if (isCurrent) onInteractionChanged else { _ -> },
            )
        }
    }
}

@Composable
private fun SmartPagedRegions(
    virtualIndex: Int,
    regions: List<SmartPdfRegionImage>,
    readingProfile: PdfReadingProfile,
    pageWidth: Float,
    onInteractionChanged: (Boolean) -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(regions.size) { listState.scrollToItem(0) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collectLatest(onInteractionChanged)
    }
    DisposableEffect(Unit) {
        onDispose { onInteractionChanged(false) }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        itemsIndexed(regions) { index, region ->
            PdfRegionImage(
                region = region,
                readingProfile = readingProfile,
                pageWidth = pageWidth,
                contentDescription = "Страница ${virtualIndex + 1}, фрагмент ${index + 1}",
            )
        }
        item { Spacer(Modifier.height(4.dp)) }
    }
}

@Composable
private fun ContinuousPdfPage(
    documentUri: String,
    virtualPage: ReaderVirtualPage,
    virtualIndex: Int,
    mode: PdfLayoutMode,
    readingProfile: PdfReadingProfile,
    pageWidth: Float,
    cache: SmartPdfReflowCache,
    isCurrent: Boolean,
    onAtmosphere: (PdfPageAtmosphere) -> Unit,
    onCenterTap: () -> Unit,
) {
    val context = LocalContext.current
    val cached = remember(documentUri, virtualPage.sourcePage, virtualPage.slice, mode) {
        cache.get(virtualPage.sourcePage, mode, virtualPage.slice)
    }
    val render by produceState(
        initialValue = cached ?: SmartPdfPageRender(),
        key1 = documentUri,
        key2 = virtualPage,
        key3 = mode,
    ) {
        if (value.regions.isEmpty() && value.error == null) {
            value = renderSmartPdfPageAsync(
                context = context,
                uri = Uri.parse(documentUri),
                requestedPage = virtualPage.sourcePage,
                mode = mode,
                cache = cache,
                slice = virtualPage.slice,
            )
        }
    }

    LaunchedEffect(isCurrent, render.atmosphere) {
        if (isCurrent) render.atmosphere?.let(onAtmosphere)
    }

    val pageBackground = profiledBackgroundColor(
        render.atmosphere?.edgeColorArgb ?: AndroidColor.WHITE,
        readingProfile,
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(pageBackground)
            .pointerInput(virtualIndex, mode) { detectTapGestures { onCenterTap() } }
            .padding(vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            render.error != null -> PdfPageError(render.error.orEmpty())
            render.regions.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(pageWidth)
                        .aspectRatio(virtualPage.expectedAspectRatio.coerceIn(0.18f, 2.4f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                render.regions.forEachIndexed { index, region ->
                    PdfRegionImage(
                        region = region,
                        readingProfile = readingProfile,
                        pageWidth = pageWidth,
                        contentDescription = "Страница ${virtualIndex + 1}, фрагмент ${index + 1}",
                    )
                }
            }
        }
    }
}

@Composable
private fun PdfRegionImage(
    region: SmartPdfRegionImage,
    readingProfile: PdfReadingProfile,
    pageWidth: Float,
    contentDescription: String,
) {
    Image(
        bitmap = region.image,
        contentDescription = contentDescription,
        contentScale = ContentScale.FillWidth,
        colorFilter = pdfReadingColorFilter(readingProfile),
        modifier = Modifier
            .fillMaxWidth(pageWidth.coerceIn(0.84f, 1f))
            .aspectRatio(region.aspectRatio.coerceAtLeast(0.1f))
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(2.dp),
                clip = false,
            )
            .clip(RoundedCornerShape(2.dp)),
    )
}

@Composable
private fun ZoomablePdfRegion(
    region: SmartPdfRegionImage,
    readingProfile: PdfReadingProfile,
    pageWidth: Float,
    resetZoomToken: Int,
    onScaleChanged: (Float) -> Unit,
    onInteractionChanged: (Boolean) -> Unit,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(resetZoomToken) {
        scale = 1f
        offset = Offset.Zero
        onScaleChanged(1f)
    }
    DisposableEffect(Unit) {
        onDispose { onInteractionChanged(false) }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        val viewportWidth = constraints.maxWidth.coerceAtLeast(1).toFloat()
        val viewportHeight = constraints.maxHeight.coerceAtLeast(1).toFloat()
        val viewportAspect = viewportWidth / viewportHeight
        val imageAspect = region.aspectRatio.coerceAtLeast(0.1f)
        val pageModifier = if (imageAspect >= viewportAspect) {
            Modifier
                .fillMaxWidth((pageWidth * 0.99f).coerceIn(0.82f, 0.99f))
                .aspectRatio(imageAspect)
        } else {
            Modifier
                .fillMaxHeight(0.97f)
                .aspectRatio(imageAspect)
        }

        Image(
            bitmap = region.image,
            contentDescription = "PDF",
            contentScale = ContentScale.Fit,
            colorFilter = pdfReadingColorFilter(readingProfile),
            modifier = pageModifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(2.dp),
                    clip = false,
                )
                .clip(RoundedCornerShape(2.dp))
                .pointerInput(resetZoomToken, viewportWidth, viewportHeight) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        var transformed = false
                        while (true) {
                            val event = awaitPointerEvent()
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()
                            val shouldTransform = event.changes.size > 1 || scale > 1.01f
                            if (shouldTransform) {
                                if (!transformed) onInteractionChanged(true)
                                transformed = true
                                val nextScale = (scale * zoom).coerceIn(1f, 5f)
                                val maxX = viewportWidth * (nextScale - 1f) * 0.5f
                                val maxY = viewportHeight * (nextScale - 1f) * 0.5f
                                scale = nextScale
                                offset = if (nextScale <= 1.01f) {
                                    Offset.Zero
                                } else {
                                    Offset(
                                        x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                                        y = (offset.y + pan.y).coerceIn(-maxY, maxY),
                                    )
                                }
                                event.changes.forEach { it.consume() }
                                onScaleChanged(scale)
                            }
                            if (event.changes.none { it.pressed }) break
                        }
                        if (transformed) onInteractionChanged(false)
                    }
                },
        )
    }
}

@Composable
private fun PdfPageError(message: String) {
    Column(
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
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PdfAtmosphereBackdrop(
    atmosphere: PdfPageAtmosphere?,
    readingProfile: PdfReadingProfile,
) {
    val edgeArgb = atmosphere?.edgeColorArgb ?: AndroidColor.WHITE
    val baseColor = profiledBackgroundColor(edgeArgb, readingProfile)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(baseColor)
            .clipToBounds(),
    ) {
        val blurred = atmosphere?.blurredBackdrop
        if (atmosphere?.useSolidColor == false && blurred != null) {
            Image(
                bitmap = blurred,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = pdfReadingColorFilter(readingProfile),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.15f
                        scaleY = 1.15f
                        alpha = 0.95f
                    },
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(baseColor.copy(alpha = 0.16f)),
            )
        }
    }
}

internal fun pdfReadingColorFilter(profile: PdfReadingProfile): ColorFilter? = when (profile) {
    PdfReadingProfile.ORIGINAL -> null
    PdfReadingProfile.SEPIA -> ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                0.90f, 0.18f, 0.04f, 0f, 4f,
                0.08f, 0.84f, 0.04f, 0f, 2f,
                0.02f, 0.12f, 0.72f, 0f, -2f,
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )
    PdfReadingProfile.NIGHT -> ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                -0.82f, 0f, 0f, 0f, 226f,
                0f, -0.82f, 0f, 0f, 224f,
                0f, 0f, -0.82f, 0f, 214f,
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )
    PdfReadingProfile.WARM -> ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                1.04f, 0.02f, 0f, 0f, 5f,
                0.01f, 0.99f, 0f, 0f, 1f,
                0f, 0.01f, 0.90f, 0f, -3f,
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )
    PdfReadingProfile.CONTRAST -> ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                1.18f, 0f, 0f, 0f, -23f,
                0f, 1.18f, 0f, 0f, -23f,
                0f, 0f, 1.18f, 0f, -23f,
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )
}

private fun profiledBackgroundColor(
    argb: Int,
    profile: PdfReadingProfile,
): Color {
    val raw = Color(argb)
    fun blend(a: Color, b: Color, amount: Float): Color = Color(
        red = a.red * (1f - amount) + b.red * amount,
        green = a.green * (1f - amount) + b.green * amount,
        blue = a.blue * (1f - amount) + b.blue * amount,
        alpha = 1f,
    )
    return when (profile) {
        PdfReadingProfile.ORIGINAL -> raw
        PdfReadingProfile.SEPIA -> blend(raw, Color(0xFFF4E3C2), 0.30f)
        PdfReadingProfile.WARM -> blend(raw, Color(0xFFFFE2BD), 0.22f)
        PdfReadingProfile.NIGHT -> Color(0xFF11100E)
        PdfReadingProfile.CONTRAST -> {
            val luminance = raw.red * 0.299f + raw.green * 0.587f + raw.blue * 0.114f
            if (luminance >= 0.5f) Color.White else Color.Black
        }
    }
}

@Composable
private fun PdfQuoteDialog(
    documentTitle: String,
    page: Int,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    var excerpt by remember(page) { mutableStateOf("") }
    var note by remember(page) { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Цитата · PDF стр. ${page + 1}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    documentTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                OutlinedTextField(
                    value = excerpt,
                    onValueChange = { excerpt = it },
                    label = { Text("Цитата") },
                    minLines = 2,
                    maxLines = 5,
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Заметка") },
                    minLines = 2,
                    maxLines = 5,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = excerpt.isNotBlank() || note.isNotBlank(),
                onClick = { onSave(excerpt.trim(), note.trim()) },
            ) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    )
}

private fun buildReaderPages(
    documentInfo: ModernPdfDocumentInfo,
    layoutMode: PdfLayoutMode,
): List<ReaderVirtualPage> = buildList {
    documentInfo.sourcePages.forEach { source ->
        val splitLandscape = layoutMode == PdfLayoutMode.SMART_CROP &&
            documentInfo.portraitDominant &&
            source.aspectRatio >= 1.15f
        if (splitLandscape) {
            val halfAspect = (source.width * 0.5f) / source.height.coerceAtLeast(1)
            add(
                ReaderVirtualPage(
                    sourcePage = source.index,
                    slice = PdfPageSlice.LEFT_HALF,
                    expectedAspectRatio = halfAspect,
                ),
            )
            add(
                ReaderVirtualPage(
                    sourcePage = source.index,
                    slice = PdfPageSlice.RIGHT_HALF,
                    expectedAspectRatio = halfAspect,
                ),
            )
        } else {
            add(
                ReaderVirtualPage(
                    sourcePage = source.index,
                    slice = PdfPageSlice.FULL,
                    expectedAspectRatio = source.aspectRatio,
                ),
            )
        }
    }
}

private fun readModernPdfDocumentInfo(
    context: Context,
    uri: Uri,
): ModernPdfDocumentInfo = runCatching {
    val descriptor = context.contentResolver.openFileDescriptor(uri, "r")
        ?: error("Файл больше недоступен")
    descriptor.use { file ->
        PdfRenderer(file).use { renderer ->
            if (renderer.pageCount <= 0) error("В документе нет страниц")
            val pages = buildList {
                repeat(renderer.pageCount) { index ->
                    renderer.openPage(index).use { page ->
                        add(
                            PdfSourcePageInfo(
                                index = index,
                                width = page.width.coerceAtLeast(1),
                                height = page.height.coerceAtLeast(1),
                            ),
                        )
                    }
                }
            }
            val portraitCount = pages.count { it.height > it.width * 1.08f }
            val landscapeCount = pages.count { it.width > it.height * 1.08f }
            val portraitShare = portraitCount.toFloat() / pages.size.coerceAtLeast(1)
            ModernPdfDocumentInfo(
                sourcePages = pages,
                portraitDominant = portraitCount >= 2 &&
                    portraitShare >= 0.55f &&
                    portraitCount > landscapeCount,
            )
        }
    }
}.getOrElse { error ->
    ModernPdfDocumentInfo(error = error.message ?: "Не удалось открыть документ")
}
