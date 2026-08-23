package com.proxyscroll.app.data

import android.content.SharedPreferences
import com.proxyscroll.app.domain.Note
import com.proxyscroll.app.domain.NotesRepository
import org.json.JSONArray
import org.json.JSONObject

class PreferencesNotesRepository(
    private val preferences: SharedPreferences,
) : NotesRepository {
    override fun getAll(): List<Note> {
        val raw = preferences.getString(KEY_NOTES, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                repeat(array.length()) { index ->
                    add(array.getJSONObject(index).toNote())
                }
            }
        }.getOrDefault(emptyList())
    }

    override fun upsert(note: Note) {
        val notes = getAll().filterNot { it.id == note.id } + note
        persist(notes)
    }

    override fun delete(noteId: String) {
        persist(getAll().filterNot { it.id == noteId })
    }

    private fun persist(notes: List<Note>) {
        val array = JSONArray()
        notes.forEach { note -> array.put(note.toJson()) }
        preferences.edit().putString(KEY_NOTES, array.toString()).apply()
    }

    private fun Note.toJson() = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("body", body)
        put("isPinned", isPinned)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

    private fun JSONObject.toNote() = Note(
        id = getString("id"),
        title = optString("title"),
        body = optString("body"),
        isPinned = optBoolean("isPinned"),
        createdAt = optLong("createdAt"),
        updatedAt = optLong("updatedAt"),
    )

    private companion object {
        const val KEY_NOTES = "notes_v1"
    }
}
