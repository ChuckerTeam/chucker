package com.chuckerteam.chucker.api.internal.support

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan

/**
 * Hightlight parts of the String when it matches the search.
 *
 * @param search the text to highlight
 */
fun String.hightlight(search: String): CharSequence {
    val startIndexes = indexesOf(this, search)
    return applySpannable(this, startIndexes, search.length)
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

private fun applySpannable(text: String, indexes: List<Int>, length: Int): SpannableStringBuilder {
    return indexes
        .fold(SpannableStringBuilder(text)) { builder, position ->
            builder.setSpan(
                UnderlineSpan(),
                position,
                position + length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builder.setSpan(
                ForegroundColorSpan(Color.RED),
                position,
                position + length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builder.setSpan(
                BackgroundColorSpan(Color.YELLOW),
                position,
                position + length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builder
        }
}
