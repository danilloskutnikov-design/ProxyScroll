from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def patch_file(rel, patches):
    path = ROOT / rel
    text = path.read_text(encoding="utf-8")
    for i, (old, new) in enumerate(patches, 1):
        count = text.count(old)
        if count != 1:
            raise RuntimeError(f"{rel}: patch {i} expected exactly once, found {count}")
        text = text.replace(old, new, 1)
    path.write_text(text, encoding="utf-8")


def patch_after(rel, marker, old, new):
    path = ROOT / rel
    text = path.read_text(encoding="utf-8")
    pos = text.find(marker)
    if pos < 0:
        raise RuntimeError(f"{rel}: marker not found: {marker[:80]}")
    tail = text[pos:]
    count = tail.count(old)
    if count < 1:
        raise RuntimeError(f"{rel}: target after marker not found")
    tail = tail.replace(old, new, 1)
    path.write_text(text[:pos] + tail, encoding="utf-8")


modern = "app/src/main/java/com/proxyscroll/app/ui/ModernPdfReader.kt"
patch_file(modern, [
    (
        "import android.content.Context\n",
        "import android.content.ClipData\nimport android.content.Context\nimport android.content.Intent\n",
    ),
    (
        "import androidx.compose.material.icons.filled.NavigateNext\n",
        "import androidx.compose.material.icons.filled.NavigateNext\nimport androidx.compose.material.icons.filled.Share\n",
    ),
    (
        "internal fun ModernPdfReaderScreen(\n    document: LibraryDocument,\n    quoteCount: Int,\n",
        "internal fun ModernPdfReaderScreen(\n    document: LibraryDocument,\n    quoteCount: Int,\n    initialSourcePage: Int? = null,\n",
    ),
    (
        "                document = document,\n                documentInfo = documentInfo,\n                quoteCount = quoteCount,\n",
        "                document = document,\n                documentInfo = documentInfo,\n                quoteCount = quoteCount,\n                initialSourcePage = initialSourcePage,\n",
    ),
    (
        "private fun ModernPdfReaderReady(\n    document: LibraryDocument,\n    documentInfo: ModernPdfDocumentInfo,\n    quoteCount: Int,\n",
        "private fun ModernPdfReaderReady(\n    document: LibraryDocument,\n    documentInfo: ModernPdfDocumentInfo,\n    quoteCount: Int,\n    initialSourcePage: Int?,\n",
    ),
    (
        "    val initialPage = remember(document.id, initialReaderPages, saved.layoutMode) {\n        val storedVirtual = settingsStore.loadLastVirtualPage(document.id, saved.layoutMode)\n        when {\n            storedVirtual != null && storedVirtual in initialReaderPages.indices -> storedVirtual\n            else -> initialReaderPages.indexOfFirst { it.sourcePage == document.lastPage }\n                .takeIf { it >= 0 } ?: 0\n        }\n    }\n",
        "    val initialPage = remember(\n        document.id,\n        initialReaderPages,\n        saved.layoutMode,\n        initialSourcePage,\n    ) {\n        val storedVirtual = settingsStore.loadLastVirtualPage(document.id, saved.layoutMode)\n        when {\n            initialSourcePage != null -> initialReaderPages\n                .indexOfFirst { it.sourcePage == initialSourcePage }\n                .takeIf { it >= 0 } ?: 0\n            storedVirtual != null && storedVirtual in initialReaderPages.indices -> storedVirtual\n            else -> initialReaderPages.indexOfFirst { it.sourcePage == document.lastPage }\n                .takeIf { it >= 0 } ?: 0\n        }\n    }\n",
    ),
    (
        "                    userScrollEnabled = layoutMode != PdfLayoutMode.ORIGINAL || currentScale <= 1.01f,\n",
        "                    userScrollEnabled = currentScale <= 1.01f,\n",
    ),
    (
        "                onBrightness = { brightness = it.coerceIn(0.25f, 1f) },\n",
        "                onBrightness = { brightness = it.coerceIn(0.25f, 1f) },\n                onShare = { sharePdfDocument(context, document) },\n",
    ),
    (
        "    onBrightness: (Float) -> Unit,\n) {\n    ProxySurface(\n",
        "    onBrightness: (Float) -> Unit,\n    onShare: () -> Unit,\n) {\n    ProxySurface(\n",
    ),
    (
        "                    onBrightness = onBrightness,\n                    onOpenBookmarks = onOpenBookmarks,\n",
        "                    onBrightness = onBrightness,\n                    onOpenBookmarks = onOpenBookmarks,\n                    onShare = onShare,\n",
    ),
    (
        "    onBrightness: (Float) -> Unit,\n    onOpenBookmarks: () -> Unit,\n) {\n    DropdownMenu(\n",
        "    onBrightness: (Float) -> Unit,\n    onOpenBookmarks: () -> Unit,\n    onShare: () -> Unit,\n) {\n    DropdownMenu(\n",
    ),
    (
        "        DropdownMenuItem(\n            text = { Text(\"Закладки · $bookmarkCount\") },\n            leadingIcon = { Icon(Icons.Default.Bookmarks, contentDescription = null) },\n            onClick = onOpenBookmarks,\n        )\n",
        "        DropdownMenuItem(\n            text = { Text(\"Закладки · $bookmarkCount\") },\n            leadingIcon = { Icon(Icons.Default.Bookmarks, contentDescription = null) },\n            onClick = onOpenBookmarks,\n        )\n        HorizontalDivider()\n        DropdownMenuItem(\n            text = { Text(\"Экспорт / поделиться PDF\") },\n            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },\n            onClick = {\n                onDismiss()\n                onShare()\n            },\n        )\n",
    ),
    (
        "            mode == PdfLayoutMode.ORIGINAL -> ZoomablePdfRegion(\n                region = render.regions.first(),\n                readingProfile = readingProfile,\n                pageWidth = pageWidth,\n                resetZoomToken = resetZoomToken,\n                onScaleChanged = onScaleChanged,\n                onInteractionChanged = onInteractionChanged,\n            )\n            else -> SmartPagedRegions(\n                virtualIndex = virtualIndex,\n                regions = render.regions,\n                readingProfile = readingProfile,\n                pageWidth = pageWidth,\n                onInteractionChanged = if (isCurrent) onInteractionChanged else { _ -> },\n            )\n",
        "            else -> ZoomablePdfRegion(\n                region = render.regions.first(),\n                readingProfile = readingProfile,\n                pageWidth = pageWidth,\n                resetZoomToken = resetZoomToken,\n                onScaleChanged = onScaleChanged,\n                onInteractionChanged = onInteractionChanged,\n            )\n",
    ),
    (
        "        val splitLandscape = layoutMode == PdfLayoutMode.SMART_CROP &&\n            documentInfo.portraitDominant &&\n            source.aspectRatio >= 1.15f\n",
        "        val splitLandscape = layoutMode == PdfLayoutMode.SMART_CROP &&\n            source.aspectRatio >= 1.18f\n",
    ),
])

