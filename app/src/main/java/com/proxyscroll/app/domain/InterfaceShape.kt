package com.proxyscroll.app.domain

const val MIN_INTERFACE_CORNER_DP = 8
const val MAX_INTERFACE_CORNER_DP = 24

data class InterfaceShape(
    val globalCornerDp: Int = 14,
    val cardCornerDp: Int = 14,
    val inputCornerDp: Int = 12,
    val buttonCornerDp: Int = 16,
    val linked: Boolean = true,
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
