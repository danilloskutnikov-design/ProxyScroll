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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.proxyscroll.app.BuildConfig
import com.proxyscroll.app.domain.AppTheme
import com.proxyscroll.app.domain.InputMotion
import com.proxyscroll.app.domain.InterfaceShape
import com.proxyscroll.app.domain.MaterialDepth
import com.proxyscroll.app.domain.MAX_STAIN_INTENSITY
import com.proxyscroll.app.domain.MIN_STAIN_INTENSITY
import com.proxyscroll.app.domain.MAX_INTERFACE_CORNER_DP
import com.proxyscroll.app.domain.MIN_INTERFACE_CORNER_DP
import com.proxyscroll.app.domain.Note
import com.proxyscroll.app.domain.NoteColorFlag
import com.proxyscroll.app.domain.NoteSpan
import com.proxyscroll.app.domain.StainMotion
import com.proxyscroll.app.domain.StainPalette
import com.proxyscroll.app.domain.StainSettings
import com.proxyscroll.app.ui.editor.MAX_NOTE_FONT_SIZE_SP
import com.proxyscroll.app.ui.editor.MIN_NOTE_FONT_SIZE_SP
import com.proxyscroll.app.ui.editor.RichTextState
import com.proxyscroll.app.ui.editor.annotatedText
import com.proxyscroll.app.ui.theme.LocalProxyShape
import com.proxyscroll.app.ui.theme.LocalStainSettings
import com.proxyscroll.app.ui.theme.ProxyBrandLockup
import com.proxyscroll.app.ui.theme.ProxyScrollTheme
import com.proxyscroll.app.ui.theme.ProxyInsetSurface
import com.proxyscroll.app.ui.theme.ProxySettingsFog
import com.proxyscroll.app.ui.theme.ProxySurface
import com.proxyscroll.app.ui.theme.ProxySurfaceRole
import com.proxyscroll.app.ui.theme.ProxyThemeBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun ProxyScrollApp(
    viewModel: NotesViewModel,
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    inputMotion: InputMotion,
    onInputMotionSelected: (InputMotion) -> Unit,
    interfaceShape: InterfaceShape,
    onInterfaceShapeChanged: (InterfaceShape) -> Unit,
    stainSettings: StainSettings,
    onStainSettingsChanged: (StainSettings) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val appScope = rememberCoroutineScope()
    var editorOpen by remember { mutableStateOf(false) }
    var editorNote by remember { mutableStateOf<Note?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var typingQuiet by remember { mutableStateOf(false) }
    var scrollingQuiet by remember { mutableStateOf(false) }
    val settingsChromeShape = remember(showSettings) { interfaceShape }
    val settingsFogProgress by animateFloatAsState(
        targetValue = if (showSettings && !editorOpen) 1f else 0f,
        animationSpec = tween(520, easing = FastOutSlowInEasing),
        label = "settings-fog-progress",
    )
    val settingsFogRadius by animateDpAsState(
        targetValue = if (showSettings && !editorOpen) 4.dp else 0.dp,
        animationSpec = tween(360, easing = FastOutSlowInEasing),
        label = "settings-fog-radius",
    )
    val settingsBackgroundScale by animateFloatAsState(
        targetValue = if (showSettings && !editorOpen) 0.985f else 1f,
        animationSpec = tween(520, easing = FastOutSlowInEasing),
        label = "settings-background-depth",
    )

    ProxyScrollTheme(
        selectedTheme = selectedTheme,
        interfaceShape = interfaceShape,
        stainSettings = stainSettings,
        motionQuiet = typingQuiet || scrollingQuiet,
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0f)
                    .blur(radius = settingsFogRadius)
                    .graphicsLayer {
                        scaleX = settingsBackgroundScale
                        scaleY = settingsBackgroundScale
                    },
            ) {
                ProxyThemeBackground(
                    selectedTheme = selectedTheme,
                    modifier = Modifier.fillMaxSize(),
                )
                AnimatedContent(
                    targetState = editorOpen,
                    transitionSpec = {
                        if (targetState) {
                            (fadeIn(tween(300)) +
                                slideInHorizontally(tween(380)) { it / 8 } +
                                scaleIn(tween(380), initialScale = 0.955f)) togetherWith
                                (fadeOut(tween(220)) +
                                    slideOutHorizontally(tween(300)) { -it / 12 } +
                                    scaleOut(tween(260), targetScale = 0.975f))
                        } else {
                            (fadeIn(tween(300)) +
                                slideInHorizontally(tween(360)) { -it / 10 } +
                                scaleIn(tween(380), initialScale = 0.965f)) togetherWith
                                (fadeOut(tween(200)) +
                                    slideOutHorizontally(tween(300)) { it / 9 } +
                                    scaleOut(tween(250), targetScale = 0.98f))
                        }
                    },
                    label = "notes-editor-transition",
                ) { isEditing ->
                    if (isEditing) {
                        NoteEditorScreen(
                            note = editorNote,
                            inputMotion = inputMotion,
                            onSave = viewModel::save,
                            onDelete = { deletedNote ->
                                viewModel.delete(deletedNote)
                                editorOpen = false
                                appScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Заметка удалена",
                                        actionLabel = "Отменить",
                                        duration = SnackbarDuration.Long,
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.restore(deletedNote)
                                    }
                                }
                            },
                            onClose = { editorOpen = false },
                            onTypingQuietChanged = { typingQuiet = it },
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
                            onColorFlagChanged = viewModel::setColorFlag,
                            onScrollQuietChanged = { scrollingQuiet = it },
                            onOpenSettings = { showSettings = true },
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showSettings && !editorOpen,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f),
                enter = fadeIn(tween(220)) +
                    slideInVertically(tween(420)) { it / 8 } +
                    scaleIn(tween(420), initialScale = 0.955f),
                exit = fadeOut(tween(180)) +
                    slideOutVertically(tween(300)) { it / 10 } +
                    scaleOut(tween(260), targetScale = 0.98f),
            ) {
                Box(Modifier.fillMaxSize()) {
                    ProxySettingsFog(
                        selectedTheme = selectedTheme,
                        progress = settingsFogProgress,
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(0f),
                    )
                    CompositionLocalProvider(
                        LocalProxyShape provides settingsChromeShape,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(1f),
                        ) {
                            SettingsSheet(
                                selectedTheme = selectedTheme,
                                onThemeSelected = onThemeSelected,
                                inputMotion = inputMotion,
                                onInputMotionSelected = onInputMotionSelected,
                                interfaceShape = interfaceShape,
                                onInterfaceShapeChanged = onInterfaceShapeChanged,
                                stainSettings = stainSettings,
                                onStainSettingsChanged = onStainSettingsChanged,
                                onDismiss = { showSettings = false },
                            )
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(20f)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) { snackbarData ->
                ProxySurface(
                    modifier = Modifier.fillMaxWidth(),
                    role = ProxySurfaceRole.OVERLAY,
                    strong = true,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = snackbarData.visuals.message,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        snackbarData.visuals.actionLabel?.let { actionLabel ->
                            TextButton(onClick = snackbarData::performAction) {
                                Text(actionLabel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Modifier.animatedClick(
    onClick: () -> Unit,
    pressedScale: Float = 0.975f,
    enabled: Boolean = true,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
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
            enabled = enabled,
            onClick = onClick,
        )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Modifier.animatedCombinedClick(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    pressedScale: Float = 0.985f,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "physical-combined-press",
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
            onLongClickLabel = "Изменить цвет заметки",
            onLongClick = onLongClick,
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
    onColorFlagChanged: (Note, NoteColorFlag) -> Unit,
    onScrollQuietChanged: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val searchInteractionSource = remember { MutableInteractionSource() }
    val searchFocused by searchInteractionSource.collectIsFocusedAsState()
    val noteGroups = remember(state.notes) { groupNotesByFlag(state.notes) }
    val listState = rememberLazyListState()
    val currentScrollQuietChanged by rememberUpdatedState(onScrollQuietChanged)
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collectLatest { scrolling ->
                if (scrolling) {
                    currentScrollQuietChanged(true)
                } else {
                    delay(180)
                    currentScrollQuietChanged(false)
                }
            }
    }
    DisposableEffect(Unit) {
        onDispose { currentScrollQuietChanged(false) }
    }
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    ProxyBrandLockup()
                },
                actions = {
                    ProxySurface(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(44.dp)
                            .animatedClick(onClick = onOpenSettings, pressedScale = 0.96f),
                        role = ProxySurfaceRole.BUTTON,
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
                    .size(60.dp)
                    .animatedClick(onClick = onCreate, pressedScale = 0.96f),
                role = ProxySurfaceRole.BUTTON,
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
                    text = notesCountLabel(count),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
            ProxySurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = tween(320)),
                role = ProxySurfaceRole.INPUT,
                active = searchFocused || state.query.isNotEmpty(),
            ) {
                val searchShape = RoundedCornerShape(
                    LocalProxyShape.current.resolvedInputCornerDp.dp,
                )
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    interactionSource = searchInteractionSource,
                    singleLine = true,
                    shape = searchShape,
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
            Spacer(Modifier.height(12.dp))

            if (state.notes.isEmpty()) {
                EmptyNotes(
                    isSearching = state.query.isNotBlank(),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 88.dp),
                )
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    noteGroups.forEach { group ->
                        item(key = "flag-group-${group.flag.storageKey}") {
                            NoteGroupHeader(
                                group = group,
                                singleGroup = noteGroups.size == 1,
                                modifier = Modifier.animateItem(),
                            )
                        }
                        itemsIndexed(
                            items = group.notes,
                            key = { _, note -> note.id },
                        ) { _, note ->
                            NoteCard(
                                note = note,
                                onClick = { onEdit(note) },
                                onTogglePinned = { onTogglePinned(note) },
                                onColorFlagChanged = { flag ->
                                    onColorFlagChanged(note, flag)
                                },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                    item { Spacer(Modifier.height(96.dp)) }
                }
            }
        }
    }
}

private data class NoteFlagGroup(
    val flag: NoteColorFlag,
    val notes: List<Note>,
)

private fun groupNotesByFlag(notes: List<Note>): List<NoteFlagGroup> {
    val groupOrder = NoteColorFlag.entries.filterNot { it == NoteColorFlag.NONE } +
        NoteColorFlag.NONE
    return groupOrder.mapNotNull { flag ->
        notes.filter { it.colorFlag == flag }
            .takeIf { it.isNotEmpty() }
            ?.let { NoteFlagGroup(flag = flag, notes = it) }
    }
}

private fun noteFlagColor(flag: NoteColorFlag): Color = when (flag) {
    NoteColorFlag.NONE -> Color(0xFF8B93A7)
    NoteColorFlag.SKY -> Color(0xFF65B9FF)
    NoteColorFlag.VIOLET -> Color(0xFF9B80FF)
    NoteColorFlag.CORAL -> Color(0xFFFF8E8A)
    NoteColorFlag.MINT -> Color(0xFF59D4B1)
    NoteColorFlag.AMBER -> Color(0xFFF3BC62)
}

@Composable
private fun NoteGroupHeader(
    group: NoteFlagGroup,
    singleGroup: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 8.dp, end = 4.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(if (group.flag == NoteColorFlag.NONE) 7.dp else 9.dp)
                .clip(CircleShape)
                .background(noteFlagColor(group.flag)),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (singleGroup && group.flag == NoteColorFlag.NONE) {
                "Все заметки"
            } else {
                group.flag.displayName
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = group.notes.size.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = noteFlagColor(group.flag).copy(alpha = 0.92f),
        )
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

private fun notesCountLabel(count: Int): String {
    val ending = when {
        count % 100 in 11..14 -> "заметок"
        count % 10 == 1 -> "заметка"
        count % 10 in 2..4 -> "заметки"
        else -> "заметок"
    }
    return "$count $ending"
}

@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePinned: () -> Unit,
    onColorFlagChanged: (NoteColorFlag) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showFlagMenu by remember(note.id) { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxWidth()) {
        ProxySurface(
            modifier = Modifier
                .fillMaxWidth()
                .animatedCombinedClick(
                    onClick = onClick,
                    onLongClick = { showFlagMenu = true },
                ),
            strong = note.isPinned,
            role = ProxySurfaceRole.CARD,
            interactive = false,
        ) {
            Box {
                if (note.colorFlag != NoteColorFlag.NONE) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .width(4.dp)
                            .height(54.dp)
                            .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        noteFlagColor(note.colorFlag).copy(alpha = 0.95f),
                                        noteFlagColor(note.colorFlag).copy(alpha = 0.44f),
                                    ),
                                ),
                            ),
                    )
                }
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
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
                        IconButton(onClick = onTogglePinned, modifier = Modifier.size(36.dp)) {
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
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = annotatedText(note.body, note.spans),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.height(9.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = DateFormat.getDateTimeInstance(
                            DateFormat.MEDIUM,
                            DateFormat.SHORT,
                        ).format(Date(note.updatedAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    if (note.colorFlag != NoteColorFlag.NONE) {
                        Text(
                            text = note.colorFlag.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = noteFlagColor(note.colorFlag),
                        )
                    }
                    }
                }
            }
        }
        NoteFlagDropdown(
            expanded = showFlagMenu,
            selected = note.colorFlag,
            helperText = "Цвет применяется сразу",
            onDismiss = { showFlagMenu = false },
            onSelected = { flag ->
                showFlagMenu = false
                onColorFlagChanged(flag)
            },
        )
    }
}

@Composable
private fun NoteFlagDropdown(
    expanded: Boolean,
    selected: NoteColorFlag,
    onSelected: (NoteColorFlag) -> Unit,
    onDismiss: () -> Unit,
    helperText: String,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.width(258.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = "Цвет заметки",
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(2.dp))
            AnimatedContent(
                targetState = selected,
                transitionSpec = {
                    fadeIn(tween(170)) togetherWith fadeOut(tween(120))
                },
                label = "note-flag-menu-label",
            ) { flag ->
                Text(
                    text = flag.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (flag == NoteColorFlag.NONE) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        noteFlagColor(flag)
                    },
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NoteColorFlag.entries.forEach { flag ->
                    NoteFlagSwatch(
                        flag = flag,
                        selected = flag == selected,
                        onClick = { onSelected(flag) },
                    )
                }
            }
            Spacer(Modifier.height(5.dp))
            Text(
                text = helperText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NoteFlagMenuButton(
    selected: NoteColorFlag,
    onSelected: (NoteColorFlag) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Цвет заметки",
                    tint = if (selected == NoteColorFlag.NONE) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        noteFlagColor(selected)
                    },
                )
                if (selected != NoteColorFlag.NONE) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(noteFlagColor(selected)),
                    )
                }
            }
        }
        NoteFlagDropdown(
            expanded = expanded,
            selected = selected,
            helperText = "Палитра не занимает место в тексте",
            onDismiss = { expanded = false },
            onSelected = { flag ->
                expanded = false
                onSelected(flag)
            },
        )
    }
}

