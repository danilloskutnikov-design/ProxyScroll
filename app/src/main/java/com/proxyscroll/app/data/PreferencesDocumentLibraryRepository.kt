package com.proxyscroll.app.data

import android.content.SharedPreferences
import com.proxyscroll.app.domain.DocumentLibraryRepository
import com.proxyscroll.app.domain.LibraryDocument
import com.proxyscroll.app.domain.LibraryReadingStatus
import org.json.JSONArray
import org.json.JSONObject

class PreferencesDocumentLibraryRepository(
    private val preferences: SharedPreferences,
) : DocumentLibraryRepository {
    override fun getAll(): List<LibraryDocument> {
        val raw = preferences.getString(KEY_DOCUMENTS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                repeat(array.length()) { index ->
                    val item = array.getJSONObject(index)
                    val lastPage = item.optInt("lastPage").coerceAtLeast(0)
                    val pageCount = item.optInt("pageCount").coerceAtLeast(0)
                    val fallbackStatus = when {
                        pageCount <= 0 -> LibraryReadingStatus.WANT_TO_READ
                        lastPage >= pageCount - 1 -> LibraryReadingStatus.COMPLETED
                        else -> LibraryReadingStatus.READING
                    }
                    val readingStatus = runCatching {
                        LibraryReadingStatus.valueOf(item.optString("readingStatus"))
                    }.getOrDefault(fallbackStatus)
                    add(
                        LibraryDocument(
                            id = item.getString("id"),
                            title = item.optString("title", "Документ"),
                            uri = item.getString("uri"),
                            addedAt = item.optLong("addedAt"),
                            lastOpenedAt = item.optLong("lastOpenedAt", item.optLong("addedAt")),
                            lastPage = lastPage,
                            pageCount = pageCount,
                            readingStatus = readingStatus,
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    override fun upsert(document: LibraryDocument) {
        persist(getAll().filterNot { it.id == document.id } + document)
    }

    override fun delete(documentId: String) {
        persist(getAll().filterNot { it.id == documentId })
    }

    private fun persist(documents: List<LibraryDocument>) {
        val array = JSONArray()
        documents.forEach { document ->
            array.put(JSONObject().apply {
                put("id", document.id)
                put("title", document.title)
                put("uri", document.uri)
                put("addedAt", document.addedAt)
                put("lastOpenedAt", document.lastOpenedAt)
                put("lastPage", document.lastPage)
                put("pageCount", document.pageCount)
                put("readingStatus", document.readingStatus.name)
            })
        }
        preferences.edit().putString(KEY_DOCUMENTS, array.toString()).apply()
    }

    private companion object {
        const val KEY_DOCUMENTS = "pdf_documents_v1"
    }
}
