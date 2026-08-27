package com.proxyscroll.app.domain

enum class LibraryReadingStatus {
    READING,
    WANT_TO_READ,
    COMPLETED,
}

data class LibraryDocument(
    val id: String,
    val title: String,
    val uri: String,
    val addedAt: Long,
    val lastOpenedAt: Long = addedAt,
    val lastPage: Int = 0,
    val pageCount: Int = 0,
    val readingStatus: LibraryReadingStatus = LibraryReadingStatus.WANT_TO_READ,
)

interface DocumentLibraryRepository {
    fun getAll(): List<LibraryDocument>
    fun upsert(document: LibraryDocument)
    fun delete(documentId: String)
}
