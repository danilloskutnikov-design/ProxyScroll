package com.proxyscroll.app.ui

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.proxyscroll.app.domain.LibraryDocument
import com.proxyscroll.app.ui.theme.ProxySurface
import com.proxyscroll.app.ui.theme.ProxySurfaceRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedHashMap
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

private data class ModernPdfDocumentInfo(
    val pageCount: Int = 0,
    val error: String? = null,
)

private data class ModernPdfPageRender(
    val frame: ModernPdfPageFrame? = null,
    val error: String? = null,
)

private data class ModernPdfPageFrame(
    val image: ImageBitmap,
    val backdrop: ImageBitmap,
)

internal enum class PdfReadingProfile(val label: String) {
    ORIGINAL("Оригинал"),
    SEPIA("Сепия"),
    NIGHT("Ночь"),
    WARM("Тёплый"),
    CONTRAST("Контраст"),
}

private class ModernPdfBitmapCache(
    private val maxEntries: Int = 7,
) {
    private val pages = object : LinkedHashMap<Int, ModernPdfPageFrame>(
        maxEntries,
        0.75f,
        true,
    ) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<Int, ModernPdfPageFrame>?,
        ): Boolean = size > maxEntries
    }

    fun get(page: Int): ModernPdfPageFrame? = synchronized(this) { pages[page] }

    fun getOrRender(page: Int, render: () -> ModernPdfPageFrame): ModernPdfPageFrame {
        get(page)?.let { return it }
        val rendered = render()
        return synchronized(this) {
            pages[page] ?: rendered.also { pages[page] = it }
        }
    }

    fun clear() = synchronized(this) { pages.clear() }
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
    val bitmapCache = remember(document.uri) { ModernPdfBitmapCache(maxEntries = 7) }
    val reflowCache = remember(document.uri) { SmartPdfReflowCache(maxEntries = 3) }
    val initialPage = document.lastPage.coerceIn(0, pageCount - 1)
    val pagerState = rememberPagerState(initialPage = initialPage) { pageCount }

    var controlsVisible by remember(document.id) { mutableStateOf(true) }
    var resetZoomToken by remember(document.id) { mutableIntStateOf(0) }
    var currentScale by remember(document.id) { mutableFloatStateOf(1f) }
    var pagerInteractionActive by remember { mutableStateOf(false) }
    var pageInteractionActive by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var scrubPage by remember(document.id) { mutableFloatStateOf(initialPage.toFloat()) }
    var isScrubbing by remember { mutableStateOf(false) }
    var readingProfile by remember(document.id) { mutableStateOf(PdfReadingProfile.ORIGINAL) }
    var layoutMode by remember(document.id) { mutableStateOf(PdfLayoutMode.SMART_CROP) }
    var quoteDialogPage by remember(document.id) { mutableStateOf<Int?>(null) }
    val latestScrollQuietChanged by rememberUpdatedState(onScrollQuietChanged)

    fun resetZoom() {
        resetZoomToken += 1
        currentScale = 1f
    }

    fun goToPage(page: Int) {
        resetZoom()
        val target = page.coerceIn(0, pageCount - 1)
        scope.launch { pagerState.animateScrollToPage(target) }
    }

    LaunchedEffect(layoutMode) {
        resetZoom()
        currentScale = 1f
    }
    LaunchedEffect(controlsVisible, menuExpanded, quoteDialogPage, isScrubbing) {
        if (controlsVisible && !menuExpanded && quoteDialogPage == null && !isScrubbing) {
            delay(3200)
            controlsVisible = false
        }
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.isScrollInProgress }
            .distinctUntilChanged()
            .collectLatest { moving -> pagerInteractionActive = moving }
    }
    LaunchedEffect(pagerInteractionActive, pageInteractionActive) {
        latestScrollQuietChanged(pagerInteractionActive || pageInteractionActive)
    }
    LaunchedEffect(pagerState, pageCount) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collectLatest { page ->
                resetZoom()
                if (!isScrubbing) scrubPage = page.toFloat()
                onProgressChanged(page, pageCount)
            }
    }
    LaunchedEffect(pagerState, pageCount, document.uri) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collectLatest { page ->
                withContext(Dispatchers.IO) {
                    listOf(page - 1, page + 1, page - 2, page + 2)
                        .filter { it in 0 until pageCount }
                        .forEach { adjacentPage ->
                            renderModernPdfPage(
                                context = context,
                                uri = Uri.parse(document.uri),
                                requestedPage = adjacentPage,
                                cache = bitmapCache,
                            )
                        }
                }
            }
    }
    LaunchedEffect(pagerState.currentPage, isScrubbing) {
        if (!isScrubbing) scrubPage = pagerState.currentPage.toFloat()
    }
    DisposableEffect(bitmapCache, reflowCache) {
        onDispose {
            bitmapCache.clear()
            reflowCache.clear()
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
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = layoutMode != PdfLayoutMode.ORIGINAL || currentScale <= 1.01f,
            beyondViewportPageCount = 1,
            pageSpacing = 12.dp,
        ) { page ->
            val pageOffset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                ).absoluteValue.coerceIn(0f, 1f)
            ModernPdfReaderPage(
                document = document,
                page = page,
                bitmapCache = bitmapCache,
                reflowCache = reflowCache,
                readingProfile = readingProfile,
                layoutMode = layoutMode,
                controlsVisible = controlsVisible,
                isCurrent = page == pagerState.currentPage,
                resetZoomToken = resetZoomToken,
                onScaleChanged = { scale ->
                    if (page == pagerState.currentPage) currentScale = scale
                },
                onInteractionChanged = { active ->
                    if (page == pagerState.currentPage) pageInteractionActive = active
                },
                onCenterTap = { controlsVisible = !controlsVisible },
                onPreviousPage = {
                    if (pagerState.currentPage > 0) goToPage(pagerState.currentPage - 1)
                },
                onNextPage = {
                    if (pagerState.currentPage < pageCount - 1) {
                        goToPage(pagerState.currentPage + 1)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 1f - pageOffset * 0.12f
                        val pageScale = 1f - pageOffset * 0.012f
                        scaleX = pageScale
                        scaleY = pageScale
                    },
            )
        }

        val readingProgress = (
            (pagerState.currentPage + pagerState.currentPageOffsetFraction + 1f) / pageCount
            ).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .zIndex(5f)
                .fillMaxWidth(readingProgress)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.primary),
        )

        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(3f),
            enter = fadeIn(tween(180)) + slideInVertically(tween(260)) { -it },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(220)) { -it },
        ) {
            ProxySurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                role = ProxySurfaceRole.OVERLAY,
                strong = true,
                interactive = false,
                deformContent = false,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
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
                                append("${pagerState.currentPage + 1} / $pageCount · ")
                                if (layoutMode == PdfLayoutMode.ORIGINAL) {
                                    append("${(currentScale * 100).roundToInt()}% · ")
                                }
                                append(layoutMode.label)
                                append(" · ${readingProfile.label}")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Box {
                        IconButton(onClick = { quoteDialogPage = pagerState.currentPage }) {
                            Icon(
                                Icons.Default.FormatQuote,
                                contentDescription = "Сохранить цитату или заметку",
                            )
                        }
                        if (quoteCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = quoteCount.coerceAtMost(99).toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }
                    }
                    IconButton(
                        enabled = layoutMode == PdfLayoutMode.ORIGINAL && currentScale > 1.01f,
                        onClick = { resetZoom() },
                    ) {
                        Icon(Icons.Default.FormatSize, contentDescription = "Вернуть масштаб")
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Настройки PDF")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Первая страница") },
                                onClick = {
                                    menuExpanded = false
                                    goToPage(0)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Последняя страница") },
                                onClick = {
                                    menuExpanded = false
                                    goToPage(pageCount - 1)
                                },
                            )
                            HorizontalDivider()
                            Text(
                                "Режим страницы",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            PdfLayoutMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.label) },
                                    trailingIcon = if (mode == layoutMode) {
                                        { Icon(Icons.Default.Check, contentDescription = "Выбрано") }
                                    } else null,
                                    onClick = {
                                        layoutMode = mode
                                        menuExpanded = false
                                    },
                                )
                            }
                            HorizontalDivider()
                            Text(
                                "Цвет чтения",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            PdfReadingProfile.entries.forEach { profile ->
                                DropdownMenuItem(
                                    text = { Text(profile.label) },
                                    trailingIcon = if (profile == readingProfile) {
                                        { Icon(Icons.Default.Check, contentDescription = "Выбрано") }
                                    } else null,
                                    onClick = {
                                        readingProfile = profile
                                        menuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(3f),
            enter = fadeIn(tween(180)) + slideInVertically(tween(260)) { it },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(220)) { it },
        ) {
            ProxySurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                role = ProxySurfaceRole.OVERLAY,
                strong = true,
                interactive = false,
                deformContent = false,
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        IconButton(
                            enabled = pagerState.currentPage > 0,
                            onClick = { goToPage(pagerState.currentPage - 1) },
                        ) {
                            Icon(Icons.Default.NavigateBefore, contentDescription = "Предыдущая страница")
                        }
                        Text(
                            "Страница ${(if (isScrubbing) scrubPage else pagerState.currentPage.toFloat()).roundToInt() + 1}",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        IconButton(
                            enabled = pagerState.currentPage < pageCount - 1,
                            onClick = { goToPage(pagerState.currentPage + 1) },
                        ) {
                            Icon(Icons.Default.NavigateNext, contentDescription = "Следующая страница")
                        }
                    }
                    Slider(
                        value = if (pageCount > 1) scrubPage else 0f,
                        onValueChange = { value ->
                            isScrubbing = true
                            scrubPage = value.roundToInt().toFloat()
                        },
                        onValueChangeFinished = {
                            val target = scrubPage.roundToInt()
                            isScrubbing = false
                            goToPage(target)
                        },
                        valueRange = 0f..(pageCount - 1).coerceAtLeast(1).toFloat(),
                        enabled = pageCount > 1 &&
                            (layoutMode != PdfLayoutMode.ORIGINAL || currentScale <= 1.01f),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (layoutMode == PdfLayoutMode.ORIGINAL && currentScale > 1.01f) {
                        Text(
                            "Навигация по страницам заблокирована во время увеличения",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                    }
                }
            }
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
    var excerpt by remember(documentTitle, page) { mutableStateOf("") }
    var note by remember(documentTitle, page) { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.FormatQuote, contentDescription = null) },
        title = { Text("Цитата или заметка") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "$documentTitle · страница ${page + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                OutlinedTextField(
                    value = excerpt,
                    onValueChange = { excerpt = it },
                    label = { Text("Цитата") },
                    placeholder = { Text("Введите или вставьте фрагмент") },
                    minLines = 3,
                    maxLines = 7,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Моя заметка") },
                    placeholder = { Text("Почему это важно?") },
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "Номер страницы сохранится автоматически.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = excerpt.isNotBlank() || note.isNotBlank(),
                onClick = { onSave(excerpt, note) },
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun ModernPdfReaderPage(
    document: LibraryDocument,
    page: Int,
    bitmapCache: ModernPdfBitmapCache,
    reflowCache: SmartPdfReflowCache,
    readingProfile: PdfReadingProfile,
    layoutMode: PdfLayoutMode,
    controlsVisible: Boolean,
    isCurrent: Boolean,
    resetZoomToken: Int,
    onScaleChanged: (Float) -> Unit,
    onInteractionChanged: (Boolean) -> Unit,
    onCenterTap: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (layoutMode != PdfLayoutMode.ORIGINAL) {
        LaunchedEffect(isCurrent, layoutMode) {
            if (isCurrent) onScaleChanged(1f)
        }
        SmartPdfReflowPage(
            documentUri = document.uri,
            page = page,
            mode = layoutMode,
            readingProfile = readingProfile,
            controlsVisible = controlsVisible,
            cache = reflowCache,
            isCurrent = isCurrent,
            onInteractionChanged = onInteractionChanged,
            onCenterTap = onCenterTap,
            onPreviousPage = onPreviousPage,
            onNextPage = onNextPage,
            modifier = modifier,
        )
        return
    }

    val context = LocalContext.current
    val cachedFrame = remember(document.uri, page) { bitmapCache.get(page) }
    val render by produceState(
        initialValue = ModernPdfPageRender(frame = cachedFrame),
        key1 = document.uri,
        key2 = page,
    ) {
        if (value.frame == null) {
            value = withContext(Dispatchers.IO) {
                renderModernPdfPage(
                    context = context,
                    uri = Uri.parse(document.uri),
                    requestedPage = page,
                    cache = bitmapCache,
                )
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        render.frame?.let { frame ->
            PdfPageBackdrop(
                image = frame.backdrop,
                readingProfile = readingProfile,
            )
        }
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
            render.frame == null -> CircularProgressIndicator()
            else -> ZoomablePdfPage(
                image = render.frame?.image ?: return@Box,
                page = page,
                readingProfile = readingProfile,
                isCurrent = isCurrent,
                resetZoomToken = resetZoomToken,
                onScaleChanged = onScaleChanged,
                onInteractionChanged = onInteractionChanged,
                onCenterTap = onCenterTap,
                onPreviousPage = onPreviousPage,
                onNextPage = onNextPage,
            )
        }
    }
}

@Composable
private fun ZoomablePdfPage(
    image: ImageBitmap,
    page: Int,
    readingProfile: PdfReadingProfile,
    isCurrent: Boolean,
    resetZoomToken: Int,
    onScaleChanged: (Float) -> Unit,
    onInteractionChanged: (Boolean) -> Unit,
    onCenterTap: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var scale by remember(page) { mutableFloatStateOf(1f) }
    var offsetX by remember(page) { mutableFloatStateOf(0f) }
    var offsetY by remember(page) { mutableFloatStateOf(0f) }
    var zoomAnimationJob by remember(page) { mutableStateOf<Job?>(null) }
    var viewportWidth by remember(page) { mutableIntStateOf(1) }
    var viewportHeight by remember(page) { mutableIntStateOf(1) }
    var transforming by remember(page) { mutableStateOf(false) }

    fun panBounds(targetScale: Float): Pair<Float, Float> {
        val width = viewportWidth.toFloat().coerceAtLeast(1f)
        val height = viewportHeight.toFloat().coerceAtLeast(1f)
        val insetWidth = width * 0.96f
        val insetHeight = height * 0.96f
        val imageAspect = image.width.toFloat() / image.height.toFloat().coerceAtLeast(1f)
        val viewportAspect = insetWidth / insetHeight
        val baseWidth: Float
        val baseHeight: Float
        if (imageAspect >= viewportAspect) {
            baseWidth = insetWidth
            baseHeight = insetWidth / imageAspect
        } else {
            baseHeight = insetHeight
            baseWidth = insetHeight * imageAspect
        }
        val maxX = ((baseWidth * targetScale - width) / 2f).coerceAtLeast(0f)
        val maxY = ((baseHeight * targetScale - height) / 2f).coerceAtLeast(0f)
        return maxX to maxY
    }

    fun clampOffsets(targetScale: Float = scale) {
        val (maxX, maxY) = panBounds(targetScale)
        offsetX = offsetX.coerceIn(-maxX, maxX)
        offsetY = offsetY.coerceIn(-maxY, maxY)
    }

    fun animateZoom(targetScale: Float, tap: Offset? = null) {
        val safeScale = targetScale.coerceIn(1f, 4f)
        val (maxX, maxY) = panBounds(safeScale)
        val targetX = if (safeScale <= 1.01f || tap == null) {
            0f
        } else {
            ((viewportWidth / 2f - tap.x) * (safeScale - 1f)).coerceIn(-maxX, maxX)
        }
        val targetY = if (safeScale <= 1.01f || tap == null) {
            0f
        } else {
            ((viewportHeight / 2f - tap.y) * (safeScale - 1f)).coerceIn(-maxY, maxY)
        }
        zoomAnimationJob?.cancel()
        val startScale = scale
        val startX = offsetX
        val startY = offsetY
        zoomAnimationJob = scope.launch {
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.84f,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            ) { progress, _ ->
                scale = startScale + (safeScale - startScale) * progress
                offsetX = startX + (targetX - startX) * progress
                offsetY = startY + (targetY - startY) * progress
            }
        }
    }

    LaunchedEffect(resetZoomToken) {
        if (resetZoomToken > 0) animateZoom(1f)
    }
    LaunchedEffect(scale, isCurrent) {
        if (isCurrent) onScaleChanged(scale)
    }
    LaunchedEffect(transforming, isCurrent) {
        onInteractionChanged(isCurrent && transforming)
    }
    LaunchedEffect(viewportWidth, viewportHeight) { clampOffsets() }
    DisposableEffect(isCurrent) {
        onDispose {
            if (isCurrent) onInteractionChanged(false)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .onGloballyPositioned { coordinates ->
                viewportWidth = coordinates.size.width.coerceAtLeast(1)
                viewportHeight = coordinates.size.height.coerceAtLeast(1)
            }
            .pointerInput(page, viewportWidth, viewportHeight) {
                detectTapGestures(
                    onTap = { position ->
                        if (scale <= 1.01f) {
                            when {
                                position.x < viewportWidth * 0.24f -> onPreviousPage()
                                position.x > viewportWidth * 0.76f -> onNextPage()
                                else -> onCenterTap()
                            }
                        } else {
                            onCenterTap()
                        }
                    },
                    onDoubleTap = { position ->
                        if (scale > 1.05f) animateZoom(1f) else animateZoom(2.25f, position)
                    },
                )
            }
            .pointerInput(page, viewportWidth, viewportHeight) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var transformedThisGesture = false
                    var pointersStillPressed: Boolean
                    do {
                        val event = awaitPointerEvent()
                        val pressedPointers = event.changes.count { it.pressed }
                        val canTransform = pressedPointers >= 2 || scale > 1.01f
                        if (canTransform) {
                            zoomAnimationJob?.cancel()
                            transformedThisGesture = true
                            transforming = true
                            val zoomChange = if (pressedPointers >= 2) event.calculateZoom() else 1f
                            val panChange = event.calculatePan()
                            val newScale = (scale * zoomChange).coerceIn(1f, 4f)
                            val (maxX, maxY) = panBounds(newScale)
                            scale = newScale
                            offsetX = (offsetX + panChange.x).coerceIn(-maxX, maxX)
                            offsetY = (offsetY + panChange.y).coerceIn(-maxY, maxY)
                            event.changes.forEach { it.consume() }
                        }
                        pointersStillPressed = event.changes.any { it.pressed }
                    } while (pointersStillPressed)

                    if (transformedThisGesture && scale <= 1.02f) {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    } else {
                        clampOffsets()
                    }
                    transforming = false
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        val imageAspect = image.width.toFloat() / image.height.toFloat().coerceAtLeast(1f)
        val viewportAspect = maxWidth.value / maxHeight.value.coerceAtLeast(1f)
        val pageModifier = if (imageAspect >= viewportAspect) {
            Modifier
                .fillMaxWidth(0.96f)
                .aspectRatio(imageAspect)
        } else {
            Modifier
                .fillMaxHeight(0.96f)
                .aspectRatio(imageAspect)
        }

        Box(
            modifier = pageModifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                }
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(3.dp),
                    clip = false,
                )
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White),
        ) {
            Image(
                bitmap = image,
                contentDescription = "Страница ${page + 1}",
                contentScale = ContentScale.Fit,
                colorFilter = pdfReadingColorFilter(readingProfile),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
internal fun PdfPageBackdrop(
    image: ImageBitmap,
    readingProfile: PdfReadingProfile,
    modifier: Modifier = Modifier,
) {
    val baseColor = when (readingProfile) {
        PdfReadingProfile.ORIGINAL -> MaterialTheme.colorScheme.background
        PdfReadingProfile.SEPIA -> Color(0xFF2F2115)
        PdfReadingProfile.NIGHT -> Color(0xFF030507)
        PdfReadingProfile.WARM -> Color(0xFF352017)
        PdfReadingProfile.CONTRAST -> Color(0xFF080A0D)
    }
    val veilColor = when (readingProfile) {
        PdfReadingProfile.ORIGINAL -> Color.Black.copy(alpha = 0.16f)
        PdfReadingProfile.SEPIA -> Color(0xFF6D3F1D).copy(alpha = 0.20f)
        PdfReadingProfile.NIGHT -> Color.Black.copy(alpha = 0.34f)
        PdfReadingProfile.WARM -> Color(0xFF8B3E1B).copy(alpha = 0.16f)
        PdfReadingProfile.CONTRAST -> Color.Black.copy(alpha = 0.24f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseColor)
            .clipToBounds(),
    ) {
        Image(
            bitmap = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = pdfReadingColorFilter(readingProfile),
            filterQuality = FilterQuality.Medium,
            alpha = 0.92f,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.12f
                    scaleY = 1.12f
                },
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(veilColor),
        )
    }
}

internal fun pdfReadingColorFilter(profile: PdfReadingProfile): ColorFilter? = when (profile) {
    PdfReadingProfile.ORIGINAL -> null
    PdfReadingProfile.SEPIA -> ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )
    PdfReadingProfile.NIGHT -> ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )
    PdfReadingProfile.WARM -> ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                1.04f, 0f, 0f, 0f, 5f,
                0f, 1.00f, 0f, 0f, 2f,
                0f, 0f, 0.82f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )
    PdfReadingProfile.CONTRAST -> ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                1.24f, 0f, 0f, 0f, -30f,
                0f, 1.24f, 0f, 0f, -30f,
                0f, 0f, 1.24f, 0f, -30f,
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )
}

