package com.chuckerteam.chucker.internal.support

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * Highlight parts of the String when it matches the search.
 *
 * @param search the text to highlight
 */
internal fun SpannableStringBuilder.highlightWithDefinedColors(
    search: String,
    startIndices: List<Int>,
    backgroundColor: Int,
    foregroundColor: Int,
): SpannableStringBuilder = applyColoredSpannable(this, startIndices, search.length, backgroundColor, foregroundColor)

internal fun CharSequence.indicesOf(input: String): List<Int> =
    try {
        Pattern
            .quote(input)
            .toRegex(RegexOption.IGNORE_CASE)
            .findAll(this)
            .map { it.range.first }
            .toList()
    } catch (e: PatternSyntaxException) {
        Logger.warn("Unable to compile pattern for input: $input", e)
        emptyList()
    }

internal fun SpannableStringBuilder.highlightWithDefinedColorsSubstring(
    search: String,
    startIndex: Int,
    backgroundColor: Int,
    foregroundColor: Int,
): SpannableStringBuilder =
    applyColoredSpannableSubstring(
        text = this,
        subStringStartPosition = startIndex,
        subStringLength = search.length,
        backgroundColor = backgroundColor,
        foregroundColor = foregroundColor,
    )

private fun applyColoredSpannableSubstring(
    text: SpannableStringBuilder,
    subStringStartPosition: Int,
    subStringLength: Int,
    backgroundColor: Int,
    foregroundColor: Int,
): SpannableStringBuilder =
    text.apply {
        setSpan(
            UnderlineSpan(),
            subStringStartPosition,
            subStringStartPosition + subStringLength,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        setSpan(
            ForegroundColorSpan(foregroundColor),
            subStringStartPosition,
            subStringStartPosition + subStringLength,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        setSpan(
            BackgroundColorSpan(backgroundColor),
            subStringStartPosition,
            subStringStartPosition + subStringLength,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
    }

private fun applyColoredSpannable(
    text: SpannableStringBuilder,
    indexes: List<Int>,
    length: Int,
    backgroundColor: Int,
    foregroundColor: Int,
): SpannableStringBuilder =
    text.apply {
        indexes.forEach {
            applyColoredSpannableSubstring(text, it, length, backgroundColor, foregroundColor)
        }
    }
