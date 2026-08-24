package com.proxyscroll.app.domain

interface NotesRepository {
    /** Active notes only. */
    fun getAll(): List<Note>
    fun getTrash(): List<Note>
    fun getGroups(): List<NoteGroup>
    fun upsertGroup(group: NoteGroup)
    fun deleteGroup(groupId: String)
    fun upsert(note: Note)
    fun upsertAll(notes: Collection<Note>)
    fun moveToTrash(noteIds: Set<String>, deletedAt: Long)
    fun restore(noteIds: Set<String>, restoredAt: Long)
    fun deleteForever(noteIds: Set<String>)
    fun purgeDeletedBefore(cutoff: Long)
}
