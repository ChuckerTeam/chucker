package com.chuckerteam.chucker.internal.support

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import java.util.regex.Pattern

/**
 * Highlight parts of the String when it matches the search.
 *
 * @param search the text to highlight
 */
internal fun SpannableStringBuilder.highlightWithDefinedColors(
    search: String,
    startIndices: List<Int>,
    backgroundColor: Int,
    foregroundColor: Int
): SpannableStringBuilder {
    return applyColoredSpannable(this, startIndices, search.length, backgroundColor, foregroundColor)
}

internal fun CharSequence.indicesOf(input: String): List<Int> =
    Pattern.compile(input, Pattern.CASE_INSENSITIVE).toRegex()
        .findAll(this)
        .map { it.range.first }
        .toCollection(mutableListOf())

internal fun SpannableStringBuilder.highlightWithDefinedColorsSubstring(
    search: String,
    startIndex: Int,
    backgroundColor: Int,
    foregroundColor: Int
): SpannableStringBuilder {
    return applyColoredSpannableSubstring(this, startIndex, search.length, backgroundColor, foregroundColor)
}

private fun applyColoredSpannableSubstring(
    text: SpannableStringBuilder,
    subStringStartPosition: Int,
    subStringLength: Int,
    backgroundColor: Int,
    foregroundColor: Int
): SpannableStringBuilder {
    return text.apply {
        setSpan(
            UnderlineSpan(),
            subStringStartPosition,
            subStringStartPosition + subStringLength,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        setSpan(
            ForegroundColorSpan(foregroundColor),
            subStringStartPosition,
            subStringStartPosition + subStringLength,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        setSpan(
            BackgroundColorSpan(backgroundColor),
            subStringStartPosition,
            subStringStartPosition + subStringLength,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}

private fun applyColoredSpannable(
    text: SpannableStringBuilder,
    indexes: List<Int>,
    length: Int,
    backgroundColor: Int,
    foregroundColor: Int
): SpannableStringBuilder {
    return text.apply {
        indexes.forEach {
            applyColoredSpannableSubstring(text, it, length, backgroundColor, foregroundColor)
        }
    }
}