private fun readModernPdfDocumentInfo(
    context: android.content.Context,
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
    ModernPdfDocumentInfo(error = error.message ?: "Неизвестная ошибка чтения")
}

private fun renderModernPdfPage(
    context: android.content.Context,
    uri: Uri,
    requestedPage: Int,
    cache: ModernPdfBitmapCache,
): ModernPdfPageRender = runCatching {
    cache.get(requestedPage)?.let { cached ->
        return@runCatching ModernPdfPageRender(frame = cached)
    }
    val descriptor = context.contentResolver.openFileDescriptor(uri, "r")
        ?: error("Файл больше недоступен")
    descriptor.use { file ->
        PdfRenderer(file).use { renderer ->
            if (renderer.pageCount <= 0) error("В документе нет страниц")
            val actualPage = requestedPage.coerceIn(0, renderer.pageCount - 1)
            val frame = cache.getOrRender(actualPage) {
                renderer.openPage(actualPage).use { page ->
                    val targetWidth = 1600
                    val targetHeight = (targetWidth.toFloat() * page.height / page.width)
                        .roundToInt()
                        .coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(
                        targetWidth,
                        targetHeight,
                        Bitmap.Config.ARGB_8888,
                    )
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    ModernPdfPageFrame(
                        image = bitmap.asImageBitmap(),
                        backdrop = createPdfBackdrop(bitmap),
                    )
                }
            }
            ModernPdfPageRender(frame = frame)
        }
    }
}.getOrElse { error ->
    ModernPdfPageRender(error = error.message ?: "Неизвестная ошибка чтения")
}

