package com.proxyscroll.app.domain

interface NotesRepository {
    fun getAll(): List<Note>
    fun upsert(note: Note)
    fun delete(noteId: String)
}
