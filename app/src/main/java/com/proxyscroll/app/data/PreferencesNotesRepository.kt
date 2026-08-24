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
        return readAll().filter { it.deletedAt == null }
    }

    override fun getTrash(): List<Note> {
        return readAll().filter { it.deletedAt != null }
    }

    override fun upsert(note: Note) {
        val notes = readAll().filterNot { it.id == note.id } + note
        persist(notes)
    }

    override fun upsertAll(notes: Collection<Note>) {
        if (notes.isEmpty()) return
        val replacements = notes.associateBy { it.id }
        val retained = readAll().filterNot { it.id in replacements }
        persist(retained + replacements.values)
    }

    override fun moveToTrash(noteIds: Set<String>, deletedAt: Long) {
        if (noteIds.isEmpty()) return
        persist(
            readAll().map { note ->
                if (note.id in noteIds) note.copy(deletedAt = deletedAt) else note
            },
        )
    }

    override fun restore(noteIds: Set<String>, restoredAt: Long) {
        if (noteIds.isEmpty()) return
        persist(
            readAll().map { note ->
                if (note.id in noteIds) {
                    note.copy(deletedAt = null, updatedAt = restoredAt)
                } else {
                    note
                }
            },
        )
    }

    override fun deleteForever(noteIds: Set<String>) {
        if (noteIds.isEmpty()) return
        persist(readAll().filterNot { it.id in noteIds })
    }

    override fun purgeDeletedBefore(cutoff: Long) {
        val notes = readAll()
        val retained = notes.filter { note ->
            note.deletedAt == null || note.deletedAt >= cutoff
        }
        if (retained.size != notes.size) persist(retained)
    }

    private fun readAll(): List<Note> {
        val raw = preferences.getString(KEY_NOTES, null) ?: return emptyList()
        val parsed = runCatching {
            val array = JSONArray(raw)
            buildList {
                repeat(array.length()) { index ->
                    add(array.getJSONObject(index).toNote())
                }
            }
        }.getOrDefault(emptyList())
        if (parsed.isEmpty()) return parsed

        // Alpha 20 migration: assign every pre-index note a permanent sequence
        // number in creation order. Duplicate/corrupt indices are repaired too.
        var nextIndex = parsed.maxOfOrNull { it.index.coerceAtLeast(0L) } ?: 0L
        val seen = mutableSetOf<Long>()
        var changed = false
        val migratedById = parsed
            .sortedWith(compareBy<Note> { it.createdAt }.thenBy { it.id })
            .associate { note ->
                val valid = note.index > 0L && seen.add(note.index)
                if (valid) {
                    note.id to note
                } else {
                    changed = true
                    nextIndex += 1L
                    seen += nextIndex
                    note.id to note.copy(index = nextIndex)
                }
            }
        val migrated = parsed.map { migratedById.getValue(it.id) }
        if (changed) persist(migrated)
        return migrated
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
        put("index", index)
        put("deletedAt", deletedAt ?: JSONObject.NULL)
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
            index = optLong("index", 0L),
            deletedAt = optLong("deletedAt", 0L).takeIf { it > 0L },
        )
    }

    private companion object {
        const val KEY_NOTES = "notes_v1"
    }
}
