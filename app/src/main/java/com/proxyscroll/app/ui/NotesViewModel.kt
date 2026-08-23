package com.proxyscroll.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.proxyscroll.app.domain.Note
import com.proxyscroll.app.domain.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val query: String = "",
)

class NotesViewModel(
    private val repository: NotesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        NotesUiState(notes = sorted(repository.getAll())),
    )
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    fun setQuery(query: String) {
        val allNotes = repository.getAll()
        _uiState.update {
            it.copy(
                query = query,
                notes = sorted(allNotes.filter { note -> note.matches(query) }),
            )
        }
    }

    fun save(existing: Note?, title: String, body: String) {
        if (title.isBlank() && body.isBlank()) return
        val now = System.currentTimeMillis()
        repository.upsert(
            Note(
                id = existing?.id ?: UUID.randomUUID().toString(),
                title = title.trim(),
                body = body.trim(),
                isPinned = existing?.isPinned ?: false,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            ),
        )
        refresh()
    }

    fun togglePinned(note: Note) {
        repository.upsert(
            note.copy(
                isPinned = !note.isPinned,
                updatedAt = System.currentTimeMillis(),
            ),
        )
        refresh()
    }

    fun delete(note: Note) {
        repository.delete(note.id)
        refresh()
    }

    private fun refresh() {
        setQuery(_uiState.value.query)
    }

    private fun Note.matches(query: String): Boolean {
        if (query.isBlank()) return true
        return title.contains(query, ignoreCase = true) ||
            body.contains(query, ignoreCase = true)
    }

    private companion object {
        fun sorted(notes: List<Note>) = notes.sortedWith(
            compareByDescending<Note> { it.isPinned }
                .thenByDescending { it.updatedAt },
        )
    }

    class Factory(
        private val repository: NotesRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(NotesViewModel::class.java))
            return NotesViewModel(repository) as T
        }
    }
}
