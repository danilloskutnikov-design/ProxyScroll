package com.proxyscroll.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.proxyscroll.app.domain.DocumentLibraryRepository
import com.proxyscroll.app.domain.LibraryDocument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class LibraryUiState(
    val documents: List<LibraryDocument> = emptyList(),
)

class LibraryViewModel(
    private val repository: DocumentLibraryRepository,
) : ViewModel() {
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
        )
        repository.upsert(document)
        refresh()
        return document
    }

    fun updateProgress(document: LibraryDocument, page: Int, pageCount: Int) {
        repository.upsert(
            document.copy(
                lastPage = page.coerceIn(0, (pageCount - 1).coerceAtLeast(0)),
                pageCount = pageCount.coerceAtLeast(0),
                lastOpenedAt = System.currentTimeMillis(),
            ),
        )
        refresh()
    }

    fun delete(document: LibraryDocument) {
        repository.delete(document.id)
        refresh()
    }

    private fun refresh() {
        _uiState.value = loadState()
    }

    private fun loadState() = LibraryUiState(
        documents = repository.getAll().sortedByDescending { it.lastOpenedAt },
    )

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