patch_after(
    modern,
    "private fun ContinuousPdfPage(",
    "                    PdfRegionImage(\n                        region = region,\n                        readingProfile = readingProfile,\n                        pageWidth = pageWidth,\n                        contentDescription = \"Страница ${virtualIndex + 1}, фрагмент ${index + 1}\",\n                    )\n",
    "                    ZoomableContinuousPdfRegion(\n                        region = region,\n                        readingProfile = readingProfile,\n                        pageWidth = pageWidth,\n                        contentDescription = \"Страница ${virtualIndex + 1}, фрагмент ${index + 1}\",\n                    )\n",
)

patch_file(modern, [
    (
        "@Composable\nprivate fun PdfRegionImage(\n",
        """@Composable
private fun ZoomableContinuousPdfRegion(
    region: SmartPdfRegionImage,
    readingProfile: PdfReadingProfile,
    pageWidth: Float,
    contentDescription: String,
) {
    var scale by remember(region.image.width, region.image.height) { mutableFloatStateOf(1f) }
    var offset by remember(region.image.width, region.image.height) { mutableStateOf(Offset.Zero) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth(pageWidth.coerceIn(0.84f, 1f))
            .aspectRatio(region.aspectRatio.coerceAtLeast(0.1f))
            .clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        val boxWidth = constraints.maxWidth.coerceAtLeast(1).toFloat()
        val boxHeight = constraints.maxHeight.coerceAtLeast(1).toFloat()
        Image(
            bitmap = region.image,
            contentDescription = contentDescription,
            contentScale = ContentScale.FillWidth,
            colorFilter = pdfReadingColorFilter(readingProfile),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(2.dp),
                    clip = false,
                )
                .clip(RoundedCornerShape(2.dp))
                .pointerInput(region.image.width, region.image.height) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        var transformed = false
                        while (true) {
                            val event = awaitPointerEvent()
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()
                            val transform = event.changes.size > 1 || scale > 1.01f
                            if (transform) {
                                transformed = true
                                val nextScale = (scale * zoom).coerceIn(1f, 5f)
                                val maxX = boxWidth * (nextScale - 1f) * 0.5f
                                val maxY = boxHeight * (nextScale - 1f) * 0.5f
                                scale = nextScale
                                offset = if (nextScale <= 1.01f) {
                                    Offset.Zero
                                } else {
                                    Offset(
                                        (offset.x + pan.x).coerceIn(-maxX, maxX),
                                        (offset.y + pan.y).coerceIn(-maxY, maxY),
                                    )
                                }
                                event.changes.forEach { it.consume() }
                            }
                            if (event.changes.none { it.pressed }) break
                        }
                        if (!transformed && scale <= 1.01f) offset = Offset.Zero
                    }
                },
        )
    }
}

@Composable
private fun PdfRegionImage(
""",
    ),
    (
        "}.getOrElse { error ->\n    ModernPdfDocumentInfo(error = error.message ?: \"Не удалось открыть документ\")\n}\n",
        """}.getOrElse { error ->
    ModernPdfDocumentInfo(error = error.message ?: "Не удалось открыть документ")
}

private fun sharePdfDocument(context: Context, document: LibraryDocument) {
    runCatching {
        val uri = Uri.parse(document.uri)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri(document.sourceTitle, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Экспорт PDF"))
    }
}
""",
    ),
])


