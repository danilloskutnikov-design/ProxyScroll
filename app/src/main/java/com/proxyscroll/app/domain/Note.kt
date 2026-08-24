package com.proxyscroll.app.domain

data class Note(
    val id: String,
    val title: String,
    val body: String,
    val spans: List<NoteSpan> = emptyList(),
    val isPinned: Boolean,
    val colorFlag: NoteColorFlag = NoteColorFlag.NONE,
    val createdAt: Long,
    val updatedAt: Long,
    /** Stable, human-readable sequence number. It is never reused or renumbered. */
    val index: Long = 0L,
    /** Null for active notes; epoch millis after moving the note to Trash. */
    val deletedAt: Long? = null,
)

enum class NoteColorFlag(
    val storageKey: String,
    val displayName: String,
) {
    NONE("none", "Без флага"),
    SKY("sky", "Небо"),
    VIOLET("violet", "Ирис"),
    CORAL("coral", "Коралл"),
    MINT("mint", "Мята"),
    AMBER("amber", "Янтарь"),
    ;

    companion object {
        fun fromStorage(value: String?): NoteColorFlag {
            return entries.firstOrNull { it.storageKey == value } ?: NONE
        }
    }
}

data class NoteSpan(
    val start: Int,
    val end: Int,
    val bold: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false,
    val fontSizeSp: Int = DEFAULT_NOTE_FONT_SIZE_SP,
)

const val DEFAULT_NOTE_FONT_SIZE_SP = 19
const val TRASH_RETENTION_DAYS = 7
const val TRASH_RETENTION_MILLIS = TRASH_RETENTION_DAYS * 24L * 60L * 60L * 1000L
