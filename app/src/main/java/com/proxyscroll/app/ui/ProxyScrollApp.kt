package com.proxyscroll.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.proxyscroll.app.domain.AppTheme
import com.proxyscroll.app.domain.InputMotion
import com.proxyscroll.app.domain.Note
import com.proxyscroll.app.domain.NoteSpan
import com.proxyscroll.app.ui.editor.MAX_NOTE_FONT_SIZE_SP
import com.proxyscroll.app.ui.editor.MIN_NOTE_FONT_SIZE_SP
import com.proxyscroll.app.ui.editor.RichTextState
import com.proxyscroll.app.ui.editor.annotatedText
import com.proxyscroll.app.ui.theme.LocalProxyVisualStyle
import com.proxyscroll.app.ui.theme.ProxyScrollTheme
import com.proxyscroll.app.ui.theme.ProxySurface
import com.proxyscroll.app.ui.theme.ProxyThemeBackground
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date

@Composable
fun ProxyScrollApp(
    viewModel: NotesViewModel,
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    inputMotion: InputMotion,
    onInputMotionSelected: (InputMotion) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editorOpen by remember { mutableStateOf(false) }
    var editorNote by remember { mutableStateOf<Note?>(null) }
    var showSettings by remember { mutableStateOf(false) }

    ProxyScrollTheme(selectedTheme = selectedTheme) {
        Box(Modifier.fillMaxSize()) {
            ProxyThemeBackground(
                selectedTheme = selectedTheme,
                modifier = Modifier.fillMaxSize(),
            )
            AnimatedContent(
                targetState = editorOpen,
                transitionSpec = {
                    if (targetState) {
                        (fadeIn(tween(300)) + slideInHorizontally(tween(380)) { it / 8 }) togetherWith
                            (fadeOut(tween(220)) + slideOutHorizontally(tween(300)) { -it / 12 })
                    } else {
                        (fadeIn(tween(300)) + slideInHorizontally(tween(360)) { -it / 10 }) togetherWith
                            (fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { it / 9 })
                    }
                },
                label = "notes-editor-transition",
            ) { isEditing ->
                if (isEditing) {
                    NoteEditorScreen(
                        note = editorNote,
                        inputMotion = inputMotion,
                        onSave = viewModel::save,
                        onDelete = viewModel::delete,
                        onClose = { editorOpen = false },
                    )
                } else {
                    NotesScreen(
                        state = state,
                        onQueryChange = viewModel::setQuery,
                        onCreate = {
                            editorNote = null
                            editorOpen = true
                        },
                        onEdit = {
                            editorNote = it
                            editorOpen = true
                        },
                        onTogglePinned = viewModel::togglePinned,
                        onOpenSettings = { showSettings = true },
                    )
                }
            }

            if (showSettings && !editorOpen) {
                SettingsSheet(
                    selectedTheme = selectedTheme,
                    onThemeSelected = onThemeSelected,
                    inputMotion = inputMotion,
                    onInputMotionSelected = onInputMotionSelected,
                    onDismiss = { showSettings = false },
                )
            }
        }
    }
}

