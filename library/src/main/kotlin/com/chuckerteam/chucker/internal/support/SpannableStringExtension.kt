package com.chuckerteam.chucker.internal.support

import android.text.SpannableStringBuilder
import androidx.core.text.getSpans

internal fun SpannableStringBuilder.spannableChunked(size: Int): List<SpannableStringBuilder> {
    val result = mutableListOf<SpannableStringBuilder>()
    var startIndex = 0
    while (startIndex < length) {
        val endIndex = (startIndex + size).coerceAtMost(length)
        val chunk = SpannableStringBuilder(subSequence(startIndex, endIndex))

        val spans = this.getSpans<Any>(startIndex, endIndex)
        for (span in spans) {
            val start = this.getSpanStart(span)
            val end = this.getSpanEnd(span)
            val flags = this.getSpanFlags(span)

            val newStart = if (start < startIndex) 0 else start - startIndex
            val newEnd = if (end > endIndex) chunk.length else end - startIndex

            chunk.setSpan(span, newStart, newEnd, flags)
        }
        result.add(chunk)

        startIndex += size
    }
    return result
}
