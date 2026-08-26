package com.proxyscroll.app.data

import android.content.SharedPreferences
import com.proxyscroll.app.domain.AppTheme
import com.proxyscroll.app.domain.InputMotion
import com.proxyscroll.app.domain.InterfaceShape
import com.proxyscroll.app.domain.LabsSettings
import com.proxyscroll.app.domain.MaterialDepth
import com.proxyscroll.app.domain.MaterialMotionQuality
import com.proxyscroll.app.domain.ReadingSettings
import com.proxyscroll.app.domain.StainMotion
import com.proxyscroll.app.domain.StainPalette
import com.proxyscroll.app.domain.StainSettings

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
            customEnabled = preferences.getBoolean(KEY_CUSTOM_SHAPE_ENABLED, false),
        )
    }

    fun setInterfaceShape(shape: InterfaceShape) {
        preferences.edit()
            .putInt(KEY_GLOBAL_CORNER, shape.globalCornerDp)
            .putInt(KEY_CARD_CORNER, shape.cardCornerDp)
            .putInt(KEY_INPUT_CORNER, shape.inputCornerDp)
            .putInt(KEY_BUTTON_CORNER, shape.buttonCornerDp)
            .putBoolean(KEY_CORNERS_LINKED, shape.linked)
            .putBoolean(KEY_CUSTOM_SHAPE_ENABLED, shape.customEnabled)
            .apply()
    }

    fun getStainSettings(): StainSettings {
        val defaults = StainSettings()
        return StainSettings(
            palette = StainPalette.fromStorage(preferences.getString(KEY_STAIN_PALETTE, null)),
            intensity = preferences.getFloat(KEY_STAIN_INTENSITY, defaults.intensity),
            depth = MaterialDepth.fromStorage(preferences.getString(KEY_MATERIAL_DEPTH, null)),
            motion = StainMotion.fromStorage(preferences.getString(KEY_STAIN_MOTION, null)),
            motionQuality = MaterialMotionQuality.fromStorage(
                preferences.getString(KEY_MATERIAL_MOTION_QUALITY, null),
            ),
        ).normalized()
    }

    fun setStainSettings(settings: StainSettings) {
        val normalized = settings.normalized()
        preferences.edit()
            .putString(KEY_STAIN_PALETTE, normalized.palette.storageKey)
            .putFloat(KEY_STAIN_INTENSITY, normalized.intensity)
            .putString(KEY_MATERIAL_DEPTH, normalized.depth.storageKey)
            .putString(KEY_STAIN_MOTION, normalized.motion.storageKey)
            .putString(KEY_MATERIAL_MOTION_QUALITY, normalized.motionQuality.storageKey)
            .apply()
    }

    fun getLabsSettings(): LabsSettings {
        val defaults = LabsSettings()
        return LabsSettings(
            microStabilizationEnabled = preferences.getBoolean(
                KEY_LABS_MICRO_STABILIZATION,
                defaults.microStabilizationEnabled,
            ),
            travelCuesEnabled = preferences.getBoolean(
                KEY_LABS_TRAVEL_CUES,
                defaults.travelCuesEnabled,
            ),
            motionStrength = preferences.getFloat(
                KEY_LABS_MOTION_STRENGTH,
                defaults.motionStrength,
            ),
        ).normalized()
    }

    fun setLabsSettings(settings: LabsSettings) {
        val normalized = settings.normalized()
        preferences.edit()
            .putBoolean(
                KEY_LABS_MICRO_STABILIZATION,
                normalized.microStabilizationEnabled,
            )
            .putBoolean(KEY_LABS_TRAVEL_CUES, normalized.travelCuesEnabled)
            .putFloat(KEY_LABS_MOTION_STRENGTH, normalized.motionStrength)
            .apply()
    }

    fun getReadingSettings(): ReadingSettings {
        val defaults = ReadingSettings()
        return ReadingSettings(
            fontScale = preferences.getFloat(KEY_READING_FONT_SCALE, defaults.fontScale),
            lineHeight = preferences.getFloat(KEY_READING_LINE_HEIGHT, defaults.lineHeight),
            pageMarginDp = preferences.getInt(KEY_READING_PAGE_MARGIN, defaults.pageMarginDp),
        ).normalized()
    }

    fun setReadingSettings(settings: ReadingSettings) {
        val normalized = settings.normalized()
        preferences.edit()
            .putFloat(KEY_READING_FONT_SCALE, normalized.fontScale)
            .putFloat(KEY_READING_LINE_HEIGHT, normalized.lineHeight)
            .putInt(KEY_READING_PAGE_MARGIN, normalized.pageMarginDp)
            .apply()
    }

    fun getActiveGroupFilter(): String? {
        return preferences.getString(KEY_ACTIVE_GROUP_FILTER, null)
    }

    fun setActiveGroupFilter(groupId: String?) {
        preferences.edit()
            .apply {
                if (groupId == null) remove(KEY_ACTIVE_GROUP_FILTER)
                else putString(KEY_ACTIVE_GROUP_FILTER, groupId)
            }
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
        const val KEY_CUSTOM_SHAPE_ENABLED = "custom_shape_enabled"
        const val KEY_STAIN_PALETTE = "stain_palette"
        const val KEY_STAIN_INTENSITY = "stain_intensity"
        const val KEY_MATERIAL_DEPTH = "material_depth"
        const val KEY_STAIN_MOTION = "stain_motion"
        const val KEY_MATERIAL_MOTION_QUALITY = "material_motion_quality"
        const val KEY_LABS_MICRO_STABILIZATION = "labs_micro_stabilization"
        const val KEY_LABS_TRAVEL_CUES = "labs_travel_cues"
        const val KEY_LABS_MOTION_STRENGTH = "labs_motion_strength"
        const val KEY_READING_FONT_SCALE = "reading_font_scale"
        const val KEY_READING_LINE_HEIGHT = "reading_line_height"
        const val KEY_READING_PAGE_MARGIN = "reading_page_margin_dp"
        const val KEY_ACTIVE_GROUP_FILTER = "active_group_filter"
    }
}
