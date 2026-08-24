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
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.proxyscroll.app.domain.DEFAULT_NOTE_FONT_SIZE_SP
import com.proxyscroll.app.domain.NoteSpan

const val MIN_NOTE_FONT_SIZE_SP = 10
const val MAX_NOTE_FONT_SIZE_SP = 72

data class CharacterStyle(
    val bold: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false,
    val fontSizeSp: Int = DEFAULT_NOTE_FONT_SIZE_SP,
)

private data class StyleRun(
    val start: Int,
    val end: Int,
    val style: CharacterStyle,
)

private data class RichTextSnapshot(
    val value: TextFieldValue,
    val styleRuns: List<StyleRun>,
    val typingStyle: CharacterStyle,
)

@Stable
class RichTextState(
    text: String,
    spans: List<NoteSpan>,
) {
    private val undoStack = ArrayDeque<RichTextSnapshot>()
    private val redoStack = ArrayDeque<RichTextSnapshot>()
    private var historyRevision by mutableIntStateOf(0)
    private var lastTextEditAtMillis = 0L
    private var lastTextEditCursor = -1
    private var lastTextEditWasCoalescible = false

    private var styleRuns by mutableStateOf(runsFromSpans(text, spans))
    private var typingStyle by mutableStateOf(
        styleRuns.lastOrNull()?.style ?: CharacterStyle(),
    )

    var value by mutableStateOf(
        TextFieldValue(
            text = text,
            selection = TextRange(text.length),
        ),
    )
        private set

    var revision by mutableIntStateOf(0)
        private set

    val visualTransformation: VisualTransformation
        get() = RunsVisualTransformation(styleRuns)

    val hasSelection: Boolean get() = !value.selection.collapsed

    val canUndo: Boolean
        get() {
            historyRevision
            return undoStack.isNotEmpty()
        }

    val canRedo: Boolean
        get() {
            historyRevision
            return redoStack.isNotEmpty()
        }

    val selectionLength: Int
        get() = kotlin.math.abs(value.selection.end - value.selection.start)

    val selectedPreview: String
        get() {
            val range = normalizedSelection()
            if (range.first == range.second) return ""
            return value.text
                .substring(range.first, range.second)
                .replace('\n', ' ')
                .trim()
                .let { if (it.length <= 30) it else it.take(29) + "…" }
        }

    val boldActive: Boolean get() = activeStyles().allOrTyping { it.bold }
    val underlineActive: Boolean get() = activeStyles().allOrTyping { it.underline }
    val strikethroughActive: Boolean get() = activeStyles().allOrTyping { it.strikethrough }
    val activeFontSizeSp: Int?
        get() {
            val styles = activeStyles()
            if (styles.isEmpty()) return typingStyle.fontSizeSp
            return styles.map { it.fontSizeSp }.distinct().singleOrNull()
        }

    fun onValueChange(newValue: TextFieldValue) {
        val oldText = value.text
        val newText = newValue.text

        if (oldText == newText) {
            val selectionChanged = value.selection != newValue.selection
            value = newValue.asPlainValue()
            if (newValue.selection.collapsed) {
                typingStyle = styleAtCursor(newValue.selection.start)
            }
            if (selectionChanged) resetTextHistoryGroup()
            return
        }

        val now = System.nanoTime() / 1_000_000L
        val coalescible = value.selection.collapsed &&
            newValue.selection.collapsed &&
            kotlin.math.abs(newText.length - oldText.length) <= 2
        val continuesTextGroup = coalescible &&
            lastTextEditWasCoalescible &&
            now - lastTextEditAtMillis <= HISTORY_GROUP_MILLIS &&
            kotlin.math.abs(value.selection.end - lastTextEditCursor) <= 1
        recordHistory(skipPush = continuesTextGroup)

        val prefix = commonPrefixLength(oldText, newText)
        val suffix = commonSuffixLength(oldText, newText, prefix)
        val oldEditEnd = oldText.length - suffix
        val newEditEnd = newText.length - suffix
        val insertedLength = newEditEnd - prefix
        val shift = newText.length - oldText.length
        val insertedStyle = if (value.selection.collapsed) {
            typingStyle
        } else {
            styleAtPosition(prefix)
        }

        val updatedRuns = buildList {
            addAll(sliceRuns(styleRuns, 0, prefix))
            if (insertedLength > 0) {
                add(StyleRun(prefix, newEditEnd, insertedStyle))
            }
            addAll(
                sliceRuns(styleRuns, oldEditEnd, oldText.length).map { run ->
                    run.copy(start = run.start + shift, end = run.end + shift)
                },
            )
        }

        styleRuns = normalizeRuns(updatedRuns, newText.length)
        value = newValue.asPlainValue()
        revision++
        lastTextEditAtMillis = now
        lastTextEditCursor = newValue.selection.end
        lastTextEditWasCoalescible = coalescible
    }

    fun undo() {
        val previous = undoStack.removeLastOrNull() ?: return
        pushBounded(redoStack, snapshot())
        restore(previous)
    }

    fun redo() {
        val next = redoStack.removeLastOrNull() ?: return
        pushBounded(undoStack, snapshot())
        restore(next)
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

    fun adjustFontSize(deltaSp: Int) {
        transformSelection(
            forceEnabled = true,
            isActive = { false },
            transform = { style, _ ->
                style.copy(
                    fontSizeSp = (style.fontSizeSp + deltaSp).coerceIn(
                        MIN_NOTE_FONT_SIZE_SP,
                        MAX_NOTE_FONT_SIZE_SP,
                    ),
                )
            },
        )
    }

    fun setFontSize(fontSizeSp: Int) {
        val safeSize = fontSizeSp.coerceIn(MIN_NOTE_FONT_SIZE_SP, MAX_NOTE_FONT_SIZE_SP)
        transformSelection(
            forceEnabled = true,
            isActive = { it.fontSizeSp == safeSize },
            transform = { style, _ -> style.copy(fontSizeSp = safeSize) },
        )
    }

    fun clearFormatting() {
        transformSelection(
            forceEnabled = true,
            isActive = { false },
            transform = { _, _ -> CharacterStyle() },
        )
    }

    fun selectWord() {
        val text = value.text
        if (text.isEmpty()) return
        var start = normalizedSelection().first.coerceAtMost(text.lastIndex)
        var end = normalizedSelection().second.coerceAtLeast(start + 1).coerceAtMost(text.length)

        while (start > 0 && text[start - 1].isWordCharacter()) start--
        while (end < text.length && text[end].isWordCharacter()) end++
        setSelection(start, end)
    }

    fun selectSentence() {
        val text = value.text
        if (text.isEmpty()) return
        var start = normalizedSelection().first.coerceAtMost(text.lastIndex)
        var end = normalizedSelection().second.coerceAtLeast(start + 1).coerceAtMost(text.length)

        while (start > 0 && !text[start - 1].isSentenceBoundary()) start--
        while (start < text.length && text[start].isWhitespace()) start++
        while (end < text.length && !text[end - 1].isSentenceBoundary()) end++
        setSelection(start, end)
    }

    fun selectParagraph() {
        val text = value.text
        if (text.isEmpty()) return
        var start = normalizedSelection().first.coerceAtMost(text.lastIndex)
        var end = normalizedSelection().second.coerceAtLeast(start + 1).coerceAtMost(text.length)

        while (start > 0 && text[start - 1] != '\n') start--
        while (end < text.length && text[end] != '\n') end++
        setSelection(start, end)
    }

    fun toSpans(): List<NoteSpan> = styleRuns.map { run ->
        NoteSpan(
            start = run.start,
            end = run.end,
            bold = run.style.bold,
            underline = run.style.underline,
            strikethrough = run.style.strikethrough,
            fontSizeSp = run.style.fontSizeSp,
        )
    }

    private fun transformSelection(
        isActive: (CharacterStyle) -> Boolean,
        forceEnabled: Boolean = false,
        transform: (CharacterStyle, Boolean) -> CharacterStyle,
    ) {
        val range = normalizedSelection()
        recordHistory(skipPush = false)
        resetTextHistoryGroup()
        if (range.first == range.second) {
            val enabled = if (forceEnabled) true else !isActive(typingStyle)
            typingStyle = transform(typingStyle, enabled)
            return
        }

        val selected = activeStyles()
        val enabled = if (forceEnabled) true else !selected.all(isActive)
        styleRuns = normalizeRuns(
            styleRuns.flatMap { run ->
                if (run.end <= range.first || run.start >= range.second) {
                    listOf(run)
                } else {
                    buildList {
                        if (run.start < range.first) {
                            add(run.copy(end = range.first))
                        }
                        val overlapStart = maxOf(run.start, range.first)
                        val overlapEnd = minOf(run.end, range.second)
                        add(
                            StyleRun(
                                overlapStart,
                                overlapEnd,
                                transform(run.style, enabled),
                            ),
                        )
                        if (run.end > range.second) {
                            add(run.copy(start = range.second))
                        }
                    }
                }
            },
            value.text.length,
        )
        revision++
    }

    private fun snapshot() = RichTextSnapshot(
        value = value.asPlainValue(),
        styleRuns = styleRuns.toList(),
        typingStyle = typingStyle,
    )

    private fun recordHistory(skipPush: Boolean) {
        var historyChanged = false
        if (!skipPush) {
            pushBounded(undoStack, snapshot())
            historyChanged = true
        }
        if (redoStack.isNotEmpty()) {
            redoStack.clear()
            historyChanged = true
        }
        if (historyChanged) historyRevision++
    }

    private fun restore(snapshot: RichTextSnapshot) {
        value = snapshot.value.asPlainValue()
        styleRuns = snapshot.styleRuns
        typingStyle = snapshot.typingStyle
        resetTextHistoryGroup()
        historyRevision++
        revision++
    }

    private fun resetTextHistoryGroup() {
        lastTextEditAtMillis = 0L
        lastTextEditCursor = -1
        lastTextEditWasCoalescible = false
    }

    private fun pushBounded(
        stack: ArrayDeque<RichTextSnapshot>,
        snapshot: RichTextSnapshot,
    ) {
        if (stack.size >= MAX_HISTORY_SNAPSHOTS) stack.removeFirst()
        stack.addLast(snapshot)
    }

    private fun activeStyles(): List<CharacterStyle> {
        val range = normalizedSelection()
        if (range.first == range.second) return emptyList()
        return styleRuns
            .asSequence()
            .filter { it.end > range.first && it.start < range.second }
            .map { it.style }
            .distinct()
            .toList()
    }

    private fun List<CharacterStyle>.allOrTyping(
        predicate: (CharacterStyle) -> Boolean,
    ): Boolean = if (isEmpty()) predicate(typingStyle) else all(predicate)

    private fun normalizedSelection(): Pair<Int, Int> {
        val start = minOf(value.selection.start, value.selection.end).coerceIn(0, value.text.length)
        val end = maxOf(value.selection.start, value.selection.end).coerceIn(start, value.text.length)
        return start to end
    }

    private fun setSelection(start: Int, end: Int) {
        value = TextFieldValue(
            text = value.text,
            selection = TextRange(
                start.coerceIn(0, value.text.length),
                end.coerceIn(0, value.text.length),
            ),
            composition = null,
        )
    }

    private fun styleAtCursor(cursor: Int): CharacterStyle {
        if (value.text.isEmpty()) return typingStyle
        val position = if (cursor > 0) cursor - 1 else 0
        return styleAtPosition(position)
    }

    private fun styleAtPosition(position: Int): CharacterStyle {
        return styleRuns.firstOrNull { position >= it.start && position < it.end }?.style
            ?: typingStyle
    }
}

private const val HISTORY_GROUP_MILLIS = 750L
private const val MAX_HISTORY_SNAPSHOTS = 80

fun annotatedText(
    text: String,
    spans: List<NoteSpan>,
): AnnotatedString = styledText(
    text,
    runsFromSpans(text, spans).map { run ->
        run.copy(
            style = run.style.copy(
                fontSizeSp = run.style.fontSizeSp.coerceIn(13, 22),
            ),
        )
    },
)

private class RunsVisualTransformation(
    private val runs: List<StyleRun>,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = styledText(text.text, runs),
            offsetMapping = OffsetMapping.Identity,
        )
    }
}