tactile = "app/src/main/java/com/proxyscroll/app/ui/TactileLibraryScreen.kt"
patch_file(tactile, [
    (
        "import android.content.Intent\n",
        "import android.content.ClipData\nimport android.content.Intent\n",
    ),
    (
        "import androidx.compose.material.icons.filled.Search\n",
        "import androidx.compose.material.icons.filled.Search\nimport androidx.compose.material.icons.filled.Share\n",
    ),
    (
        "    onOpenDocument: (LibraryDocument) -> Unit,\n",
        "    onOpenDocument: (LibraryDocument) -> Unit,\n    onOpenQuote: (BookQuote) -> Unit,\n",
    ),
    (
        "    onUpdateQuote: (BookQuote, String, String) -> Unit,\n",
        "    onUpdateQuote: (BookQuote, String, String, String) -> Unit,\n",
    ),
    (
        "            onSave = { excerpt, note ->\n                onUpdateQuote(quote, excerpt, note)\n",
        "            onSave = { title, excerpt, note ->\n                onUpdateQuote(quote, title, excerpt, note)\n",
    ),
    (
        "                            QuoteCarousel(\n                                quotes = state.visibleQuotes,\n                                documents = state.documents,\n                                onEdit = { editingQuote = it },\n                            )\n",
        "                            QuoteCarousel(\n                                quotes = state.visibleQuotes,\n                                documents = state.documents,\n                                onOpen = onOpenQuote,\n                                onEdit = { editingQuote = it },\n                            )\n",
    ),
    (
        "private fun QuoteCarousel(\n    quotes: List<BookQuote>,\n    documents: List<LibraryDocument>,\n    onEdit: (BookQuote) -> Unit,\n) {\n",
        "private fun QuoteCarousel(\n    quotes: List<BookQuote>,\n    documents: List<LibraryDocument>,\n    onOpen: (BookQuote) -> Unit,\n    onEdit: (BookQuote) -> Unit,\n) {\n",
    ),
    (
        "            QuoteCard(\n                quote = quote,\n                document = document,\n                onClick = { onEdit(quote) },\n            )\n",
        "            QuoteCard(\n                quote = quote,\n                document = document,\n                onOpen = { onOpen(quote) },\n                onEdit = { onEdit(quote) },\n            )\n",
    ),
    (
        "@Composable\nprivate fun QuoteCard(\n    quote: BookQuote,\n    document: LibraryDocument?,\n    onClick: () -> Unit,\n) {\n",
        "@OptIn(ExperimentalFoundationApi::class)\n@Composable\nprivate fun QuoteCard(\n    quote: BookQuote,\n    document: LibraryDocument?,\n    onOpen: () -> Unit,\n    onEdit: () -> Unit,\n) {\n",
    ),
    (
        "            .heightIn(min = 174.dp)\n            .clickable(onClick = onClick),\n",
        "            .heightIn(min = 174.dp)\n            .combinedClickable(\n                onClick = onOpen,\n                onLongClick = onEdit,\n            ),\n",
    ),
    (
        "        Column(modifier = Modifier.padding(12.dp)) {\n            Row(verticalAlignment = Alignment.Top) {\n",
        "        Column(modifier = Modifier.padding(12.dp)) {\n            if (quote.title.isNotBlank()) {\n                Text(\n                    text = quote.title,\n                    style = MaterialTheme.typography.titleSmall,\n                    fontWeight = FontWeight.SemiBold,\n                    maxLines = 1,\n                    overflow = TextOverflow.Ellipsis,\n                )\n                Spacer(Modifier.height(7.dp))\n            }\n            Row(verticalAlignment = Alignment.Top) {\n",
    ),
    (
        "                        \"стр. ${quote.page + 1} · нажмите, чтобы изменить\",\n",
        "                        \"стр. ${quote.page + 1} · тап — открыть · удержать — изменить\",\n",
    ),
    (
        "                Spacer(Modifier.height(12.dp))\n                TextButton(onClick = onDelete) {\n",
        "                Spacer(Modifier.height(12.dp))\n                OutlinedButton(onClick = { shareLibraryPdf(context, document) }) {\n                    Icon(Icons.Default.Share, contentDescription = null)\n                    Spacer(Modifier.width(7.dp))\n                    Text(\"Экспорт / поделиться PDF\")\n                }\n                Spacer(Modifier.height(6.dp))\n                TextButton(onClick = onDelete) {\n",
    ),
    (
        "private fun BookQuoteEditDialog(\n    quote: BookQuote,\n    document: LibraryDocument?,\n    onDismiss: () -> Unit,\n    onSave: (String, String) -> Unit,\n    onDelete: () -> Unit,\n) {\n    var excerpt by remember(quote.id) { mutableStateOf(quote.excerpt) }\n",
        "private fun BookQuoteEditDialog(\n    quote: BookQuote,\n    document: LibraryDocument?,\n    onDismiss: () -> Unit,\n    onSave: (String, String, String) -> Unit,\n    onDelete: () -> Unit,\n) {\n    var title by remember(quote.id) { mutableStateOf(quote.title) }\n    var excerpt by remember(quote.id) { mutableStateOf(quote.excerpt) }\n",
    ),
    (
        "                Text(\n                    \"Страница ${quote.page + 1}\",\n                    style = MaterialTheme.typography.labelLarge,\n                    color = MaterialTheme.colorScheme.primary,\n                )\n                OutlinedTextField(\n                    value = excerpt,\n",
        "                Text(\n                    \"Страница ${quote.page + 1}\",\n                    style = MaterialTheme.typography.labelLarge,\n                    color = MaterialTheme.colorScheme.primary,\n                )\n                OutlinedTextField(\n                    value = title,\n                    onValueChange = { title = it },\n                    label = { Text(\"Название заметки\") },\n                    singleLine = true,\n                    modifier = Modifier.fillMaxWidth(),\n                )\n                OutlinedTextField(\n                    value = excerpt,\n",
    ),
    (
        "                onClick = { onSave(excerpt, note) },\n",
        "                onClick = { onSave(title, excerpt, note) },\n",
    ),
])

