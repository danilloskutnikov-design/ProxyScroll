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
