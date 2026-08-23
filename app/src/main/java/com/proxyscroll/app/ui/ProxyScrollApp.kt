package com.proxyscroll.app.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.proxyscroll.app.R
import com.proxyscroll.app.domain.AppTheme
import com.proxyscroll.app.domain.Note
import com.proxyscroll.app.ui.theme.LocalProxyVisualStyle
import com.proxyscroll.app.ui.theme.ProxyScrollTheme
import com.proxyscroll.app.ui.theme.ProxySurface
import com.proxyscroll.app.ui.theme.ProxyThemeBackground
import java.text.DateFormat
import java.util.Date

@Composable
fun ProxyScrollApp(
    viewModel: NotesViewModel,
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var noteBeingEdited by remember { mutableStateOf<Note?>(null) }
    var isCreating by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    ProxyScrollTheme(selectedTheme = selectedTheme) {
        Box(Modifier.fillMaxSize()) {
            ProxyThemeBackground(
                selectedTheme = selectedTheme,
                modifier = Modifier.fillMaxSize(),
            )
            NotesScreen(
                state = state,
                onQueryChange = viewModel::setQuery,
                onCreate = { isCreating = true },
                onEdit = { noteBeingEdited = it },
                onTogglePinned = viewModel::togglePinned,
                onOpenSettings = { showSettings = true },
            )

            if (isCreating || noteBeingEdited != null) {
                NoteEditorSheet(
                    note = noteBeingEdited,
                    onDismiss = {
                        isCreating = false
                        noteBeingEdited = null
                    },
                    onSave = { title, body ->
                        viewModel.save(noteBeingEdited, title, body)
                        isCreating = false
                        noteBeingEdited = null
                    },
                    onDelete = noteBeingEdited?.let { note ->
                        {
                            viewModel.delete(note)
                            noteBeingEdited = null
                        }
                    },
                )
            }

            if (showSettings) {
                SettingsSheet(
                    selectedTheme = selectedTheme,
                    onThemeSelected = onThemeSelected,
                    onDismiss = { showSettings = false },
                )
            }
        }
    }
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
                            .clickable(onClick = onOpenSettings),
                        shape = CircleShape,
                        strong = true,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
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
                    .size(62.dp)
                    .clickable(onClick = onCreate),
                shape = RoundedCornerShape(24.dp),
                strong = true,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Новая заметка",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(29.dp),
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
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = state.notes.size.toString() + " в текущем списке",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            ProxySurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
            ) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp),
                    label = { Text("Поиск по заметкам") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Очистить поиск",
                                )
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
                    items(
                        items = state.notes,
                        key = { it.id },
                    ) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onEdit(note) },
                            onTogglePinned = { onTogglePinned(note) },
                        )
                    }
                    item {
                        Spacer(Modifier.height(96.dp))
                    }
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
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        ProxySurface(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (isSearching) "Ничего не найдено" else "Заметок пока нет",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isSearching) {
                        "Попробуйте изменить поисковый запрос"
                    } else {
                        "Нажмите +, чтобы создать первую заметку"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
            .clickable(onClick = onClick)
            .animateContentSize(),
        strong = note.isPinned,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = note.title.ifBlank { "Без названия" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onTogglePinned,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = if (note.isPinned) {
                            "Открепить заметку"
                        } else {
                            "Закрепить заметку"
                        },
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
                    text = note.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(14.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onDismiss: () -> Unit,
) {
    val style = LocalProxyVisualStyle.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        scrimColor = style.scrim,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = "Настройки",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Внешний вид ProxyScroll",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            ThemeOption(
                theme = AppTheme.LIQUID_GLASS,
                title = "Liquid Glass",
                description = "Свет, глубина и прозрачные живые поверхности",
                isSelected = selectedTheme == AppTheme.LIQUID_GLASS,
                onClick = { onThemeSelected(AppTheme.LIQUID_GLASS) },
            )
            Spacer(Modifier.height(12.dp))
            ThemeOption(
                theme = AppTheme.ROYAL_GRAPHITE,
                title = "Royal Graphite",
                description = "Холодный графит, уголь и северный дождь",
                isSelected = selectedTheme == AppTheme.ROYAL_GRAPHITE,
                onClick = { onThemeSelected(AppTheme.ROYAL_GRAPHITE) },
            )
            Spacer(Modifier.height(22.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
            Spacer(Modifier.height(14.dp))
            Text(
                text = "ProxyScroll 0.2.0-alpha02",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
            .clickable(onClick = onClick),
        strong = isSelected,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ThemeSwatch(theme = theme)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Выбрано",
                    tint = MaterialTheme.colorScheme.primary,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.58f),
                            shape = CircleShape,
                        ),
                )
            }
        }
    }
}

@Composable
private fun ThemeSwatch(theme: AppTheme) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .size(66.dp)
            .clip(shape),
    ) {
        when (theme) {
            AppTheme.LIQUID_GLASS -> {
                Canvas(Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFFDCE6FF), Color(0xFFBCECF0)),
                        ),
                    )
                    drawCircle(
                        color = Color(0xFF7D72EE).copy(alpha = 0.52f),
                        radius = size.width * 0.46f,
                        center = Offset(size.width * 0.20f, size.height * 0.20f),
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.58f),
                        radius = size.width * 0.34f,
                        center = Offset(size.width * 0.67f, size.height * 0.64f),
                    )
                }
            }

            AppTheme.ROYAL_GRAPHITE -> {
                Image(
                    painter = painterResource(R.drawable.royal_graphite),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF071015).copy(alpha = 0.34f)),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.36f),
                    shape = shape,
                ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteEditorSheet(
    note: Note?,
    onDismiss: () -> Unit,
    onSave: (title: String, body: String) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var title by remember(note?.id) { mutableStateOf(note?.title.orEmpty()) }
    var body by remember(note?.id) { mutableStateOf(note?.body.orEmpty()) }
    val canSave = title.isNotBlank() || body.isNotBlank()
    val style = LocalProxyVisualStyle.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        scrimColor = style.scrim,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
        ) {
            Text(
                text = if (note == null) "Новая заметка" else "Редактирование",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(18.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                maxLines = 3,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Текст заметки") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .animateContentSize(),
                shape = RoundedCornerShape(18.dp),
            )
            Spacer(Modifier.height(18.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.size(6.dp))
                        Text("Удалить")
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
                Button(
                    onClick = { onSave(title, body) },
                    enabled = canSave,
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}
