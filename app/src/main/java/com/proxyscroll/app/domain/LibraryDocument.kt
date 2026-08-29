package com.proxyscroll.app.domain

enum class LibraryReadingStatus {
    READING,
    WANT_TO_READ,
    COMPLETED,
}

enum class LibraryCoverStyle {
    CLASSIC,
    CLOTH,
    PAPER,
    NIGHT,
    MINIMAL,
}

const val DEFAULT_LIBRARY_COVER_COLOR_ARGB: Long = 0xFF343044L

data class LibraryDocument(
    val id: String,
    val title: String,
    val uri: String,
    val addedAt: Long,
    val sourceTitle: String = title,
    val author: String = "",
    val coverStyle: LibraryCoverStyle = LibraryCoverStyle.CLOTH,
    val coverColorArgb: Long = DEFAULT_LIBRARY_COVER_COLOR_ARGB,
    val coverImageUri: String? = null,
    val lastOpenedAt: Long = addedAt,
    val lastPage: Int = 0,
    val pageCount: Int = 0,
    val readingStatus: LibraryReadingStatus = LibraryReadingStatus.WANT_TO_READ,
)

data class BookQuote(
    val id: String,
    val documentId: String,
    val excerpt: String,
    val note: String,
    val page: Int,
    val createdAt: Long,
    val updatedAt: Long = createdAt,
    val isFavorite: Boolean = false,
    val title: String = "",
)

interface DocumentLibraryRepository {
    fun getAll(): List<LibraryDocument>
    fun upsert(document: LibraryDocument)
    fun delete(documentId: String)
    fun getQuotes(): List<BookQuote>
    fun upsertQuote(quote: BookQuote)
    fun deleteQuote(quoteId: String)
}
