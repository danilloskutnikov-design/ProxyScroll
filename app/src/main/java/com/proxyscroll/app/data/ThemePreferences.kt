package com.proxyscroll.app.data

import android.content.SharedPreferences
import com.proxyscroll.app.domain.AppTheme

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

    private companion object {
        const val KEY_THEME = "selected_theme"
    }
}
