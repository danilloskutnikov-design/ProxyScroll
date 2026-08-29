package com.proxyscroll.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proxyscroll.app.data.PreferencesDocumentLibraryRepository
import com.proxyscroll.app.data.PreferencesNotesRepository
import com.proxyscroll.app.data.ThemePreferences
import com.proxyscroll.app.ui.LibraryViewModel
import com.proxyscroll.app.ui.NotesViewModel
import com.proxyscroll.app.ui.ProxyScrollApp
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class IncomingPdfRequest(
    val uri: String,
    val title: String,
)

class MainActivity : ComponentActivity() {
    private val incomingPdfState = mutableStateOf<IncomingPdfRequest?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferences = getSharedPreferences("proxyscroll_notes", MODE_PRIVATE)
        val repository = PreferencesNotesRepository(preferences)
        val libraryRepository = PreferencesDocumentLibraryRepository(
            getSharedPreferences("proxyscroll_library", MODE_PRIVATE),
        )
        val themePreferences = ThemePreferences(
            getSharedPreferences("proxyscroll_settings", MODE_PRIVATE),
        )

        captureIncomingPdf(intent)

        setContent {
            var selectedTheme by remember {
                mutableStateOf(themePreferences.getTheme())
            }
            var inputMotion by remember {
                mutableStateOf(themePreferences.getInputMotion())
            }
            var interfaceShape by remember {
                mutableStateOf(themePreferences.getInterfaceShape())
            }
            var stainSettings by remember {
                mutableStateOf(themePreferences.getStainSettings())
            }
            var labsSettings by remember {
                mutableStateOf(themePreferences.getLabsSettings())
            }
            var readingSettings by remember {
                mutableStateOf(themePreferences.getReadingSettings())
            }
            var activeGroupFilter by remember {
                mutableStateOf(themePreferences.getActiveGroupFilter())
            }
            LaunchedEffect(stainSettings) {
                delay(320)
                themePreferences.setStainSettings(stainSettings)
            }
            LaunchedEffect(labsSettings) {
                delay(220)
                themePreferences.setLabsSettings(labsSettings)
            }
            LaunchedEffect(readingSettings) {
                delay(220)
                themePreferences.setReadingSettings(readingSettings)
            }
            val notesViewModel: NotesViewModel = viewModel(
                factory = NotesViewModel.Factory(repository),
            )
            val libraryViewModel: LibraryViewModel = viewModel(
                factory = LibraryViewModel.Factory(libraryRepository),
            )
            val incomingPdf = incomingPdfState.value
            ProxyScrollApp(
                viewModel = notesViewModel,
                libraryViewModel = libraryViewModel,
                selectedTheme = selectedTheme,
                onThemeSelected = { theme ->
                    selectedTheme = theme
                    themePreferences.setTheme(theme)
                },
                inputMotion = inputMotion,
                onInputMotionSelected = { motion ->
                    inputMotion = motion
                    themePreferences.setInputMotion(motion)
                },
                interfaceShape = interfaceShape,
                onInterfaceShapeChanged = { shape ->
                    interfaceShape = shape
                    themePreferences.setInterfaceShape(shape)
                },
                stainSettings = stainSettings,
                onStainSettingsChanged = { settings ->
                    stainSettings = settings
                },
                labsSettings = labsSettings,
                onLabsSettingsChanged = { settings ->
                    labsSettings = settings.normalized()
                },
                readingSettings = readingSettings,
                onReadingSettingsChanged = { settings ->
                    readingSettings = settings.normalized()
                },
                activeGroupFilter = activeGroupFilter,
                onActiveGroupFilterChanged = { groupId ->
                    activeGroupFilter = groupId
                    themePreferences.setActiveGroupFilter(groupId)
                },
                incomingPdfUri = incomingPdf?.uri,
                incomingPdfTitle = incomingPdf?.title,
                onIncomingPdfConsumed = { incomingPdfState.value = null },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        captureIncomingPdf(intent)
    }

    private fun captureIncomingPdf(intent: Intent?) {
        val requestIntent = intent ?: return
        val incomingUri = when (requestIntent.action) {
            Intent.ACTION_VIEW -> requestIntent.data
            Intent.ACTION_SEND -> {
                @Suppress("DEPRECATION")
                requestIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            }
            else -> null
        } ?: return

        val mime = requestIntent.type
            ?: runCatching { contentResolver.getType(incomingUri) }.getOrNull()
        val looksLikePdf = mime.equals("application/pdf", ignoreCase = true) ||
            mime.equals("application/x-pdf", ignoreCase = true) ||
            incomingUri.toString().substringBefore('?').endsWith(".pdf", ignoreCase = true)
        if (!looksLikePdf) return

        lifecycleScope.launch {
            val request = withContext(Dispatchers.IO) {
                prepareIncomingPdf(incomingUri, requestIntent.flags)
            }
            if (request != null) incomingPdfState.value = request
        }
    }

    private fun prepareIncomingPdf(uri: Uri, intentFlags: Int): IncomingPdfRequest? {
        val displayName = queryDisplayName(uri)
            .takeIf { it.isNotBlank() }
            ?: "PDF-документ.pdf"

        // A PDF opened from another app may only have a temporary URI permission.
        // Copy it into app-private storage so a library entry still opens after a
        // reboot or after the source app revokes its grant.
        val copied = runCatching {
            val directory = File(filesDir, "imported_pdfs").apply { mkdirs() }
            val safeName = displayName
                .replace(Regex("[^A-Za-zА-Яа-я0-9._ -]"), "_")
                .trim()
                .take(96)
                .ifBlank { "document.pdf" }
                .let { if (it.endsWith(".pdf", ignoreCase = true)) it else "$it.pdf" }
            val prefix = Integer.toHexString(uri.toString().hashCode())
            val destination = File(directory, "${prefix}_$safeName")
            contentResolver.openInputStream(uri)?.use { input ->
                destination.outputStream().use { output -> input.copyTo(output) }
            } ?: error("Не удалось прочитать PDF")
            check(destination.length() > 0L) { "PDF пуст" }
            FileProvider.getUriForFile(
                this,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                destination,
            )
        }.getOrNull()

        val stableUri = copied ?: run {
            if (intentFlags and Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION != 0) {
                runCatching {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
            }
            uri
        }

        return IncomingPdfRequest(
            uri = stableUri.toString(),
            title = displayName.removeSuffix(".pdf").removeSuffix(".PDF").ifBlank { "PDF-документ" },
        )
    }

    private fun queryDisplayName(uri: Uri): String = runCatching {
        contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0).orEmpty() else ""
        }.orEmpty()
    }.getOrDefault("")
}
