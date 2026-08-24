package com.proxyscroll.app.domain

data class Note(
    val id: String,
    val title: String,
    val body: String,
    val spans: List<NoteSpan> = emptyList(),
    val isPinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

data class NoteSpan(
    val start: Int,
    val end: Int,
    val bold: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false,
    val fontSizeSp: Int = DEFAULT_NOTE_FONT_SIZE_SP,
)

const val DEFAULT_NOTE_FONT_SIZE_SP = 19