@Composable
private fun Modifier.animatedClick(
    onClick: () -> Unit,
    pressedScale: Float = 0.965f,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "physical-press",
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesScreen(
    state: NotesUiState,
    onQueryChange: (String) -> Unit,
    onCreate: () -> Unit,
    onEdit: (Note) -> Unit,
    onTogglePinned: (Note) -> Unit,
    onOpenSettings: () -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "ProxyScroll",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                actions = {
                    ProxySurface(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(44.dp)
                            .animatedClick(onClick = onOpenSettings, pressedScale = 0.90f),
                        shape = CircleShape,
                        strong = true,
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Настройки",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        floatingActionButton = {
            ProxySurface(
                modifier = Modifier
                    .size(64.dp)
                    .animatedClick(onClick = onCreate, pressedScale = 0.90f),
                shape = RoundedCornerShape(25.dp),
                strong = true,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Новая заметка",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp),
                    )
                }
            }
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "Заметки",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            AnimatedContent(
                targetState = state.notes.size,
                transitionSpec = {
                    (fadeIn(tween(260)) + slideInVertically { it / 2 }) togetherWith
                        (fadeOut(tween(180)) + slideOutVertically { -it / 2 })
                },
                label = "note-count",
            ) { count ->
                Text(
                    text = "$count в текущем списке",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(16.dp))
            ProxySurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = tween(320)),
                shape = RoundedCornerShape(24.dp),
            ) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    placeholder = { Text("Найти заметку") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Очистить поиск")
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                )
            }
            Spacer(Modifier.height(16.dp))

            if (state.notes.isEmpty()) {
                EmptyNotes(
                    isSearching = state.query.isNotBlank(),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 88.dp),
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(
                        items = state.notes,
                        key = { _, note -> note.id },
                    ) { index, note ->
                        var visible by remember(note.id) { mutableStateOf(false) }
                        LaunchedEffect(note.id) {
                            delay((index.coerceAtMost(8) * 48L) + 30L)
                            visible = true
                        }
                        AnimatedVisibility(
                            visible = visible,
                            modifier = Modifier.animateItem(),
                            enter = fadeIn(tween(420)) +
                                slideInVertically(tween(480, easing = FastOutSlowInEasing)) {
                                    it / 3
                                } +
                                scaleIn(tween(460), initialScale = 0.975f),
                            exit = fadeOut(tween(220)) +
                                slideOutVertically(tween(260)) { -it / 5 } +
                                scaleOut(tween(220), targetScale = 0.98f),
                        ) {
                            NoteCard(
                                note = note,
                                onClick = { onEdit(note) },
                                onTogglePinned = { onTogglePinned(note) },
                            )
                        }
                    }
                    item { Spacer(Modifier.height(96.dp)) }
                }
            }
        }
    }
}

