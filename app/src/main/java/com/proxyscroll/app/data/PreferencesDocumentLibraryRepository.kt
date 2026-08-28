package com.proxyscroll.app.data

import android.content.SharedPreferences
import com.proxyscroll.app.domain.BookQuote
import com.proxyscroll.app.domain.DEFAULT_LIBRARY_COVER_COLOR_ARGB
import com.proxyscroll.app.domain.DocumentLibraryRepository
import com.proxyscroll.app.domain.LibraryCoverStyle
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
                            sourceTitle = item.optString(
                                "sourceTitle",
                                item.optString("title", "Документ"),
                            ),
                            author = item.optString("author"),
                            coverStyle = runCatching {
                                LibraryCoverStyle.valueOf(item.optString("coverStyle"))
                            }.getOrDefault(LibraryCoverStyle.CLOTH),
                            coverColorArgb = item.optLong(
                                "coverColorArgb",
                                DEFAULT_LIBRARY_COVER_COLOR_ARGB,
                            ),
                            coverImageUri = item.optString("coverImageUri")
                                .takeIf { it.isNotBlank() },
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
        persistQuotes(getQuotes().filterNot { it.documentId == documentId })
    }

    override fun getQuotes(): List<BookQuote> {
        val raw = preferences.getString(KEY_QUOTES, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                repeat(array.length()) { index ->
                    val item = array.getJSONObject(index)
                    val createdAt = item.optLong("createdAt")
                    add(
                        BookQuote(
                            id = item.getString("id"),
                            documentId = item.getString("documentId"),
                            excerpt = item.optString("excerpt"),
                            note = item.optString("note"),
                            page = item.optInt("page").coerceAtLeast(0),
                            createdAt = createdAt,
                            updatedAt = item.optLong("updatedAt", createdAt),
                            isFavorite = item.optBoolean("isFavorite"),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    override fun upsertQuote(quote: BookQuote) {
        persistQuotes(getQuotes().filterNot { it.id == quote.id } + quote)
    }

    override fun deleteQuote(quoteId: String) {
        persistQuotes(getQuotes().filterNot { it.id == quoteId })
    }

    private fun persist(documents: List<LibraryDocument>) {
        val array = JSONArray()
        documents.forEach { document ->
            array.put(JSONObject().apply {
                put("id", document.id)
                put("title", document.title)
                put("uri", document.uri)
                put("addedAt", document.addedAt)
                put("sourceTitle", document.sourceTitle)
                put("author", document.author)
                put("coverStyle", document.coverStyle.name)
                put("coverColorArgb", document.coverColorArgb)
                put("coverImageUri", document.coverImageUri ?: "")
                put("lastOpenedAt", document.lastOpenedAt)
                put("lastPage", document.lastPage)
                put("pageCount", document.pageCount)
                put("readingStatus", document.readingStatus.name)
            })
        }
        preferences.edit().putString(KEY_DOCUMENTS, array.toString()).apply()
    }

    private fun persistQuotes(quotes: List<BookQuote>) {
        val array = JSONArray()
        quotes.forEach { quote ->
            array.put(JSONObject().apply {
                put("id", quote.id)
                put("documentId", quote.documentId)
                put("excerpt", quote.excerpt)
                put("note", quote.note)
                put("page", quote.page)
                put("createdAt", quote.createdAt)
                put("updatedAt", quote.updatedAt)
                put("isFavorite", quote.isFavorite)
            })
        }
        preferences.edit().putString(KEY_QUOTES, array.toString()).apply()
    }

    private companion object {
        const val KEY_DOCUMENTS = "pdf_documents_v1"
        const val KEY_QUOTES = "book_quotes_v1"
    }
}
