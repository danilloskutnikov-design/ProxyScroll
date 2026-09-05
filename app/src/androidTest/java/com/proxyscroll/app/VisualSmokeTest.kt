package com.proxyscroll.app

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.SystemClock
import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.proxyscroll.app.data.PreferencesNotesRepository
import com.proxyscroll.app.domain.Note
import com.proxyscroll.app.domain.NoteColorFlag
import com.proxyscroll.app.ui.theme.createGlassEffect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Outline
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Device checks target native shader support, usable navigation, persistence and
 * PDF integration. Screenshots are retained for visual review, not golden tests. */
@RunWith(AndroidJUnit4::class)
class VisualSmokeTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val device = UiDevice.getInstance(instrumentation)

    private fun element(selector: BySelector): UiObject2 =
        requireNotNull(device.wait(Until.findObject(selector), 15_000)) { "Missing $selector" }

    private fun screenshot(name: String) {
        SystemClock.sleep(900)
        val folder = File(context.getExternalFilesDir(null), "qa").apply { mkdirs() }
        assertTrue(device.takeScreenshot(File(folder, "$name.png")))
        device.dumpWindowHierarchy(File(folder, "$name.xml"))
    }

    @Test
    fun opticalMaterialAndEverydayFlows() {
        if (Build.VERSION.SDK_INT >= 33) {
            assertNotNull(createGlassEffect(Size(600f, 200f),
                Outline.Rounded(RoundRect(0f, 0f, 600f, 200f, CornerRadius(48f))), 2f, 1f, 5f))
        }
        val notes = context.getSharedPreferences("proxyscroll_notes", Context.MODE_PRIVATE)
        notes.edit().clear().commit()
        context.getSharedPreferences("proxyscroll_settings", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("proxyscroll_library", Context.MODE_PRIVATE).edit().clear().commit()
        val repository = PreferencesNotesRepository(notes)
        val titles = listOf("Осязаемый интерфейс", "Мысли на полях", "Свет и пространство", "Заметить главное",
            "Ритм повседневности", "Идеи для следующей главы", "Тихое утро", "Новая перспектива")
        titles.forEachIndexed { i, title ->
            repository.upsert(Note("qa-$i", title,
                "Материал начинается с ощущения. Мягкий свет, ясный текст и внимание к каждой детали помогают удержать мысль.",
                isPinned = i == 0, colorFlag = if (i % 2 == 0) NoteColorFlag.SKY else NoteColorFlag.VIOLET,
                createdAt = 1788650400000L, updatedAt = 1788650400000L - i * 3600000L, index = i + 1L))
        }
        context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        element(By.desc("Создать заметку"))
        screenshot("01-notes")
        val add = element(By.desc("Создать заметку")).visibleBounds
        assertTrue("Create action is clipped", add.width() >= 80 && add.height() >= 80)
        device.swipe(device.displayWidth / 2, device.displayHeight * 3 / 4,
            device.displayWidth / 2, device.displayHeight / 3, 24)
        screenshot("02-scrolled-glass")
        element(By.text("Настройки")).click()
        element(By.desc("Закрыть настройки"))
        screenshot("03-settings")
        element(By.desc("Закрыть настройки")).click()
        element(By.text("Библиотека")).click()
        element(By.desc("Импортировать PDF"))
        screenshot("04-library-empty")
        element(By.text("Заметки")).click()
        element(By.desc("Создать заметку")).click()
        element(By.desc("Готово"))
        val fields = device.wait(Until.findObjects(By.clazz("android.widget.EditText")), 10_000)
        assertTrue("Editor has no input", !fields.isNullOrEmpty())
        fields!!.last().text = "Alpha 42 сохраняет мои заметки"
        screenshot("05-editor")
        element(By.desc("Готово")).click()
        element(By.desc("К списку заметок"))
        assertTrue(repository.getAll().any { it.body.contains("Alpha 42 сохраняет мои заметки") })
        screenshot("06-note-reader")
        element(By.desc("К списку заметок")).click()

        val pdfFile = File(context.filesDir, "imported_pdfs/Glass-and-light.pdf")
        pdfFile.parentFile!!.mkdirs()
        PdfDocument().use { pdf ->
            repeat(3) { index ->
                val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, index + 1).create())
                page.canvas.drawColor(android.graphics.Color.rgb(248, 244, 233))
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(37, 49, 68); textSize = 28f }
                page.canvas.drawText("MATERIAL & LIGHT", 48f, 100f, paint)
                paint.textSize = 16f
                repeat(20) { row -> page.canvas.drawText("A quiet place for reading, thinking and noticing.", 48f, 160f + row * 26, paint) }
                pdf.finishPage(page)
            }
            pdfFile.outputStream().use { pdf.writeTo(it) }
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        context.startActivity(Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        })
        element(By.desc("В библиотеку"))
        screenshot("07-pdf-reader")
        element(By.desc("В библиотеку")).click()
        element(By.desc("Импортировать PDF"))
        screenshot("08-library")
        element(By.text("Настройки")).click()
        val themeTile = element(By.text("Living Glass")).visibleBounds
        device.swipe(device.displayWidth - 30, themeTile.centerY(), 30, themeTile.centerY(), 30)
        element(By.text("LiteLife")).click()
        element(By.desc("Закрыть настройки")).click()
        screenshot("09-lite-fallback")
    }
}