@Composable
private fun EmptyNotes(
    isSearching: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (isSearching) "Ничего не найдено" else "Здесь пока тихо",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isSearching) {
                    "Попробуйте изменить запрос"
                } else {
                    "Нажмите + и начните первую заметку"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePinned: () -> Unit,
) {
    ProxySurface(
        modifier = Modifier
            .fillMaxWidth()
            .animatedClick(onClick = onClick, pressedScale = 0.982f),
        strong = note.isPinned,
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = note.title.ifBlank { "Без названия" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onTogglePinned, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = if (note.isPinned) "Открепить" else "Закрепить",
                        tint = if (note.isPinned) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
            if (note.body.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = annotatedText(note.body, note.spans),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(13.dp))
            Text(
                text = DateFormat.getDateTimeInstance(
                    DateFormat.MEDIUM,
                    DateFormat.SHORT,
                ).format(Date(note.updatedAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private enum class EditorSaveState { CLEAN, EDITING, SAVED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteEditorScreen(
    note: Note?,
    inputMotion: InputMotion,
    onSave: (Note?, String, String, List<NoteSpan>) -> Note?,
    onDelete: (Note) -> Unit,
    onClose: () -> Unit,
) {
    var savedNote by remember(note?.id) { mutableStateOf(note) }
    var title by remember(note?.id) { mutableStateOf(note?.title.orEmpty()) }
    val richText = remember(note?.id) {
        RichTextState(note?.body.orEmpty(), note?.spans.orEmpty())
    }
    var hasChanges by remember(note?.id) { mutableStateOf(false) }
    var saveState by remember(note?.id) { mutableStateOf(EditorSaveState.CLEAN) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val bodyFocusRequester = remember(note?.id) { FocusRequester() }
    val bodyInteractionSource = remember(note?.id) { MutableInteractionSource() }
    val bodyFocused by bodyInteractionSource.collectIsFocusedAsState()
    val focusGlow by animateFloatAsState(
        targetValue = if (bodyFocused) 1f else 0f,
        animationSpec = tween(
            durationMillis = inputMotion.pulseMillis.coerceAtLeast(90),
            easing = FastOutSlowInEasing,
        ),
        label = "editor-focus-glow",
    )
    val automaticTitle = remember(richText.value.text) {
        richText.value.text
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(3)
            .joinToString(" ")
    }

    fun saveNow() {
        if (!hasChanges) return
        val result = onSave(
            savedNote,
            title,
            richText.value.text,
            richText.toSpans(),
        )
        if (result != null) savedNote = result
        hasChanges = false
        saveState = if (result == null) EditorSaveState.CLEAN else EditorSaveState.SAVED
    }

    fun finishEditing() {
        saveNow()
        onClose()
    }

    BackHandler(onBack = ::finishEditing)

    LaunchedEffect(note?.id) {
        delay(260)
        bodyFocusRequester.requestFocus()
    }

    LaunchedEffect(title, richText.revision) {
        if (!hasChanges) return@LaunchedEffect
        delay(inputMotion.autosaveDelayMillis)
        saveNow()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (savedNote == null) "Новая заметка" else "Редактор",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        AnimatedContent(
                            targetState = saveState,
                            label = "save-state",
                        ) { state ->
                            Text(
                                text = when (state) {
                                    EditorSaveState.CLEAN -> "Локально"
                                    EditorSaveState.EDITING -> "Сохраняю…"
                                    EditorSaveState.SAVED -> "Сохранено"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = ::finishEditing) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    AnimatedVisibility(visible = savedNote != null) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Удалить",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    IconButton(onClick = ::finishEditing) {
                        Icon(Icons.Default.Check, contentDescription = "Готово")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        bottomBar = {
            FormattingToolbar(
                richText = richText,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                onFormatChanged = {
                    hasChanges = true
                    saveState = EditorSaveState.EDITING
                },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 16.dp),
        ) {
            BasicTextField(
                value = title,
                onValueChange = {
                    title = it
                    hasChanges = true
                    saveState = EditorSaveState.EDITING
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 14.dp),
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box {
                        if (title.isEmpty()) {
                            Text(
                                text = "Название — необязательно",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
                            )
                        }
                        innerTextField()
                    }
                },
            )
            AnimatedVisibility(
                visible = title.isBlank() && automaticTitle.isNotBlank(),
                enter = fadeIn(tween(240)) + slideInVertically(tween(280)) { -it / 3 },
                exit = fadeOut(tween(160)) + slideOutVertically(tween(200)) { -it / 3 },
            ) {
                Text(
                    text = "Название: $automaticTitle",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 6.dp, bottom = 10.dp),
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f),
            )
            Spacer(Modifier.height(12.dp))
            val glowColor = MaterialTheme.colorScheme.primary
            ProxySurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .drawBehind {
                        if (focusGlow > 0f) {
                            drawRoundRect(
                                color = glowColor.copy(alpha = focusGlow * 0.15f),
                                cornerRadius = CornerRadius(32.dp.toPx()),
                            )
                        }
                    },
                shape = RoundedCornerShape(30.dp),
                strong = true,
            ) {
                BasicTextField(
                    value = richText.value,
                    onValueChange = {
                        richText.onValueChange(it)
                        hasChanges = true
                        saveState = EditorSaveState.EDITING
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(bodyFocusRequester)
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    interactionSource = bodyInteractionSource,
                    visualTransformation = richText.visualTransformation,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(Modifier.fillMaxSize()) {
                            if (richText.value.text.isEmpty()) {
                                Text(
                                    text = "Начните писать…",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Удалить заметку?") },
            text = { Text("Это действие нельзя отменить.") },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Отмена")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        savedNote?.let(onDelete)
                        showDeleteConfirmation = false
                        onClose()
                    },
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
        )
    }
}

@Composable
private fun FormattingToolbar(
    richText: RichTextState,
    modifier: Modifier = Modifier,
    onFormatChanged: () -> Unit,
) {
    ProxySurface(modifier = modifier.fillMaxWidth(), strong = true) {
        Column {
            AnimatedVisibility(
                visible = richText.hasSelection,
                enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 2 },
                exit = fadeOut(tween(130)) + slideOutVertically(tween(170)) { it / 2 },
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(start = 14.dp, end = 10.dp, top = 9.dp, bottom = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.width(118.dp)) {
                            Text(
                                text = "Фрагмент · ${richText.selectionLength}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = richText.selectedPreview.ifBlank { "Выделено" },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        SelectionLensButton("Слово", richText::selectWord)
                        SelectionLensButton("Фраза", richText::selectSentence)
                        SelectionLensButton("Абзац", richText::selectParagraph)
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FormatButton(
                    selected = richText.boldActive,
                    onClick = {
                        richText.toggleBold()
                        onFormatChanged()
                    },
                ) {
                    Icon(Icons.Default.FormatBold, contentDescription = "Жирный")
                }
                FormatButton(
                    selected = richText.underlineActive,
                    onClick = {
                        richText.toggleUnderline()
                        onFormatChanged()
                    },
                ) {
                    Icon(Icons.Default.FormatUnderlined, contentDescription = "Подчёркнутый")
                }
                FormatButton(
                    selected = richText.strikethroughActive,
                    onClick = {
                        richText.toggleStrikethrough()
                        onFormatChanged()
                    },
                ) {
                    Icon(Icons.Default.FormatStrikethrough, contentDescription = "Зачёркнутый")
                }
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                )
                FontSizeControl(
                    activeSize = richText.activeFontSizeSp,
                    onDecrease = {
                        richText.adjustFontSize(-2)
                        onFormatChanged()
                    },
                    onIncrease = {
                        richText.adjustFontSize(2)
                        onFormatChanged()
                    },
                    onCustomSize = {
                        richText.setFontSize(it)
                        onFormatChanged()
                    },
                )
            }
        }
    }
}

@Composable
private fun SelectionLensButton(
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f))
            .animatedClick(onClick = onClick, pressedScale = 0.92f)
            .padding(horizontal = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun FormatButton(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)
                } else {
                    Color.Transparent
                },
            )
            .animatedClick(onClick = onClick, pressedScale = 0.88f),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun FontSizeControl(
    activeSize: Int?,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onCustomSize: (Int) -> Unit,
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .animatedClick(onClick = onDecrease, pressedScale = 0.86f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Уменьшить на 2")
        }
        Row(
            modifier = Modifier
                .height(40.dp)
                .animatedClick(
                    onClick = { showCustomDialog = true },
                    pressedScale = 0.94f,
                )
                .padding(horizontal = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.FormatSize, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = activeSize?.toString() ?: "mix",
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .animatedClick(onClick = onIncrease, pressedScale = 0.86f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Add, contentDescription = "Увеличить на 2")
        }
    }

    if (showCustomDialog) {
        var customSize by remember(activeSize) {
            mutableStateOf((activeSize ?: 19).toString())
        }
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("Точный размер текста") },
            text = {
                Column {
                    Text(
                        text = "От $MIN_NOTE_FONT_SIZE_SP до $MAX_NOTE_FONT_SIZE_SP sp",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customSize,
                        onValueChange = { value ->
                            customSize = value.filter(Char::isDigit).take(2)
                        },
                        singleLine = true,
                        label = { Text("Размер") },
                        suffix = { Text("sp") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text("Отмена")
                }
            },
            confirmButton = {
                val parsedSize = customSize.toIntOrNull()
                TextButton(
                    enabled = parsedSize != null &&
                        parsedSize in MIN_NOTE_FONT_SIZE_SP..MAX_NOTE_FONT_SIZE_SP,
                    onClick = {
                        parsedSize?.let(onCustomSize)
                        showCustomDialog = false
                    },
                ) {
                    Text("Применить")
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    inputMotion: InputMotion,
    onInputMotionSelected: (InputMotion) -> Unit,
    onDismiss: () -> Unit,
) {
    val style = LocalProxyVisualStyle.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        scrimColor = style.scrim,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Text("Настройки", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Материал интерфейса",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(18.dp))
            ThemeOption(
                theme = AppTheme.LIQUID_GLASS,
                title = "Liquid Glass",
                description = "Преломлённый свет, прозрачность и живая глубина",
                isSelected = selectedTheme == AppTheme.LIQUID_GLASS,
                onClick = { onThemeSelected(AppTheme.LIQUID_GLASS) },
            )
            Spacer(Modifier.height(10.dp))
            ThemeOption(
                theme = AppTheme.ROYAL_GRAPHITE,
                title = "Royal Graphite",
                description = "Холодный уголь, слоистый графит и мокрый блеск",
                isSelected = selectedTheme == AppTheme.ROYAL_GRAPHITE,
                onClick = { onThemeSelected(AppTheme.ROYAL_GRAPHITE) },
            )
            Spacer(Modifier.height(22.dp))
            Text("Плавность ввода", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Настраивает движение фокуса и ритм автосохранения",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MotionOption(
                    label = "Прямо",
                    selected = inputMotion == InputMotion.DIRECT,
                    onClick = { onInputMotionSelected(InputMotion.DIRECT) },
                    modifier = Modifier.weight(1f),
                )
                MotionOption(
                    label = "Мягко",
                    selected = inputMotion == InputMotion.GENTLE,
                    onClick = { onInputMotionSelected(InputMotion.GENTLE) },
                    modifier = Modifier.weight(1f),
                )
                MotionOption(
                    label = "Текуче",
                    selected = inputMotion == InputMotion.FLOWING,
                    onClick = { onInputMotionSelected(InputMotion.FLOWING) },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f))
            Spacer(Modifier.height(12.dp))
            Text(
                text = "ProxyScroll 0.4.0-alpha05",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MotionOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProxySurface(
        modifier = modifier
            .height(48.dp)
            .animatedClick(onClick = onClick, pressedScale = 0.95f),
        shape = RoundedCornerShape(18.dp),
        strong = selected,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(3.dp))
            }
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ThemeOption(
    theme: AppTheme,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    ProxySurface(
        modifier = Modifier
            .fillMaxWidth()
            .animatedClick(onClick = onClick, pressedScale = 0.985f),
        strong = isSelected,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ThemeSwatch(theme)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(3.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Выбрано",
                    tint = MaterialTheme.colorScheme.primary,
                )
            } else {
                Box(
                    Modifier
                        .size(22.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.58f),
                            CircleShape,
                        ),
                )
            }
        }
    }
}

@Composable
private fun ThemeSwatch(theme: AppTheme) {
    val shape = RoundedCornerShape(18.dp)
    Canvas(
        modifier = Modifier
            .size(66.dp)
            .clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.34f), shape),
    ) {
        if (theme == AppTheme.LIQUID_GLASS) {
            drawRect(
                brush = Brush.linearGradient(
                    listOf(Color(0xFFE7EDFF), Color(0xFFC8F0EF)),
                ),
            )
            drawCircle(
                color = Color(0xFF6E7BE9).copy(alpha = 0.45f),
                radius = size.width * 0.48f,
                center = Offset(size.width * 0.18f, size.height * 0.18f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.72f), Color.Transparent),
                ),
                radius = size.width * 0.42f,
                center = Offset(size.width * 0.72f, size.height * 0.68f),
            )
        } else {
            drawRect(
                brush = Brush.linearGradient(
                    listOf(Color(0xFF30393D), Color(0xFF090C0E)),
                ),
            )
            repeat(12) { index ->
                val x = size.width * index / 11f
                drawLine(
                    color = Color(0xFFC6D7DD).copy(alpha = if (index % 4 == 0) 0.16f else 0.05f),
                    start = Offset(x, 0f),
                    end = Offset(x - 3f, size.height),
                    strokeWidth = if (index % 4 == 0) 1.2f else 0.6f,
                )
            }
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFB8D0D9).copy(alpha = 0.12f), Color.Transparent),
                ),
                center = Offset(size.width * 0.28f, size.height * 0.22f),
                radius = size.width * 0.55f,
            )
        }
    }
}
