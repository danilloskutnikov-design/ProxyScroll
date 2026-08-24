package com.proxyscroll.app.domain

enum class AppTheme(val storageKey: String) {
    LIQUID_GLASS("liquid_glass"),
    ROYAL_GRAPHITE("royal_graphite"),
    OLD_SCROLL("old_scroll"),
    LITE_LIFE("lite_life"),
    ;

    companion object {
        fun fromStorage(value: String?): AppTheme {
            return entries.firstOrNull { it.storageKey == value } ?: LIQUID_GLASS
        }
    }
}
