package com.proxyscroll.app.domain

data class LibraryDocument(
    val id: String,
    val title: String,
    val uri: String,
    val addedAt: Long,
    val lastOpenedAt: Long = addedAt,
    val lastPage: Int = 0,
    val pageCount: Int = 0,
)

interface DocumentLibraryRepository {
    fun getAll(): List<LibraryDocument>
    fun upsert(document: LibraryDocument)
    fun delete(documentId: String)
}
