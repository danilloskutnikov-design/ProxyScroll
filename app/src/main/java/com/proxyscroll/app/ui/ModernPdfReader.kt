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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.proxyscroll.app.domain.LibraryDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt

private data class ModernPdfDocumentInfo(
    val pageCount: Int = 0,
    val error: String? = null,
)

internal enum class PdfReadingProfile(val label: String) {
    ORIGINAL("Оригинал"),
    SEPIA("Сепия"),
    NIGHT("Ночь"),
    WARM("Тёплый"),
    CONTRAST("Контраст"),
}

private enum class PdfNavigationMode(val label: String) {
    PAGED("Листание"),
    CONTINUOUS("Прокрутка"),
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
                pageCount = documentInfo.pageCount,
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
    pageCount: Int,
    quoteCount: Int,
    onBack: () -> Unit,
    onProgressChanged: (Int, Int) -> Unit,
    onSaveQuote: (Int, String, String) -> Unit,
    onScrollQuietChanged: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uri = remember(document.uri) { Uri.parse(document.uri) }
    val renderCache = remember(document.uri) { SmartPdfReflowCache(maxEntries = 8) }
    val initialPage = document.lastPage.coerceIn(0, pageCount - 1)
    val pagerState = rememberPagerState(initialPage = initialPage) { pageCount }
    val continuousState = rememberLazyListState(initialFirstVisibleItemIndex = initialPage)

    var controlsVisible by remember(document.id) { mutableStateOf(true) }
    var menuExpanded by remember { mutableStateOf(false) }
    var currentPage by remember(document.id) { mutableIntStateOf(initialPage) }
    var scrubPage by remember(document.id) { mutableFloatStateOf(initialPage.toFloat()) }
    var isScrubbing by remember { mutableStateOf(false) }
    var readingProfile by remember(document.id) { mutableStateOf(PdfReadingProfile.ORIGINAL) }
    var layoutMode by remember(document.id) { mutableStateOf(PdfLayoutMode.SMART_CROP) }
    var navigationMode by remember(document.id) { mutableStateOf(PdfNavigationMode.PAGED) }
    var activeAtmosphere by remember(document.id) { mutableStateOf<PdfPageAtmosphere?>(null) }
    var resetZoomToken by remember(document.id) { mutableIntStateOf(0) }
    var currentScale by remember(document.id) { mutableFloatStateOf(1f) }
    var navigationInteractionActive by remember { mutableStateOf(false) }
    var pageInteractionActive by remember { mutableStateOf(false) }
    var quoteDialogPage by remember(document.id) { mutableStateOf<Int?>(null) }
    val latestScrollQuietChanged by rememberUpdatedState(onScrollQuietChanged)

    fun resetZoom() {
        resetZoomToken += 1
        currentScale = 1f
    }

    fun goToPage(page: Int) {
        val target = page.coerceIn(0, pageCount - 1)
        resetZoom()
        scope.launch {
            when (navigationMode) {
                PdfNavigationMode.PAGED -> pagerState.animateScrollToPage(target)
                PdfNavigationMode.CONTINUOUS -> continuousState.animateScrollToItem(target)
            }
        }
    }

    LaunchedEffect(navigationMode) {
        resetZoom()
        when (navigationMode) {
            PdfNavigationMode.PAGED -> pagerState.scrollToPage(currentPage)
            PdfNavigationMode.CONTINUOUS -> continuousState.scrollToItem(currentPage)
        }
    }

    LaunchedEffect(layoutMode) {
        resetZoom()
        activeAtmosphere = renderCache.get(currentPage, layoutMode)?.atmosphere
    }

