package com.proxyscroll.app.domain

enum class InputMotion(
    val storageKey: String,
    val pulseMillis: Int,
    val autosaveDelayMillis: Long,
) {
    DIRECT("direct", 0, 420L),
    GENTLE("gentle", 150, 560L),
    FLOWING("flowing", 260, 720L),
    ;

    companion object {
        fun fromStorage(value: String?): InputMotion {
            return entries.firstOrNull { it.storageKey == value } ?: FLOWING
        }
    }
}
