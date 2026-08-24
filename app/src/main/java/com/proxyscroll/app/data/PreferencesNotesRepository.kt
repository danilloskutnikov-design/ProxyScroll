package com.proxyscroll.app.data

import android.content.SharedPreferences
import com.proxyscroll.app.domain.Note
import com.proxyscroll.app.domain.NoteColorFlag
import com.proxyscroll.app.domain.NoteSpan
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
        put("spans", JSONArray().apply {
            spans.forEach { span ->
                put(JSONObject().apply {
                    put("start", span.start)
                    put("end", span.end)
                    put("bold", span.bold)
                    put("underline", span.underline)
                    put("strikethrough", span.strikethrough)
                    put("fontSizeSp", span.fontSizeSp)
                })
            }
        })
        put("isPinned", isPinned)
        put("colorFlag", colorFlag.storageKey)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

    private fun JSONObject.toNote(): Note {
        val body = optString("body")
        val rawSpans = optJSONArray("spans") ?: JSONArray()
        val spans = buildList {
            repeat(rawSpans.length()) { index ->
                val span = rawSpans.getJSONObject(index)
                val start = span.optInt("start").coerceIn(0, body.length)
                val end = span.optInt("end").coerceIn(start, body.length)
                if (end > start) {
                    add(
                        NoteSpan(
                            start = start,
                            end = end,
                            bold = span.optBoolean("bold"),
                            underline = span.optBoolean("underline"),
                            strikethrough = span.optBoolean("strikethrough"),
                            fontSizeSp = span.optInt("fontSizeSp", 19),
                        ),
                    )
                }
            }
        }
        return Note(
            id = getString("id"),
            title = optString("title"),
            body = body,
            spans = spans,
            isPinned = optBoolean("isPinned"),
            colorFlag = NoteColorFlag.fromStorage(optString("colorFlag", null)),
            createdAt = optLong("createdAt"),
            updatedAt = optLong("updatedAt"),
        )
    }

    private companion object {
        const val KEY_NOTES = "notes_v1"
    }
}
