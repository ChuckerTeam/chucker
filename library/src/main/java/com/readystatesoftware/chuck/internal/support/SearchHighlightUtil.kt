package com.readystatesoftware.chuck.internal.support

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan

import java.util.ArrayList

object SearchHighlightUtil {

    @JvmStatic
    fun format(text: String, criteria: String): CharSequence {
        val startIndexes = indexesOf(text, criteria)
        return if (startIndexes[0] == -1) {
            text
        } else {
            applySpannable(text, startIndexes, criteria.length)
        }
    }

    private fun indexesOf(text: String, criteria: String): List<Int> {
        val startPositions = ArrayList<Int>()
        var index = text.indexOf(criteria, 0, true)
        do {
            startPositions.add(index)
            index = text.indexOf(criteria, index + 1, true)
        } while (index >= 0)
        return startPositions
    }

    private fun applySpannable(text: String, indexes: List<Int>, length: Int): SpannableStringBuilder {
        val builder = SpannableStringBuilder(text)
        for (position in indexes) {
            builder.setSpan(UnderlineSpan(),
                            position, position + length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(ForegroundColorSpan(Color.RED),
                            position, position + length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(BackgroundColorSpan(Color.YELLOW),
                            position, position + length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return builder
    }

}