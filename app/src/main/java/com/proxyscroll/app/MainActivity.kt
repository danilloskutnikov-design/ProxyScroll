package com.proxyscroll.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proxyscroll.app.data.PreferencesNotesRepository
import com.proxyscroll.app.ui.NotesViewModel
import com.proxyscroll.app.ui.ProxyScrollApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferences = getSharedPreferences("proxyscroll_notes", MODE_PRIVATE)
        val repository = PreferencesNotesRepository(preferences)

        setContent {
            val notesViewModel: NotesViewModel = viewModel(
                factory = NotesViewModel.Factory(repository),
            )
            ProxyScrollApp(viewModel = notesViewModel)
        }
    }
}
