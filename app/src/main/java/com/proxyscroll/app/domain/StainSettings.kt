package com.proxyscroll.app.domain

enum class StainPalette(
    val storageKey: String,
    val displayName: String,
) {
    AURORA_OPAL("aurora_opal", "Aurora Opal"),
    CORAL_GLACIER("coral_glacier", "Coral Glacier"),
    NORDIC_BLOOM("nordic_bloom", "Nordic Bloom"),
    ;

    companion object {
        fun fromStorage(value: String?): StainPalette {
            return entries.firstOrNull { it.storageKey == value } ?: AURORA_OPAL
        }
    }
}

enum class MaterialDepth(
    val storageKey: String,
    val displayName: String,
    val opticalFactor: Float,
) {
    FLAT("flat", "Плоско", 0.55f),
    NATURAL("natural", "Натурально", 1f),
    DEEP("deep", "Глубоко", 1.35f),
    ;

    companion object {
        fun fromStorage(value: String?): MaterialDepth {
            return entries.firstOrNull { it.storageKey == value } ?: NATURAL
        }
    }
}

enum class StainMotion(
    val storageKey: String,
    val displayName: String,
    val amplitudeFactor: Float,
    val cycleMillis: Int,
) {
    STILL("still", "Выключено", 0f, 28_000),
    QUIET("quiet", "Тихо", 0.55f, 24_000),
    ALIVE("alive", "Живо", 1f, 18_000),
    ;

    companion object {
        fun fromStorage(value: String?): StainMotion {
            return entries.firstOrNull { it.storageKey == value } ?: QUIET
        }
    }
}

data class StainSettings(
    val palette: StainPalette = StainPalette.AURORA_OPAL,
    val intensity: Float = 0.46f,
    val depth: MaterialDepth = MaterialDepth.NATURAL,
    val motion: StainMotion = StainMotion.QUIET,
) {
    fun normalized(): StainSettings = copy(
        intensity = intensity.coerceIn(MIN_STAIN_INTENSITY, MAX_STAIN_INTENSITY),
    )
}

const val MIN_STAIN_INTENSITY = 0.18f
const val MAX_STAIN_INTENSITY = 0.82f
