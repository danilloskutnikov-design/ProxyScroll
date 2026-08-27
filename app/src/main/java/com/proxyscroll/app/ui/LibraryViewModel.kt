package com.proxyscroll.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.proxyscroll.app.domain.DocumentLibraryRepository
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
        val document = existing?.copy(
            title = title.ifBlank { existing.title },
            lastOpenedAt = now,
        ) ?: LibraryDocument(
            id = UUID.randomUUID().toString(),
            title = title.removeSuffix(".pdf").ifBlank { "PDF-документ" },
            uri = uri,
            addedAt = now,
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

    fun delete(document: LibraryDocument) {
        repository.delete(document.id)
        refresh()
    }

    private fun refresh() {
        _uiState.value = loadState()
    }

    private fun loadState(): LibraryUiState {
        val documents = repository.getAll().sortedByDescending { it.lastOpenedAt }
        val normalizedQuery = query.trim()
        val visibleDocuments = documents.filter { document ->
            val matchesQuery = normalizedQuery.isEmpty() ||
                document.title.contains(normalizedQuery, ignoreCase = true)
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
            query = query,
            filter = filter,
        )
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
