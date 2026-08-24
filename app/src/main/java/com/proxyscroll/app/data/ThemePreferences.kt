package com.proxyscroll.app.data

import android.content.SharedPreferences
import com.proxyscroll.app.domain.AppTheme
import com.proxyscroll.app.domain.InputMotion
import com.proxyscroll.app.domain.InterfaceShape

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

    fun getInterfaceShape(): InterfaceShape {
        val defaults = InterfaceShape()
        return InterfaceShape(
            globalCornerDp = preferences.getInt(KEY_GLOBAL_CORNER, defaults.globalCornerDp),
            cardCornerDp = preferences.getInt(KEY_CARD_CORNER, defaults.cardCornerDp),
            inputCornerDp = preferences.getInt(KEY_INPUT_CORNER, defaults.inputCornerDp),
            buttonCornerDp = preferences.getInt(KEY_BUTTON_CORNER, defaults.buttonCornerDp),
            linked = preferences.getBoolean(KEY_CORNERS_LINKED, defaults.linked),
        )
    }

    fun setInterfaceShape(shape: InterfaceShape) {
        preferences.edit()
            .putInt(KEY_GLOBAL_CORNER, shape.globalCornerDp)
            .putInt(KEY_CARD_CORNER, shape.cardCornerDp)
            .putInt(KEY_INPUT_CORNER, shape.inputCornerDp)
            .putInt(KEY_BUTTON_CORNER, shape.buttonCornerDp)
            .putBoolean(KEY_CORNERS_LINKED, shape.linked)
            .apply()
    }

    private companion object {
        const val KEY_THEME = "selected_theme"
        const val KEY_INPUT_MOTION = "input_motion"
        const val KEY_GLOBAL_CORNER = "global_corner_dp"
        const val KEY_CARD_CORNER = "card_corner_dp"
        const val KEY_INPUT_CORNER = "input_corner_dp"
        const val KEY_BUTTON_CORNER = "button_corner_dp"
        const val KEY_CORNERS_LINKED = "corners_linked"
    }
}
