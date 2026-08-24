package com.proxyscroll.app.ui.editor

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.proxyscroll.app.domain.DEFAULT_NOTE_FONT_SIZE_SP
import com.proxyscroll.app.domain.NoteSpan

const val SMALL_NOTE_FONT_SIZE_SP = 16
const val LARGE_NOTE_FONT_SIZE_SP = 24
const val DISPLAY_NOTE_FONT_SIZE_SP = 30

data class CharacterStyle(
    val bold: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false,
    val fontSizeSp: Int = DEFAULT_NOTE_FONT_SIZE_SP,
)

@Stable
class RichTextState(
    text: String,
    spans: List<NoteSpan>,
) {
    private var characterStyles by mutableStateOf(stylesFromSpans(text, spans))
    private var typingStyle by mutableStateOf(
        characterStyles.lastOrNull() ?: CharacterStyle(),
    )

    var value by mutableStateOf(
        TextFieldValue(
            annotatedString = annotatedText(text, characterStyles),
            selection = TextRange(text.length),
        ),
    )
        private set

    var revision by mutableIntStateOf(0)
        private set

    val boldActive: Boolean get() = selectedStyles().allOrTyping { it.bold }
    val underlineActive: Boolean get() = selectedStyles().allOrTyping { it.underline }
    val strikethroughActive: Boolean get() = selectedStyles().allOrTyping { it.strikethrough }
    val activeFontSizeSp: Int
        get() = selectedStyles().distinctBy { it.fontSizeSp }.singleOrNull()?.fontSizeSp
            ?: typingStyle.fontSizeSp

    fun onValueChange(newValue: TextFieldValue) {
        val oldText = value.text
        val newText = newValue.text

        if (oldText == newText) {
            value = TextFieldValue(
                annotatedString = annotatedText(newText, characterStyles),
                selection = newValue.selection,
                composition = newValue.composition,
            )
            if (newValue.selection.collapsed) {
                typingStyle = styleAtCursor(newValue.selection.start)
            }
            return
        }

        val prefix = commonPrefixLength(oldText, newText)
        val suffix = commonSuffixLength(oldText, newText, prefix)
        val insertedLength = newText.length - prefix - suffix
        val newStyles = buildList(newText.length) {
            addAll(characterStyles.take(prefix))
            repeat(insertedLength.coerceAtLeast(0)) { add(typingStyle) }
            if (suffix > 0) addAll(characterStyles.takeLast(suffix))
        }

        characterStyles = newStyles
        value = TextFieldValue(
            annotatedString = annotatedText(newText, newStyles),
            selection = newValue.selection,
            composition = newValue.composition,
        )
        revision++
    }

    fun toggleBold() = transformSelection(
        isActive = { it.bold },
        transform = { style, enabled -> style.copy(bold = enabled) },
    )

    fun toggleUnderline() = transformSelection(
        isActive = { it.underline },
        transform = { style, enabled -> style.copy(underline = enabled) },
    )

    fun toggleStrikethrough() = transformSelection(
        isActive = { it.strikethrough },
        transform = { style, enabled -> style.copy(strikethrough = enabled) },
    )

    fun setFontSize(fontSizeSp: Int) {
        transformSelection(
            isActive = { it.fontSizeSp == fontSizeSp },
            forceEnabled = true,
            transform = { style, _ -> style.copy(fontSizeSp = fontSizeSp) },
        )
    }

    fun toSpans(): List<NoteSpan> {
        if (characterStyles.isEmpty()) return emptyList()
        return buildList {
            var start = 0
            var current = characterStyles.first()
            for (index in 1..characterStyles.size) {
                val next = characterStyles.getOrNull(index)
                if (next != current) {
                    add(current.toNoteSpan(start, index))
                    start = index
                    if (next != null) current = next
                }
            }
        }
    }

    private fun transformSelection(
        isActive: (CharacterStyle) -> Boolean,
        forceEnabled: Boolean = false,
        transform: (CharacterStyle, Boolean) -> CharacterStyle,
    ) {
        val range = normalizedSelection()
        if (range.first == range.last) {
            val enabled = if (forceEnabled) true else !isActive(typingStyle)
            typingStyle = transform(typingStyle, enabled)
            return
        }

        val selected = characterStyles.subList(range.first, range.last)
        val enabled = if (forceEnabled) true else !selected.all(isActive)
        characterStyles = characterStyles.mapIndexed { index, style ->
            if (index in range.first until range.last) transform(style, enabled) else style
        }
        value = value.copy(annotatedString = annotatedText(value.text, characterStyles))
        revision++
    }

    private fun selectedStyles(): List<CharacterStyle> {
        val range = normalizedSelection()
        return if (range.first == range.last) emptyList() else {
            characterStyles.subList(range.first, range.last)
        }
    }

    private fun List<CharacterStyle>.allOrTyping(
        predicate: (CharacterStyle) -> Boolean,
    ): Boolean = if (isEmpty()) predicate(typingStyle) else all(predicate)

    private fun normalizedSelection(): Pair<Int, Int> {
        val start = minOf(value.selection.start, value.selection.end)
            .coerceIn(0, characterStyles.size)
        val end = maxOf(value.selection.start, value.selection.end)
            .coerceIn(start, characterStyles.size)
        return start to end
    }

    private fun styleAtCursor(cursor: Int): CharacterStyle {
        return characterStyles.getOrNull((cursor - 1).coerceAtLeast(0))
            ?: characterStyles.getOrNull(cursor)
            ?: typingStyle
    }
}

