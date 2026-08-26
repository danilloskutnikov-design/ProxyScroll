package com.proxyscroll.app.data

import android.content.SharedPreferences
import com.proxyscroll.app.domain.Note
import com.proxyscroll.app.domain.NoteColorFlag
import com.proxyscroll.app.domain.NoteGroup
import com.proxyscroll.app.domain.NoteSpan
import com.proxyscroll.app.domain.NoteTextAlignment
import com.proxyscroll.app.domain.NotesRepository
import com.proxyscroll.app.domain.DEFAULT_NOTE_GROUPS
import com.proxyscroll.app.domain.defaultGroupId
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

    override fun getGroups(): List<NoteGroup> {
        val raw = preferences.getString(KEY_GROUPS, null)
        val custom = runCatching {
            val array = JSONArray(raw ?: "[]")
            buildList {
                repeat(array.length()) { index ->
                    val item = array.getJSONObject(index)
                    add(
                        NoteGroup(
                            id = item.getString("id"),
                            name = item.optString("name").trim().take(28),
                            colorArgb = item.optLong("colorArgb", 0xFF65B9FF),
                            order = item.optInt("order", DEFAULT_NOTE_GROUPS.size + index),
                            builtIn = false,
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
        return (DEFAULT_NOTE_GROUPS + custom)
            .distinctBy { it.id }
            .sortedBy { it.order }
    }

    override fun upsertGroup(group: NoteGroup) {
        if (group.builtIn || group.name.isBlank()) return
        val custom = getGroups().filterNot { it.builtIn || it.id == group.id } +
            group.copy(name = group.name.trim().take(28), builtIn = false)
        persistGroups(custom)
    }

    override fun deleteGroup(groupId: String) {
        if (DEFAULT_NOTE_GROUPS.any { it.id == groupId }) return
        val custom = getGroups().filterNot { it.builtIn || it.id == groupId }
        persistGroups(custom)
        val notes = readAll().map { note ->
            if (note.groupId == groupId) {
                note.copy(groupId = null, colorFlag = NoteColorFlag.NONE)
            } else {
                note
            }
        }
        persist(notes)
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
        put("textAlignment", textAlignment.storageKey)
        put("isPinned", isPinned)
        put("colorFlag", colorFlag.storageKey)
        put("groupId", groupId ?: JSONObject.NULL)
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
        val colorFlag = NoteColorFlag.fromStorage(optString("colorFlag", null))
        val groupId = if (has("groupId") && !isNull("groupId")) {
            optString("groupId").takeIf { it.isNotBlank() }
        } else {
            colorFlag.defaultGroupId()
        }
        return Note(
            id = getString("id"),
            title = optString("title"),
            body = body,
            spans = spans,
            textAlignment = NoteTextAlignment.fromStorage(optString("textAlignment", null)),
            isPinned = optBoolean("isPinned"),
            colorFlag = colorFlag,
            groupId = groupId,
            createdAt = optLong("createdAt"),
            updatedAt = optLong("updatedAt"),
            index = optLong("index", 0L),
            deletedAt = optLong("deletedAt", 0L).takeIf { it > 0L },
        )
    }

    private fun persistGroups(groups: List<NoteGroup>) {
        val array = JSONArray()
        groups.filterNot { it.builtIn }.sortedBy { it.order }.forEach { group ->
            array.put(JSONObject().apply {
                put("id", group.id)
                put("name", group.name)
                put("colorArgb", group.colorArgb)
                put("order", group.order)
            })
        }
        preferences.edit().putString(KEY_GROUPS, array.toString()).apply()
    }

    private companion object {
        const val KEY_NOTES = "notes_v1"
        const val KEY_GROUPS = "note_groups_v1"
    }
}