@Composable
private fun NoteFlagSwatch(
    flag: NoteColorFlag,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val swatchSize by animateDpAsState(
        targetValue = if (selected) 26.dp else 20.dp,
        animationSpec = spring(dampingRatio = 0.66f, stiffness = 440f),
        label = "flag-swatch-${flag.storageKey}",
    )
    Box(
        modifier = Modifier
            .size(32.dp)
            .animatedClick(onClick = onClick, pressedScale = 0.86f),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(swatchSize)) {
            if (selected) {
                drawCircle(
                    color = noteFlagColor(flag).copy(alpha = 0.18f),
                    radius = size.minDimension * 0.54f,
                )
            }
            if (flag == NoteColorFlag.NONE) {
                drawCircle(
                    color = outlineColor.copy(alpha = 0.68f),
                    radius = size.minDimension * 0.38f,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = if (selected) 2.2.dp.toPx() else 1.3.dp.toPx(),
                    ),
                )
                drawLine(
                    color = outlineColor.copy(alpha = 0.72f),
                    start = Offset(size.width * 0.30f, size.height * 0.70f),
                    end = Offset(size.width * 0.70f, size.height * 0.30f),
                    strokeWidth = 1.4.dp.toPx(),
                )
            } else {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.58f),
                            noteFlagColor(flag),
                            noteFlagColor(flag).copy(alpha = 0.72f),
                        ),
                        center = Offset(size.width * 0.34f, size.height * 0.28f),
                        radius = size.minDimension * 0.72f,
                    ),
                    radius = size.minDimension * 0.38f,
                )
            }
        }
    }
}