patch_file(tactile, [
    (
        "private fun tactilePdfDisplayName(\n    context: android.content.Context,\n    uri: Uri,\n): String {\n",
        """private fun shareLibraryPdf(context: android.content.Context, document: LibraryDocument) {
    runCatching {
        val uri = Uri.parse(document.uri)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri(document.sourceTitle, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Экспорт PDF"))
    }
}

private fun tactilePdfDisplayName(
    context: android.content.Context,
    uri: Uri,
): String {
""",
    ),
])


app = "app/src/main/java/com/proxyscroll/app/ui/ProxyScrollApp.kt"
patch_file(app, [
    (
        "    activeGroupFilter: String?,\n    onActiveGroupFilterChanged: (String?) -> Unit,\n) {\n",
        "    activeGroupFilter: String?,\n    onActiveGroupFilterChanged: (String?) -> Unit,\n    incomingPdfUri: String? = null,\n    incomingPdfTitle: String? = null,\n    onIncomingPdfConsumed: () -> Unit = {},\n) {\n",
    ),
    (
        "    var openPdfDocument by remember { mutableStateOf<LibraryDocument?>(null) }\n",
        "    var openPdfDocument by remember { mutableStateOf<LibraryDocument?>(null) }\n    var pdfInitialSourcePage by remember { mutableStateOf<Int?>(null) }\n",
    ),
    (
        "    var scrollingQuiet by remember { mutableStateOf(false) }\n    val motionCompensation = rememberMotionCompensationState(\n",
        "    var scrollingQuiet by remember { mutableStateOf(false) }\n\n    LaunchedEffect(incomingPdfUri) {\n        val uri = incomingPdfUri?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect\n        val document = libraryViewModel.importPdf(\n            uri = uri,\n            title = incomingPdfTitle.orEmpty().ifBlank { \"PDF-документ\" },\n        )\n        showLibrary = true\n        openPdfDocument = document\n        pdfInitialSourcePage = null\n        pdfReaderOpen = true\n        onIncomingPdfConsumed()\n    }\n\n    val motionCompensation = rememberMotionCompensationState(\n",
    ),
    (
        "                                    it.documentId == document.id\n                                },\n                                onBack = {\n",
        "                                    it.documentId == document.id\n                                },\n                                initialSourcePage = pdfInitialSourcePage,\n                                onBack = {\n",
    ),
    (
        "                                    pdfReaderOpen = false\n                                    openPdfDocument = null\n",
        "                                    pdfReaderOpen = false\n                                    openPdfDocument = null\n                                    pdfInitialSourcePage = null\n",
    ),
    (
        "                            onImport = { uri, title ->\n                                val document = libraryViewModel.importPdf(uri, title)\n                                openPdfDocument = document\n                                pdfReaderOpen = true\n                            },\n                            onOpenDocument = { document ->\n                                openPdfDocument = document\n                                pdfReaderOpen = true\n                            },\n",
        "                            onImport = { uri, title ->\n                                val document = libraryViewModel.importPdf(uri, title)\n                                openPdfDocument = document\n                                pdfInitialSourcePage = null\n                                pdfReaderOpen = true\n                            },\n                            onOpenDocument = { document ->\n                                openPdfDocument = document\n                                pdfInitialSourcePage = null\n                                pdfReaderOpen = true\n                            },\n                            onOpenQuote = { quote ->\n                                val document = libraryState.documents.firstOrNull {\n                                    it.id == quote.documentId\n                                }\n                                if (document != null) {\n                                    openPdfDocument = document\n                                    pdfInitialSourcePage = quote.page\n                                    pdfReaderOpen = true\n                                }\n                            },\n",
    ),
])

print("Alpha 41 patches applied")
