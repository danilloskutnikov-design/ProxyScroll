package com.proxyscroll.app.ui

import android.content.ClipData
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proxyscroll.app.domain.BookQuote
import com.proxyscroll.app.domain.LibraryCoverStyle
import com.proxyscroll.app.domain.LibraryDocument
import com.proxyscroll.app.domain.LibraryReadingStatus
import com.proxyscroll.app.ui.theme.rememberGlassBackdrop
import com.proxyscroll.app.ui.theme.glassBackdropSource
import com.proxyscroll.app.ui.theme.ProxyInsetSurface
import com.proxyscroll.app.ui.theme.ProxySurface
import com.proxyscroll.app.ui.theme.ProxySurfaceRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private data class LibraryCoverColor(
    val label: String,
    val argb: Long,
)

private val libraryCoverColors = listOf(
    LibraryCoverColor("Чернила", 0xFF293449L),
    LibraryCoverColor("Слива", 0xFF4A344FL),
    LibraryCoverColor("Коралл", 0xFF6B3C38L),
    LibraryCoverColor("Хвоя", 0xFF234542L),
    LibraryCoverColor("Пергамент", 0xFF887968L),
    LibraryCoverColor("Ночь", 0xFF263451L),
    LibraryCoverColor("Графит", 0xFF34343CL),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TactileLibraryScreen(
    state: LibraryUiState,
    onOpenNotes: () -> Unit,
    onOpenSettings: () -> Unit,
    onQueryChange: (String) -> Unit,
    onFilterChange: (LibraryFilter) -> Unit,
    onImport: (String, String) -> Unit,
    onOpenDocument: (LibraryDocument) -> Unit,
    onOpenQuote: (BookQuote) -> Unit,
    onEditDocument: (
        LibraryDocument,
        String,
        String,
        LibraryCoverStyle,
        Long,
        String?,
        LibraryReadingStatus,
    ) -> Unit,
    onUpdateQuote: (BookQuote, String, String, String) -> Unit,
    onDeleteQuote: (BookQuote) -> Unit,
    onDelete: (LibraryDocument) -> Unit,
) {
    val glassBackdrop = rememberGlassBackdrop()
    val context = LocalContext.current
    var searchExpanded by remember { mutableStateOf(false) }
    var filtersExpanded by remember { mutableStateOf(state.filter != LibraryFilter.ALL) }
    var editingDocument by remember { mutableStateOf<LibraryDocument?>(null) }
    var editingQuote by remember { mutableStateOf<BookQuote?>(null) }
    var pendingDocumentDelete by remember { mutableStateOf<LibraryDocument?>(null) }
    var pendingQuoteDelete by remember { mutableStateOf<BookQuote?>(null) }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            onImport(uri.toString(), tactilePdfDisplayName(context, uri))
        }
    }
    val launchImport = { importLauncher.launch(arrayOf("application/pdf")) }
    val continueDocument = remember(state.documents) {
        state.documents.firstOrNull {
            it.readingStatus == LibraryReadingStatus.READING && it.pageCount > 0
        } ?: state.documents.firstOrNull()
    }

    BackHandler {
        if (searchExpanded || state.query.isNotBlank()) {
            searchExpanded = false
            onQueryChange("")
        } else if (filtersExpanded) {
            filtersExpanded = false
        } else {
            onOpenNotes()
        }
    }

    editingDocument?.let { document ->
        BookAppearanceDialog(
            document = document,
            onDismiss = { editingDocument = null },
            onSave = { title, author, style, color, imageUri, readingStatus ->
                onEditDocument(
                    document,
                    title,
                    author,
                    style,
                    color,
                    imageUri,
                    readingStatus,
                )
                editingDocument = null
            },
            onDelete = {
                editingDocument = null
                pendingDocumentDelete = document
            },
        )
    }

    editingQuote?.let { quote ->
        BookQuoteEditDialog(
            quote = quote,
            document = state.documents.firstOrNull { it.id == quote.documentId },
            onDismiss = { editingQuote = null },
            onSave = { title, excerpt, note ->
                onUpdateQuote(quote, title, excerpt, note)
                editingQuote = null
            },
            onDelete = {
                editingQuote = null
                pendingQuoteDelete = quote
            },
        )
    }

    pendingDocumentDelete?.let { document ->
        AlertDialog(
            onDismissRequest = { pendingDocumentDelete = null },
            title = { Text("Убрать книгу с полки?") },
            text = {
                Text(
                    "Файл останется на устройстве. ProxyScroll удалит оформление, " +
                        "прогресс и связанные цитаты.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(document)
                        pendingDocumentDelete = null
                    },
                ) {
                    Text("Убрать", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDocumentDelete = null }) { Text("Отмена") }
            },
        )
    }

    pendingQuoteDelete?.let { quote ->
        AlertDialog(
            onDismissRequest = { pendingQuoteDelete = null },
            title = { Text("Удалить цитату?") },
            text = { Text("Цитата и комментарий будут удалены из библиотеки.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteQuote(quote)
                        pendingQuoteDelete = null
                    },
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingQuoteDelete = null }) { Text("Отмена") }
            },
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            MainSectionBar(
                backdrop = glassBackdrop,
                notesSelected = false,
                onOpenNotes = onOpenNotes,
                onOpenLibrary = {},
                onPrimaryAction = launchImport,
                primaryActionDescription = "Импортировать PDF",
                onOpenSettings = onOpenSettings,
                searchSelected = searchExpanded || state.query.isNotBlank(),
                onOpenSearch = { searchExpanded = true },
            )
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding())
                .statusBarsPadding()
                .glassBackdropSource(glassBackdrop),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = contentPadding.calculateBottomPadding() + 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                LibraryHeader(
                    documentCount = state.documents.size,
                    searchExpanded = searchExpanded,
                    query = state.query,
                    onSearchToggle = {
                        searchExpanded = !searchExpanded
                        if (!searchExpanded) onQueryChange("")
                    },
                    onQueryChange = onQueryChange,
                    filtersActive = filtersExpanded || state.filter != LibraryFilter.ALL,
                    onFiltersToggle = { filtersExpanded = !filtersExpanded },
                )
            }

                if (filtersExpanded || state.filter != LibraryFilter.ALL) {
                    item {
                        ReadingStatusRail(
                            selected = state.filter,
                            onSelected = onFilterChange,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                    }
                }

            if (state.documents.isEmpty()) {
                item {
                    EmptyTactileLibrary(onImport = launchImport)
                }
            } else {
                if (state.query.isBlank() && state.filter == LibraryFilter.ALL) {
                    continueDocument?.let { document ->
                        item {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                LibrarySectionTitle(
                                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                                    title = "Продолжить чтение",
                                    detail = null,
                                )
                                ContinueReadingShelf(
                                    document = document,
                                    quoteCount = state.quotes.count {
                                        it.documentId == document.id
                                    },
                                    onOpen = { onOpenDocument(document) },
                                    onEdit = { editingDocument = document },
                                )
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LibrarySectionTitle(
                            icon = { Icon(Icons.Default.Bookmarks, contentDescription = null) },
                            title = "Моя полка",
                            detail = "${state.visibleDocuments.size} книг",
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                        if (state.visibleDocuments.isEmpty()) {
                            EmptyLibraryResult(modifier = Modifier.padding(horizontal = 12.dp))
                        } else {
                            BookshelfRow(
                                documents = state.visibleDocuments,
                                quotes = state.quotes,
                                onOpenDocument = onOpenDocument,
                                onEditDocument = { editingDocument = it },
                            )
                        }
                    }
                }



                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LibrarySectionTitle(
                            icon = { Icon(Icons.Default.FormatQuote, contentDescription = null) },
                            title = "Цитаты и заметки",
                            detail = if (state.visibleQuotes.isEmpty()) null else {
                                state.visibleQuotes.size.toString()
                            },
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                        if (state.visibleQuotes.isEmpty()) {
                            EmptyBookQuotes(modifier = Modifier.padding(horizontal = 12.dp))
                        } else {
                            QuoteCarousel(
                                quotes = state.visibleQuotes,
                                documents = state.documents,
                                onOpen = onOpenQuote,
                                onEdit = { editingQuote = it },
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun LibraryHeader(
    documentCount: Int,
    searchExpanded: Boolean,
    query: String,
    onSearchToggle: () -> Unit,
    onQueryChange: (String) -> Unit,
    filtersActive: Boolean,
    onFiltersToggle: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "ProxyScroll",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Библиотека",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = if (documentCount == 0) {
                        "Личное пространство для чтения"
                    } else {
                        "$documentCount ${tactileDocumentCountLabel(documentCount)} · всегда под рукой"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onSearchToggle) {
                Icon(
                    if (searchExpanded) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (searchExpanded) "Закрыть поиск" else "Поиск",
                    modifier = Modifier.size(24.dp),
                )
            }
            IconButton(onClick = onFiltersToggle) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = "Фильтры",
                    tint = if (filtersActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
        if (searchExpanded || query.isNotBlank()) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (query.isNotBlank()) {
                    {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Очистить")
                        }
                    }
                } else null,
                placeholder = { Text("Название или автор") },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.46f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.28f),
                ),
            )
        }
    }
}

@Composable
private fun LibrarySectionTitle(
    icon: @Composable () -> Unit,
    title: String,
    detail: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.primary,
                content = icon,
            )
        }
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        detail?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ContinueReadingShelf(
    document: LibraryDocument,
    quoteCount: Int,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
) {
    val progress = tactileLibraryProgress(document)
    Column {
        ProxySurface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onOpen),
            role = ProxySurfaceRole.CARD,
            strong = true,
            interactive = true,
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                TactileBookCover(
                    document = document,
                    quoteCount = quoteCount,
                    modifier = Modifier
                        .width(96.dp)
                        .height(142.dp)
                        .shadow(16.dp, RoundedCornerShape(5.dp)),
                )
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = document.readingStatus.tactileStatusLabel().uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = document.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Оформление книги")
                        }
                    }
                    if (document.author.isNotBlank()) {
                        Text(
                            text = document.author,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${(progress * 100).roundToInt()}%",
                        fontSize = 25.sp,
                        color = tactileCoverAccent(document),
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(tactileCoverAccent(document)),
                        )
                    }
                    Spacer(Modifier.height(7.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tactileLibraryPosition(document),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        if (quoteCount > 0) {
                            ProxyInsetSurface(
                                modifier = Modifier.height(36.dp),
                                role = ProxySurfaceRole.BUTTON,
                                selected = false,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    Icon(
                                        Icons.Default.Bookmarks,
                                        contentDescription = null,
                                        modifier = Modifier.size(17.dp),
                                    )
                                    Text(quoteCount.toString(), style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
        }
        ShelfPlinth(accent = tactileCoverAccent(document))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookshelfRow(
    documents: List<LibraryDocument>,
    quotes: List<BookQuote>,
    onOpenDocument: (LibraryDocument) -> Unit,
    onEditDocument: (LibraryDocument) -> Unit,
) {
    Column {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            items(documents, key = { it.id }) { document ->
                val quoteCount = quotes.count { it.documentId == document.id }
                Column(
                    modifier = Modifier
                        .width(126.dp)
                        .combinedClickable(
                            onClick = { onOpenDocument(document) },
                            onLongClick = { onEditDocument(document) },
                        ),
                ) {
                    TactileBookCover(
                        document = document,
                        quoteCount = quoteCount,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(178.dp)
                            .shadow(14.dp, RoundedCornerShape(5.dp)),
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${(tactileLibraryProgress(document) * 100).roundToInt()}%",
                            style = MaterialTheme.typography.titleSmall,
                            color = tactileCoverAccent(document),
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            document.readingStatus.tactileStatusLabel(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        ShelfPlinth(
            accent = if (documents.isEmpty()) {
                MaterialTheme.colorScheme.primary
            } else {
                tactileCoverAccent(documents.first())
            },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
}

@Composable
private fun ShelfPlinth(
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            accent.copy(alpha = 0.72f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.48f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(13.dp)
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.66f),
                            Color.Black.copy(alpha = 0.46f),
                        ),
                    ),
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.20f),
                    RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                ),
        )
    }
}

@Composable
private fun ReadingStatusRail(
    selected: LibraryFilter,
    onSelected: (LibraryFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.30f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                RoundedCornerShape(18.dp),
            )
            .horizontalScroll(rememberScrollState())
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        LibraryFilter.entries.forEach { filter ->
            val active = filter == selected
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (active) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        } else {
                            Color.Transparent
                        },
                    )
                    .border(
                        1.dp,
                        if (active) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.48f)
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                        },
                        RoundedCornerShape(14.dp),
                    )
                    .clickable { onSelected(filter) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                when (filter) {
                    LibraryFilter.ALL -> Icon(Icons.Default.MenuBook, null, Modifier.size(17.dp))
                    LibraryFilter.READING -> Icon(Icons.Default.Bookmarks, null, Modifier.size(17.dp))
                    LibraryFilter.WANT_TO_READ -> Icon(Icons.Default.Bookmark, null, Modifier.size(17.dp))
                    LibraryFilter.COMPLETED -> Icon(Icons.Default.Check, null, Modifier.size(17.dp))
                }
                Text(
                    filter.tactileFilterLabel(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (active) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun QuoteCarousel(
    quotes: List<BookQuote>,
    documents: List<LibraryDocument>,
    onOpen: (BookQuote) -> Unit,
    onEdit: (BookQuote) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(quotes, key = { it.id }) { quote ->
            val document = documents.firstOrNull { it.id == quote.documentId }
            QuoteCard(
                quote = quote,
                document = document,
                onOpen = { onOpen(quote) },
                onEdit = { onEdit(quote) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuoteCard(
    quote: BookQuote,
    document: LibraryDocument?,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
) {
    val accent = document?.let(::tactileCoverAccent) ?: MaterialTheme.colorScheme.primary
    ProxySurface(
        modifier = Modifier
            .width(252.dp)
            .heightIn(min = 174.dp)
            .combinedClickable(
                onClick = onOpen,
                onLongClick = onEdit,
            ),
        role = ProxySurfaceRole.CARD,
        strong = true,
        interactive = false,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (quote.title.isNotBlank()) {
                Text(
                    text = quote.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(7.dp))
            }
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier
                        .width(3.dp)
                        .height(94.dp)
                        .clip(CircleShape)
                        .background(accent),
                )
                Spacer(Modifier.width(10.dp))
                Icon(
                    Icons.Default.FormatQuote,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = quote.excerpt.ifBlank { quote.note },
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 21.sp,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            if (quote.excerpt.isNotBlank() && quote.note.isNotBlank()) {
                Text(
                    text = quote.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 13.dp, top = 7.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
            Spacer(Modifier.height(7.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (document != null) {
                    TactileBookCover(
                        document = document,
                        quoteCount = 0,
                        showProgress = false,
                        modifier = Modifier
                            .width(34.dp)
                            .height(48.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        document?.title ?: "Документ",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "стр. ${quote.page + 1} · тап — открыть · удержать — изменить",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun EmptyBookQuotes(modifier: Modifier = Modifier) {
    ProxySurface(
        modifier = modifier.fillMaxWidth(),
        role = ProxySurfaceRole.CARD,
        interactive = false,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ProxyInsetSurface(
                modifier = Modifier.size(42.dp),
                role = ProxySurfaceRole.BUTTON,
                selected = false,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Column {
                Text("Сохраните первую мысль", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Откройте PDF и нажмите значок цитаты — страница привяжется автоматически.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptyTactileLibrary(onImport: () -> Unit) {
    ProxySurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        role = ProxySurfaceRole.CARD,
        strong = true,
        interactive = false,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Default.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(10.dp))
            Text("Соберите свою первую полку", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(7.dp))
            Text(
                "Импортируйте PDF, выберите материал обложки и сохраняйте мысли " +
                    "с привязкой к странице.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(14.dp))
            OutlinedButton(onClick = onImport) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Импортировать PDF")
            }
        }
    }
}

@Composable
private fun EmptyLibraryResult(modifier: Modifier = Modifier) {
    ProxySurface(
        modifier = modifier.fillMaxWidth(),
        role = ProxySurfaceRole.CARD,
        interactive = false,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(38.dp))
            Spacer(Modifier.height(8.dp))
            Text("На этой полке ничего не найдено", style = MaterialTheme.typography.titleMedium)
            Text(
                "Измените запрос или статус чтения",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BookAppearanceDialog(
    document: LibraryDocument,
    onDismiss: () -> Unit,
    onSave: (
        String,
        String,
        LibraryCoverStyle,
        Long,
        String?,
        LibraryReadingStatus,
    ) -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    var title by remember(document.id) { mutableStateOf(document.title) }
    var author by remember(document.id) { mutableStateOf(document.author) }
    var coverStyle by remember(document.id) { mutableStateOf(document.coverStyle) }
    var coverColor by remember(document.id) { mutableStateOf(document.coverColorArgb) }
    var coverImageUri by remember(document.id) { mutableStateOf(document.coverImageUri) }
    var readingStatus by remember(document.id) { mutableStateOf(document.readingStatus) }
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            coverImageUri = uri.toString()
        }
    }
    val preview = document.copy(
        title = title.ifBlank { document.sourceTitle },
        author = author,
        coverStyle = coverStyle,
        coverColorArgb = coverColor,
        coverImageUri = coverImageUri,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Palette, contentDescription = null) },
        title = { Text("Оформление книги") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(min = 280.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TactileBookCover(
                        document = preview,
                        quoteCount = 0,
                        showProgress = false,
                        modifier = Modifier
                            .width(86.dp)
                            .height(124.dp),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Отображаемое название") },
                            singleLine = false,
                            maxLines = 2,
                        )
                        OutlinedTextField(
                            value = author,
                            onValueChange = { author = it },
                            label = { Text("Автор / подпись") },
                            singleLine = true,
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text("Материал обложки", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LibraryCoverStyle.entries.forEach { style ->
                        val selected = style == coverStyle
                        Text(
                            text = style.tactileCoverStyleLabel(),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f)
                                    },
                                )
                                .border(
                                    1.dp,
                                    if (selected) MaterialTheme.colorScheme.primary else {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)
                                    },
                                    RoundedCornerShape(12.dp),
                                )
                                .clickable { coverStyle = style }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) MaterialTheme.colorScheme.primary else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text("Цвет", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    libraryCoverColors.forEach { preset ->
                        val selected = preset.argb == coverColor
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(preset.argb.toInt()))
                                .border(
                                    if (selected) 3.dp else 1.dp,
                                    if (selected) MaterialTheme.colorScheme.primary else {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.46f)
                                    },
                                    CircleShape,
                                )
                                .clickable { coverColor = preset.argb },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = preset.label,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text("Статус чтения", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LibraryReadingStatus.entries.forEach { status ->
                        val selected = status == readingStatus
                        Text(
                            text = status.tactileStatusLabel(),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f)
                                    },
                                )
                                .border(
                                    1.dp,
                                    if (selected) MaterialTheme.colorScheme.primary else {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)
                                    },
                                    RoundedCornerShape(12.dp),
                                )
                                .clickable { readingStatus = status }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(onClick = { imageLauncher.launch(arrayOf("image/*")) }) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(Modifier.width(7.dp))
                        Text(if (coverImageUri == null) "Выбрать изображение" else "Заменить изображение")
                    }
                    if (coverImageUri != null) {
                        TextButton(onClick = { coverImageUri = null }) { Text("Убрать") }
                    }
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    "Исходный файл: ${document.sourceTitle}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { shareLibraryPdf(context, document) }) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(7.dp))
                    Text("Экспорт / поделиться PDF")
                }
                Spacer(Modifier.height(6.dp))
                TextButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Убрать из библиотеки", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() || document.sourceTitle.isNotBlank(),
                onClick = {
                    onSave(
                        title,
                        author,
                        coverStyle,
                        coverColor,
                        coverImageUri,
                        readingStatus,
                    )
                },
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun BookQuoteEditDialog(
    quote: BookQuote,
    document: LibraryDocument?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
    onDelete: () -> Unit,
) {
    var title by remember(quote.id) { mutableStateOf(quote.title) }
    var excerpt by remember(quote.id) { mutableStateOf(quote.excerpt) }
    var note by remember(quote.id) { mutableStateOf(quote.note) }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.FormatQuote, contentDescription = null) },
        title = { Text(document?.title ?: "Цитата") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Страница ${quote.page + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название заметки") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = excerpt,
                    onValueChange = { excerpt = it },
                    label = { Text("Цитата") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Комментарий") },
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = excerpt.isNotBlank() || note.isNotBlank(),
                onClick = { onSave(title, excerpt, note) },
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun TactileBookCover(
    document: LibraryDocument,
    quoteCount: Int,
    modifier: Modifier = Modifier,
    showProgress: Boolean = true,
) {
    val context = LocalContext.current
    val coverImage by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = document.coverImageUri,
    ) {
        value = document.coverImageUri?.let { uri ->
            withContext(Dispatchers.IO) {
                decodeLibraryCover(context, Uri.parse(uri))
            }
        }
    }
    val base = Color(document.coverColorArgb.toInt())
    val foreground = if (base.luminance() > 0.54f && coverImage == null) {
        Color(0xFF19171BL)
    } else {
        Color(0xFFF4EFE8L)
    }
    val shape = RoundedCornerShape(topStart = 4.dp, topEnd = 9.dp, bottomEnd = 5.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        base.copy(red = (base.red * 1.16f).coerceAtMost(1f)),
                        base,
                        base.copy(red = base.red * 0.72f, green = base.green * 0.72f),
                    ),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.18f), shape),
    ) {
        coverImage?.let { image ->
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.04f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.78f),
                            ),
                        ),
                    ),
            )
        }

        BookMaterialArtwork(
            document = document,
            base = base,
            imageVisible = coverImage != null,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(7.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.56f),
                            base.copy(alpha = 0.96f),
                            Color.White.copy(alpha = 0.16f),
                            Color.Black.copy(alpha = 0.24f),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 15.dp, top = 11.dp, end = 10.dp, bottom = 11.dp),
        ) {
            Text(
                "PDF",
                style = MaterialTheme.typography.labelSmall,
                color = foreground.copy(alpha = 0.72f),
                letterSpacing = 1.1.sp,
            )
            Spacer(Modifier.weight(1f))
            Text(
                document.title,
                style = MaterialTheme.typography.titleSmall,
                fontFamily = if (document.coverStyle == LibraryCoverStyle.CLASSIC) {
                    FontFamily.Serif
                } else {
                    FontFamily.Default
                },
                fontWeight = FontWeight.SemiBold,
                color = if (coverImage == null) foreground else Color.White,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
            if (document.author.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    document.author,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (coverImage == null) {
                        foreground.copy(alpha = 0.68f)
                    } else {
                        Color.White.copy(alpha = 0.76f)
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (document.pageCount > 0) {
                    "${document.lastPage + 1} / ${document.pageCount}"
                } else {
                    "НА ПОЛКЕ"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (coverImage == null) foreground.copy(alpha = 0.62f) else {
                    Color.White.copy(alpha = 0.70f)
                },
            )
        }

        if (document.readingStatus == LibraryReadingStatus.READING) {
            Canvas(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(21.dp)
                    .height(39.dp),
            ) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height)
                    lineTo(size.width / 2f, size.height * 0.76f)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, Color(0xFFEF6258))
                drawLine(
                    Color.White.copy(alpha = 0.42f),
                    Offset(2f, 0f),
                    Offset(2f, size.height * 0.86f),
                    1f,
                )
            }
        }

        if (quoteCount > 0) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.62f))
                    .padding(horizontal = 5.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Icon(
                    Icons.Default.Bookmarks,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.88f),
                    modifier = Modifier.size(11.dp),
                )
                Text(
                    quoteCount.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        if (showProgress) {
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(tactileLibraryProgress(document))
                    .height(3.dp)
                    .background(tactileCoverAccent(document)),
            )
        }
    }
}

@Composable
private fun BookMaterialArtwork(
    document: LibraryDocument,
    base: Color,
    imageVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    val seed = document.id.hashCode().toLong()
    Canvas(modifier = modifier) {
        if (imageVisible) {
            drawRect(Color.Black.copy(alpha = 0.08f))
            return@Canvas
        }
        when (document.coverStyle) {
            LibraryCoverStyle.CLASSIC -> {
                val inset = size.minDimension * 0.09f
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.24f),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2f, size.height - inset * 2f),
                    cornerRadius = CornerRadius(7f, 7f),
                    style = Stroke(width = 1.4f),
                )
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.24f),
                    topLeft = Offset(inset + 4f, inset + 4f),
                    size = Size(size.width - inset * 2f - 8f, size.height - inset * 2f - 8f),
                    cornerRadius = CornerRadius(5f, 5f),
                    style = Stroke(width = 1f),
                )
            }
            LibraryCoverStyle.CLOTH -> {
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        Color.White.copy(alpha = 0.045f),
                        Offset(x, 0f),
                        Offset(x + size.height * 0.05f, size.height),
                        1f,
                    )
                    x += 5f
                }
                var y = 1f
                while (y < size.height) {
                    drawLine(
                        Color.Black.copy(alpha = 0.055f),
                        Offset(0f, y),
                        Offset(size.width, y + 2f),
                        1f,
                    )
                    y += 6f
                }
            }
            LibraryCoverStyle.PAPER -> {
                repeat(18) { index ->
                    val x = seededFraction(seed, index * 2) * size.width
                    val y = seededFraction(seed, index * 2 + 1) * size.height
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.035f),
                        radius = 1f + seededFraction(seed, index + 61) * 2.4f,
                        center = Offset(x, y),
                    )
                }
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = size.width * 0.38f,
                    center = Offset(size.width * 0.78f, size.height * 0.42f),
                    style = Stroke(width = 1.2f),
                )
                drawLine(
                    Color.Black.copy(alpha = 0.15f),
                    Offset(size.width * 0.44f, size.height * 0.40f),
                    Offset(size.width * 0.92f, size.height * 0.66f),
                    1.1f,
                )
            }
            LibraryCoverStyle.NIGHT -> {
                drawRect(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF101827),
                            base.copy(alpha = 0.66f),
                            Color(0xFF080A10),
                        ),
                    ),
                )
                repeat(11) { index ->
                    val x = seededFraction(seed, index * 3) * size.width
                    val y = seededFraction(seed, index * 3 + 1) * size.height * 0.56f
                    drawCircle(
                        Color.White.copy(alpha = 0.15f + seededFraction(seed, index + 77) * 0.30f),
                        radius = 0.8f + seededFraction(seed, index + 42) * 1.4f,
                        center = Offset(x, y),
                    )
                }
                drawLine(
                    Color(0xFFFFC36A).copy(alpha = 0.68f),
                    Offset(size.width * 0.75f, size.height * 0.40f),
                    Offset(size.width * 0.75f, size.height * 0.68f),
                    2f,
                )
                drawCircle(
                    Color(0xFFFFD589).copy(alpha = 0.84f),
                    radius = size.width * 0.035f,
                    center = Offset(size.width * 0.75f, size.height * 0.42f),
                )
            }
            LibraryCoverStyle.MINIMAL -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(size.width * 0.78f, size.height * 0.22f),
                        radius = size.width * 0.62f,
                    ),
                    radius = size.width * 0.62f,
                    center = Offset(size.width * 0.78f, size.height * 0.22f),
                )
                drawLine(
                    Color.White.copy(alpha = 0.21f),
                    Offset(size.width * 0.20f, size.height * 0.22f),
                    Offset(size.width * 0.84f, size.height * 0.22f),
                    2f,
                )
            }
        }

        drawRect(
            Brush.horizontalGradient(
                listOf(
                    Color.Black.copy(alpha = 0.28f),
                    Color.Transparent,
                    Color.White.copy(alpha = 0.07f),
                    Color.Black.copy(alpha = 0.22f),
                ),
            ),
        )
        repeat(20) { index ->
            val x = seededFraction(seed + 11, index * 2) * size.width
            val y = seededFraction(seed + 37, index * 2 + 1) * size.height
            drawCircle(
                color = Color.White.copy(alpha = 0.025f),
                radius = 0.7f,
                center = Offset(x, y),
            )
        }
    }
}