private enum class EditorSaveState { CLEAN, EDITING, SAVED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteEditorScreen(
    note: Note?,
    inputMotion: InputMotion,
    onSave: (Note?, String, String, List<NoteSpan>, NoteColorFlag) -> Note?,
    onDelete: (Note) -> Unit,
    onClose: () -> Unit,
    onTypingQuietChanged: (Boolean) -> Unit,
) {
    var savedNote by remember(note?.id) { mutableStateOf(note) }
    var title by remember(note?.id) { mutableStateOf(note?.title.orEmpty()) }
    var colorFlag by remember(note?.id) {
        mutableStateOf(note?.colorFlag ?: NoteColorFlag.NONE)
    }
    val richText = remember(note?.id) {
        RichTextState(note?.body.orEmpty(), note?.spans.orEmpty())
    }
    var hasChanges by remember(note?.id) { mutableStateOf(false) }
    var saveState by remember(note?.id) { mutableStateOf(EditorSaveState.CLEAN) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEditorMenu by remember { mutableStateOf(false) }
    val bodyFocusRequester = remember(note?.id) { FocusRequester() }
    val bodyInteractionSource = remember(note?.id) { MutableInteractionSource() }
    var bodyTextLayout by remember(note?.id) { mutableStateOf<TextLayoutResult?>(null) }
    var typingPulseIndex by remember(note?.id) { mutableIntStateOf(-1) }
    var typingPulseRevision by remember(note?.id) { mutableIntStateOf(0) }
    val typingReveal = remember(note?.id) { Animatable(1f) }
    val bodyFocused by bodyInteractionSource.collectIsFocusedAsState()
    val focusGlow by animateFloatAsState(
        targetValue = if (bodyFocused) 1f else 0f,
        animationSpec = tween(
            durationMillis = inputMotion.pulseMillis.coerceAtLeast(90),
            easing = FastOutSlowInEasing,
        ),
        label = "editor-focus-glow",
    )
    val editorPlaneAlpha by animateFloatAsState(
        targetValue = if (bodyFocused) 0.34f else 0.18f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "editor-content-contrast",
    )
    val titleVerticalPadding by animateDpAsState(
        targetValue = if (bodyFocused) 6.dp else 14.dp,
        animationSpec = tween(240, easing = FastOutSlowInEasing),
        label = "editor-title-space",
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
            colorFlag,
        )
        if (result != null) savedNote = result
        hasChanges = false
        saveState = if (result == null) EditorSaveState.CLEAN else EditorSaveState.SAVED
    }

    fun finishEditing() {
        saveNow()
        onClose()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val saveBeforeBackground by rememberUpdatedState(newValue = { saveNow() })
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) saveBeforeBackground()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            saveBeforeBackground()
            onTypingQuietChanged(false)
        }
    }

    BackHandler(onBack = ::finishEditing)

    LaunchedEffect(note?.id) {
        delay(260)
        bodyFocusRequester.requestFocus()
    }

    LaunchedEffect(title, richText.revision, colorFlag) {
        if (!hasChanges) return@LaunchedEffect
        delay(inputMotion.autosaveDelayMillis)
        saveNow()
    }

    LaunchedEffect(title, richText.revision) {
        if (!hasChanges) return@LaunchedEffect
        onTypingQuietChanged(true)
        delay(600)
        onTypingQuietChanged(false)
    }

    LaunchedEffect(typingPulseRevision) {
        if (typingPulseRevision == 0) return@LaunchedEffect
        typingReveal.snapTo(0f)
        typingReveal.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = (inputMotion.pulseMillis * 2.3f)
                    .roundToInt()
                    .coerceIn(380, 680),
                easing = LinearOutSlowInEasing,
            ),
        )
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
                    NoteFlagMenuButton(
                        selected = colorFlag,
                        onSelected = { selectedFlag ->
                            if (colorFlag != selectedFlag) {
                                colorFlag = selectedFlag
                                hasChanges = true
                                saveState = EditorSaveState.EDITING
                            }
                        },
                    )
                    if (savedNote != null) {
                        Box {
                            IconButton(onClick = { showEditorMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Ещё")
                            }
                            DropdownMenu(
                                expanded = showEditorMenu,
                                onDismissRequest = { showEditorMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Удалить заметку") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    },
                                    onClick = {
                                        showEditorMenu = false
                                        showDeleteConfirmation = true
                                    },
                                )
                            }
                        }
                    }
                    ProxySurface(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(44.dp)
                            .animatedClick(onClick = ::finishEditing, pressedScale = 0.96f),
                        role = ProxySurfaceRole.BUTTON,
                        strong = true,
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, contentDescription = "Готово")
                        }
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
                    .then(if (bodyFocused) Modifier else Modifier.navigationBarsPadding())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
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
                    .animateContentSize(animationSpec = tween(240))
                    .padding(horizontal = 6.dp, vertical = titleVerticalPadding),
                textStyle = (if (bodyFocused) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.headlineMedium
                }).copy(
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
                                style = if (bodyFocused) {
                                    MaterialTheme.typography.titleLarge
                                } else {
                                    MaterialTheme.typography.headlineMedium
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
                            )
                        }
                        innerTextField()
                    }
                },
            )
            AnimatedVisibility(
                visible = !bodyFocused && title.isBlank() && automaticTitle.isNotBlank(),
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
            Spacer(Modifier.height(if (bodyFocused) 7.dp else 12.dp))
            val glowColor = MaterialTheme.colorScheme.primary
            val editorCornerDp = LocalProxyShape.current.resolvedInputCornerDp
            val editorMorphCorner by animateDpAsState(
                targetValue = (editorCornerDp + if (bodyFocused) 6 else 0).dp,
                animationSpec = spring(
                    dampingRatio = 0.74f,
                    stiffness = 360f,
                ),
                label = "editor-liquid-corner",
            )
            val editorShape = RoundedCornerShape(editorMorphCorner)
            ProxySurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = editorShape,
                role = ProxySurfaceRole.INPUT,
                strong = bodyFocused,
                active = bodyFocused,
                deformContent = false,
                interactive = false,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = editorPlaneAlpha))
                        .drawBehind {
                            if (focusGlow > 0f) {
                                drawRoundRect(
                                    color = glowColor.copy(alpha = focusGlow * 0.10f),
                                    cornerRadius = CornerRadius(editorMorphCorner.toPx()),
                                )
                                drawRoundRect(
                                    color = glowColor.copy(alpha = focusGlow * 0.20f),
                                    cornerRadius = CornerRadius(editorMorphCorner.toPx()),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 1.dp.toPx(),
                                    ),
                                )
                            }
                        },
                ) {
                    TypingOpticalTrail(
                        layoutResult = bodyTextLayout,
                        characterIndex = typingPulseIndex,
                        reveal = typingReveal.value,
                        color = if (colorFlag == NoteColorFlag.NONE) {
                            glowColor
                        } else {
                            noteFlagColor(colorFlag)
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                    BasicTextField(
                    value = richText.value,
                    onValueChange = { nextValue ->
                        val previousLength = richText.value.text.length
                        if (
                            nextValue.text.length > previousLength &&
                            nextValue.selection.collapsed
                        ) {
                            typingPulseIndex = (nextValue.selection.end - 1)
                                .coerceAtLeast(0)
                            typingPulseRevision++
                        }
                        richText.onValueChange(nextValue)
                        hasChanges = true
                        saveState = EditorSaveState.EDITING
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(bodyFocusRequester)
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    interactionSource = bodyInteractionSource,
                    onTextLayout = { bodyTextLayout = it },
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
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Удалить заметку?") },
            text = { Text("После удаления заметку можно сразу вернуть.") },
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
private fun TypingOpticalTrail(
    layoutResult: TextLayoutResult?,
    characterIndex: Int,
    reveal: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val layout = layoutResult ?: return@Canvas
        if (characterIndex !in 0 until layout.layoutInput.text.length) return@Canvas
        val glyphBox = layout.getBoundingBox(characterIndex)
        val progress = reveal.coerceIn(0f, 1f)
        val fade = (1f - progress) * (1f - progress * 0.42f)
        if (fade <= 0.002f) return@Canvas

        val padding = Offset(20.dp.toPx(), 18.dp.toPx())
        val center = padding + glyphBox.center + Offset(0f, glyphBox.height * 0.18f)
        val baseRadius = maxOf(glyphBox.height * 2.45f, 30.dp.toPx())
        val radius = baseRadius * (0.88f + progress * 0.62f)
        val tailCenter = center - Offset(radius * 0.72f, 0f)

        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = fade * 0.075f),
                    color.copy(alpha = fade * 0.036f),
                    Color.Transparent,
                ),
                center = tailCenter,
                radius = radius * 1.72f,
            ),
            topLeft = Offset(
                x = center.x - radius * 1.92f,
                y = center.y - radius * 0.64f,
            ),
            size = Size(radius * 3.35f, radius * 1.28f),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = fade * 0.045f),
                    color.copy(alpha = fade * 0.035f),
                    Color.Transparent,
                ),
                center = center,
                radius = radius * 0.74f,
            ),
            center = center,
            radius = radius * 0.74f,
        )
    }
}