fun annotatedText(
    text: String,
    spans: List<NoteSpan>,
): AnnotatedString = annotatedText(text, stylesFromSpans(text, spans))

private fun annotatedText(
    text: String,
    styles: List<CharacterStyle>,
): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")
    return AnnotatedString.Builder(text).apply {
        var start = 0
        var current = styles.getOrNull(0) ?: CharacterStyle()
        for (index in 1..text.length) {
            val next = styles.getOrNull(index)
            if (next != current) {
                addStyle(current.toSpanStyle(), start, index)
                start = index
                if (next != null) current = next
            }
        }
    }.toAnnotatedString()
}

private fun stylesFromSpans(
    text: String,
    spans: List<NoteSpan>,
): List<CharacterStyle> {
    val styles = MutableList(text.length) { CharacterStyle() }
    spans.forEach { span ->
        val start = span.start.coerceIn(0, text.length)
        val end = span.end.coerceIn(start, text.length)
        for (index in start until end) {
            styles[index] = CharacterStyle(
                bold = span.bold,
                underline = span.underline,
                strikethrough = span.strikethrough,
                fontSizeSp = span.fontSizeSp.coerceIn(
                    SMALL_NOTE_FONT_SIZE_SP,
                    DISPLAY_NOTE_FONT_SIZE_SP,
                ),
            )
        }
    }
    return styles
}

private fun CharacterStyle.toNoteSpan(start: Int, end: Int) = NoteSpan(
    start = start,
    end = end,
    bold = bold,
    underline = underline,
    strikethrough = strikethrough,
    fontSizeSp = fontSizeSp,
)

private fun CharacterStyle.toSpanStyle(): SpanStyle {
    val decorations = buildList {
        if (underline) add(TextDecoration.Underline)
        if (strikethrough) add(TextDecoration.LineThrough)
    }
    return SpanStyle(
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        textDecoration = when (decorations.size) {
            0 -> TextDecoration.None
            1 -> decorations.first()
            else -> TextDecoration.combine(decorations)
        },
        fontSize = fontSizeSp.sp,
    )
}

private fun commonPrefixLength(old: String, new: String): Int {
    var index = 0
    val limit = minOf(old.length, new.length)
    while (index < limit && old[index] == new[index]) index++
    return index
}

private fun commonSuffixLength(old: String, new: String, prefix: Int): Int {
    var suffix = 0
    val limit = minOf(old.length, new.length) - prefix
    while (
        suffix < limit &&
        old[old.lastIndex - suffix] == new[new.lastIndex - suffix]
    ) {
        suffix++
    }
    return suffix
}
