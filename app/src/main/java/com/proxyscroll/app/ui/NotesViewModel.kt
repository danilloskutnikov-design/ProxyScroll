package com.proxyscroll.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.proxyscroll.app.domain.Note
import com.proxyscroll.app.domain.NoteColorFlag
import com.proxyscroll.app.domain.NoteGroup
import com.proxyscroll.app.domain.NoteSpan
import com.proxyscroll.app.domain.NoteTextAlignment
import com.proxyscroll.app.domain.NotesRepository
import com.proxyscroll.app.domain.TRASH_RETENTION_MILLIS
import com.proxyscroll.app.domain.defaultGroupId
import com.proxyscroll.app.domain.legacyFlagForGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val trash: List<Note> = emptyList(),
    val groups: List<NoteGroup> = emptyList(),
    val query: String = "",
)

class NotesViewModel(
    private val repository: NotesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(loadState(query = "", purgeExpired = true))
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    fun setQuery(query: String) {
        _uiState.value = loadState(query)
    }

    fun save(
        existing: Note?,
        title: String,
        body: String,
        spans: List<NoteSpan>,
        textAlignment: NoteTextAlignment,
        groupId: String?,
    ): Note? {
        if (existing == null && title.isBlank() && body.isBlank()) return null
        val now = System.currentTimeMillis()
        val resolvedTitle = title.trimEnd().ifBlank { titleFromBody(body) }
        val resolvedGroupId = groupId?.takeIf { candidate ->
            repository.getGroups().any { it.id == candidate }
        }
        val saved = Note(
            id = existing?.id ?: UUID.randomUUID().toString(),
            title = resolvedTitle,
            body = body,
            spans = spans,
            textAlignment = textAlignment,
            isPinned = existing?.isPinned ?: false,
            colorFlag = legacyFlagForGroup(resolvedGroupId),
            groupId = resolvedGroupId,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            index = existing?.index?.takeIf { it > 0L } ?: nextIndex(),
            deletedAt = null,
        )
        repository.upsert(saved)
        refresh()
        return saved
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

    fun setColorFlag(note: Note, colorFlag: NoteColorFlag) {
        if (note.colorFlag == colorFlag) return
        repository.upsert(
            note.copy(
                colorFlag = colorFlag,
                groupId = colorFlag.defaultGroupId(),
                updatedAt = System.currentTimeMillis(),
            ),
        )
        refresh()
    }

    fun moveToTrash(note: Note) {
        moveToTrash(listOf(note))
    }

    fun moveToTrash(notes: Collection<Note>) {
        repository.moveToTrash(notes.mapTo(mutableSetOf()) { it.id }, System.currentTimeMillis())
        refresh()
    }

    fun restore(note: Note) {
        restore(listOf(note))
    }

    fun restore(notes: Collection<Note>) {
        repository.restore(notes.mapTo(mutableSetOf()) { it.id }, System.currentTimeMillis())
        refresh()
    }

    fun deleteForever(notes: Collection<Note>) {
        repository.deleteForever(notes.mapTo(mutableSetOf()) { it.id })
        refresh()
    }

    fun emptyTrash() {
        repository.deleteForever(repository.getTrash().mapTo(mutableSetOf()) { it.id })
        refresh()
    }

    fun setPinned(notes: Collection<Note>, pinned: Boolean) {
        val now = System.currentTimeMillis()
        repository.upsertAll(notes.map { it.copy(isPinned = pinned, updatedAt = now) })
        refresh()
    }

    fun setColorFlag(notes: Collection<Note>, colorFlag: NoteColorFlag) {
        val now = System.currentTimeMillis()
        repository.upsertAll(
            notes.map {
                it.copy(
                    colorFlag = colorFlag,
                    groupId = colorFlag.defaultGroupId(),
                    updatedAt = now,
                )
            },
        )
        refresh()
    }

    fun setGroup(notes: Collection<Note>, groupId: String?) {
        val now = System.currentTimeMillis()
        repository.upsertAll(
            notes.map {
                it.copy(
                    groupId = groupId,
                    colorFlag = legacyFlagForGroup(groupId),
                    updatedAt = now,
                )
            },
        )
        refresh()
    }

    fun createGroup(name: String, colorArgb: Long) {
        val trimmed = name.trim().take(28)
        if (trimmed.isBlank()) return
        val order = (repository.getGroups().maxOfOrNull { it.order } ?: 0) + 1
        repository.upsertGroup(
            NoteGroup(
                id = "custom.${UUID.randomUUID()}",
                name = trimmed,
                colorArgb = colorArgb,
                order = order,
            ),
        )
        refresh()
    }

    fun updateGroup(group: NoteGroup, name: String, colorArgb: Long) {
        if (group.builtIn) return
        val trimmed = name.trim().take(28)
        if (trimmed.isBlank()) return
        repository.upsertGroup(
            group.copy(
                name = trimmed,
                colorArgb = colorArgb,
            ),
        )
        refresh()
    }

    fun deleteGroup(groupId: String) {
        repository.deleteGroup(groupId)
        refresh()
    }

    private fun refresh() {
        _uiState.value = loadState(_uiState.value.query)
    }

    private fun loadState(query: String, purgeExpired: Boolean = false): NotesUiState {
        if (purgeExpired) {
            repository.purgeDeletedBefore(System.currentTimeMillis() - TRASH_RETENTION_MILLIS)
        }
        val active = repository.getAll()
        return NotesUiState(
            query = query,
            notes = sorted(active.filter { note -> note.matches(query) }),
            trash = repository.getTrash().sortedByDescending { it.deletedAt ?: Long.MIN_VALUE },
            groups = repository.getGroups(),
        )
    }

    private fun nextIndex(): Long {
        val highest = (repository.getAll() + repository.getTrash())
            .maxOfOrNull { it.index }
            ?: 0L
        return highest + 1L
    }

    private fun Note.matches(query: String): Boolean {
        if (query.isBlank()) return true
        return title.contains(query, ignoreCase = true) ||
            body.contains(query, ignoreCase = true)
    }

    private fun titleFromBody(body: String): String {
        return body
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(3)
            .joinToString(" ")
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