@Composable
private fun FormattingToolbar(
    richText: RichTextState,
    modifier: Modifier = Modifier,
    onFormatChanged: () -> Unit,
) {
    ProxySurface(
        modifier = modifier.fillMaxWidth(),
        role = ProxySurfaceRole.OVERLAY,
        strong = true,
        active = richText.hasSelection,
    ) {
        Column {
            AnimatedVisibility(
                visible = richText.hasSelection,
                enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 2 },
                exit = fadeOut(tween(130)) + slideOutVertically(tween(170)) { it / 2 },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 12.dp, end = 9.dp, top = 6.dp, bottom = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Выделено · ${richText.selectionLength}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 2.dp),
                        maxLines = 1,
                    )
                    SelectionLensButton("Слово", richText::selectWord)
                    SelectionLensButton("Фраза", richText::selectSentence)
                    SelectionLensButton("Абзац", richText::selectParagraph)
                }
            }
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FormatButton(
                    selected = false,
                    enabled = richText.canUndo,
                    onClick = {
                        richText.undo()
                        onFormatChanged()
                    },
                ) {
                    Icon(Icons.Default.Undo, contentDescription = "Отменить")
                }
                FormatButton(
                    selected = false,
                    enabled = richText.canRedo,
                    onClick = {
                        richText.redo()
                        onFormatChanged()
                    },
                ) {
                    Icon(Icons.Default.Redo, contentDescription = "Повторить")
                }
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                )
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
                FormatButton(
                    selected = false,
                    enabled = richText.hasSelection,
                    onClick = {
                        richText.clearFormatting()
                        onFormatChanged()
                    },
                ) {
                    Icon(Icons.Default.FormatClear, contentDescription = "Сбросить форматирование")
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
            .height(30.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f))
            .animatedClick(onClick = onClick, pressedScale = 0.92f)
            .padding(horizontal = 10.dp),
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
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.34f }
            .clip(CircleShape)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)
                } else {
                    Color.Transparent
                },
            )
            .animatedClick(
                onClick = onClick,
                pressedScale = 0.88f,
                enabled = enabled,
            ),
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
            .height(40.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .animatedClick(onClick = onDecrease, pressedScale = 0.86f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Уменьшить на 2")
        }
        Row(
            modifier = Modifier
                .height(36.dp)
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
                .size(36.dp)
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
    interfaceShape: InterfaceShape,
    onInterfaceShapeChanged: (InterfaceShape) -> Unit,
    stainSettings: StainSettings,
    onStainSettingsChanged: (StainSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetCorner = (LocalProxyShape.current.globalCornerDp + 8).coerceAtMost(32).dp
    val dismissInteraction = remember { MutableInteractionSource() }
    BackHandler(onBack = onDismiss)

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101728).copy(alpha = 0.055f))
                .clickable(
                    interactionSource = dismissInteraction,
                    indication = null,
                    onClick = onDismiss,
                ),
        )
        ProxySurface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(
                topStart = sheetCorner,
                topEnd = sheetCorner,
            ),
            role = ProxySurfaceRole.OVERLAY,
            strong = true,
            active = false,
            deformContent = false,
            interactive = false,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.surface.copy(
                            alpha = when (selectedTheme) {
                                AppTheme.LIQUID_GLASS -> 0.70f
                                AppTheme.ROYAL_GRAPHITE -> 0.86f
                                AppTheme.OLD_SCROLL -> 0.88f
                            },
                        ),
                    )
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 14.dp, bottom = 24.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(42.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f)),
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Настройки", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = "Материал и пластика интерфейса",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        onClick = { onInterfaceShapeChanged(InterfaceShape()) },
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Сбросить форму")
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть настройки")
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CompactThemeOption(
                        theme = AppTheme.LIQUID_GLASS,
                        title = "Liquid Glass",
                        selected = selectedTheme == AppTheme.LIQUID_GLASS,
                        onClick = { onThemeSelected(AppTheme.LIQUID_GLASS) },
                        modifier = Modifier.weight(1f),
                    )
                    CompactThemeOption(
                        theme = AppTheme.ROYAL_GRAPHITE,
                        title = "Royal Graphite",
                        selected = selectedTheme == AppTheme.ROYAL_GRAPHITE,
                        onClick = { onThemeSelected(AppTheme.ROYAL_GRAPHITE) },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                CompactThemeOption(
                    theme = AppTheme.OLD_SCROLL,
                    title = "OldScroll",
                    selected = selectedTheme == AppTheme.OLD_SCROLL,
                    onClick = { onThemeSelected(AppTheme.OLD_SCROLL) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(22.dp))
                Text(
                    text = if (selectedTheme == AppTheme.OLD_SCROLL) {
                        "Характер бумаги"
                    } else {
                        "Цвет внутри материала"
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when (selectedTheme) {
                        AppTheme.LIQUID_GLASS ->
                            "Единое световое поле проходит через все поверхности"
                        AppTheme.ROYAL_GRAPHITE ->
                            "Graphite Oil — холодные цветные включения под мокрым камнем"
                        AppTheme.OLD_SCROLL ->
                            "Слоновая кость · старые волокна · тёплая пыль и потемневший край"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                if (selectedTheme == AppTheme.LIQUID_GLASS) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        StainPalette.entries.forEach { palette ->
                            StainPaletteOption(
                                palette = palette,
                                selected = stainSettings.palette == palette,
                                onClick = {
                                    onStainSettingsChanged(stainSettings.copy(palette = palette))
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                } else if (selectedTheme == AppTheme.ROYAL_GRAPHITE) {
                    GraphiteOilBadge()
                } else {
                    OldScrollBadge()
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text("Интенсивность", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${(stainSettings.intensity * 100).roundToInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Slider(
                    value = stainSettings.intensity,
                    onValueChange = {
                        onStainSettingsChanged(stainSettings.copy(intensity = it))
                    },
                    valueRange = MIN_STAIN_INTENSITY..MAX_STAIN_INTENSITY,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Text("Глубина материала", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MaterialDepth.entries.forEach { depth ->
                        MotionOption(
                            label = depth.displayName,
                            selected = stainSettings.depth == depth,
                            onClick = {
                                onStainSettingsChanged(stainSettings.copy(depth = depth))
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("Дыхание света", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StainMotion.entries.forEach { motion ->
                        MotionOption(
                            label = motion.displayName,
                            selected = stainSettings.motion == motion,
                            onClick = {
                                onStainSettingsChanged(stainSettings.copy(motion = motion))
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                ShapeLivePreview(interfaceShape)
                Spacer(Modifier.height(22.dp))
                Text("Форма интерфейса", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Настройте характер углов и сразу увидите результат",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text("Характер углов", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${interfaceShape.globalCornerDp} dp",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Slider(
                    value = interfaceShape.globalCornerDp.toFloat(),
                    onValueChange = {
                        onInterfaceShapeChanged(interfaceShape.withGlobalCorner(it.roundToInt()))
                    },
                    valueRange = MIN_INTERFACE_CORNER_DP.toFloat()..MAX_INTERFACE_CORNER_DP.toFloat(),
                    steps = MAX_INTERFACE_CORNER_DP - MIN_INTERFACE_CORNER_DP - 1,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        "Угловато",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Мягко",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ShapePreset(
                        label = "Угловато",
                        value = 8,
                        selected = interfaceShape.globalCornerDp == 8,
                        onClick = {
                            onInterfaceShapeChanged(interfaceShape.withGlobalCorner(8))
                        },
                        modifier = Modifier.weight(1f),
                    )
                    ShapePreset(
                        label = "Баланс",
                        value = 14,
                        selected = interfaceShape.globalCornerDp == 14,
                        onClick = {
                            onInterfaceShapeChanged(interfaceShape.withGlobalCorner(14))
                        },
                        modifier = Modifier.weight(1f),
                    )
                    ShapePreset(
                        label = "Мягко",
                        value = 24,
                        selected = interfaceShape.globalCornerDp == 24,
                        onClick = {
                            onInterfaceShapeChanged(interfaceShape.withGlobalCorner(24))
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(10.dp))
                ProxyInsetSurface(
                    modifier = Modifier.fillMaxWidth(),
                    role = ProxySurfaceRole.CARD,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Связать все элементы", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = if (interfaceShape.linked) {
                                    "Один характер углов для всего"
                                } else {
                                    "Точная настройка каждого элемента"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = interfaceShape.linked,
                            onCheckedChange = {
                                onInterfaceShapeChanged(interfaceShape.withLinked(it))
                            },
                        )
                    }
                }
                AnimatedVisibility(
                    visible = !interfaceShape.linked,
                    enter = fadeIn(tween(220)) + slideInVertically(tween(260)) { -it / 4 },
                    exit = fadeOut(tween(160)) + slideOutVertically(tween(200)) { -it / 4 },
                ) {
                    Column {
                        Spacer(Modifier.height(10.dp))
                        ProxyInsetSurface(
                            modifier = Modifier.fillMaxWidth(),
                            role = ProxySurfaceRole.CARD,
                        ) {
                            Column {
                                CornerControlRow(
                                    label = "Карточки",
                                    value = interfaceShape.cardCornerDp,
                                    onValueChange = {
                                        onInterfaceShapeChanged(interfaceShape.withCardCorner(it))
                                    },
                                )
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
                                )
                                CornerControlRow(
                                    label = "Поля ввода",
                                    value = interfaceShape.inputCornerDp,
                                    onValueChange = {
                                        onInterfaceShapeChanged(interfaceShape.withInputCorner(it))
                                    },
                                )
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
                                )
                                CornerControlRow(
                                    label = "Кнопки",
                                    value = interfaceShape.buttonCornerDp,
                                    onValueChange = {
                                        onInterfaceShapeChanged(interfaceShape.withButtonCorner(it))
                                    },
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(22.dp))
                Text("Плавность ввода", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Движение фокуса и ритм автосохранения",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
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
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "ProxyScroll · ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StainPaletteOption(
    palette: StainPalette,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = when (palette) {
        StainPalette.AURORA_OPAL -> listOf(
            Color(0xFF6F7BF7),
            Color(0xFF71D9E8),
            Color(0xFFC8A9FF),
        )
        StainPalette.CORAL_GLACIER -> listOf(
            Color(0xFFFF8F88),
            Color(0xFF80D8F3),
            Color(0xFFA8A7FF),
        )
        StainPalette.NORDIC_BLOOM -> listOf(
            Color(0xFF5FD0B5),
            Color(0xFF6C91E8),
            Color(0xFFC38FD8),
        )
    }
    ProxyInsetSurface(
        modifier = modifier
            .height(82.dp)
            .animatedClick(onClick = onClick, pressedScale = 0.95f),
        role = ProxySurfaceRole.BUTTON,
        selected = selected,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Canvas(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.56f),
                        shape = CircleShape,
                    ),
            ) {
                drawCircle(
                    brush = Brush.sweepGradient(colors + colors.first()),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.70f), Color.Transparent),
                        center = Offset(size.width * 0.28f, size.height * 0.22f),
                        radius = size.width * 0.62f,
                    ),
                )
            }
            Spacer(Modifier.height(5.dp))
            Text(
                text = palette.displayName,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun GraphiteOilBadge() {
    ProxyInsetSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp),
        role = ProxySurfaceRole.CARD,
        selected = true,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ThemeSwatch(
                theme = AppTheme.ROYAL_GRAPHITE,
                modifier = Modifier.size(42.dp),
            )
            Column(Modifier.weight(1f)) {
                Text("Graphite Oil", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Сталь · нефть · северный свет",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Активная палитра",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun OldScrollBadge() {
    ProxyInsetSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp),
        role = ProxySurfaceRole.CARD,
        selected = true,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ThemeSwatch(
                theme = AppTheme.OLD_SCROLL,
                modifier = Modifier.size(42.dp),
            )
            Column(Modifier.weight(1f)) {
                Text("Ivory Archive", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Старая бумага · волокна · пыль",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Активный материал",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun CompactThemeOption(
    theme: AppTheme,
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProxyInsetSurface(
        modifier = modifier
            .height(68.dp)
            .animatedClick(onClick = onClick, pressedScale = 0.96f),
        role = ProxySurfaceRole.CARD,
        selected = selected,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            ThemeSwatch(theme, modifier = Modifier.size(38.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                modifier = Modifier.weight(1f),
            )
            if (selected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Выбрано",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun ShapeLivePreview(shapeSettings: InterfaceShape) {
    val cardCorner by animateDpAsState(
        targetValue = shapeSettings.resolvedCardCornerDp.dp,
        animationSpec = tween(220),
        label = "preview-card-corner",
    )
    val inputCorner by animateDpAsState(
        targetValue = shapeSettings.resolvedInputCornerDp.dp,
        animationSpec = tween(220),
        label = "preview-input-corner",
    )
    val buttonCorner by animateDpAsState(
        targetValue = shapeSettings.resolvedButtonCornerDp.dp,
        animationSpec = tween(220),
        label = "preview-button-corner",
    )
    ProxyInsetSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        role = ProxySurfaceRole.OVERLAY,
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = "LIVE MATERIAL PREVIEW",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Нажмите и удерживайте: материал сожмётся и станет прозрачнее",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(7.dp))
            ProxySurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(inputCorner),
                role = ProxySurfaceRole.INPUT,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Найти заметку",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ProxySurface(
                    modifier = Modifier
                        .weight(1f)
                        .height(126.dp),
                    shape = RoundedCornerShape(cardCorner),
                    role = ProxySurfaceRole.CARD,
                ) {
                    Column(Modifier.padding(13.dp)) {
                        Text("Новая заметка", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = "Форма и свет меняются мгновенно.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "Сейчас",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                ProxySurface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(buttonCorner),
                    role = ProxySurfaceRole.BUTTON,
                    strong = true,
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShapePreset(
    label: String,
    value: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProxyInsetSurface(
        modifier = modifier
            .height(56.dp)
            .animatedClick(onClick = onClick, pressedScale = 0.95f),
        shape = RoundedCornerShape(value.dp),
        role = ProxySurfaceRole.BUTTON,
        selected = selected,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun CornerControlRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            text = "$value dp",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f))
                .animatedClick(
                    onClick = { onValueChange(value - 2) },
                    pressedScale = 0.88f,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Уменьшить", modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f))
                .animatedClick(
                    onClick = { onValueChange(value + 2) },
                    pressedScale = 0.88f,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Add, contentDescription = "Увеличить", modifier = Modifier.size(18.dp))
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
    ProxyInsetSurface(
        modifier = modifier
            .height(48.dp)
            .animatedClick(onClick = onClick, pressedScale = 0.95f),
        role = ProxySurfaceRole.BUTTON,
        selected = selected,
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
private fun ThemeSwatch(
    theme: AppTheme,
    modifier: Modifier = Modifier,
) {
    val shape = if (theme == AppTheme.OLD_SCROLL) {
        RoundedCornerShape(6.dp)
    } else {
        RoundedCornerShape(18.dp)
    }
    val stainSettings = LocalStainSettings.current
    val liquidColors = when (stainSettings.palette) {
        StainPalette.AURORA_OPAL -> listOf(
            Color(0xFF6F7BF7),
            Color(0xFF71D9E8),
            Color(0xFFC8A9FF),
        )
        StainPalette.CORAL_GLACIER -> listOf(
            Color(0xFFFF8F88),
            Color(0xFF80D8F3),
            Color(0xFFA8A7FF),
        )
        StainPalette.NORDIC_BLOOM -> listOf(
            Color(0xFF5FD0B5),
            Color(0xFF6C91E8),
            Color(0xFFC38FD8),
        )
    }
    Canvas(
        modifier = modifier
            .size(66.dp)
            .clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.34f), shape),
    ) {
        when (theme) {
            AppTheme.LIQUID_GLASS -> {
            drawRect(
                brush = Brush.linearGradient(
                    listOf(Color(0xFFF6F8FF), Color(0xFFEAF2F5)),
                ),
            )
            drawCircle(
                color = liquidColors[0].copy(alpha = 0.52f),
                radius = size.width * 0.48f,
                center = Offset(size.width * 0.18f, size.height * 0.18f),
            )
            drawCircle(
                color = liquidColors[1].copy(alpha = 0.34f),
                radius = size.width * 0.44f,
                center = Offset(size.width * 0.86f, size.height * 0.72f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.72f), Color.Transparent),
                ),
                radius = size.width * 0.42f,
                center = Offset(size.width * 0.72f, size.height * 0.68f),
            )
            }
            AppTheme.ROYAL_GRAPHITE -> {
            drawRect(
                brush = Brush.linearGradient(
                    listOf(Color(0xFF3E4B51), Color(0xFF11181B), Color(0xFF06090A)),
                ),
            )
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFD9EDF3).copy(alpha = 0.16f),
                        Color.Transparent,
                    ),
                    start = Offset(-size.width * 0.15f, 0f),
                    end = Offset(size.width, size.height),
                ),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFB8D0D9).copy(alpha = 0.14f), Color.Transparent),
                ),
                center = Offset(size.width * 0.28f, size.height * 0.22f),
                radius = size.width * 0.55f,
            )
            }
            AppTheme.OLD_SCROLL -> {
                drawRect(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFFF1D1), Color(0xFFE3C995), Color(0xFFCBAA70)),
                    ),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFFFFF7DE).copy(alpha = 0.76f), Color.Transparent),
                    ),
                    center = Offset(size.width * 0.28f, size.height * 0.20f),
                    radius = size.width * 0.62f,
                )
                repeat(7) { index ->
                    val y = size.height * (0.14f + index * 0.115f)
                    drawLine(
                        color = Color(0xFF76532F).copy(alpha = 0.12f + (index % 2) * 0.025f),
                        start = Offset(size.width * 0.08f, y),
                        end = Offset(size.width * (0.62f + (index % 3) * 0.11f), y + index % 2),
                        strokeWidth = 0.7f,
                    )
                }
                drawRect(
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF604321).copy(alpha = 0.16f),
                            Color.Transparent,
                            Color(0xFF604321).copy(alpha = 0.12f),
                        ),
                    ),
                )
            }
        }
    }
}
