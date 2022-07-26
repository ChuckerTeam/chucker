package com.chuckerteam.chucker.internal.support

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan

/**
 * Highlight parts of the String when it matches the search.
 *
 * @param search the text to highlight
 */
internal fun String.highlightWithDefinedColors(
    search: String,
    backgroundColor: Int,
    foregroundColor: Int
): SpannableStringBuilder {
    val startIndexes = indexesOf(this, search)
    return applyColoredSpannable(this, startIndexes, search.length, backgroundColor, foregroundColor)
}

private fun indexesOf(text: String, search: String): List<Int> {
    val startPositions = mutableListOf<Int>()
    var index = text.indexOf(search, 0, true)
    while (index >= 0) {
        startPositions.add(index)
        index = text.indexOf(search, index + 1, true)
    }
    return startPositions
}

private fun applyColoredSpannable(
    text: String,
    indexes: List<Int>,
    length: Int,
    backgroundColor: Int,
    foregroundColor: Int
): SpannableStringBuilder {
    return indexes
        .fold(SpannableStringBuilder(text)) { builder, position ->
            builder.setSpan(
                UnderlineSpan(),
                position,
                position + length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builder.setSpan(
                ForegroundColorSpan(foregroundColor),
                position,
                position + length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builder.setSpan(
                BackgroundColorSpan(backgroundColor),
                position,
                position + length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builder
        }
}
