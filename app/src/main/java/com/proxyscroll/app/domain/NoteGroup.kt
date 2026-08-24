package com.proxyscroll.app.domain

data class NoteGroup(
    val id: String,
    val name: String,
    val colorArgb: Long,
    val order: Int,
    val builtIn: Boolean = false,
)

val DEFAULT_NOTE_GROUPS: List<NoteGroup> = listOf(
    NoteGroup("builtin.sky", "Небо", 0xFF65B9FF, 0, builtIn = true),
    NoteGroup("builtin.violet", "Ирис", 0xFF9B80FF, 1, builtIn = true),
    NoteGroup("builtin.coral", "Коралл", 0xFFFF8E8A, 2, builtIn = true),
    NoteGroup("builtin.mint", "Мята", 0xFF59D4B1, 3, builtIn = true),
    NoteGroup("builtin.amber", "Янтарь", 0xFFF3BC62, 4, builtIn = true),
)

fun NoteColorFlag.defaultGroupId(): String? = when (this) {
    NoteColorFlag.NONE -> null
    NoteColorFlag.SKY -> "builtin.sky"
    NoteColorFlag.VIOLET -> "builtin.violet"
    NoteColorFlag.CORAL -> "builtin.coral"
    NoteColorFlag.MINT -> "builtin.mint"
    NoteColorFlag.AMBER -> "builtin.amber"
}

fun legacyFlagForGroup(groupId: String?): NoteColorFlag = when (groupId) {
    "builtin.sky" -> NoteColorFlag.SKY
    "builtin.violet" -> NoteColorFlag.VIOLET
    "builtin.coral" -> NoteColorFlag.CORAL
    "builtin.mint" -> NoteColorFlag.MINT
    "builtin.amber" -> NoteColorFlag.AMBER
    else -> NoteColorFlag.NONE
}
