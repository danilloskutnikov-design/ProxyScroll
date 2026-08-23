package com.proxyscroll.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proxyscroll.app.data.PreferencesNotesRepository
import com.proxyscroll.app.data.ThemePreferences
import com.proxyscroll.app.ui.NotesViewModel
import com.proxyscroll.app.ui.ProxyScrollApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferences = getSharedPreferences("proxyscroll_notes", MODE_PRIVATE)
        val repository = PreferencesNotesRepository(preferences)
        val themePreferences = ThemePreferences(
            getSharedPreferences("proxyscroll_settings", MODE_PRIVATE),
        )

        setContent {
            var selectedTheme by remember {
                mutableStateOf(themePreferences.getTheme())
            }
            val notesViewModel: NotesViewModel = viewModel(
                factory = NotesViewModel.Factory(repository),
            )
            ProxyScrollApp(
                viewModel = notesViewModel,
                selectedTheme = selectedTheme,
                onThemeSelected = { theme ->
                    selectedTheme = theme
                    themePreferences.setTheme(theme)
                },
            )
        }
    }
}
