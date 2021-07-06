package com.chuckerteam.chucker.internal.support

import kotlin.math.pow

internal object ColorCreator {

    private const val PATTERN = 0xFFFFFF
    private const val SHIFT_FACTOR = 5
    private val extract by lazy { 2.0.pow(24.0).toInt() }

    fun getColorFromText(text: String): Pair<Int, Int> {
        var hash = 0
        for (element in text) {
            hash = element.toInt() + ((hash shl SHIFT_FACTOR) - hash)
        }
        var color = hash and PATTERN
        color -= extract
        return color to (color xor PATTERN)
    }
}
