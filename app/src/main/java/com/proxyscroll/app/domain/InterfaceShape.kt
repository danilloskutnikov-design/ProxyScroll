package com.proxyscroll.app.domain

const val MIN_INTERFACE_CORNER_DP = 4
const val MAX_INTERFACE_CORNER_DP = 24

data class InterfaceShape(
    val globalCornerDp: Int = 14,
    val cardCornerDp: Int = 14,
    val inputCornerDp: Int = 12,
    val buttonCornerDp: Int = 16,
    val linked: Boolean = true,
    /** Theme geometry wins until the user explicitly enables Shape Studio. */
    val customEnabled: Boolean = false,
) {
    val resolvedCardCornerDp: Int
        get() = if (linked) globalCornerDp else cardCornerDp

    val resolvedInputCornerDp: Int
        get() = if (linked) globalCornerDp else inputCornerDp

    val resolvedButtonCornerDp: Int
        get() = if (linked) globalCornerDp else buttonCornerDp

    fun withGlobalCorner(value: Int): InterfaceShape {
        val corner = value.coerceIn(MIN_INTERFACE_CORNER_DP, MAX_INTERFACE_CORNER_DP)
        return if (linked) {
            copy(
                globalCornerDp = corner,
                cardCornerDp = corner,
                inputCornerDp = corner,
                buttonCornerDp = corner,
            )
        } else {
            copy(globalCornerDp = corner)
        }
    }

    fun withLinked(value: Boolean): InterfaceShape {
        return if (value) {
            copy(
                linked = true,
                cardCornerDp = globalCornerDp,
                inputCornerDp = globalCornerDp,
                buttonCornerDp = globalCornerDp,
            )
        } else {
            copy(linked = false)
        }
    }

    fun withCardCorner(value: Int) = copy(
        cardCornerDp = value.coerceIn(MIN_INTERFACE_CORNER_DP, MAX_INTERFACE_CORNER_DP),
    )

    fun withInputCorner(value: Int) = copy(
        inputCornerDp = value.coerceIn(MIN_INTERFACE_CORNER_DP, MAX_INTERFACE_CORNER_DP),
    )

    fun withButtonCorner(value: Int) = copy(
        buttonCornerDp = value.coerceIn(MIN_INTERFACE_CORNER_DP, MAX_INTERFACE_CORNER_DP),
    )
}

fun AppTheme.defaultInterfaceShape(): InterfaceShape = when (this) {
    AppTheme.LIQUID_GLASS -> InterfaceShape(
        globalCornerDp = 22,
        cardCornerDp = 20,
        inputCornerDp = 24,
        buttonCornerDp = 24,
        linked = false,
    )
    AppTheme.ROYAL_GRAPHITE -> InterfaceShape(
        globalCornerDp = 13,
        cardCornerDp = 12,
        inputCornerDp = 12,
        buttonCornerDp = 14,
        linked = false,
    )
    AppTheme.OLD_SCROLL -> InterfaceShape(
        globalCornerDp = 4,
        cardCornerDp = 4,
        inputCornerDp = 5,
        buttonCornerDp = 6,
        linked = false,
    )
    AppTheme.LITE_LIFE -> InterfaceShape(
        globalCornerDp = 8,
        cardCornerDp = 7,
        inputCornerDp = 10,
        buttonCornerDp = 18,
        linked = false,
    )
}

fun InterfaceShape.resolveFor(theme: AppTheme): InterfaceShape {
    return if (customEnabled) this else theme.defaultInterfaceShape()
}
