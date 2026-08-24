package com.proxyscroll.app.data

import android.content.SharedPreferences
import com.proxyscroll.app.domain.AppTheme
import com.proxyscroll.app.domain.InputMotion

class ThemePreferences(
    private val preferences: SharedPreferences,
) {
    fun getTheme(): AppTheme {
        return AppTheme.fromStorage(preferences.getString(KEY_THEME, null))
    }

    fun setTheme(theme: AppTheme) {
        preferences.edit()
            .putString(KEY_THEME, theme.storageKey)
            .apply()
    }

    fun getInputMotion(): InputMotion {
        return InputMotion.fromStorage(preferences.getString(KEY_INPUT_MOTION, null))
    }

    fun setInputMotion(inputMotion: InputMotion) {
        preferences.edit()
            .putString(KEY_INPUT_MOTION, inputMotion.storageKey)
            .apply()
    }

    private companion object {
        const val KEY_THEME = "selected_theme"
        const val KEY_INPUT_MOTION = "input_motion"
    }
}
