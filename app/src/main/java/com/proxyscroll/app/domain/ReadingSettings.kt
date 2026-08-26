package com.proxyscroll.app.domain

const val MIN_READING_FONT_SCALE = 0.80f
const val MAX_READING_FONT_SCALE = 1.60f
const val MIN_READING_LINE_HEIGHT = 0.90f
const val MAX_READING_LINE_HEIGHT = 1.40f

data class ReadingSettings(
    val fontScale: Float = 1.0f,
    val lineHeight: Float = 1.08f,
    val pageMarginDp: Int = 20,
) {
    fun normalized(): ReadingSettings = copy(
        fontScale = fontScale.coerceIn(MIN_READING_FONT_SCALE, MAX_READING_FONT_SCALE),
        lineHeight = lineHeight.coerceIn(MIN_READING_LINE_HEIGHT, MAX_READING_LINE_HEIGHT),
        pageMarginDp = pageMarginDp.coerceIn(8, 40),
    )
}