/**
 * Creates a tiny, CPU-blurred page atmosphere. It is cheap enough to generate
 * with the page render and keeps the immersive background available before
 * Android 12, where a render-effect blur is not guaranteed.
 */
internal fun createPdfBackdrop(source: Bitmap): ImageBitmap {
    val targetWidth = 56
    val targetHeight = (
        targetWidth.toFloat() * source.height / source.width.coerceAtLeast(1)
        ).roundToInt().coerceIn(56, 112)
    val thumbnail = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
    val pixels = IntArray(targetWidth * targetHeight)
    thumbnail.getPixels(pixels, 0, targetWidth, 0, 0, targetWidth, targetHeight)

    var sourcePixels = pixels
    var destinationPixels = IntArray(pixels.size)
    repeat(3) {
        blurPdfBackdropPass(
            source = sourcePixels,
            destination = destinationPixels,
            width = targetWidth,
            height = targetHeight,
            radius = 5,
            horizontal = true,
        )
        val horizontalPixels = destinationPixels
        destinationPixels = sourcePixels
        sourcePixels = horizontalPixels
        blurPdfBackdropPass(
            source = sourcePixels,
            destination = destinationPixels,
            width = targetWidth,
            height = targetHeight,
            radius = 5,
            horizontal = false,
        )
        val verticalPixels = destinationPixels
        destinationPixels = sourcePixels
        sourcePixels = verticalPixels
    }

    val blurred = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
    blurred.setPixels(sourcePixels, 0, targetWidth, 0, 0, targetWidth, targetHeight)
    if (thumbnail !== source) thumbnail.recycle()
    return blurred.asImageBitmap()
}

private fun blurPdfBackdropPass(
    source: IntArray,
    destination: IntArray,
    width: Int,
    height: Int,
    radius: Int,
    horizontal: Boolean,
) {
    for (y in 0 until height) {
        for (x in 0 until width) {
            var red = 0
            var green = 0
            var blue = 0
            var samples = 0
            for (offset in -radius..radius) {
                val sampleX = if (horizontal) (x + offset).coerceIn(0, width - 1) else x
                val sampleY = if (horizontal) y else (y + offset).coerceIn(0, height - 1)
                val color = source[sampleY * width + sampleX]
                red += color shr 16 and 0xFF
                green += color shr 8 and 0xFF
                blue += color and 0xFF
                samples += 1
            }
            destination[y * width + x] = (0xFF shl 24) or
                ((red / samples) shl 16) or
                ((green / samples) shl 8) or
                (blue / samples)
        }
    }
}