    LaunchedEffect(controlsVisible, menuExpanded, quoteDialogPage, isScrubbing) {
        if (controlsVisible && !menuExpanded && quoteDialogPage == null && !isScrubbing) {
            kotlinx.coroutines.delay(3200)
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

    LaunchedEffect(currentPage, pageCount) {
        if (!isScrubbing) scrubPage = currentPage.toFloat()
        onProgressChanged(currentPage, pageCount)
        renderCache.get(currentPage, layoutMode)?.atmosphere?.let { activeAtmosphere = it }
    }

    LaunchedEffect(currentPage, layoutMode, document.uri) {
        withContext(Dispatchers.IO) {
            listOf(currentPage - 1, currentPage, currentPage + 1)
                .filter { it in 0 until pageCount }
                .forEach { page ->
                    renderSmartPdfPage(
                        context = context,
                        uri = uri,
                        requestedPage = page,
                        mode = layoutMode,
                        cache = renderCache,
                    )
                }
        }
        renderCache.get(currentPage, layoutMode)?.atmosphere?.let { activeAtmosphere = it }
    }

    DisposableEffect(renderCache) {
        onDispose {
            renderCache.clear()
            latestScrollQuietChanged(false)
        }
    }

    quoteDialogPage?.let { page ->
        PdfQuoteDialog(
            documentTitle = document.title,
            page = page,
            onDismiss = { quoteDialogPage = null },
            onSave = { excerpt, note ->
                onSaveQuote(page, excerpt, note)
                quoteDialogPage = null
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

        // The atmosphere stays edge-to-edge, but actual paper lives in a safe viewport.
        // This is the important split that prevents PDF content from sitting under status/nav bars.
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
                    pageSpacing = 10.dp,
                ) { page ->
                    PagedPdfPage(
                        documentUri = document.uri,
                        page = page,
                        mode = layoutMode,
                        readingProfile = readingProfile,
                        cache = renderCache,
                        isCurrent = page == currentPage,
                        resetZoomToken = resetZoomToken,
                        onScaleChanged = { scale ->
                            if (page == currentPage) currentScale = scale
                        },
                        onInteractionChanged = { active ->
                            if (page == currentPage) pageInteractionActive = active
                        },
                        onAtmosphere = { atmosphere ->
                            if (page == currentPage) activeAtmosphere = atmosphere
                        },
                        onTap = { xFraction ->
                            when {
                                xFraction < 0.17f && currentPage > 0 -> goToPage(currentPage - 1)
                                xFraction > 0.83f && currentPage < pageCount - 1 -> goToPage(currentPage + 1)
                                else -> controlsVisible = !controlsVisible
                            }
                        },
                    )
                }

                PdfNavigationMode.CONTINUOUS -> LazyColumn(
                    state = continuousState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(pageCount, key = { it }) { page ->
                        ContinuousPdfPage(
                            documentUri = document.uri,
                            page = page,
                            mode = layoutMode,
                            readingProfile = readingProfile,
                            cache = renderCache,
                            isCurrent = page == currentPage,
                            onAtmosphere = { atmosphere ->
                                if (page == currentPage) activeAtmosphere = atmosphere
                            },
                            onCenterTap = { controlsVisible = !controlsVisible },
                        )
                    }
                }
            }
        }

        val readingProgress = ((currentPage + 1f) / pageCount).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .zIndex(6f)
                .fillMaxWidth(readingProgress)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.primary),
        )

        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(5f),
            enter = fadeIn(tween(160)) + slideInVertically(tween(240)) { -it },
            exit = fadeOut(tween(140)) + slideOutVertically(tween(210)) { -it },
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
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
                            document.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            buildString {
                                append("${currentPage + 1} / $pageCount · ")
                                append(layoutMode.label)
                                append(" · ${navigationMode.label}")
                                if (navigationMode == PdfNavigationMode.PAGED && layoutMode == PdfLayoutMode.ORIGINAL) {
                                    append(" · ${(currentScale * 100).roundToInt()}%")
                                }
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    IconButton(onClick = { quoteDialogPage = currentPage }) {
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
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Настройки чтения")
                        }
                        ReaderSettingsMenu(
                            expanded = menuExpanded,
                            layoutMode = layoutMode,
                            navigationMode = navigationMode,
                            readingProfile = readingProfile,
                            onDismiss = { menuExpanded = false },
                            onLayoutMode = { mode ->
                                layoutMode = mode
                                menuExpanded = false
                            },
                            onNavigationMode = { mode ->
                                navigationMode = mode
                                menuExpanded = false
                            },
                            onReadingProfile = { profile ->
                                readingProfile = profile
                                menuExpanded = false
                            },
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(5f),
            enter = fadeIn(tween(160)) + slideInVertically(tween(240)) { it },
            exit = fadeOut(tween(140)) + slideOutVertically(tween(210)) { it },
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        enabled = currentPage > 0,
                        onClick = { goToPage(currentPage - 1) },
                    ) {
                        Icon(Icons.Default.NavigateBefore, contentDescription = "Предыдущая страница")
                    }
                    Slider(
                        value = scrubPage.coerceIn(0f, (pageCount - 1).coerceAtLeast(0).toFloat()),
                        onValueChange = { value ->
                            isScrubbing = true
                            scrubPage = value.coerceIn(0f, (pageCount - 1).coerceAtLeast(0).toFloat())
                        },
                        onValueChangeFinished = {
                            isScrubbing = false
                            goToPage(scrubPage.roundToInt())
                        },
                        valueRange = 0f..(pageCount - 1).coerceAtLeast(1).toFloat(),
                        steps = (pageCount - 2).coerceIn(0, 50),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        enabled = currentPage < pageCount - 1,
                        onClick = { goToPage(currentPage + 1) },
                    ) {
                        Icon(Icons.Default.NavigateNext, contentDescription = "Следующая страница")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderSettingsMenu(
    expanded: Boolean,
    layoutMode: PdfLayoutMode,
    navigationMode: PdfNavigationMode,
    readingProfile: PdfReadingProfile,
    onDismiss: () -> Unit,
    onLayoutMode: (PdfLayoutMode) -> Unit,
    onNavigationMode: (PdfNavigationMode) -> Unit,
    onReadingProfile: (PdfReadingProfile) -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        Text(
            "Размер страницы",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        PdfLayoutMode.entries.forEach { mode ->
            DropdownMenuItem(
                text = { Text(mode.label) },
                leadingIcon = if (mode == layoutMode) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else {
                    null
                },
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
                } else {
                    null
                },
                onClick = { onNavigationMode(mode) },
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
                } else {
                    null
                },
                onClick = { onReadingProfile(profile) },
            )
        }
    }
}

@Composable
private fun PagedPdfPage(
    documentUri: String,
    page: Int,
    mode: PdfLayoutMode,
    readingProfile: PdfReadingProfile,
    cache: SmartPdfReflowCache,
    isCurrent: Boolean,
    resetZoomToken: Int,
    onScaleChanged: (Float) -> Unit,
    onInteractionChanged: (Boolean) -> Unit,
    onAtmosphere: (PdfPageAtmosphere) -> Unit,
    onTap: (Float) -> Unit,
) {
    val context = LocalContext.current
    var viewportWidth by remember(page, mode) { mutableIntStateOf(1) }
    val cached = remember(documentUri, page, mode) { cache.get(page, mode) }
    val render by produceState(
        initialValue = cached ?: SmartPdfPageRender(),
        key1 = documentUri,
        key2 = page,
        key3 = mode,
    ) {
        if (value.regions.isEmpty() && value.error == null) {
            value = renderSmartPdfPageAsync(
                context = context,
                uri = Uri.parse(documentUri),
                requestedPage = page,
                mode = mode,
                cache = cache,
            )
        }
    }

    LaunchedEffect(isCurrent, render.atmosphere) {
        if (isCurrent) render.atmosphere?.let(onAtmosphere)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(page, mode, viewportWidth) {
                detectTapGestures { position ->
                    onTap((position.x / viewportWidth.coerceAtLeast(1)).coerceIn(0f, 1f))
                }
            }
            .onSizeChangedCompat { width -> viewportWidth = width },
        contentAlignment = Alignment.Center,
    ) {
        when {
            render.error != null -> PdfPageError(render.error.orEmpty())
            render.regions.isEmpty() -> CircularProgressIndicator()
            mode == PdfLayoutMode.ORIGINAL -> ZoomablePdfRegion(
                region = render.regions.first(),
                readingProfile = readingProfile,
                resetZoomToken = resetZoomToken,
                onScaleChanged = onScaleChanged,
                onInteractionChanged = onInteractionChanged,
            )
            else -> SmartPagedRegions(
                page = page,
                regions = render.regions,
                readingProfile = readingProfile,
                onInteractionChanged = if (isCurrent) onInteractionChanged else { _ -> },
            )
        }
    }
}

@Composable
private fun SmartPagedRegions(
    page: Int,
    regions: List<SmartPdfRegionImage>,
    readingProfile: PdfReadingProfile,
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
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        itemsIndexed(regions) { index, region ->
            PdfRegionImage(
                region = region,
                readingProfile = readingProfile,
                contentDescription = "Страница ${page + 1}, фрагмент ${index + 1}",
            )
        }
        item { Spacer(Modifier.height(6.dp)) }
    }
}

