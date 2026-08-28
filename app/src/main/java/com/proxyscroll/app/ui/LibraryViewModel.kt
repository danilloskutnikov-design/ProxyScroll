package com.proxyscroll.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.proxyscroll.app.domain.BookQuote
import com.proxyscroll.app.domain.DocumentLibraryRepository
import com.proxyscroll.app.domain.LibraryCoverStyle
import com.proxyscroll.app.domain.LibraryDocument
import com.proxyscroll.app.domain.LibraryReadingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class LibraryFilter {
    ALL,
    READING,
    WANT_TO_READ,
    COMPLETED,
}

data class LibraryUiState(
    val documents: List<LibraryDocument> = emptyList(),
    val visibleDocuments: List<LibraryDocument> = emptyList(),
    val quotes: List<BookQuote> = emptyList(),
    val visibleQuotes: List<BookQuote> = emptyList(),
    val query: String = "",
    val filter: LibraryFilter = LibraryFilter.ALL,
)

class LibraryViewModel(
    private val repository: DocumentLibraryRepository,
) : ViewModel() {
    private var query = ""
    private var filter = LibraryFilter.ALL
    private val _uiState = MutableStateFlow(loadState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    fun importPdf(uri: String, title: String): LibraryDocument {
        val existing = repository.getAll().firstOrNull { it.uri == uri }
        val now = System.currentTimeMillis()
        val cleanSourceTitle = title.removeSuffix(".pdf").ifBlank { "PDF-документ" }
        val document = existing?.copy(
            sourceTitle = cleanSourceTitle,
            lastOpenedAt = now,
        ) ?: LibraryDocument(
            id = UUID.randomUUID().toString(),
            title = cleanSourceTitle,
            uri = uri,
            addedAt = now,
            sourceTitle = cleanSourceTitle,
            coverStyle = defaultCoverStyle(cleanSourceTitle),
            coverColorArgb = defaultCoverColor(cleanSourceTitle),
            lastOpenedAt = now,
            readingStatus = LibraryReadingStatus.READING,
        )
        repository.upsert(document)
        refresh()
        return document
    }

    fun updateProgress(document: LibraryDocument, page: Int, pageCount: Int) {
        val safePageCount = pageCount.coerceAtLeast(0)
        val safePage = page.coerceIn(0, (safePageCount - 1).coerceAtLeast(0))
        val status = if (safePageCount > 0 && safePage >= safePageCount - 1) {
            LibraryReadingStatus.COMPLETED
        } else {
            LibraryReadingStatus.READING
        }
        repository.upsert(
            document.copy(
                lastPage = safePage,
                pageCount = safePageCount,
                lastOpenedAt = System.currentTimeMillis(),
                readingStatus = status,
            ),
        )
        refresh()
    }

    fun setQuery(value: String) {
        query = value
        refresh()
    }

    fun setFilter(value: LibraryFilter) {
        filter = value
        refresh()
    }

    fun updateStatus(document: LibraryDocument, status: LibraryReadingStatus) {
        repository.upsert(document.copy(readingStatus = status))
        refresh()
    }

    fun updateAppearance(
        document: LibraryDocument,
        title: String,
        author: String,
        coverStyle: LibraryCoverStyle,
        coverColorArgb: Long,
        coverImageUri: String?,
        readingStatus: LibraryReadingStatus,
    ) {
        repository.upsert(
            document.copy(
                title = title.trim().ifBlank { document.sourceTitle },
                author = author.trim(),
                coverStyle = coverStyle,
                coverColorArgb = coverColorArgb,
                coverImageUri = coverImageUri,
                readingStatus = readingStatus,
            ),
        )
        refresh()
    }

    fun addQuote(
        document: LibraryDocument,
        page: Int,
        excerpt: String,
        note: String,
    ): BookQuote? {
        if (excerpt.isBlank() && note.isBlank()) return null
        val now = System.currentTimeMillis()
        val quote = BookQuote(
            id = UUID.randomUUID().toString(),
            documentId = document.id,
            excerpt = excerpt.trim(),
            note = note.trim(),
            page = page.coerceAtLeast(0),
            createdAt = now,
        )
        repository.upsertQuote(quote)
        refresh()
        return quote
    }

    fun updateQuote(quote: BookQuote, excerpt: String, note: String) {
        if (excerpt.isBlank() && note.isBlank()) {
            deleteQuote(quote)
            return
        }
        repository.upsertQuote(
            quote.copy(
                excerpt = excerpt.trim(),
                note = note.trim(),
                updatedAt = System.currentTimeMillis(),
            ),
        )
        refresh()
    }

    fun deleteQuote(quote: BookQuote) {
        repository.deleteQuote(quote.id)
        refresh()
    }

    fun delete(document: LibraryDocument) {
        repository.delete(document.id)
        refresh()
    }

    private fun refresh() {
        _uiState.value = loadState()
    }

    private fun loadState(): LibraryUiState {
        val documents = repository.getAll().sortedByDescending { it.lastOpenedAt }
        val documentIds = documents.mapTo(mutableSetOf()) { it.id }
        val quotes = repository.getQuotes()
            .filter { it.documentId in documentIds }
            .sortedByDescending { it.updatedAt }
        val normalizedQuery = query.trim()
        val visibleDocuments = documents.filter { document ->
            val matchesQuery = normalizedQuery.isEmpty() ||
                document.title.contains(normalizedQuery, ignoreCase = true) ||
                document.author.contains(normalizedQuery, ignoreCase = true)
            val matchesFilter = when (filter) {
                LibraryFilter.ALL -> true
                LibraryFilter.READING -> document.readingStatus == LibraryReadingStatus.READING
                LibraryFilter.WANT_TO_READ -> {
                    document.readingStatus == LibraryReadingStatus.WANT_TO_READ
                }
                LibraryFilter.COMPLETED -> {
                    document.readingStatus == LibraryReadingStatus.COMPLETED
                }
            }
            matchesQuery && matchesFilter
        }
        return LibraryUiState(
            documents = documents,
            visibleDocuments = visibleDocuments,
            quotes = quotes,
            visibleQuotes = quotes.filter { quote ->
                visibleDocuments.any { it.id == quote.documentId }
            },
            query = query,
            filter = filter,
        )
    }

    private fun defaultCoverStyle(seed: String): LibraryCoverStyle {
        val styles = LibraryCoverStyle.entries
        return styles[(seed.hashCode() and Int.MAX_VALUE) % styles.size]
    }

    private fun defaultCoverColor(seed: String): Long {
        val colors = longArrayOf(
            0xFF293449L,
            0xFF4A344FL,
            0xFF5B3B36L,
            0xFF234542L,
            0xFF746A5EL,
            0xFF2C3856L,
        )
        return colors[(seed.hashCode() and Int.MAX_VALUE) % colors.size]
    }

    class Factory(
        private val repository: DocumentLibraryRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(LibraryViewModel::class.java))
            return LibraryViewModel(repository) as T
        }
    }
}