private fun styledText(text: String, runs: List<StyleRun>): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")
    return AnnotatedString.Builder(text).apply {
        runs.forEach { run ->
            val start = run.start.coerceIn(0, text.length)
            val end = run.end.coerceIn(start, text.length)
            if (end > start) addStyle(run.style.toSpanStyle(), start, end)
        }
    }.toAnnotatedString()
}

private fun runsFromSpans(text: String, spans: List<NoteSpan>): List<StyleRun> {
    if (text.isEmpty()) return emptyList()
    if (spans.isEmpty()) return listOf(StyleRun(0, text.length, CharacterStyle()))

    val boundaries = buildSet {
        add(0)
        add(text.length)
        spans.forEach { span ->
            add(span.start.coerceIn(0, text.length))
            add(span.end.coerceIn(0, text.length))
        }
    }.sorted()

    val runs = boundaries.zipWithNext().mapNotNull { (start, end) ->
        if (end <= start) return@mapNotNull null
        val span = spans.lastOrNull { it.start <= start && it.end >= end }
        StyleRun(
            start = start,
            end = end,
            style = span?.toCharacterStyle() ?: CharacterStyle(),
        )
    }
    return normalizeRuns(runs, text.length)
}

private fun normalizeRuns(runs: List<StyleRun>, textLength: Int): List<StyleRun> {
    if (textLength == 0) return emptyList()
    val sorted = runs
        .mapNotNull { run ->
            val start = run.start.coerceIn(0, textLength)
            val end = run.end.coerceIn(start, textLength)
            if (end > start) run.copy(start = start, end = end) else null
        }
        .sortedBy { it.start }

    val filled = buildList {
        var cursor = 0
        sorted.forEach { run ->
            if (run.start > cursor) add(StyleRun(cursor, run.start, CharacterStyle()))
            val safeStart = maxOf(cursor, run.start)
            if (run.end > safeStart) add(run.copy(start = safeStart))
            cursor = maxOf(cursor, run.end)
        }
        if (cursor < textLength) add(StyleRun(cursor, textLength, CharacterStyle()))
    }

    return buildList {
        filled.forEach { run ->
            val previous = lastOrNull()
            if (previous != null && previous.end == run.start && previous.style == run.style) {
                removeAt(lastIndex)
                add(previous.copy(end = run.end))
            } else {
                add(run)
            }
        }
    }
}

private fun sliceRuns(
    runs: List<StyleRun>,
    start: Int,
    end: Int,
): List<StyleRun> {
    if (end <= start) return emptyList()
    return runs.mapNotNull { run ->
        val clippedStart = maxOf(run.start, start)
        val clippedEnd = minOf(run.end, end)
        if (clippedEnd > clippedStart) {
            run.copy(start = clippedStart, end = clippedEnd)
        } else {
            null
        }
    }
}

private fun NoteSpan.toCharacterStyle() = CharacterStyle(
    bold = bold,
    underline = underline,
    strikethrough = strikethrough,
    fontSizeSp = fontSizeSp.coerceIn(MIN_NOTE_FONT_SIZE_SP, MAX_NOTE_FONT_SIZE_SP),
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

private fun TextFieldValue.asPlainValue() = TextFieldValue(
    text = text,
    selection = selection,
    composition = composition,
)

private fun Char.isWordCharacter(): Boolean = isLetterOrDigit() || this == '-' || this == '’' || this == '\''

private fun Char.isSentenceBoundary(): Boolean = this == '.' || this == '!' || this == '?' || this == '\n'

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
