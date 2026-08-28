package com.proxyscroll.app.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.proxyscroll.app.BuildConfig
import com.proxyscroll.app.domain.AppTheme
import com.proxyscroll.app.domain.InputMotion
import com.proxyscroll.app.domain.InterfaceShape
import com.proxyscroll.app.domain.LabsSettings
import com.proxyscroll.app.domain.LibraryDocument
import com.proxyscroll.app.domain.LibraryReadingStatus
import com.proxyscroll.app.domain.MaterialDepth
import com.proxyscroll.app.domain.MaterialMotionQuality
import com.proxyscroll.app.domain.MAX_LABS_MOTION_STRENGTH
import com.proxyscroll.app.domain.MAX_READING_FONT_SCALE
import com.proxyscroll.app.domain.MAX_READING_LINE_HEIGHT
import com.proxyscroll.app.domain.MAX_STAIN_INTENSITY
import com.proxyscroll.app.domain.MIN_LABS_MOTION_STRENGTH
import com.proxyscroll.app.domain.MIN_READING_FONT_SCALE
import com.proxyscroll.app.domain.MIN_READING_LINE_HEIGHT
import com.proxyscroll.app.domain.MIN_STAIN_INTENSITY
import com.proxyscroll.app.domain.MAX_INTERFACE_CORNER_DP
import com.proxyscroll.app.domain.MIN_INTERFACE_CORNER_DP
import com.proxyscroll.app.domain.Note
import com.proxyscroll.app.domain.NoteColorFlag
import com.proxyscroll.app.domain.NoteGroup
import com.proxyscroll.app.domain.NoteSpan
import com.proxyscroll.app.domain.NoteTextAlignment
import com.proxyscroll.app.domain.ReadingSettings
import com.proxyscroll.app.domain.resolveFor
import com.proxyscroll.app.domain.StainMotion
import com.proxyscroll.app.domain.StainPalette
import com.proxyscroll.app.domain.StainSettings
import com.proxyscroll.app.domain.TRASH_RETENTION_DAYS
import com.proxyscroll.app.domain.TRASH_RETENTION_MILLIS
import com.proxyscroll.app.ui.editor.MAX_NOTE_FONT_SIZE_SP
import com.proxyscroll.app.ui.editor.MIN_NOTE_FONT_SIZE_SP
import com.proxyscroll.app.ui.editor.RichTextState
import com.proxyscroll.app.ui.editor.annotatedText
import com.proxyscroll.app.ui.editor.readingAnnotatedText
import com.proxyscroll.app.ui.labs.MotionCompensationFrame
import com.proxyscroll.app.ui.labs.MotionSensorAvailability
import com.proxyscroll.app.ui.labs.TravelMotionCues
import com.proxyscroll.app.ui.labs.rememberMotionCompensationState
import com.proxyscroll.app.ui.theme.LocalProxyShape
import com.proxyscroll.app.ui.theme.LocalProxyVisualStyle
import com.proxyscroll.app.ui.theme.LocalMaterialMotionProfile
import com.proxyscroll.app.ui.theme.LocalStainPaletteColors
import com.proxyscroll.app.ui.theme.LocalStainSettings
import com.proxyscroll.app.ui.theme.ProxyBrandLockup
import com.proxyscroll.app.ui.theme.ProxyScrollTheme
import com.proxyscroll.app.ui.theme.ProxyInsetSurface
import com.proxyscroll.app.ui.theme.ProxySettingsFog
import com.proxyscroll.app.ui.theme.ProxySurface
import com.proxyscroll.app.ui.theme.ProxySurfaceRole
import com.proxyscroll.app.ui.theme.ProxyThemeBackground
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date
import java.util.LinkedHashMap
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class ProxyDestination { NOTES, LIBRARY, PDF_READER, READER, EDITOR, TRASH }
private enum class SettingsTab { APPEARANCE, LABS }