private fun seededFraction(seed: Long, index: Int): Float {
    var value = seed xor (index.toLong() * -7046029254386353131L)
    value = (value xor (value ushr 33)) * -49064778989728563L
    value = value xor (value ushr 29)
    return ((value and 0xFFFFL).toFloat() / 65535f).coerceIn(0f, 1f)
}

private fun tactileCoverAccent(document: LibraryDocument): Color {
    val base = Color(document.coverColorArgb.toInt())
    return Color(
        red = (base.red * 1.34f + 0.16f).coerceIn(0f, 1f),
        green = (base.green * 1.20f + 0.08f).coerceIn(0f, 1f),
        blue = (base.blue * 1.22f + 0.12f).coerceIn(0f, 1f),
        alpha = 1f,
    )
}

private fun tactileLibraryProgress(document: LibraryDocument): Float = if (document.pageCount > 0) {
    ((document.lastPage + 1).toFloat() / document.pageCount).coerceIn(0f, 1f)
} else {
    0f
}

private fun tactileLibraryPosition(document: LibraryDocument): String = if (document.pageCount > 0) {
    "стр. ${document.lastPage + 1} из ${document.pageCount}"
} else {
    "ещё не открыт"
}

private fun LibraryReadingStatus.tactileStatusLabel(): String = when (this) {
    LibraryReadingStatus.READING -> "Читаю"
    LibraryReadingStatus.WANT_TO_READ -> "Хочу прочитать"
    LibraryReadingStatus.COMPLETED -> "Завершено"
}

