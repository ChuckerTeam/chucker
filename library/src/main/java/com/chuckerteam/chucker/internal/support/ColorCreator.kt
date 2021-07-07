package com.chuckerteam.chucker.internal.support

import android.graphics.Color

internal object ColorCreator {

    private const val PATTERN = 0xFFFFFF
    private const val SHIFT_FACTOR = 5
    private const val EXTRACT = 16777216
    private const val RED_CONTRAST_FACTOR = 0.299
    private const val GREEN_CONTRAST_FACTOR = 0.587
    private const val BLUE_CONTRAST_FACTOR = 0.114
    private const val CONTRAST_DIVIDER = 255
    private const val CONTRAST_CHECK = 0.5

    fun getColorFromText(text: String): Pair<Int, Int> {
        var hash = 0
        for (element in text) {
            hash = element.code + ((hash shl SHIFT_FACTOR) - hash)
        }
        val color = (hash and PATTERN) - EXTRACT
        return color to color.getContrastColor()
    }

    private fun Int.getContrastColor(): Int {
        // Counting the perceptive luminance - human eye favors green color...
        val a = 1 - (
            RED_CONTRAST_FACTOR * Color.red(this) +
                GREEN_CONTRAST_FACTOR * Color.green(this) +
                BLUE_CONTRAST_FACTOR * Color.blue(this)
            ) / CONTRAST_DIVIDER
        return if (a < CONTRAST_CHECK) Color.BLACK else Color.WHITE
    }
}