@Composable
fun ProxyScrollApp(
    viewModel: NotesViewModel,
    libraryViewModel: LibraryViewModel,
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    inputMotion: InputMotion,
    onInputMotionSelected: (InputMotion) -> Unit,
    interfaceShape: InterfaceShape,
    onInterfaceShapeChanged: (InterfaceShape) -> Unit,
    stainSettings: StainSettings,
    onStainSettingsChanged: (StainSettings) -> Unit,
    labsSettings: LabsSettings,
    onLabsSettingsChanged: (LabsSettings) -> Unit,
    readingSettings: ReadingSettings,
    onReadingSettingsChanged: (ReadingSettings) -> Unit,
    activeGroupFilter: String?,
    onActiveGroupFilterChanged: (String?) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val libraryState by libraryViewModel.uiState.collectAsStateWithLifecycle()
    val staticLiteLife = selectedTheme == AppTheme.LITE_LIFE
    val snackbarHostState = remember { SnackbarHostState() }
    val appScope = rememberCoroutineScope()
    var editorOpen by remember { mutableStateOf(false) }
    var readerOpen by remember { mutableStateOf(false) }
    var editorNote by remember { mutableStateOf<Note?>(null) }
    var editorInitialCursor by remember { mutableStateOf<Int?>(null) }
    var showLibrary by remember { mutableStateOf(false) }
    var pdfReaderOpen by remember { mutableStateOf(false) }
    var openPdfDocument by remember { mutableStateOf<LibraryDocument?>(null) }
    var showTrash by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var typingQuiet by remember { mutableStateOf(false) }
    var scrollingQuiet by remember { mutableStateOf(false) }
    val motionCompensation = rememberMotionCompensationState(
        sensorsEnabled = labsSettings.sensorsEnabled,
    )
    val microStabilizationActive = labsSettings.microStabilizationEnabled &&
        !editorOpen && !showTrash && !showSettings && !scrollingQuiet
    val settingsChromeShape = remember(showSettings) {
        interfaceShape.resolveFor(selectedTheme)
    }
    val settingsFogProgress by animateFloatAsState(
        targetValue = if (showSettings && !editorOpen && !showTrash) 1f else 0f,
        animationSpec = tween(if (staticLiteLife) 1 else 520, easing = FastOutSlowInEasing),
        label = "settings-fog-progress",
    )
    val settingsFogRadius by animateDpAsState(
        targetValue = if (
            !staticLiteLife && showSettings && !editorOpen && !showTrash
        ) {
            if (selectedTheme == AppTheme.LIQUID_GLASS) 14.dp else 4.dp
        } else {
            0.dp
        },
        animationSpec = tween(if (staticLiteLife) 1 else 360, easing = FastOutSlowInEasing),
        label = "settings-fog-radius",
    )
    val settingsBackgroundScale by animateFloatAsState(
        targetValue = if (
            !staticLiteLife && showSettings && !editorOpen && !showTrash
        ) 0.985f else 1f,
        animationSpec = tween(if (staticLiteLife) 1 else 520, easing = FastOutSlowInEasing),
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
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            if (microStabilizationActive) {
                                translationX = motionCompensation.frame.shakeX *
                                    5.dp.toPx() * labsSettings.motionStrength
                                translationY = motionCompensation.frame.shakeY *
                                    5.dp.toPx() * labsSettings.motionStrength
                            }
                        },
                    targetState = when {
                        editorOpen -> ProxyDestination.EDITOR
                        readerOpen -> ProxyDestination.READER
                        pdfReaderOpen -> ProxyDestination.PDF_READER
                        showTrash -> ProxyDestination.TRASH
                        showLibrary -> ProxyDestination.LIBRARY
                        else -> ProxyDestination.NOTES
                    },
                    transitionSpec = {
                        if (staticLiteLife) {
                            fadeIn(tween(1)) togetherWith fadeOut(tween(1))
                        } else if (targetState == ProxyDestination.EDITOR) {
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
                ) { destination ->
                    if (destination == ProxyDestination.EDITOR) {
                        NoteEditorScreen(
                            note = editorNote,
                            initialBodyCursor = editorInitialCursor,
                            groups = state.groups,
                            inputMotion = inputMotion,
                            onSave = viewModel::save,
                            onDelete = { deletedNote ->
                                viewModel.moveToTrash(deletedNote)
                                editorOpen = false
                                readerOpen = false
                                editorNote = null
                                appScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Заметка перемещена в корзину",
                                        actionLabel = "Отменить",
                                        duration = SnackbarDuration.Long,
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.restore(deletedNote)
                                    }
                                }
                            },
                            onClose = { savedNote ->
                                editorOpen = false
                                editorInitialCursor = null
                                editorNote = savedNote
                                readerOpen = savedNote != null
                            },
                            onTypingQuietChanged = { typingQuiet = it },
                        )
                    } else if (destination == ProxyDestination.READER) {
                        editorNote?.let { note ->
                            NoteReaderScreen(
                                note = note,
                                group = state.groups.firstOrNull { it.id == note.groupId },
                                settings = readingSettings,
                                onSettingsChanged = onReadingSettingsChanged,
                                onScrollQuietChanged = { scrollingQuiet = it },
                                onBack = {
                                    readerOpen = false
                                    editorNote = null
                                },
                                onEdit = { cursor ->
                                    editorInitialCursor = cursor
                                    readerOpen = false
                                    editorOpen = true
                                },
                            )
                        }
                    } else if (destination == ProxyDestination.PDF_READER) {
                        openPdfDocument?.let { document ->
                            ModernPdfReaderScreen(
                                document = document,
                                quoteCount = libraryState.quotes.count {
                                    it.documentId == document.id
                                },
                                onBack = {
                                    pdfReaderOpen = false
                                    openPdfDocument = null
                                },
                                onProgressChanged = { page, pageCount ->
                                    libraryViewModel.updateProgress(document, page, pageCount)
                                },
                                onSaveQuote = { page, excerpt, note ->
                                    libraryViewModel.addQuote(
                                        document = document,
                                        page = page,
                                        excerpt = excerpt,
                                        note = note,
                                    )
                                    appScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Цитата сохранена · стр. ${page + 1}",
                                            duration = SnackbarDuration.Short,
                                        )
                                    }
                                },
                                onScrollQuietChanged = { scrollingQuiet = it },
                            )
                        }
                    } else if (destination == ProxyDestination.TRASH) {
                        TrashScreen(
                            notes = state.trash,
                            onBack = { showTrash = false },
                            onRestore = viewModel::restore,
                            onDeleteForever = viewModel::deleteForever,
                            onEmptyTrash = viewModel::emptyTrash,
                        )
                    } else if (destination == ProxyDestination.LIBRARY) {
                        TactileLibraryScreen(
                            state = libraryState,
                            onOpenNotes = { showLibrary = false },
                            onOpenSettings = { showSettings = true },
                            onQueryChange = libraryViewModel::setQuery,
                            onFilterChange = libraryViewModel::setFilter,
                            onImport = { uri, title ->
                                val document = libraryViewModel.importPdf(uri, title)
                                openPdfDocument = document
                                pdfReaderOpen = true
                            },
                            onOpenDocument = { document ->
                                openPdfDocument = document
                                pdfReaderOpen = true
                            },
                            onEditDocument = libraryViewModel::updateAppearance,
                            onUpdateQuote = libraryViewModel::updateQuote,
                            onDeleteQuote = libraryViewModel::deleteQuote,
                            onDelete = libraryViewModel::delete,
                        )
                    } else {
                        NotesScreen(
                            state = state,
                            onQueryChange = viewModel::setQuery,
                            onCreate = {
                                editorNote = null
                                editorInitialCursor = null
                                readerOpen = false
                                editorOpen = true
                            },
                            onEdit = {
                                editorNote = it
                                editorInitialCursor = null
                                readerOpen = true
                            },
                            onTogglePinned = viewModel::togglePinned,
                            onBulkPinned = viewModel::setPinned,
                            onAssignGroup = viewModel::setGroup,
                            onCreateGroup = viewModel::createGroup,
                            onUpdateGroup = viewModel::updateGroup,
                            onDeleteGroup = viewModel::deleteGroup,
                            onMoveToTrash = { notes ->
                                viewModel.moveToTrash(notes)
                                appScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = if (notes.size == 1) {
                                            "Заметка перемещена в корзину"
                                        } else {
                                            "${notes.size} заметок перемещено в корзину"
                                        },
                                        actionLabel = "Отменить",
                                        duration = SnackbarDuration.Long,
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.restore(notes)
                                    }
                                }
                            },
                            onScrollQuietChanged = { scrollingQuiet = it },
                            onOpenSettings = { showSettings = true },
                            onOpenTrash = { showTrash = true },
                            onOpenLibrary = { showLibrary = true },
                            activeGroupFilter = activeGroupFilter,
                            onActiveGroupFilterChanged = onActiveGroupFilterChanged,
                        )
                    }
                }
            }

            TravelMotionCues(
                frame = motionCompensation.frame,
                settings = labsSettings,
                flat = staticLiteLife,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(6f),
            )

            AnimatedVisibility(
                visible = showSettings && !editorOpen && !showTrash,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f),
                enter = fadeIn(tween(if (staticLiteLife) 1 else 220)) +
                    slideInVertically(tween(if (staticLiteLife) 1 else 420)) { it / 8 } +
                    scaleIn(
                        tween(if (staticLiteLife) 1 else 420),
                        initialScale = if (staticLiteLife) 1f else 0.955f,
                    ),
                exit = fadeOut(tween(if (staticLiteLife) 1 else 180)) +
                    slideOutVertically(tween(if (staticLiteLife) 1 else 300)) { it / 10 } +
                    scaleOut(
                        tween(if (staticLiteLife) 1 else 260),
                        targetScale = if (staticLiteLife) 1f else 0.98f,
                    ),
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
                                labsSettings = labsSettings,
                                onLabsSettingsChanged = onLabsSettingsChanged,
                                motionAvailability = motionCompensation.availability,
                                motionFrame = motionCompensation.frame,
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
    val theme = LocalProxyVisualStyle.current.theme
    val stableSurface = theme == AppTheme.LITE_LIFE || theme == AppTheme.LIQUID_GLASS
    if (stableSurface) {
        return this.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick,
        )
    }
    val pressed by interactionSource.collectIsPressedAsState()
    val motionProfile = LocalMaterialMotionProfile.current
    val effectivePressedScale = 1f - (1f - pressedScale) * motionProfile.deformation
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) effectivePressedScale else 1f,
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
    val theme = LocalProxyVisualStyle.current.theme
    val stableSurface = theme == AppTheme.LITE_LIFE || theme == AppTheme.LIQUID_GLASS
    if (stableSurface) {
        return this.combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
            onLongClickLabel = "Изменить группу",
            onLongClick = onLongClick,
        )
    }
    val pressed by interactionSource.collectIsPressedAsState()
    val motionProfile = LocalMaterialMotionProfile.current
    val effectivePressedScale = 1f - (1f - pressedScale) * motionProfile.deformation
    val scale by animateFloatAsState(
        targetValue = if (pressed) effectivePressedScale else 1f,
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
    onBulkPinned: (Collection<Note>, Boolean) -> Unit,
    onAssignGroup: (Collection<Note>, String?) -> Unit,
    onCreateGroup: (String, Long) -> Unit,
    onUpdateGroup: (NoteGroup, String, Long) -> Unit,
    onDeleteGroup: (String) -> Unit,
    onMoveToTrash: (List<Note>) -> Unit,
    onScrollQuietChanged: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenLibrary: () -> Unit,
    activeGroupFilter: String?,
    onActiveGroupFilterChanged: (String?) -> Unit,
) {
    val searchInteractionSource = remember { MutableInteractionSource() }
    val searchFocusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val filterRevealDistancePx = remember(density) { with(density) { 54.dp.toPx() } }
    val filterHoldSlopPx = remember(density) { with(density) { 10.dp.toPx() } }
    val liteLife = LocalProxyVisualStyle.current.theme == AppTheme.LITE_LIFE
    val themePrimaryColor = MaterialTheme.colorScheme.primary
    var showGroupPicker by remember { mutableStateOf(false) }
    var searchExpanded by remember { mutableStateOf(state.query.isNotBlank()) }
    var showCreateGroup by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<NoteGroup?>(null) }
    var pendingDeleteGroup by remember { mutableStateOf<NoteGroup?>(null) }
    var tintOrigin by remember { mutableStateOf(Offset(160f, 220f)) }
    var tintPulseColor by remember { mutableStateOf(Color.Transparent) }
    var tintRevision by remember { mutableIntStateOf(0) }
    val selectedGroup = state.groups.firstOrNull { it.id == activeGroupFilter }
    val visibleNotes = remember(state.notes, activeGroupFilter) {
        activeGroupFilter?.let { id -> state.notes.filter { it.groupId == id } } ?: state.notes
    }
    val noteGroups = remember(visibleNotes, state.groups) {
        groupNotes(visibleNotes, state.groups)
    }
    val tintColor by animateColorAsState(
        targetValue = selectedGroup?.let { Color(it.colorArgb) } ?: Color.Transparent,
        animationSpec = tween(520, easing = FastOutSlowInEasing),
        label = "group-tint-color",
    )
    val tintWave = remember { Animatable(0f) }
    LaunchedEffect(tintRevision) {
        if (tintRevision == 0 || liteLife) return@LaunchedEffect
        tintWave.snapTo(0f)
        tintWave.animateTo(1f, tween(920, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(state.groups.map { it.id }) {
        if (activeGroupFilter != null && state.groups.none { it.id == activeGroupFilter }) {
            onActiveGroupFilterChanged(null)
        }
    }
    var selectedIds by remember { mutableStateOf<List<String>>(emptyList()) }
    val selectedIdSet = selectedIds.toSet()
    val notesById = visibleNotes.associateBy { it.id }
    val selectedNotes = selectedIds.mapNotNull(notesById::get)
    val selectionMode = selectedIds.isNotEmpty()
    val allVisibleSelected = visibleNotes.isNotEmpty() && selectedIds.size == visibleNotes.size
    val allSelectedPinned = selectedNotes.isNotEmpty() && selectedNotes.all { it.isPinned }
    fun toggleSelection(noteId: String) {
        selectedIds = if (noteId in selectedIdSet) {
            selectedIds.filterNot { it == noteId }
        } else {
            selectedIds + noteId
        }
    }
    BackHandler(enabled = selectionMode) { selectedIds = emptyList() }
    BackHandler(enabled = showGroupPicker && !selectionMode) { showGroupPicker = false }
    BackHandler(
        enabled = searchExpanded && !selectionMode && !showGroupPicker,
    ) {
        searchExpanded = false
        onQueryChange("")
    }
    LaunchedEffect(searchExpanded) {
        if (searchExpanded) {
            delay(80)
            searchFocusRequester.requestFocus()
        }
    }
    LaunchedEffect(visibleNotes.map { it.id }) {
        val activeIds = visibleNotes.mapTo(mutableSetOf()) { it.id }
        selectedIds = selectedIds.filter { it in activeIds }
    }
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                if (!liteLife && selectedGroup != null) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                tintColor.copy(alpha = 0.042f),
                                tintColor.copy(alpha = 0.016f),
                                Color.Transparent,
                            ),
                            center = tintOrigin,
                            radius = maxOf(size.width, size.height) * 1.25f,
                        ),
                    )
                }
                if (!liteLife && tintWave.value in 0.001f..0.999f) {
                    val progress = tintWave.value
                    val pulse = sin(progress * Math.PI).toFloat().coerceAtLeast(0f)
                    val radius = maxOf(size.width, size.height) *
                        (0.06f + progress * 1.52f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = pulse * 0.11f),
                                tintPulseColor.copy(alpha = pulse * 0.22f),
                                tintPulseColor.copy(alpha = pulse * 0.07f),
                                Color.Transparent,
                            ),
                            center = tintOrigin,
                            radius = radius,
                        ),
                        center = tintOrigin,
                        radius = radius,
                    )
                    drawCircle(
                        color = tintPulseColor.copy(alpha = pulse * 0.10f),
                        center = tintOrigin,
                        radius = radius * 0.72f,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = (3.5f * (1f - progress) + 0.6f).dp.toPx(),
                        ),
                    )
                }
            },
    ) {
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (!selectionMode) {
                MainSectionBar(
                    notesSelected = true,
                    onOpenNotes = {},
                    onOpenLibrary = onOpenLibrary,
                    onPrimaryAction = onCreate,
                    primaryActionDescription = "Создать заметку",
                    onOpenSettings = onOpenSettings,
                    searchSelected = searchExpanded || state.query.isNotBlank(),
                    onOpenSearch = { searchExpanded = true },
                )
            }
        },
        topBar = {
            if (selectionMode) {
                CenterAlignedTopAppBar(
                    title = { Text("${selectedIds.size} выбрано") },
                    navigationIcon = {
                        IconButton(onClick = { selectedIds = emptyList() }) {
                            Icon(Icons.Default.Close, contentDescription = "Отменить выбор")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                selectedIds = if (allVisibleSelected) {
                                    emptyList()
                                } else {
                                    visibleNotes.map { it.id }
                                }
                            },
                        ) {
                            Icon(Icons.Default.SelectAll, contentDescription = "Выбрать все")
                        }
                        IconButton(
                            onClick = {
                                onBulkPinned(selectedNotes, !allSelectedPinned)
                            },
                        ) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = if (allSelectedPinned) {
                                    "Открепить выбранные"
                                } else {
                                    "Закрепить выбранные"
                                },
                                tint = if (allSelectedPinned) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        }
                        IconButton(
                            onClick = {
                                val moving = selectedNotes
                                selectedIds = emptyList()
                                onMoveToTrash(moving)
                            },
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Переместить в корзину",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
            }
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .then(if (selectionMode) Modifier else Modifier.statusBarsPadding())
                .padding(horizontal = 12.dp)
                .pointerInput(selectionMode, showGroupPicker, searchExpanded) {
                    if (selectionMode || showGroupPicker || searchExpanded) {
                        return@pointerInput
                    }
                    coroutineScope {
                        val gestureScope = this
                        awaitEachGesture {
                            val down = awaitFirstDown(
                                requireUnconsumed = false,
                                pass = PointerEventPass.Initial,
                            )
                            var lastPosition = down.position
                            var holdOrigin = down.position
                            var upwardTravel = 0f
                            var pointerPressed = true
                            var revealed = false
                            var holdJob: Job? = null

                            while (pointerPressed && !revealed) {
                                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                val change = event.changes
                                    .firstOrNull { it.id == down.id }
                                    ?: break
                                pointerPressed = change.pressed
                                val deltaY = change.position.y - lastPosition.y
                                lastPosition = change.position
                                upwardTravel = if (deltaY < 0f) {
                                    upwardTravel - deltaY
                                } else {
                                    (upwardTravel - deltaY * 1.35f).coerceAtLeast(0f)
                                }

                                if (pointerPressed && upwardTravel >= filterRevealDistancePx) {
                                    val movedX = (
                                        change.position.x - holdOrigin.x
                                        ).absoluteValue
                                    val movedY = (
                                        change.position.y - holdOrigin.y
                                        ).absoluteValue
                                    if (holdJob == null ||
                                        movedX > filterHoldSlopPx ||
                                        movedY > filterHoldSlopPx
                                    ) {
                                        holdJob?.cancel()
                                        holdOrigin = change.position
                                        holdJob = gestureScope.launch {
                                            delay(280)
                                            if (pointerPressed && !revealed) {
                                                hapticFeedback.performHapticFeedback(
                                                    HapticFeedbackType.LongPress,
                                                )
                                                showGroupPicker = true
                                                revealed = true
                                            }
                                        }
                                    }
                                } else {
                                    holdJob?.cancel()
                                    holdJob = null
                                }
                            }
                            holdJob?.cancel()
                        }
                    }
                },
        ) {
            if (!selectionMode) {
                Text(
                    text = "ProxyScroll",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 2.dp, top = 2.dp),
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Заметки",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        AnimatedContent(
                            targetState = visibleNotes.size,
                            transitionSpec = {
                                val duration = if (liteLife) 1 else 240
                                fadeIn(tween(duration)) togetherWith fadeOut(tween(duration))
                            },
                            label = "note-count",
                        ) { count ->
                            Text(
                                text = if (selectedGroup != null) {
                                    "${notesCountLabel(count)} · ${selectedGroup.name}"
                                } else {
                                    notesCountLabel(count)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            searchExpanded = !searchExpanded
                            if (!searchExpanded) onQueryChange("")
                        },
                    ) {
                        Icon(
                            imageVector = if (searchExpanded) {
                                Icons.Default.Close
                            } else {
                                Icons.Default.Search
                            },
                            contentDescription = if (searchExpanded) {
                                "Закрыть поиск"
                            } else {
                                "Поиск"
                            },
                            tint = if (searchExpanded) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    IconButton(onClick = onOpenTrash) {
                        Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.RestoreFromTrash,
                                contentDescription = "Корзина",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (state.trash.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(14.dp)
                                        .clip(if (liteLife) RectangleShape else CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = state.trash.size.coerceAtMost(9).toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }
                            }
                        }
                    }
                    IconButton(onClick = { showGroupPicker = !showGroupPicker }) {
                        Icon(
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = if (showGroupPicker) {
                                "Скрыть группы"
                            } else {
                                "Показать группы"
                            },
                            tint = selectedGroup?.let(::noteGroupColor)
                                ?: if (showGroupPicker) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = showGroupPicker || selectionMode,
                enter = fadeIn(tween(if (liteLife) 1 else 180)) +
                    slideInVertically(tween(if (liteLife) 1 else 280)) { -it / 3 },
                exit = fadeOut(tween(if (liteLife) 1 else 140)) +
                    slideOutVertically(tween(if (liteLife) 1 else 220)) { -it / 3 },
            ) {
                Column {
                    Spacer(Modifier.height(6.dp))
                    LiquidGroupRail(
                        groups = state.groups,
                        notes = state.notes,
                        selectedGroupId = activeGroupFilter,
                        assignmentMode = selectionMode,
                        onSelected = { group, origin ->
                            tintOrigin = origin
                            tintPulseColor = group?.let(::noteGroupColor) ?: themePrimaryColor
                            tintRevision++
                            if (selectionMode) {
                                onAssignGroup(selectedNotes, group?.id)
                                selectedIds = emptyList()
                                onActiveGroupFilterChanged(group?.id)
                            } else {
                                onActiveGroupFilterChanged(group?.id)
                                showGroupPicker = false
                            }
                        },
                        onCreate = { showCreateGroup = true },
                        onLongPress = { group ->
                            if (!group.builtIn) editingGroup = group
                        },
                    )
                }
            }
            AnimatedVisibility(
                visible = !selectionMode && (searchExpanded || state.query.isNotBlank()),
                enter = fadeIn(tween(if (liteLife) 1 else 160)) +
                    slideInVertically(tween(if (liteLife) 1 else 220)) { -it / 4 },
                exit = fadeOut(tween(if (liteLife) 1 else 120)),
            ) {
                Column {
                    Spacer(Modifier.height(6.dp))
                    val searchShape = RoundedCornerShape(
                        LocalProxyShape.current.resolvedInputCornerDp.dp,
                    )
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(searchFocusRequester),
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
                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.54f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.34f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.52f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                        ),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            if (visibleNotes.isEmpty()) {
                EmptyNotes(
                    isSearching = state.query.isNotBlank() || activeGroupFilter != null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 24.dp),
                )
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    noteGroups.forEach { group ->
                        item(key = "note-group-${group.group?.id ?: "ungrouped"}") {
                            NoteGroupHeader(
                                group = group,
                                singleGroup = noteGroups.size == 1,
                                modifier = if (liteLife) Modifier else Modifier.animateItem(),
                            )
                        }
                        itemsIndexed(
                            items = group.notes,
                            key = { _, note -> note.id },
                        ) { _, note ->
                            NoteCard(
                                note = note,
                                group = group.group,
                                selected = note.id in selectedIdSet,
                                selectionMode = selectionMode,
                                selectionOrder = selectedIds.indexOf(note.id) + 1,
                                onClick = {
                                    if (selectionMode) toggleSelection(note.id) else onEdit(note)
                                },
                                onLongClick = { toggleSelection(note.id) },
                                onTogglePinned = { onTogglePinned(note) },
                                modifier = if (liteLife) Modifier else Modifier.animateItem(),
                            )
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
    if (showCreateGroup) {
        GroupStudioDialog(
            group = null,
            onDismiss = { showCreateGroup = false },
            onSave = { name, color ->
                onCreateGroup(name, color)
                showCreateGroup = false
            },
        )
    }
    editingGroup?.let { group ->
        GroupStudioDialog(
            group = group,
            onDismiss = { editingGroup = null },
            onSave = { name, color ->
                onUpdateGroup(group, name, color)
                editingGroup = null
            },
            onDelete = {
                editingGroup = null
                pendingDeleteGroup = group
            },
        )
    }
    pendingDeleteGroup?.let { group ->
        AlertDialog(
            onDismissRequest = { pendingDeleteGroup = null },
            title = { Text("Удалить группу «${group.name}»?") },
            text = { Text("Заметки останутся на месте и перейдут в раздел без группы.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteGroup(group.id)
                        pendingDeleteGroup = null
                    },
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteGroup = null }) { Text("Отмена") }
            },
        )
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrashScreen(
    notes: List<Note>,
    onBack: () -> Unit,
    onRestore: (Collection<Note>) -> Unit,
    onDeleteForever: (Collection<Note>) -> Unit,
    onEmptyTrash: () -> Unit,
) {
    val liteLife = LocalProxyVisualStyle.current.theme == AppTheme.LITE_LIFE
    var selectedIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var pendingPermanentDelete by remember { mutableStateOf<List<Note>?>(null) }
    var confirmEmptyTrash by remember { mutableStateOf(false) }
    val selectedSet = selectedIds.toSet()
    val notesById = notes.associateBy { it.id }
    val selectedNotes = selectedIds.mapNotNull(notesById::get)
    val selectionMode = selectedIds.isNotEmpty()
    val allSelected = notes.isNotEmpty() && selectedIds.size == notes.size
    fun toggleSelection(noteId: String) {
        selectedIds = if (noteId in selectedSet) {
            selectedIds.filterNot { it == noteId }
        } else {
            selectedIds + noteId
        }
    }
    BackHandler {
        if (selectionMode) selectedIds = emptyList() else onBack()
    }
    LaunchedEffect(notes.map { it.id }) {
        val available = notes.mapTo(mutableSetOf()) { it.id }
        selectedIds = selectedIds.filter { it in available }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(if (selectionMode) "${selectedIds.size} выбрано" else "Корзина")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectionMode) selectedIds = emptyList() else onBack()
                        },
                    ) {
                        Icon(
                            if (selectionMode) Icons.Default.Close else Icons.Default.ArrowBack,
                            contentDescription = if (selectionMode) "Отменить выбор" else "Назад",
                        )
                    }
                },
                actions = {
                    if (selectionMode) {
                        IconButton(
                            onClick = {
                                selectedIds = if (allSelected) emptyList() else notes.map { it.id }
                            },
                        ) {
                            Icon(Icons.Default.SelectAll, contentDescription = "Выбрать всё")
                        }
                        IconButton(
                            onClick = {
                                val restoring = selectedNotes
                                selectedIds = emptyList()
                                onRestore(restoring)
                            },
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = "Восстановить выбранные")
                        }
                        IconButton(onClick = { pendingPermanentDelete = selectedNotes }) {
                            Icon(
                                Icons.Default.DeleteForever,
                                contentDescription = "Удалить выбранные навсегда",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    } else if (notes.isNotEmpty()) {
                        IconButton(onClick = { confirmEmptyTrash = true }) {
                            Icon(
                                Icons.Default.DeleteForever,
                                contentDescription = "Очистить корзину",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 16.dp),
        ) {
            ProxyInsetSurface(
                modifier = Modifier.fillMaxWidth(),
                role = ProxySurfaceRole.CARD,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.RestoreFromTrash,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Удалённые заметки хранятся $TRASH_RETENTION_DAYS дней",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            "Можно восстановить или удалить навсегда раньше",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (notes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Корзина пуста", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Удалённые заметки появятся здесь",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(notes, key = { _, note -> note.id }) { _, note ->
                        TrashNoteCard(
                            note = note,
                            selected = note.id in selectedSet,
                            selectionMode = selectionMode,
                            selectionOrder = selectedIds.indexOf(note.id) + 1,
                            onClick = {
                                if (selectionMode) toggleSelection(note.id)
                            },
                            onLongClick = { toggleSelection(note.id) },
                            onRestore = { onRestore(listOf(note)) },
                            onDeleteForever = { pendingPermanentDelete = listOf(note) },
                            modifier = if (liteLife) Modifier else Modifier.animateItem(),
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }

    pendingPermanentDelete?.let { deleting ->
        AlertDialog(
            onDismissRequest = { pendingPermanentDelete = null },
            title = { Text("Удалить навсегда?") },
            text = {
                Text(
                    if (deleting.size == 1) {
                        "Заметку нельзя будет восстановить."
                    } else {
                        "${deleting.size} заметок нельзя будет восстановить."
                    },
                )
            },
            dismissButton = {
                TextButton(onClick = { pendingPermanentDelete = null }) { Text("Отмена") }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteForever(deleting)
                        selectedIds = emptyList()
                        pendingPermanentDelete = null
                    },
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
        )
    }
    if (confirmEmptyTrash) {
        AlertDialog(
            onDismissRequest = { confirmEmptyTrash = false },
            title = { Text("Очистить корзину?") },
            text = { Text("Все ${notes.size} заметок будут удалены без возможности восстановления.") },
            dismissButton = {
                TextButton(onClick = { confirmEmptyTrash = false }) { Text("Отмена") }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEmptyTrash()
                        confirmEmptyTrash = false
                    },
                ) {
                    Text("Очистить", color = MaterialTheme.colorScheme.error)
                }
            },
        )
    }
}

@Composable
private fun TrashNoteCard(
    note: Note,
    selected: Boolean,
    selectionMode: Boolean,
    selectionOrder: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRestore: () -> Unit,
    onDeleteForever: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val liteLife = LocalProxyVisualStyle.current.theme == AppTheme.LITE_LIFE
    val dayMillis = TRASH_RETENTION_MILLIS / TRASH_RETENTION_DAYS
    val deletedAt = note.deletedAt ?: System.currentTimeMillis()
    val remainingMillis = (TRASH_RETENTION_MILLIS -
        (System.currentTimeMillis() - deletedAt)).coerceAtLeast(0L)
    val daysRemaining = ((remainingMillis + dayMillis - 1L) / dayMillis).coerceAtLeast(1L)
    ProxySurface(
        modifier = modifier
            .fillMaxWidth()
            .animatedCombinedClick(onClick = onClick, onLongClick = onLongClick),
        role = ProxySurfaceRole.CARD,
        strong = selected,
        active = selected,
        interactive = false,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(if (liteLife) RoundedCornerShape(0.dp) else CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            selectionOrder.coerceAtLeast(1).toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                } else {
                    Text(
                        "#${note.index.toString().padStart(3, '0')}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.width(9.dp))
                Text(
                    note.title.ifBlank { "Без названия" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            if (note.body.isNotBlank()) {
                Spacer(Modifier.height(5.dp))
                Text(
                    annotatedText(note.body, note.spans),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(9.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Осталось: $daysRemaining дн.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (!selectionMode) {
                    IconButton(onClick = onRestore, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Undo, contentDescription = "Восстановить")
                    }
                    IconButton(onClick = onDeleteForever, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.DeleteForever,
                            contentDescription = "Удалить навсегда",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidGroupRail(
    groups: List<NoteGroup>,
    notes: List<Note>,
    selectedGroupId: String?,
    assignmentMode: Boolean,
    onSelected: (NoteGroup?, Offset) -> Unit,
    onCreate: () -> Unit,
    onLongPress: (NoteGroup) -> Unit,
) {
    val liteLife = LocalProxyVisualStyle.current.theme == AppTheme.LITE_LIFE
    Column {
        AnimatedVisibility(
            visible = assignmentMode,
            enter = fadeIn(tween(if (liteLife) 1 else 180)) +
                slideInVertically(tween(if (liteLife) 1 else 240)) { it / 3 },
            exit = fadeOut(tween(if (liteLife) 1 else 130)) +
                slideOutVertically(tween(if (liteLife) 1 else 180)) { it / 3 },
        ) {
            Text(
                text = "Коснитесь шара, чтобы назначить группу выбранным заметкам",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 7.dp),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            LiquidGroupOrb(
                label = "Все",
                color = MaterialTheme.colorScheme.primary,
                count = notes.size,
                selected = selectedGroupId == null,
                onClick = { origin -> onSelected(null, origin) },
                onLongClick = {},
            )
            groups.forEach { group ->
                LiquidGroupOrb(
                    label = group.name,
                    color = noteGroupColor(group),
                    count = notes.count { it.groupId == group.id },
                    selected = selectedGroupId == group.id,
                    onClick = { origin -> onSelected(group, origin) },
                    onLongClick = { onLongPress(group) },
                )
            }
            Column(
                modifier = Modifier.width(70.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ProxySurface(
                    modifier = Modifier
                        .size(48.dp)
                        .animatedClick(onClick = onCreate, pressedScale = 0.90f),
                    shape = CircleShape,
                    role = ProxySurfaceRole.BUTTON,
                    strong = false,
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Создать цветовую группу",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Новая",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LiquidGroupOrb(
    label: String,
    color: Color,
    count: Int,
    selected: Boolean,
    onClick: (Offset) -> Unit,
    onLongClick: () -> Unit,
) {
    var origin by remember { mutableStateOf(Offset.Zero) }
    val liteLife = LocalProxyVisualStyle.current.theme == AppTheme.LITE_LIFE
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val baseSize = if (liteLife) 52.dp else if (selected) 62.dp else 52.dp
    val orbWidth by animateDpAsState(
        targetValue = if (!liteLife && pressed) baseSize * 1.16f else baseSize,
        animationSpec = if (liteLife) {
            tween(1)
        } else {
            spring(dampingRatio = 0.48f, stiffness = 320f)
        },
        label = "group-orb-width-$label",
    )
    val orbHeight by animateDpAsState(
        targetValue = if (!liteLife && pressed) baseSize * 0.86f else baseSize,
        animationSpec = if (liteLife) {
            tween(1)
        } else {
            spring(dampingRatio = 0.46f, stiffness = 300f)
        },
        label = "group-orb-height-$label",
    )
    Column(
        modifier = Modifier.width(76.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProxySurface(
            modifier = Modifier
                .width(orbWidth)
                .height(orbHeight)
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInRoot()
                    origin = Offset(
                        x = position.x + coordinates.size.width / 2f,
                        y = position.y + coordinates.size.height / 2f,
                    )
                }
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onClick(origin) },
                    onLongClickLabel = "Настроить группу",
                    onLongClick = onLongClick,
                ),
            shape = if (liteLife) RoundedCornerShape(0.dp) else CircleShape,
            role = ProxySurfaceRole.BUTTON,
            strong = selected,
            active = selected,
            interactive = false,
        ) {
            Canvas(Modifier.fillMaxSize()) {
                if (liteLife) {
                    drawRect(color = color.copy(alpha = if (selected) 1f else 0.74f))
                } else {
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (pressed) 0.66f else 0.46f),
                                color.copy(alpha = if (selected) 0.82f else 0.64f),
                                color.copy(alpha = 0.26f),
                                Color.Transparent,
                            ),
                            center = Offset(
                                size.width * if (pressed) 0.42f else 0.30f,
                                size.height * 0.24f,
                            ),
                            radius = size.maxDimension * 0.78f,
                        ),
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (pressed) 0.28f else 0.18f),
                                color.copy(alpha = 0.10f),
                                Color.Transparent,
                            ),
                            center = Offset(size.width * 0.74f, size.height * 0.68f),
                            radius = size.minDimension * 0.42f,
                        ),
                        center = Offset(size.width * 0.74f, size.height * 0.68f),
                        radius = size.minDimension * 0.42f,
                    )
                    drawArc(
                        color = Color.White.copy(alpha = if (selected) 0.72f else 0.40f),
                        startAngle = 202f,
                        sweepAngle = if (pressed) 132f else 108f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = if (selected) 1.7.dp.toPx() else 1.dp.toPx(),
                        ),
                    )
                }
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = count.coerceAtMost(99).toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) {
                color
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GroupStudioDialog(
    group: NoteGroup?,
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val liteLife = LocalProxyVisualStyle.current.theme == AppTheme.LITE_LIFE
    val colorShape = if (liteLife) RoundedCornerShape(0.dp) else CircleShape
    val colors = remember(group?.colorArgb) {
        buildList {
            group?.colorArgb?.let { add(it) }
            addAll(
                listOf(
                    0xFF65B9FF,
                    0xFF9B80FF,
                    0xFFFF8E8A,
                    0xFF59D4B1,
                    0xFFF3BC62,
                    0xFFFF6685,
                    0xFF57D7EA,
                    0xFF7587FF,
                ),
            )
        }.distinct()
    }
    var name by remember(group?.id) { mutableStateOf(group?.name.orEmpty()) }
    var selectedColor by remember(group?.id) {
        mutableStateOf(group?.colorArgb ?: colors.first())
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (group == null) "Новая группа" else "Настроить группу") },
        text = {
            Column {
                Text(
                    if (group == null) {
                        "Создайте свой быстрый фильтр для заметок"
                    } else {
                        "Название и цвет обновятся во всех заметках группы"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                ProxySurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    role = ProxySurfaceRole.CARD,
                    strong = true,
                    active = true,
                    interactive = false,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .size(34.dp)
                                .clip(colorShape)
                                .background(Color(selectedColor)),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = name.ifBlank { "Название группы" },
                            style = MaterialTheme.typography.titleMedium,
                            color = if (name.isBlank()) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(28) },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    colors.forEach { colorArgb ->
                        val selected = colorArgb == selectedColor
                        Box(
                            modifier = Modifier
                                .size(if (selected) 42.dp else 36.dp)
                                .clip(colorShape)
                                .background(Color(colorArgb))
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) {
                                        Color.White
                                    } else {
                                        Color.White.copy(alpha = 0.34f)
                                    },
                                    shape = colorShape,
                                )
                                .animatedClick(
                                    onClick = { selectedColor = colorArgb },
                                    pressedScale = 0.86f,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onSave(name.trim(), selectedColor) },
            ) {
                Text(if (group == null) "Создать" else "Сохранить")
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Отмена") }
            }
        },
    )
}

private data class NoteListGroup(
    val group: NoteGroup?,
    val notes: List<Note>,
)

private fun groupNotes(notes: List<Note>, groups: List<NoteGroup>): List<NoteListGroup> {
    val knownIds = groups.mapTo(mutableSetOf()) { it.id }
    val grouped = groups.mapNotNull { group ->
        notes.filter { it.groupId == group.id }
            .takeIf { it.isNotEmpty() }
            ?.let { NoteListGroup(group = group, notes = it) }
    }
    val ungrouped = notes.filter { it.groupId == null || it.groupId !in knownIds }
        .takeIf { it.isNotEmpty() }
        ?.let { NoteListGroup(group = null, notes = it) }
    return grouped + listOfNotNull(ungrouped)
}

private fun noteGroupColor(group: NoteGroup?): Color {
    return group?.let { Color(it.colorArgb) } ?: Color(0xFF8B93A7)
}

private fun NoteTextAlignment.toComposeTextAlign(): TextAlign = when (this) {
    NoteTextAlignment.START -> TextAlign.Start
    NoteTextAlignment.CENTER -> TextAlign.Center
    NoteTextAlignment.END -> TextAlign.End
    NoteTextAlignment.JUSTIFY -> TextAlign.Justify
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
    group: NoteListGroup,
    singleGroup: Boolean,
    modifier: Modifier = Modifier,
) {
    val liteLife = LocalProxyVisualStyle.current.theme == AppTheme.LITE_LIFE
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(if (group.group == null) 7.dp else 9.dp)
                .clip(if (liteLife) RoundedCornerShape(0.dp) else CircleShape)
                .background(noteGroupColor(group.group)),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (singleGroup && group.group == null) {
                "Все заметки"
            } else {
                group.group?.name ?: "Без группы"
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = group.notes.size.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = noteGroupColor(group.group).copy(alpha = 0.92f),
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
    group: NoteGroup?,
    selected: Boolean,
    selectionMode: Boolean,
    selectionOrder: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onTogglePinned: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val liteLife = LocalProxyVisualStyle.current.theme == AppTheme.LITE_LIFE
    val legacyAccent = if (note.colorFlag != NoteColorFlag.NONE) {
        noteFlagColor(note.colorFlag)
    } else {
        null
    }
    val accentColor = group?.let(::noteGroupColor) ?: legacyAccent
    Box(modifier = modifier.fillMaxWidth()) {
        ProxySurface(
            modifier = Modifier
                .fillMaxWidth()
                .animatedCombinedClick(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
            strong = note.isPinned || selected,
            active = selected,
            role = ProxySurfaceRole.CARD,
            interactive = false,
        ) {
            Box {
                if (accentColor != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .width(4.dp)
                            .height(54.dp)
                            .clip(
                                if (liteLife) RoundedCornerShape(0.dp)
                                else RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp),
                            )
                            .then(
                                if (liteLife) {
                                    Modifier.background(accentColor)
                                } else {
                                    Modifier.background(
                                        Brush.verticalGradient(
                                            listOf(
                                                accentColor.copy(alpha = 0.95f),
                                                accentColor.copy(alpha = 0.44f),
                                            ),
                                        ),
                                    )
                                },
                            ),
                    )
                }
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AnimatedContent(
                            targetState = selected,
                            transitionSpec = {
                                val duration = if (liteLife) 1 else 140
                                fadeIn(tween(duration)) togetherWith fadeOut(tween(duration))
                            },
                            label = "note-selection-${note.id}",
                        ) { isSelected ->
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(
                                            if (liteLife) RoundedCornerShape(0.dp) else CircleShape,
                                        )
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = selectionOrder.coerceAtLeast(1).toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }
                            } else {
                                Text(
                                    text = "#${note.index.toString().padStart(3, '0')}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        Text(
                            text = note.title.ifBlank { "Без названия" },
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (!selectionMode) {
                            IconButton(onClick = onTogglePinned, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = if (note.isPinned) {
                                        "Открепить"
                                    } else {
                                        "Закрепить"
                                    },
                                    tint = if (note.isPinned) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                        }
                    }
                    if (note.body.isNotBlank()) {
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = annotatedText(note.body, note.spans),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textAlign = note.textAlignment.toComposeTextAlign(),
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
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
                    if (accentColor != null) {
                        Text(
                            text = group?.name ?: note.colorFlag.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                        )
                    }
                    }
                }
            }
        }
    }
}

@Composable
private fun BulkFlagMenuButton(
    onSelected: (NoteColorFlag) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Palette, contentDescription = "Цвет выбранных заметок")
        }
        NoteFlagDropdown(
            expanded = expanded,
            selected = NoteColorFlag.NONE,
            helperText = "Цвет применится ко всему выбранному массиву",
            onDismiss = { expanded = false },
            onSelected = { flag ->
                expanded = false
                onSelected(flag)
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
private fun NoteGroupMenuButton(
    selectedGroupId: String?,
    groups: List<NoteGroup>,
    onSelected: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val liteLife = LocalProxyVisualStyle.current.theme == AppTheme.LITE_LIFE
    val markerShape = if (liteLife) RoundedCornerShape(0.dp) else CircleShape
    val selectedGroup = groups.firstOrNull { it.id == selectedGroupId }
    Box {
        IconButton(onClick = { expanded = true }) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Группа заметки",
                    tint = selectedGroup?.let(::noteGroupColor)
                        ?: MaterialTheme.colorScheme.onSurfaceVariant,
                )
                selectedGroup?.let { group ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(8.dp)
                            .clip(markerShape)
                            .background(noteGroupColor(group)),
                    )
                }
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Без группы") },
                leadingIcon = {
                    Box(
                        Modifier
                            .size(18.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, markerShape),
                    )
                },
                trailingIcon = {
                    if (selectedGroupId == null) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                },
                onClick = {
                    expanded = false
                    onSelected(null)
                },
            )
            groups.forEach { group ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = group.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    leadingIcon = {
                        Box(
                            Modifier
                                .size(18.dp)
                                .clip(markerShape)
                                .background(noteGroupColor(group)),
                        )
                    },
                    trailingIcon = {
                        if (selectedGroupId == group.id) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelected(group.id)
                    },
                )
            }
        }
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

@Composable
internal fun MainSectionBar(
    notesSelected: Boolean,
    onOpenNotes: () -> Unit,
    onOpenLibrary: () -> Unit,
    onPrimaryAction: () -> Unit,
    primaryActionDescription: String,
    onOpenSettings: () -> Unit,
    searchSelected: Boolean = false,
    onOpenSearch: () -> Unit = {},
) {
    val navigationInset = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val contentHeight = 66.dp
    val lensOverlap = 11.dp

    // The central lens is a sibling of the clipped slab. Keeping it outside the
    // slab's shape prevents its lower optical rim from being cut on 3-button nav.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(contentHeight + navigationInset + lensOverlap),
    ) {
        ProxySurface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(contentHeight + navigationInset),
            shape = RectangleShape,
            role = ProxySurfaceRole.OVERLAY,
            strong = true,
            interactive = false,
            deformContent = false,
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(contentHeight)
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MainNavigationItem(
                    label = "Заметки",
                    selected = notesSelected && !searchSelected,
                    onClick = onOpenNotes,
                    icon = {
                        Icon(Icons.Default.Description, contentDescription = null)
                    },
                    modifier = Modifier.weight(1f),
                )
                MainNavigationItem(
                    label = "Библиотека",
                    selected = !notesSelected && !searchSelected,
                    onClick = onOpenLibrary,
                    icon = {
                        Icon(Icons.Default.MenuBook, contentDescription = null)
                    },
                    modifier = Modifier.weight(1f),
                )
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .height(contentHeight),
                )
                MainNavigationItem(
                    label = "Поиск",
                    selected = searchSelected,
                    onClick = onOpenSearch,
                    icon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    modifier = Modifier.weight(1f),
                )
                MainNavigationItem(
                    label = "Настройки",
                    selected = false,
                    onClick = onOpenSettings,
                    icon = {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        ProxySurface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f)
                .size(60.dp)
                .animatedClick(onClick = onPrimaryAction, pressedScale = 0.94f),
            shape = CircleShape,
            role = ProxySurfaceRole.BUTTON,
            strong = true,
            interactive = false,
            deformContent = false,
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = primaryActionDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(31.dp),
                )
            }
        }
    }
}

@Composable
private fun MainNavigationItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = modifier
            .height(66.dp)
            .animatedClick(onClick = onClick, pressedScale = 0.96f)
            .padding(horizontal = 2.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Box(
            Modifier
                .width(22.dp)
                .height(2.dp)
                .background(if (selected) contentColor else Color.Transparent),
        )
        CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides contentColor) {
            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) { icon() }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryScreen(
    state: LibraryUiState,
    onOpenNotes: () -> Unit,
    onOpenSettings: () -> Unit,
    onQueryChange: (String) -> Unit,
    onFilterChange: (LibraryFilter) -> Unit,
    onImport: (String, String) -> Unit,
    onOpenDocument: (LibraryDocument) -> Unit,
    onStatusChange: (LibraryDocument, LibraryReadingStatus) -> Unit,
    onDelete: (LibraryDocument) -> Unit,
) {
    val context = LocalContext.current
    var searchExpanded by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<LibraryDocument?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            onImport(uri.toString(), pdfDisplayName(context, uri))
        }
    }
    val launchImport = { launcher.launch(arrayOf("application/pdf")) }
    val continueDocument = remember(state.documents) {
        state.documents.firstOrNull {
            it.readingStatus == LibraryReadingStatus.READING && it.pageCount > 0
        } ?: state.documents.firstOrNull()
    }
    BackHandler {
        if (searchExpanded || state.query.isNotBlank()) {
            searchExpanded = false
            onQueryChange("")
        } else {
            onOpenNotes()
        }
    }

    pendingDelete?.let { document ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Убрать из библиотеки?") },
            text = {
                Text(
                    "Файл «${document.title}» останется на устройстве, " +
                        "но прогресс чтения ProxyScroll будет удалён.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(document)
                        pendingDelete = null
                    },
                ) {
                    Text("Убрать", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Отмена") }
            },
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { ProxyBrandLockup() },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        bottomBar = {
            MainSectionBar(
                notesSelected = false,
                onOpenNotes = onOpenNotes,
                onOpenLibrary = {},
                onPrimaryAction = launchImport,
                primaryActionDescription = "Импортировать PDF",
                onOpenSettings = onOpenSettings,
            )
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Библиотека",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = if (state.documents.isEmpty()) {
                                "Личное пространство для чтения и знаний"
                            } else {
                                "${state.documents.size} ${pdfCountLabel(state.documents.size)} · локально"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        onClick = {
                            searchExpanded = !searchExpanded
                            if (!searchExpanded) onQueryChange("")
                        },
                    ) {
                        Icon(
                            if (searchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (searchExpanded) "Закрыть поиск" else "Поиск",
                        )
                    }
                }
            }

            if (searchExpanded || state.query.isNotBlank()) {
                item {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (state.query.isNotBlank()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Очистить")
                                }
                            }
                        },
                        placeholder = { Text("Название книги или документа") },
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.30f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f),
                        ),
                    )
                }
            }

            if (state.documents.isEmpty()) {
                item {
                    EmptyLibraryState(onImport = launchImport)
                }
            } else {
                if (state.query.isBlank() && state.filter == LibraryFilter.ALL) {
                    continueDocument?.let { document ->
                        item {
                            ContinueReadingCard(
                                document = document,
                                onOpen = { onOpenDocument(document) },
                            )
                        }
                    }
                }

                item {
                    LibraryFilterRow(
                        selected = state.filter,
                        onSelected = onFilterChange,
                    )
                }

                if (state.visibleDocuments.isEmpty()) {
                    item { EmptyLibrarySearchState() }
                } else {
                    item {
                        LibrarySectionHeader(
                            title = "Моя полка",
                            detail = "${state.visibleDocuments.size} на полке",
                        )
                    }
                    item {
                        VirtualBookShelf(
                            documents = state.visibleDocuments,
                            onOpenDocument = onOpenDocument,
                        )
                    }
                    item {
                        LibrarySectionHeader(
                            title = "Все документы",
                            detail = state.visibleDocuments.size.toString(),
                        )
                    }
                    itemsIndexed(
                        items = state.visibleDocuments,
                        key = { _, document -> document.id },
                    ) { _, document ->
                        PdfLibraryCard(
                            document = document,
                            onOpen = { onOpenDocument(document) },
                            onStatusChange = { status -> onStatusChange(document, status) },
                            onDelete = { pendingDelete = document },
                        )
                    }
                    item { KnowledgeHubPreview() }
                }
            }
            item { Spacer(Modifier.height(6.dp)) }
        }
    }
}

@Composable
private fun EmptyLibraryState(
    onImport: () -> Unit,
) {
    ProxySurface(
        modifier = Modifier.fillMaxWidth(),
        role = ProxySurfaceRole.CARD,
        strong = true,
        interactive = false,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 38.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Default.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(58.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(14.dp))
            Text("Соберите свою первую полку", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(7.dp))
            Text(
                "Импортируйте PDF. Файл останется на устройстве, а ProxyScroll " +
                    "запомнит последнюю страницу.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(18.dp))
            ProxyInsetSurface(
                modifier = Modifier
                    .height(48.dp)
                    .animatedClick(onClick = onImport, pressedScale = 0.96f),
                role = ProxySurfaceRole.BUTTON,
                selected = true,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Импортировать PDF", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun ContinueReadingCard(
    document: LibraryDocument,
    onOpen: () -> Unit,
) {
    val progress = libraryProgress(document)
    ProxySurface(
        modifier = Modifier
            .fillMaxWidth()
            .animatedClick(onClick = onOpen, pressedScale = 0.98f),
        role = ProxySurfaceRole.CARD,
        strong = true,
        interactive = false,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LibraryBookCover(
                document = document,
                modifier = Modifier
                    .width(92.dp)
                    .height(130.dp),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    "Продолжить чтение",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    document.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    libraryPositionLabel(document),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${(progress * 100).roundToInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(
                        Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            }
            Icon(
                Icons.Default.NavigateNext,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LibraryFilterRow(
    selected: LibraryFilter,
    onSelected: (LibraryFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LibraryFilter.entries.forEach { filter ->
            MotionOption(
                label = filter.libraryLabel(),
                selected = filter == selected,
                onClick = { onSelected(filter) },
            )
        }
    }
}

@Composable
private fun LibrarySectionHeader(
    title: String,
    detail: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        Text(
            detail,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VirtualBookShelf(
    documents: List<LibraryDocument>,
    onOpenDocument: (LibraryDocument) -> Unit,
) {
    val palette = LocalStainPaletteColors.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            documents.forEach { document ->
                Column(
                    modifier = Modifier
                        .width(126.dp)
                        .animatedClick(
                            onClick = { onOpenDocument(document) },
                            pressedScale = 0.965f,
                        ),
                ) {
                    LibraryBookCover(
                        document = document,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(174.dp),
                    )
                    Spacer(Modifier.height(7.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${(libraryProgress(document) * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = libraryAccent(document),
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            document.readingStatus.libraryLabel(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            palette.primary.copy(alpha = 0.32f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                            Color.Black.copy(alpha = 0.34f),
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun LibraryBookCover(
    document: LibraryDocument,
    modifier: Modifier = Modifier,
) {
    val accent = libraryAccent(document)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 7.dp, topEnd = 11.dp, bottomEnd = 7.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        accent.copy(alpha = 0.34f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    ),
                ),
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.26f),
                RoundedCornerShape(topStart = 7.dp, topEnd = 11.dp, bottomEnd = 7.dp),
            ),
    ) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(5.dp)
                .background(accent),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, top = 12.dp, end = 10.dp, bottom = 12.dp),
        ) {
            Text(
                "PDF",
                style = MaterialTheme.typography.labelSmall,
                color = accent,
            )
            Spacer(Modifier.weight(1f))
            Text(
                document.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(9.dp))
            Text(
                if (document.pageCount > 0) {
                    "${document.lastPage + 1} / ${document.pageCount}"
                } else {
                    "На полке"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(libraryProgress(document))
                .height(3.dp)
                .background(accent),
        )
    }
}

@Composable
private fun PdfLibraryCard(
    document: LibraryDocument,
    onOpen: () -> Unit,
    onStatusChange: (LibraryReadingStatus) -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val progress = libraryProgress(document)
    ProxySurface(
        modifier = Modifier
            .fillMaxWidth()
            .animatedClick(onClick = onOpen, pressedScale = 0.985f),
        role = ProxySurfaceRole.CARD,
        strong = document.lastPage > 0,
        interactive = false,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LibraryBookCover(
                document = document,
                modifier = Modifier
                    .width(52.dp)
                    .height(72.dp),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${document.readingStatus.libraryLabel()} · " +
                        libraryPositionLabel(document),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(7.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(libraryAccent(document)),
                    )
                }
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Действия с документом",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    LibraryReadingStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.libraryLabel()) },
                            onClick = {
                                menuExpanded = false
                                onStatusChange(status)
                            },
                            trailingIcon = {
                                if (status == document.readingStatus) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            },
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Убрать из библиотеки",
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyLibrarySearchState() {
    ProxySurface(
        modifier = Modifier.fillMaxWidth(),
        role = ProxySurfaceRole.CARD,
        interactive = false,
    ) {
        Column(
            modifier = Modifier.padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            Text("На этой полке ничего не найдено", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Измените запрос или выберите другой статус",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun KnowledgeHubPreview() {
    ProxySurface(
        modifier = Modifier.fillMaxWidth(),
        role = ProxySurfaceRole.CARD,
        interactive = false,
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ProxyInsetSurface(
                modifier = Modifier.size(50.dp),
                role = ProxySurfaceRole.BUTTON,
                selected = false,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Bookmarks,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text("Цитаты и книжные заметки", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Здесь будут собираться выделения и фрагменты PDF со ссылкой на страницу.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "Скоро",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun libraryAccent(document: LibraryDocument): Color {
    val palette = LocalStainPaletteColors.current
    return when ((document.title.hashCode() and Int.MAX_VALUE) % 3) {
        0 -> palette.primary
        1 -> palette.secondary
        else -> palette.tertiary
    }
}

private fun libraryProgress(document: LibraryDocument): Float = if (document.pageCount > 0) {
    ((document.lastPage + 1).toFloat() / document.pageCount).coerceIn(0f, 1f)
} else {
    0f
}

private fun libraryPositionLabel(document: LibraryDocument): String = if (document.pageCount > 0) {
    "стр. ${document.lastPage + 1} из ${document.pageCount}"
} else {
    "ещё не открыт"
}

private fun LibraryReadingStatus.libraryLabel(): String = when (this) {
    LibraryReadingStatus.READING -> "Читаю"
    LibraryReadingStatus.WANT_TO_READ -> "Хочу прочитать"
    LibraryReadingStatus.COMPLETED -> "Завершено"
}

private fun LibraryFilter.libraryLabel(): String = when (this) {
    LibraryFilter.ALL -> "Все"
    LibraryFilter.READING -> "Читаю"
    LibraryFilter.WANT_TO_READ -> "Хочу прочитать"
    LibraryFilter.COMPLETED -> "Завершено"
}

private fun pdfCountLabel(count: Int): String {
    val mod100 = count % 100
    val mod10 = count % 10
    return when {
        mod100 in 11..14 -> "PDF-документов"
        mod10 == 1 -> "PDF-документ"
        mod10 in 2..4 -> "PDF-документа"
        else -> "PDF-документов"
    }
}

private data class PdfDocumentInfo(
    val pageCount: Int = 0,
    val error: String? = null,
)

private data class PdfPageRender(
    val image: androidx.compose.ui.graphics.ImageBitmap? = null,
    val error: String? = null,
)

private class PdfBitmapCache(
    private val maxEntries: Int = 3,
) {
    private val pages = object : LinkedHashMap<Int, androidx.compose.ui.graphics.ImageBitmap>(
        maxEntries,
        0.75f,
        true,
    ) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<Int, androidx.compose.ui.graphics.ImageBitmap>?,
        ): Boolean = size > maxEntries
    }

    @Synchronized
    fun get(page: Int): androidx.compose.ui.graphics.ImageBitmap? = pages[page]

    @Synchronized
    fun getOrRender(
        page: Int,
        render: () -> androidx.compose.ui.graphics.ImageBitmap,
    ): androidx.compose.ui.graphics.ImageBitmap = pages[page] ?: render().also {
        pages[page] = it
    }

    @Synchronized
    fun clear() {
        pages.clear()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfReaderScreen(
    document: LibraryDocument,
    onBack: () -> Unit,
    onProgressChanged: (Int, Int) -> Unit,
    onScrollQuietChanged: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val documentInfo by produceState(
        initialValue = PdfDocumentInfo(),
        key1 = document.uri,
    ) {
        value = withContext(Dispatchers.IO) {
            readPdfDocumentInfo(context, Uri.parse(document.uri))
        }
    }
    val currentScrollQuietChanged by rememberUpdatedState(onScrollQuietChanged)
    DisposableEffect(Unit) {
        onDispose { currentScrollQuietChanged(false) }
    }
    BackHandler(onBack = onBack)

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            documentInfo.error != null -> PdfReaderErrorState(
                message = documentInfo.error.orEmpty(),
                onBack = onBack,
            )
            documentInfo.pageCount <= 0 -> CircularProgressIndicator()
            else -> PdfReaderReady(
                document = document,
                pageCount = documentInfo.pageCount,
                onBack = onBack,
                onProgressChanged = onProgressChanged,
                onScrollQuietChanged = onScrollQuietChanged,
            )
        }
    }
}

@Composable
private fun PdfReaderErrorState(
    message: String,
    onBack: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp),
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "В библиотеку")
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.height(12.dp))
            Text("Не удалось открыть PDF", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PdfReaderReady(
    document: LibraryDocument,
    pageCount: Int,
    onBack: () -> Unit,
    onProgressChanged: (Int, Int) -> Unit,
    onScrollQuietChanged: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cache = remember(document.uri) { PdfBitmapCache() }
    val initialPage = document.lastPage.coerceIn(0, pageCount - 1)
    val pagerState = rememberPagerState(initialPage = initialPage) { pageCount }
    var controlsVisible by remember(document.id) { mutableStateOf(true) }
    var gestureHintVisible by remember(document.id) { mutableStateOf(true) }
    var resetZoomToken by remember(document.id) { mutableIntStateOf(0) }
    var currentScale by remember(document.id) { mutableFloatStateOf(1f) }
    var pagerInteractionActive by remember { mutableStateOf(false) }
    var pageInteractionActive by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var scrubPage by remember(document.id) { mutableFloatStateOf(initialPage.toFloat()) }
    var isScrubbing by remember { mutableStateOf(false) }
    val currentScrollQuietChanged by rememberUpdatedState(onScrollQuietChanged)

    fun resetZoom() {
        resetZoomToken += 1
        currentScale = 1f
    }

    fun goToPage(page: Int) {
        val target = page.coerceIn(0, pageCount - 1)
        resetZoom()
        scope.launch {
            pagerState.animateScrollToPage(target)
        }
    }

    LaunchedEffect(document.id) {
        delay(4200)
        gestureHintVisible = false
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.isScrollInProgress }
            .distinctUntilChanged()
            .collectLatest { moving -> pagerInteractionActive = moving }
    }
    LaunchedEffect(pagerInteractionActive, pageInteractionActive) {
        currentScrollQuietChanged(pagerInteractionActive || pageInteractionActive)
    }
    LaunchedEffect(pagerState, pageCount) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collectLatest { page ->
                resetZoom()
                if (!isScrubbing) scrubPage = page.toFloat()
                onProgressChanged(page, pageCount)
                withContext(Dispatchers.IO) {
                    listOf(page - 1, page + 1)
                        .filter { it in 0 until pageCount }
                        .forEach { adjacentPage ->
                            renderPdfPage(
                                context = context,
                                uri = Uri.parse(document.uri),
                                requestedPage = adjacentPage,
                                cache = cache,
                            )
                        }
                }
            }
    }
    LaunchedEffect(pagerState.currentPage, isScrubbing) {
        if (!isScrubbing) scrubPage = pagerState.currentPage.toFloat()
    }
    DisposableEffect(cache) {
        onDispose {
            cache.clear()
            currentScrollQuietChanged(false)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds(),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = currentScale <= 1.01f,
            pageSpacing = 12.dp,
        ) { page ->
            val pageOffset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                ).absoluteValue.coerceIn(0f, 1f)
            PdfReaderPage(
                document = document,
                page = page,
                cache = cache,
                isCurrent = page == pagerState.currentPage,
                resetZoomToken = resetZoomToken,
                onScaleChanged = { scale ->
                    if (page == pagerState.currentPage) currentScale = scale
                },
                onInteractionChanged = { active ->
                    if (page == pagerState.currentPage) pageInteractionActive = active
                },
                onCenterTap = {
                    gestureHintVisible = false
                    controlsVisible = !controlsVisible
                },
                onPreviousPage = {
                    if (pagerState.currentPage > 0) goToPage(pagerState.currentPage - 1)
                },
                onNextPage = {
                    if (pagerState.currentPage < pageCount - 1) {
                        goToPage(pagerState.currentPage + 1)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 1f - pageOffset * 0.16f
                        val pageScale = 1f - pageOffset * 0.018f
                        scaleX = pageScale
                        scaleY = pageScale
                    },
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(3f),
            enter = fadeIn(tween(180)) + slideInVertically(tween(260)) { -it },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(220)) { -it },
        ) {
            ProxySurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                role = ProxySurfaceRole.OVERLAY,
                strong = true,
                interactive = false,
                deformContent = false,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "В библиотеку")
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            document.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            "${pagerState.currentPage + 1} / $pageCount · " +
                                "${(currentScale * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        enabled = currentScale > 1.01f,
                        onClick = ::resetZoom,
                    ) {
                        Icon(Icons.Default.FormatSize, contentDescription = "По ширине страницы")
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Навигация по документу")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Первая страница") },
                                onClick = {
                                    menuExpanded = false
                                    goToPage(0)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Последняя страница") },
                                onClick = {
                                    menuExpanded = false
                                    goToPage(pageCount - 1)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Вернуть масштаб 100%") },
                                onClick = {
                                    menuExpanded = false
                                    resetZoom()
                                },
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(3f),
            enter = fadeIn(tween(180)) + slideInVertically(tween(260)) { it },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(220)) { it },
        ) {
            ProxySurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                role = ProxySurfaceRole.OVERLAY,
                strong = true,
                interactive = false,
                deformContent = false,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        IconButton(
                            enabled = pagerState.currentPage > 0,
                            onClick = { goToPage(pagerState.currentPage - 1) },
                        ) {
                            Icon(
                                Icons.Default.NavigateBefore,
                                contentDescription = "Предыдущая страница",
                            )
                        }
                        Text(
                            "Страница ${(if (isScrubbing) scrubPage else pagerState.currentPage.toFloat()).roundToInt() + 1}",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        IconButton(
                            enabled = pagerState.currentPage < pageCount - 1,
                            onClick = { goToPage(pagerState.currentPage + 1) },
                        ) {
                            Icon(
                                Icons.Default.NavigateNext,
                                contentDescription = "Следующая страница",
                            )
                        }
                    }
                    Slider(
                        value = if (pageCount > 1) scrubPage else 0f,
                        onValueChange = { value ->
                            isScrubbing = true
                            scrubPage = value.roundToInt().toFloat()
                        },
                        onValueChangeFinished = {
                            val target = scrubPage.roundToInt()
                            isScrubbing = false
                            goToPage(target)
                        },
                        valueRange = 0f..(pageCount - 1).coerceAtLeast(1).toFloat(),
                        enabled = pageCount > 1,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = gestureHintVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = if (controlsVisible) 132.dp else 22.dp)
                .zIndex(4f),
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(220)),
        ) {
            ProxySurface(
                role = ProxySurfaceRole.OVERLAY,
                strong = true,
                interactive = false,
                deformContent = false,
            ) {
                Text(
                    "Свайп — страница · щипок — масштаб · двойной тап",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun PdfReaderPage(
    document: LibraryDocument,
    page: Int,
    cache: PdfBitmapCache,
    isCurrent: Boolean,
    resetZoomToken: Int,
    onScaleChanged: (Float) -> Unit,
    onInteractionChanged: (Boolean) -> Unit,
    onCenterTap: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val cachedImage = remember(document.uri, page) { cache.get(page) }
    val render by produceState(
        initialValue = PdfPageRender(image = cachedImage),
        key1 = document.uri,
        key2 = page,
    ) {
        if (value.image == null) {
            value = withContext(Dispatchers.IO) {
                renderPdfPage(
                    context = context,
                    uri = Uri.parse(document.uri),
                    requestedPage = page,
                    cache = cache,
                )
            }
        }
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when {
            render.error != null -> Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    render.error.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            render.image == null -> CircularProgressIndicator()
            else -> ZoomablePdfPage(
                image = render.image ?: return@Box,
                page = page,
                isCurrent = isCurrent,
                resetZoomToken = resetZoomToken,
                onScaleChanged = onScaleChanged,
                onInteractionChanged = onInteractionChanged,
                onCenterTap = onCenterTap,
                onPreviousPage = onPreviousPage,
                onNextPage = onNextPage,
            )
        }
    }
}

@Composable
private fun ZoomablePdfPage(
    image: androidx.compose.ui.graphics.ImageBitmap,
    page: Int,
    isCurrent: Boolean,
    resetZoomToken: Int,
    onScaleChanged: (Float) -> Unit,
    onInteractionChanged: (Boolean) -> Unit,
    onCenterTap: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var scale by remember(page) { mutableFloatStateOf(1f) }
    var offsetX by remember(page) { mutableFloatStateOf(0f) }
    var offsetY by remember(page) { mutableFloatStateOf(0f) }
    var zoomAnimationJob by remember(page) { mutableStateOf<Job?>(null) }
    val scrollState = rememberScrollState()
    var viewportWidth by remember(page) { mutableIntStateOf(1) }
    var viewportHeight by remember(page) { mutableIntStateOf(1) }
    var transforming by remember(page) { mutableStateOf(false) }
    var verticallyScrolling by remember(page) { mutableStateOf(false) }

    fun panBounds(targetScale: Float): Pair<Float, Float> {
        val availableWidth = viewportWidth.toFloat() * 0.96f
        val imageHeight = availableWidth * image.height / image.width
        val maxX = ((availableWidth * targetScale - viewportWidth) / 2f).coerceAtLeast(0f)
        val maxY = ((imageHeight * targetScale - viewportHeight) / 2f).coerceAtLeast(0f)
        return maxX to maxY
    }

    fun animateZoom(targetScale: Float, tap: Offset? = null) {
        val safeScale = targetScale.coerceIn(1f, 4f)
        val (maxX, maxY) = panBounds(safeScale)
        val targetX = if (safeScale <= 1.01f || tap == null) {
            0f
        } else {
            ((viewportWidth / 2f - tap.x) * (safeScale - 1f)).coerceIn(-maxX, maxX)
        }
        val targetY = if (safeScale <= 1.01f || tap == null) {
            0f
        } else {
            ((viewportHeight / 2f - tap.y) * (safeScale - 1f)).coerceIn(-maxY, maxY)
        }
        zoomAnimationJob?.cancel()
        val startScale = scale
        val startX = offsetX
        val startY = offsetY
        zoomAnimationJob = scope.launch {
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.84f, stiffness = Spring.StiffnessMediumLow),
            ) { progress, _ ->
                scale = startScale + (safeScale - startScale) * progress
                offsetX = startX + (targetX - startX) * progress
                offsetY = startY + (targetY - startY) * progress
            }
        }
    }

    LaunchedEffect(resetZoomToken) {
        if (resetZoomToken > 0) animateZoom(1f)
    }
    LaunchedEffect(scale, isCurrent) {
        if (isCurrent) onScaleChanged(scale)
    }
    LaunchedEffect(scrollState, isCurrent) {
        snapshotFlow { scrollState.isScrollInProgress }
            .distinctUntilChanged()
            .collectLatest { scrolling ->
                verticallyScrolling = scrolling
            }
    }
    LaunchedEffect(transforming, verticallyScrolling, isCurrent) {
        onInteractionChanged(isCurrent && (transforming || verticallyScrolling))
    }
    DisposableEffect(isCurrent) {
        onDispose {
            if (isCurrent) onInteractionChanged(false)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .onGloballyPositioned { coordinates ->
                viewportWidth = coordinates.size.width.coerceAtLeast(1)
                viewportHeight = coordinates.size.height.coerceAtLeast(1)
            }
            .pointerInput(page) {
                detectTapGestures(
                    onTap = { position ->
                        if (scale <= 1.01f) {
                            when {
                                position.x < viewportWidth * 0.24f -> onPreviousPage()
                                position.x > viewportWidth * 0.76f -> onNextPage()
                                else -> onCenterTap()
                            }
                        } else {
                            onCenterTap()
                        }
                    },
                    onDoubleTap = { position ->
                        if (scale > 1.05f) {
                            animateZoom(1f)
                        } else {
                            animateZoom(2.25f, position)
                        }
                    },
                )
            }
            .pointerInput(page) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var transformedThisGesture = false
                    var pointersStillPressed: Boolean
                    do {
                        val event = awaitPointerEvent()
                        val pressedPointers = event.changes.count { it.pressed }
                        val canTransform = pressedPointers >= 2 || scale > 1.01f
                        if (canTransform) {
                            zoomAnimationJob?.cancel()
                            transformedThisGesture = true
                            transforming = true
                            val zoomChange = if (pressedPointers >= 2) {
                                event.calculateZoom()
                            } else {
                                1f
                            }
                            val panChange = event.calculatePan()
                            val newScale = (scale * zoomChange).coerceIn(1f, 4f)
                            val (maxX, maxY) = panBounds(newScale)
                            scale = newScale
                            offsetX = (offsetX + panChange.x).coerceIn(-maxX, maxX)
                            offsetY = (offsetY + panChange.y).coerceIn(-maxY, maxY)
                            event.changes.forEach { it.consume() }
                        }
                        pointersStillPressed = event.changes.any { it.pressed }
                    } while (pointersStillPressed)
                    if (transformedThisGesture && scale <= 1.02f) {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    }
                    transforming = false
                }
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    state = scrollState,
                    enabled = scale <= 1.01f,
                )
                .padding(horizontal = 8.dp, vertical = 10.dp),
        ) {
            ProxySurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offsetX
                        translationY = offsetY
                    },
                role = ProxySurfaceRole.CARD,
                strong = true,
                interactive = false,
                deformContent = false,
            ) {
                Image(
                    bitmap = image,
                    contentDescription = "Страница ${page + 1}",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(image.width.toFloat() / image.height.toFloat()),
                )
            }
        }
    }
}

private fun pdfDisplayName(context: android.content.Context, uri: Uri): String {
    val queried = runCatching {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }.getOrNull()
    return queried?.removeSuffix(".pdf")?.takeIf { it.isNotBlank() } ?: "PDF-документ"
}

private fun readPdfDocumentInfo(
    context: android.content.Context,
    uri: Uri,
): PdfDocumentInfo = runCatching {
    val descriptor = context.contentResolver.openFileDescriptor(uri, "r")
        ?: error("Файл больше недоступен")
    descriptor.use { file ->
        PdfRenderer(file).use { renderer ->
            if (renderer.pageCount <= 0) error("В документе нет страниц")
            PdfDocumentInfo(pageCount = renderer.pageCount)
        }
    }
}.getOrElse { error ->
    PdfDocumentInfo(error = error.message ?: "Неизвестная ошибка чтения")
}

private fun renderPdfPage(
    context: android.content.Context,
    uri: Uri,
    requestedPage: Int,
    cache: PdfBitmapCache,
): PdfPageRender = runCatching {
    cache.get(requestedPage)?.let { cached ->
        return@runCatching PdfPageRender(image = cached)
    }
    val descriptor = context.contentResolver.openFileDescriptor(uri, "r")
        ?: error("Файл больше недоступен")
    descriptor.use { file ->
        PdfRenderer(file).use { renderer ->
            if (renderer.pageCount <= 0) error("В документе нет страниц")
            val actualPage = requestedPage.coerceIn(0, renderer.pageCount - 1)
            val image = cache.getOrRender(actualPage) {
                renderer.openPage(actualPage).use { page ->
                    val targetWidth = 1600
                    val targetHeight = (targetWidth.toFloat() * page.height / page.width)
                        .roundToInt()
                        .coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(
                        targetWidth,
                        targetHeight,
                        Bitmap.Config.ARGB_8888,
                    )
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap.asImageBitmap()
                }
            }
            PdfPageRender(image = image)
        }
    }
}.getOrElse { error ->
    PdfPageRender(error = error.message ?: "Неизвестная ошибка чтения")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteReaderScreen(
    note: Note,
    group: NoteGroup?,
    settings: ReadingSettings,
    onSettingsChanged: (ReadingSettings) -> Unit,
    onScrollQuietChanged: (Boolean) -> Unit,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
) {
    val liteLife = LocalProxyVisualStyle.current.theme == AppTheme.LITE_LIFE
    val scrollState = rememberScrollState()
    var showReadingControls by remember(note.id) { mutableStateOf(false) }
    var textLayout by remember(note.id, settings.fontScale) {
        mutableStateOf<TextLayoutResult?>(null)
    }
    val accentColor = group?.let(::noteGroupColor)
    val currentScrollQuietChanged by rememberUpdatedState(onScrollQuietChanged)
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.isScrollInProgress }
            .distinctUntilChanged()
            .collectLatest { scrolling ->
                currentScrollQuietChanged(scrolling)
                if (scrolling) {
                    delay(140)
                }
            }
    }
    DisposableEffect(Unit) {
        onDispose { currentScrollQuietChanged(false) }
    }
    BackHandler(onBack = onBack)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Чтение", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "#${note.index.toString().padStart(3, '0')}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "К списку заметок")
                    }
                },
                actions = {
                    ProxySurface(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(44.dp)
                            .animatedClick(
                                onClick = { onEdit(note.body.length) },
                                pressedScale = 0.96f,
                            ),
                        role = ProxySurfaceRole.BUTTON,
                        strong = true,
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать")
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
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = settings.pageMarginDp.dp)
                    .padding(top = 14.dp, bottom = 116.dp),
            ) {
                Text(
                    text = note.title.ifBlank { "Без названия" },
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = (32f * settings.fontScale.coerceIn(0.86f, 1.28f)).sp,
                        lineHeight = (38f * settings.fontScale.coerceIn(0.86f, 1.28f)).sp,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = DateFormat.getDateTimeInstance(
                            DateFormat.MEDIUM,
                            DateFormat.SHORT,
                        ).format(Date(note.updatedAt)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    group?.let {
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = accentColor ?: MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                HorizontalDivider(
                    color = (accentColor ?: MaterialTheme.colorScheme.outline)
                        .copy(alpha = if (accentColor == null) 0.18f else 0.38f),
                )
                Spacer(Modifier.height(22.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 340.dp)
                        .readerTextGestures(
                            key = Triple(note.id, textLayout, settings.fontScale),
                            onTap = { position ->
                                val cursor = textLayout
                                    ?.getOffsetForPosition(position)
                                    ?: note.body.length
                                onEdit(cursor)
                            },
                            onZoom = { zoomChange ->
                                onSettingsChanged(
                                    settings.copy(
                                        fontScale = settings.fontScale * zoomChange,
                                    ).normalized(),
                                )
                            },
                        ),
                ) {
                    if (note.body.isBlank()) {
                        Text(
                            text = "Пустая заметка\nКоснитесь, чтобы начать писать",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 26.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = readingAnnotatedText(
                                text = note.body,
                                spans = note.spans,
                                fontScale = settings.fontScale,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onBackground,
                                lineHeight = (
                                    28f * settings.fontScale * settings.lineHeight
                                ).sp,
                                textAlign = note.textAlignment.toComposeTextAlign(),
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            onTextLayout = { textLayout = it },
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showReadingControls,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 14.dp, bottom = 76.dp)
                    .zIndex(2f),
                enter = fadeIn(tween(if (liteLife) 1 else 180)) +
                    slideInVertically(tween(if (liteLife) 1 else 260)) { it / 4 } +
                    scaleIn(tween(if (liteLife) 1 else 260), initialScale = 0.94f),
                exit = fadeOut(tween(if (liteLife) 1 else 140)) +
                    slideOutVertically(tween(if (liteLife) 1 else 200)) { it / 4 } +
                    scaleOut(tween(if (liteLife) 1 else 180), targetScale = 0.96f),
            ) {
                ReadingControls(
                    settings = settings,
                    onSettingsChanged = onSettingsChanged,
                    modifier = Modifier.fillMaxWidth(0.88f),
                )
            }

            ProxySurface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 14.dp, bottom = 12.dp)
                    .size(52.dp)
                    .animatedClick(
                        onClick = { showReadingControls = !showReadingControls },
                        pressedScale = if (liteLife) 1f else 0.92f,
                    ),
                role = ProxySurfaceRole.BUTTON,
                strong = showReadingControls,
                active = showReadingControls,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.FormatSize,
                        contentDescription = "Настроить чтение",
                        tint = if (showReadingControls) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }
        }
    }
}

private fun Modifier.readerTextGestures(
    key: Any?,
    onTap: (Offset) -> Unit,
    onZoom: (Float) -> Unit,
): Modifier = pointerInput(key) {
    awaitEachGesture {
        val firstDown = awaitFirstDown(requireUnconsumed = false)
        val startPosition = firstDown.position
        val touchSlop = viewConfiguration.touchSlop
        var maximumMovement = 0f
        var usedMultiplePointers = false
        var event = awaitPointerEvent()
        do {
            val pressed = event.changes.filter { it.pressed }
            if (pressed.size >= 2) {
                usedMultiplePointers = true
                val zoom = event.calculateZoom()
                if (zoom.isFinite() && kotlin.math.abs(zoom - 1f) > 0.002f) {
                    onZoom(zoom)
                    event.changes.forEach { it.consume() }
                }
            } else {
                event.changes.firstOrNull { it.id == firstDown.id }?.let { change ->
                    maximumMovement = maxOf(
                        maximumMovement,
                        (change.position - startPosition).getDistance(),
                    )
                }
            }
            if (event.changes.none { it.pressed }) break
            event = awaitPointerEvent()
        } while (true)

        if (!usedMultiplePointers && maximumMovement < touchSlop) {
            onTap(startPosition)
        }
    }
}

@Composable
private fun ReadingControls(
    settings: ReadingSettings,
    onSettingsChanged: (ReadingSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    ProxySurface(
        modifier = modifier,
        role = ProxySurfaceRole.OVERLAY,
        strong = true,
        interactive = false,
        deformContent = false,
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Текст", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${(settings.fontScale * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Slider(
                value = settings.fontScale,
                onValueChange = {
                    onSettingsChanged(settings.copy(fontScale = it).normalized())
                },
                valueRange = MIN_READING_FONT_SCALE..MAX_READING_FONT_SCALE,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Воздух между строками", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${(settings.lineHeight * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Slider(
                value = settings.lineHeight,
                onValueChange = {
                    onSettingsChanged(settings.copy(lineHeight = it).normalized())
                },
                valueRange = MIN_READING_LINE_HEIGHT..MAX_READING_LINE_HEIGHT,
            )
            Text("Ширина страницы", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Шире" to 12, "Баланс" to 20, "Уже" to 32).forEach { (label, margin) ->
                    MotionOption(
                        label = label,
                        selected = settings.pageMarginDp == margin,
                        onClick = {
                            onSettingsChanged(settings.copy(pageMarginDp = margin))
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private enum class EditorSaveState { CLEAN, EDITING, SAVED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteEditorScreen(
    note: Note?,
    initialBodyCursor: Int?,
    groups: List<NoteGroup>,
    inputMotion: InputMotion,
    onSave: (
        Note?,
        String,
        String,
        List<NoteSpan>,
        NoteTextAlignment,
        String?,
    ) -> Note?,
    onDelete: (Note) -> Unit,
    onClose: (Note?) -> Unit,
    onTypingQuietChanged: (Boolean) -> Unit,
) {
    var savedNote by remember(note?.id) { mutableStateOf(note) }
    var title by remember(note?.id) { mutableStateOf(note?.title.orEmpty()) }
    var selectedGroupId by remember(note?.id) {
        mutableStateOf(note?.groupId)
    }
    var textAlignment by remember(note?.id) {
        mutableStateOf(note?.textAlignment ?: NoteTextAlignment.START)
    }
    val selectedGroup = groups.firstOrNull { it.id == selectedGroupId }
    val liteLife = LocalProxyVisualStyle.current.theme == AppTheme.LITE_LIFE
    val richText = remember(note?.id, initialBodyCursor) {
        RichTextState(note?.body.orEmpty(), note?.spans.orEmpty()).also { state ->
            initialBodyCursor?.let(state::moveCursorTo)
        }
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
        targetValue = when {
            liteLife && bodyFocused -> 0.18f
            liteLife -> 0.08f
            bodyFocused -> 0.34f
            else -> 0.18f
        },
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "editor-content-contrast",
    )
    val titleVerticalPadding by animateDpAsState(
        targetValue = if (bodyFocused || liteLife) 6.dp else 14.dp,
        animationSpec = tween(240, easing = FastOutSlowInEasing),
        label = "editor-title-space",
    )
    val editorPagePadding by animateDpAsState(
        targetValue = if (bodyFocused) 8.dp else 16.dp,
        animationSpec = tween(240, easing = FastOutSlowInEasing),
        label = "editor-page-width",
    )
    val editorTextPadding by animateDpAsState(
        targetValue = if (bodyFocused) 12.dp else 18.dp,
        animationSpec = tween(240, easing = FastOutSlowInEasing),
        label = "editor-text-padding",
    )
    val automaticTitle = remember(richText.value.text) {
        richText.value.text
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(3)
            .joinToString(" ")
    }

    fun saveNow(): Note? {
        if (!hasChanges) return savedNote
        val result = onSave(
            savedNote,
            title,
            richText.value.text,
            richText.toSpans(),
            textAlignment,
            selectedGroupId,
        )
        if (result != null) savedNote = result
        hasChanges = false
        saveState = if (result == null) EditorSaveState.CLEAN else EditorSaveState.SAVED
        return savedNote
    }

    fun finishEditing() {
        onClose(saveNow())
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

    val focusManager = LocalFocusManager.current
    BackHandler {
        if (bodyFocused) {
            focusManager.clearFocus()
            onTypingQuietChanged(false)
        } else {
            finishEditing()
        }
    }

    LaunchedEffect(groups.map { it.id }) {
        if (selectedGroupId != null && groups.none { it.id == selectedGroupId }) {
            selectedGroupId = null
            hasChanges = true
            saveState = EditorSaveState.EDITING
        }
    }

    LaunchedEffect(note?.id, initialBodyCursor) {
        delay(260)
        bodyFocusRequester.requestFocus()
    }

    LaunchedEffect(title, richText.revision, selectedGroupId, textAlignment) {
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
                    NoteGroupMenuButton(
                        selectedGroupId = selectedGroupId,
                        groups = groups,
                        onSelected = { groupId ->
                            if (selectedGroupId != groupId) {
                                selectedGroupId = groupId
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
                textAlignment = textAlignment,
                onTextAlignmentChanged = { alignment ->
                    if (textAlignment != alignment) {
                        textAlignment = alignment
                        hasChanges = true
                        saveState = EditorSaveState.EDITING
                    }
                },
                modifier = Modifier
                    .navigationBarsPadding()
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
                .padding(horizontal = editorPagePadding),
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
                textStyle = (if (bodyFocused || liteLife) {
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
                                style = if (bodyFocused || liteLife) {
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
                targetValue = (
                    editorCornerDp + if (bodyFocused && !liteLife) 6 else 0
                ).dp,
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
                strong = bodyFocused && !liteLife,
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
                    if (!liteLife) {
                        TypingOpticalTrail(
                            layoutResult = bodyTextLayout,
                            characterIndex = typingPulseIndex,
                            reveal = typingReveal.value,
                            color = selectedGroup?.let(::noteGroupColor) ?: glowColor,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
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
                        .padding(horizontal = editorTextPadding, vertical = 14.dp),
                    interactionSource = bodyInteractionSource,
                    onTextLayout = { bodyTextLayout = it },
                    visualTransformation = richText.visualTransformation,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = textAlignment.toComposeTextAlign(),
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
                        onClose(null)
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
    textAlignment: NoteTextAlignment,
    onTextAlignmentChanged: (NoteTextAlignment) -> Unit,
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
                TextPresetMenu(
                    onSelected = { size, bold ->
                        richText.applyTextPreset(size, bold)
                        onFormatChanged()
                    },
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
                AlignmentMenu(
                    alignment = textAlignment,
                    onSelected = onTextAlignmentChanged,
                )
            }
        }
    }
}

private data class EditorTextPreset(
    val label: String,
    val description: String,
    val sizeSp: Int,
    val bold: Boolean,
)

@Composable
private fun TextPresetMenu(
    onSelected: (Int, Boolean) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val presets = remember {
        listOf(
            EditorTextPreset("Обычный текст", "19 sp", 19, false),
            EditorTextPreset("Заголовок 1", "32 sp · жирный", 32, true),
            EditorTextPreset("Заголовок 2", "26 sp · жирный", 26, true),
            EditorTextPreset("Подзаголовок", "22 sp · жирный", 22, true),
            EditorTextPreset("Подпись", "14 sp", 14, false),
        )
    }
    Box {
        FormatButton(
            selected = expanded,
            onClick = { expanded = true },
        ) {
            Text("Aa", style = MaterialTheme.typography.titleMedium)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            presets.forEach { preset ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(preset.label, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                preset.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    onClick = {
                        onSelected(preset.sizeSp, preset.bold)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun AlignmentMenu(
    alignment: NoteTextAlignment,
    onSelected: (NoteTextAlignment) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FormatButton(
            selected = expanded || alignment != NoteTextAlignment.START,
            onClick = { expanded = true },
        ) {
            when (alignment) {
                NoteTextAlignment.START -> Icon(
                    Icons.Default.FormatAlignLeft,
                    contentDescription = "Выравнивание",
                )
                NoteTextAlignment.CENTER -> Icon(
                    Icons.Default.FormatAlignCenter,
                    contentDescription = "Выравнивание по центру",
                )
                NoteTextAlignment.END -> Icon(
                    Icons.Default.FormatAlignRight,
                    contentDescription = "Выравнивание справа",
                )
                NoteTextAlignment.JUSTIFY -> Icon(
                    Icons.Default.FormatAlignJustify,
                    contentDescription = "Выравнивание по ширине",
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            NoteTextAlignment.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.displayName) },
                    leadingIcon = {
                        when (option) {
                            NoteTextAlignment.START -> Icon(
                                Icons.Default.FormatAlignLeft,
                                contentDescription = null,
                            )
                            NoteTextAlignment.CENTER -> Icon(
                                Icons.Default.FormatAlignCenter,
                                contentDescription = null,
                            )
                            NoteTextAlignment.END -> Icon(
                                Icons.Default.FormatAlignRight,
                                contentDescription = null,
                            )
                            NoteTextAlignment.JUSTIFY -> Icon(
                                Icons.Default.FormatAlignJustify,
                                contentDescription = null,
                            )
                        }
                    },
                    trailingIcon = if (option == alignment) {
                        { Icon(Icons.Default.Check, contentDescription = "Выбрано") }
                    } else {
                        null
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
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
    val liteLife = LocalProxyVisualStyle.current.theme == AppTheme.LITE_LIFE
    Box(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(if (liteLife) 0.dp else 14.dp))
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
    val liteLife = LocalProxyVisualStyle.current.theme == AppTheme.LITE_LIFE
    Box(
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.34f }
            .clip(if (liteLife) RoundedCornerShape(0.dp) else CircleShape)
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
    val liteLife = LocalProxyVisualStyle.current.theme == AppTheme.LITE_LIFE
    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(if (liteLife) 0.dp else 18.dp))
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
    labsSettings: LabsSettings,
    onLabsSettingsChanged: (LabsSettings) -> Unit,
    motionAvailability: MotionSensorAvailability,
    motionFrame: MotionCompensationFrame,
    onDismiss: () -> Unit,
) {
    var settingsTab by remember { mutableStateOf(SettingsTab.APPEARANCE) }
    val sheetCorner = (LocalProxyShape.current.globalCornerDp + 8).coerceAtMost(32).dp
    val resolvedShape = interfaceShape.resolveFor(selectedTheme)
    val themeShapeLabel = when (selectedTheme) {
        AppTheme.LIQUID_GLASS -> "Литая оптическая кромка"
        AppTheme.ROYAL_GRAPHITE -> "Сдержанная геометрия графита"
        AppTheme.OLD_SCROLL -> "Твёрдый бумажный срез"
        AppTheme.LITE_LIFE -> "Прямоугольная статичная геометрия"
        AppTheme.CYBERPUNK -> "Асимметричные техно-срезы"
    }
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
                .fillMaxHeight(0.97f),
            shape = if (selectedTheme == AppTheme.CYBERPUNK) {
                CutCornerShape(
                    topStart = sheetCorner,
                    topEnd = 0.dp,
                    bottomEnd = 0.dp,
                    bottomStart = 0.dp,
                )
            } else {
                RoundedCornerShape(
                    topStart = sheetCorner,
                    topEnd = sheetCorner,
                )
            },
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
                                AppTheme.LITE_LIFE -> 0.98f
                                AppTheme.CYBERPUNK -> 0.94f
                            },
                        ),
                    )
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
                    .padding(top = 8.dp, bottom = 16.dp),
            ) {
                if (selectedTheme != AppTheme.LITE_LIFE) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(42.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
                            ),
                    )
                    Spacer(Modifier.height(8.dp))
                } else {
                    Spacer(Modifier.height(2.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Настройки", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = if (settingsTab == SettingsTab.APPEARANCE) {
                                "Материал и пластика интерфейса"
                            } else {
                                "Экспериментальные режимы движения"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (settingsTab == SettingsTab.APPEARANCE) {
                        IconButton(
                            onClick = { onInterfaceShapeChanged(InterfaceShape()) },
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Сбросить форму")
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть настройки")
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MotionOption(
                        label = "Оформление",
                        selected = settingsTab == SettingsTab.APPEARANCE,
                        onClick = { settingsTab = SettingsTab.APPEARANCE },
                        modifier = Modifier.weight(1f),
                    )
                    MotionOption(
                        label = "ProxyScroll Labs",
                        selected = settingsTab == SettingsTab.LABS,
                        onClick = { settingsTab = SettingsTab.LABS },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(12.dp))
                if (settingsTab == SettingsTab.LABS) {
                    LabsSettingsPanel(
                        settings = labsSettings,
                        onSettingsChanged = onLabsSettingsChanged,
                        availability = motionAvailability,
                        frame = motionFrame,
                        flat = selectedTheme == AppTheme.LITE_LIFE,
                    )
                } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppTheme.entries.forEach { theme ->
                        CompactThemeOption(
                            theme = theme,
                            title = when (theme) {
                                AppTheme.LIQUID_GLASS -> "Optical Glass"
                                AppTheme.ROYAL_GRAPHITE -> "Royal Graphite"
                                AppTheme.OLD_SCROLL -> "OldScroll"
                                AppTheme.LITE_LIFE -> "LiteLife"
                                AppTheme.CYBERPUNK -> "Cyberpunk"
                            },
                            selected = selectedTheme == theme,
                            onClick = { onThemeSelected(theme) },
                            modifier = Modifier.width(148.dp),
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = when (selectedTheme) {
                        AppTheme.LIQUID_GLASS -> "Оптика стекла"
                        AppTheme.OLD_SCROLL -> "Характер бумаги"
                        AppTheme.LITE_LIFE -> "Лёгкий интерфейс"
                        AppTheme.CYBERPUNK -> "Протокол Night Signal"
                        else -> "Цвет внутри материала"
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when (selectedTheme) {
                        AppTheme.LIQUID_GLASS ->
                            "Фон преломляется, увеличивается и смягчается внутри стекла"
                        AppTheme.ROYAL_GRAPHITE ->
                            "Graphite Oil — холодные цветные включения под мокрым камнем"
                        AppTheme.OLD_SCROLL ->
                            "Слоновая кость · старые волокна · тёплая пыль и потемневший край"
                        AppTheme.LITE_LIFE ->
                            "Тихий контраст · чистые плоскости · минимум оптической нагрузки"
                        AppTheme.CYBERPUNK ->
                            "Сигнальный жёлтый · аварийный красный · RGB-разрывы и техно-трассы"
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
                } else if (selectedTheme == AppTheme.OLD_SCROLL) {
                    OldScrollBadge()
                } else if (selectedTheme == AppTheme.CYBERPUNK) {
                    CyberpunkBadge()
                } else {
                    LiteLifeBadge()
                }
                if (selectedTheme != AppTheme.LITE_LIFE) {
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth()) {
                    Text(
                        if (selectedTheme == AppTheme.LIQUID_GLASS) {
                            "Цвет окружения"
                        } else {
                            "Интенсивность"
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )
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
                Text(
                    if (selectedTheme == AppTheme.LIQUID_GLASS) {
                        "Толщина стекла"
                    } else {
                        "Глубина материала"
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(8.dp))
                    Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MaterialDepth.entries.forEach { depth ->
                        MotionOption(
                            label = if (selectedTheme == AppTheme.LIQUID_GLASS) {
                                when (depth) {
                                    MaterialDepth.FLAT -> "Тонкое"
                                    MaterialDepth.NATURAL -> "Литое"
                                    MaterialDepth.DEEP -> "Толстое"
                                }
                            } else {
                                depth.displayName
                            },
                            selected = stainSettings.depth == depth,
                            onClick = {
                                onStainSettingsChanged(stainSettings.copy(depth = depth))
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    if (selectedTheme == AppTheme.LIQUID_GLASS) {
                        "Движение отражения"
                    } else {
                        "Дыхание света"
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
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
                Text(
                    if (selectedTheme == AppTheme.LIQUID_GLASS) {
                        "Качество оптики"
                    } else {
                        "Пластика материала"
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (selectedTheme == AppTheme.LIQUID_GLASS) {
                        when (stainSettings.motionQuality) {
                            MaterialMotionQuality.AUTO ->
                                "Аппаратное размытие включается с учётом устройства"
                            MaterialMotionQuality.FULL ->
                                "Полное преломление, мягкое размытие и живая кромка"
                            MaterialMotionQuality.LITE ->
                                "Преломление без дорогого размытия для ровной прокрутки"
                        }
                    } else {
                        when (stainSettings.motionQuality) {
                            MaterialMotionQuality.AUTO ->
                                "Авто учитывает производительность устройства"
                            MaterialMotionQuality.FULL ->
                                "Полная деформация, живые блики и мягкий оптический шлейф"
                            MaterialMotionQuality.LITE ->
                                "Меньше движения и слоёв для максимально ровного интерфейса"
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MaterialMotionQuality.entries.forEach { quality ->
                        MotionOption(
                            label = quality.displayName,
                            selected = stainSettings.motionQuality == quality,
                            onClick = {
                                onStainSettingsChanged(
                                    stainSettings.copy(motionQuality = quality),
                                )
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                } else {
                    Spacer(Modifier.height(12.dp))
                    ProxyInsetSurface(
                        modifier = Modifier.fillMaxWidth(),
                        role = ProxySurfaceRole.CARD,
                        selected = true,
                    ) {
                        Text(
                            text = "Оптика, свечение, деформация и дыхание отключены темой.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(14.dp),
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text("Форма интерфейса", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                ProxyInsetSurface(
                    modifier = Modifier.fillMaxWidth(),
                    role = ProxySurfaceRole.CARD,
                    selected = interfaceShape.customEnabled && selectedTheme != AppTheme.LITE_LIFE,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Настраивать вручную", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = if (selectedTheme == AppTheme.LITE_LIFE) {
                                    "LiteLife всегда использует углы 0 dp"
                                } else if (interfaceShape.customEnabled) {
                                    "Shape Studio управляет всеми углами"
                                } else {
                                    "$themeShapeLabel · тема управляет формой"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = interfaceShape.customEnabled &&
                                selectedTheme != AppTheme.LITE_LIFE,
                            enabled = selectedTheme != AppTheme.LITE_LIFE,
                            onCheckedChange = {
                                onInterfaceShapeChanged(interfaceShape.copy(customEnabled = it))
                            },
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                ShapeLivePreview(resolvedShape)
                AnimatedVisibility(
                    visible = interfaceShape.customEnabled &&
                        selectedTheme != AppTheme.LITE_LIFE,
                    enter = fadeIn(tween(220)) + slideInVertically(tween(300)) { -it / 5 },
                    exit = fadeOut(tween(160)) + slideOutVertically(tween(220)) { -it / 5 },
                ) {
                    Column {
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
                                onInterfaceShapeChanged(
                                    interfaceShape.withGlobalCorner(it.roundToInt()),
                                )
                            },
                            valueRange = MIN_INTERFACE_CORNER_DP.toFloat()..
                                MAX_INTERFACE_CORNER_DP.toFloat(),
                            steps = MAX_INTERFACE_CORNER_DP - MIN_INTERFACE_CORNER_DP - 1,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(Modifier.fillMaxWidth()) {
                            Text(
                                "Твёрдо",
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
                            listOf("Строго" to 4, "Баланс" to 14, "Мягко" to 24)
                                .forEach { (label, value) ->
                                    ShapePreset(
                                        label = label,
                                        value = value,
                                        selected = interfaceShape.globalCornerDp == value,
                                        onClick = {
                                            onInterfaceShapeChanged(
                                                interfaceShape.withGlobalCorner(value),
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
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
                                    Text(
                                        "Связать все элементы",
                                        style = MaterialTheme.typography.titleMedium,
                                    )
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
                            enter = fadeIn(tween(220)) +
                                slideInVertically(tween(260)) { -it / 4 },
                            exit = fadeOut(tween(160)) +
                                slideOutVertically(tween(200)) { -it / 4 },
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
                                                onInterfaceShapeChanged(
                                                    interfaceShape.withCardCorner(it),
                                                )
                                            },
                                        )
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outline
                                                .copy(alpha = 0.14f),
                                        )
                                        CornerControlRow(
                                            label = "Поля ввода",
                                            value = interfaceShape.inputCornerDp,
                                            onValueChange = {
                                                onInterfaceShapeChanged(
                                                    interfaceShape.withInputCorner(it),
                                                )
                                            },
                                        )
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outline
                                                .copy(alpha = 0.14f),
                                        )
                                        CornerControlRow(
                                            label = "Кнопки",
                                            value = interfaceShape.buttonCornerDp,
                                            onValueChange = {
                                                onInterfaceShapeChanged(
                                                    interfaceShape.withButtonCorner(it),
                                                )
                                            },
                                        )
                                    }
                                }
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
private fun LabsSettingsPanel(
    settings: LabsSettings,
    onSettingsChanged: (LabsSettings) -> Unit,
    availability: MotionSensorAvailability,
    frame: MotionCompensationFrame,
    flat: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Motion Comfort", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Телефон реагирует на физическое движение",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ProxyInsetSurface(
            role = ProxySurfaceRole.BUTTON,
            selected = true,
        ) {
            Text(
                text = "LABS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }
    Spacer(Modifier.height(14.dp))
    MotionLabToggle(
        title = "Micro Stabilization",
        description = if (availability.rotation) {
            "Компенсирует только мелкую угловую тряску в списке. Отключается при прокрутке и редактировании."
        } else {
            "На этом устройстве не найден датчик вращения."
        },
        checked = settings.microStabilizationEnabled && availability.rotation,
        enabled = availability.rotation,
        onCheckedChange = {
            onSettingsChanged(settings.copy(microStabilizationEnabled = it))
        },
    )
    Spacer(Modifier.height(10.dp))
    MotionLabToggle(
        title = "Travel Cues",
        description = if (availability.acceleration) {
            "Периферийные маркеры показывают направление ускорения, не двигая текст и элементы управления."
        } else {
            "На этом устройстве не найден датчик ускорения."
        },
        checked = settings.travelCuesEnabled && availability.acceleration,
        enabled = availability.acceleration,
        onCheckedChange = {
            onSettingsChanged(settings.copy(travelCuesEnabled = it))
        },
    )

    AnimatedVisibility(
        visible = settings.sensorsEnabled,
        enter = fadeIn(tween(180)) + slideInVertically(tween(240)) { -it / 5 },
        exit = fadeOut(tween(140)) + slideOutVertically(tween(180)) { -it / 5 },
    ) {
        Column {
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth()) {
                Text("Сила компенсации", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${(settings.motionStrength * 100).roundToInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Slider(
                value = settings.motionStrength,
                onValueChange = {
                    onSettingsChanged(settings.copy(motionStrength = it).normalized())
                },
                valueRange = MIN_LABS_MOTION_STRENGTH..MAX_LABS_MOTION_STRENGTH,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(Modifier.fillMaxWidth()) {
                Text(
                    "Тише",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "Сильнее",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(14.dp))
            Text("Живой тест датчиков", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            ProxyInsetSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(154.dp),
                role = ProxySurfaceRole.INPUT,
                selected = true,
            ) {
                Box(Modifier.fillMaxSize()) {
                    TravelMotionCues(
                        frame = frame,
                        settings = settings,
                        flat = flat,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .graphicsLayer {
                                if (settings.microStabilizationEnabled) {
                                    translationX = frame.shakeX * 5.dp.toPx() *
                                        settings.motionStrength
                                    translationY = frame.shakeY * 5.dp.toPx() *
                                        settings.motionStrength
                                }
                            }
                            .padding(horizontal = 46.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Двигайте телефон",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Текст компенсирует мелкую тряску, точки показывают ускорение",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    ProxyInsetSurface(
        modifier = Modifier.fillMaxWidth(),
        role = ProxySurfaceRole.CARD,
    ) {
        Text(
            text = "Экспериментальная функция: она может улучшить читаемость в движении, но не является медицинским средством от укачивания. Если становится некомфортно — отключите режим.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(14.dp),
        )
    }
}

@Composable
private fun MotionLabToggle(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ProxyInsetSurface(
        modifier = Modifier.fillMaxWidth(),
        role = ProxySurfaceRole.CARD,
        selected = checked,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = onCheckedChange,
            )
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
private fun CyberpunkBadge() {
    ProxyInsetSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
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
                theme = AppTheme.CYBERPUNK,
                modifier = Modifier.size(44.dp),
            )
            Column(Modifier.weight(1f)) {
                Text("NIGHT//SIGNAL", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Жёлтый сигнал · красная тревога · RGB glitch",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Активный киберпанк-протокол",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun LiteLifeBadge() {
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
                theme = AppTheme.LITE_LIFE,
                modifier = Modifier.size(42.dp),
            )
            Column(Modifier.weight(1f)) {
                Text("LiteLife", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Плоско · прямоугольно · полностью статично",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Активная тема",
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
            .height(58.dp)
            .animatedClick(onClick = onClick, pressedScale = 0.96f),
        role = ProxySurfaceRole.CARD,
        selected = selected,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            ThemeSwatch(theme, modifier = Modifier.size(32.dp))
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
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun ShapeLivePreview(shapeSettings: InterfaceShape) {
    val liteLife = LocalProxyVisualStyle.current.theme == AppTheme.LITE_LIFE
    val cornerDuration = if (liteLife) 1 else 220
    val cardCorner by animateDpAsState(
        targetValue = shapeSettings.resolvedCardCornerDp.dp,
        animationSpec = tween(cornerDuration),
        label = "preview-card-corner",
    )
    val inputCorner by animateDpAsState(
        targetValue = shapeSettings.resolvedInputCornerDp.dp,
        animationSpec = tween(cornerDuration),
        label = "preview-input-corner",
    )
    val buttonCorner by animateDpAsState(
        targetValue = shapeSettings.resolvedButtonCornerDp.dp,
        animationSpec = tween(cornerDuration),
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
                text = if (liteLife) "STATIC INTERFACE PREVIEW" else "LIVE MATERIAL PREVIEW",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (liteLife) {
                    "Плоские поверхности без света, деформации и движения"
                } else {
                    "Нажмите и удерживайте: материал сожмётся и станет прозрачнее"
                },
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
                            text = if (liteLife) {
                                "Строгая форма 0 dp."
                            } else {
                                "Форма и свет меняются мгновенно."
                            },
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
    val shape = when (theme) {
        AppTheme.OLD_SCROLL -> RoundedCornerShape(6.dp)
        AppTheme.LITE_LIFE -> RoundedCornerShape(0.dp)
        AppTheme.CYBERPUNK -> CutCornerShape(
            topStart = 0.dp,
            topEnd = 11.dp,
            bottomEnd = 0.dp,
            bottomStart = 7.dp,
        )
        else -> RoundedCornerShape(18.dp)
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
            AppTheme.LITE_LIFE -> {
                drawRect(color = Color(0xFF17191F))
                drawRect(
                    color = Color(0xFF22262E),
                    topLeft = Offset(size.width * 0.10f, size.height * 0.18f),
                    size = Size(size.width * 0.80f, size.height * 0.64f),
                )
                drawRect(
                    color = Color(0xFF3B8CFF),
                    topLeft = Offset(size.width * 0.10f, size.height * 0.18f),
                    size = Size(size.width * 0.08f, size.height * 0.64f),
                )
            }
            AppTheme.CYBERPUNK -> {
                drawRect(color = Color(0xFF08090A))
                val signal = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width * 0.72f, 0f)
                    lineTo(size.width * 0.58f, size.height * 0.27f)
                    lineTo(size.width * 0.24f, size.height * 0.27f)
                    lineTo(size.width * 0.18f, size.height * 0.72f)
                    lineTo(0f, size.height * 0.86f)
                    close()
                }
                drawPath(path = signal, color = Color(0xFFF4E900))
                drawRect(
                    color = Color(0xFFFF3B30),
                    topLeft = Offset(size.width * 0.34f, size.height * 0.48f),
                    size = Size(size.width * 0.58f, size.height * 0.075f),
                )
                drawRect(
                    color = Color(0xFF00E7FF),
                    topLeft = Offset(size.width * 0.44f, size.height * 0.60f),
                    size = Size(size.width * 0.42f, size.height * 0.045f),
                )
                repeat(3) { index ->
                    val x = size.width * (0.55f + index * 0.12f)
                    drawLine(
                        color = Color(0xFFF4E900).copy(alpha = 0.72f),
                        start = Offset(x, size.height * 0.72f),
                        end = Offset(x, size.height * 0.94f),
                        strokeWidth = 1.2f,
                    )
                }
                drawCircle(
                    color = Color(0xFFFF3B30),
                    radius = size.width * 0.045f,
                    center = Offset(size.width * 0.83f, size.height * 0.83f),
                )
            }
        }
    }
}