private fun LibraryFilter.tactileFilterLabel(): String = when (this) {
    LibraryFilter.ALL -> "Все"
    LibraryFilter.READING -> "Читаю"
    LibraryFilter.WANT_TO_READ -> "Хочу прочитать"
    LibraryFilter.COMPLETED -> "Завершено"
}

private fun LibraryCoverStyle.tactileCoverStyleLabel(): String = when (this) {
    LibraryCoverStyle.CLASSIC -> "Классика"
    LibraryCoverStyle.CLOTH -> "Ткань"
    LibraryCoverStyle.PAPER -> "Бумага"
    LibraryCoverStyle.NIGHT -> "Ночь"
    LibraryCoverStyle.MINIMAL -> "Минимал"
}

private fun tactileDocumentCountLabel(count: Int): String {
    val mod100 = count % 100
    val mod10 = count % 10
    return when {
        mod100 in 11..14 -> "документов"
        mod10 == 1 -> "документ"
        mod10 in 2..4 -> "документа"
        else -> "документов"
    }
}

private fun shareLibraryPdf(context: android.content.Context, document: LibraryDocument) {
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
    val fromProvider = runCatching {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }.getOrNull()
    return fromProvider?.takeIf { it.isNotBlank() } ?: "PDF-документ"
}

private fun decodeLibraryCover(
    context: android.content.Context,
    uri: Uri,
): ImageBitmap? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri).use { stream ->
        BitmapFactory.decodeStream(stream, null, bounds)
    }
    val largestSide = maxOf(bounds.outWidth, bounds.outHeight).coerceAtLeast(1)
    var sampleSize = 1
    while (largestSide / (sampleSize * 2) >= 720) sampleSize *= 2
    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
    }
    context.contentResolver.openInputStream(uri).use { stream ->
        BitmapFactory.decodeStream(stream, null, options)?.asImageBitmap()
    }
}.getOrNull()