@Composable
private fun ContinuousPdfPage(
    documentUri: String,
    page: Int,
    mode: PdfLayoutMode,
    readingProfile: PdfReadingProfile,
    cache: SmartPdfReflowCache,
    isCurrent: Boolean,
    onAtmosphere: (PdfPageAtmosphere) -> Unit,
    onCenterTap: () -> Unit,
) {
    val context = LocalContext.current
    val cached = remember(documentUri, page, mode) { cache.get(page, mode) }
    val render by produceState(
        initialValue = cached ?: SmartPdfPageRender(),
        key1 = documentUri,
        key2 = page,
        key3 = mode,
    ) {
        if (value.regions.isEmpty() && value.error == null) {
            value = renderSmartPdfPageAsync(
                context = context,
                uri = Uri.parse(documentUri),
                requestedPage = page,
                mode = mode,
                cache = cache,
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(pageBackground)
            .pointerInput(page, mode) { detectTapGestures { onCenterTap() } }
            .padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when {
            render.error != null -> PdfPageError(render.error.orEmpty())
            render.regions.isEmpty() -> {
                Spacer(Modifier.height(36.dp))
                CircularProgressIndicator()
                Spacer(Modifier.height(36.dp))
            }
            else -> render.regions.forEachIndexed { index, region ->
                PdfRegionImage(
                    region = region,
                    readingProfile = readingProfile,
                    contentDescription = "Страница ${page + 1}, фрагмент ${index + 1}",
                )
            }
        }
    }
}

@Composable
private fun PdfRegionImage(
    region: SmartPdfRegionImage,
    readingProfile: PdfReadingProfile,
    contentDescription: String,
) {
    Image(
        bitmap = region.image,
        contentDescription = contentDescription,
        contentScale = ContentScale.FillWidth,
        colorFilter = pdfReadingColorFilter(readingProfile),
        modifier = Modifier
            .fillMaxWidth(0.985f)
            .aspectRatio(region.aspectRatio.coerceAtLeast(0.1f))
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(3.dp),
                clip = false,
            )
            .clip(RoundedCornerShape(3.dp)),
    )
}

@Composable
private fun ZoomablePdfRegion(
    region: SmartPdfRegionImage,
    readingProfile: PdfReadingProfile,
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
                .fillMaxWidth(0.97f)
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
                    elevation = 10.dp,
                    shape = RoundedCornerShape(3.dp),
                    clip = false,
                )
                .clip(RoundedCornerShape(3.dp))
                .pointerInput(resetZoomToken, viewportWidth, viewportHeight) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        var transformed = false
                        do {
                            val event = awaitPointerEvent()
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()
                            val shouldTransform = event.changes.size > 1 || scale > 1.01f
                            if (shouldTransform) {
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
                        } while (event.changes.any { it.pressed })
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
        title = { Text("Цитата · стр. ${page + 1}") },
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

private fun readModernPdfDocumentInfo(
    context: Context,
    uri: Uri,
): ModernPdfDocumentInfo = runCatching {
    val descriptor = context.contentResolver.openFileDescriptor(uri, "r")
        ?: error("Файл больше недоступен")
    descriptor.use { file ->
        PdfRenderer(file).use { renderer ->
            if (renderer.pageCount <= 0) error("В документе нет страниц")
            ModernPdfDocumentInfo(pageCount = renderer.pageCount)
        }
    }
}.getOrElse { error ->
    ModernPdfDocumentInfo(error = error.message ?: "Не удалось открыть документ")
}

/** Small compatibility helper keeps the reader independent of layout callback objects. */
private fun Modifier.onSizeChangedCompat(onWidth: (Int) -> Unit): Modifier =
    this.then(
        Modifier.pointerInput(Unit) {
            onWidth(size.width.coerceAtLeast(1))
        },
    )
