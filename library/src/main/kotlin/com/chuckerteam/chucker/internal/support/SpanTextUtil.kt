package com.chuckerteam.chucker.internal.support

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.chuckerteam.chucker.R

public class SpanTextUtil(context: Context) {
    private val jsonKeyColor: Int
    private val jsonValueColor: Int
    private val jsonDigitsAndNullValueColor: Int
    private val jsonSignElementsColor: Int
    private val jsonBooleanColor: Int

    private companion object {
        // corresponds to length of word 'true'
        private const val BOOLEAN_TRUE_INDEX_OFFSET = 4

        // corresponds to length of word 'false'
        private const val BOOLEAN_FALSE_INDEX_OFFSET = 5
    }

    init {
        jsonKeyColor = ContextCompat.getColor(context, R.color.chucker_json_key_color)
        jsonValueColor = ContextCompat.getColor(context, R.color.chucker_json_value_color)
        jsonDigitsAndNullValueColor =
            ContextCompat.getColor(context, R.color.chucker_json_digit_and_null_value_color)
        jsonSignElementsColor = ContextCompat.getColor(context, R.color.chucker_json_elements_color)
        jsonBooleanColor = ContextCompat.getColor(context, R.color.chucker_json_boolean_color)
    }

    private enum class TokenType(val delimiters: Set<String>) {
        STRING(setOf("\"")),
        ARRAY(setOf("[", "]")),
        OBJECT(setOf("{", "}")),
        KEY_SEPARATOR(setOf(":")),
        VALUE_SEPARATOR(setOf(",")),
        BOOLEAN(setOf("true", "false")),
        NONE(setOf());

        companion object {
            val allPossibleTokens = values().map { it.delimiters }.flatten().toSet()
        }
    }

    private fun CharSequence.indexOfNextToken(
        startIndex: Int = 0
    ): Pair<Int, TokenType> {
        val (index, matched) = findAnyOf(
            strings = TokenType.allPossibleTokens,
            startIndex = startIndex,
            ignoreCase = true
        ) ?: return -1 to TokenType.NONE
        val tokenType = when (matched) {
            in TokenType.ARRAY.delimiters -> TokenType.ARRAY
            in TokenType.OBJECT.delimiters -> TokenType.OBJECT
            in TokenType.KEY_SEPARATOR.delimiters -> TokenType.KEY_SEPARATOR
            in TokenType.VALUE_SEPARATOR.delimiters -> TokenType.VALUE_SEPARATOR
            in TokenType.STRING.delimiters -> TokenType.STRING
            in TokenType.BOOLEAN.delimiters -> TokenType.BOOLEAN
            else -> null
        }
        tokenType?.let {
            return index to it
        }
        return -1 to TokenType.NONE
    }

    private fun CharSequence.indexOfNextUnescapedQuote(startIndex: Int = 0): Int {
        var index = indexOf('"', startIndex)
        while (index < length) {
            if (this[index] == '"' && (index == 0 || this[index - 1] != '\\')) {
                return index
            }
            index = indexOf('"', index + 1)
        }
        return -1
    }
    public fun spanJson(input: CharSequence): SpannableStringBuilder {
        // First handle the pretty printing step via gson built-in support
        val prettyPrintedInput = FormatUtils.formatJson(input.toString())

        var lastTokenType: TokenType? = null
        var index = 0

        val sb = SpannableStringBuilder(prettyPrintedInput)
        // First we set a span for all text to match the digits and null value color since other
        // cases will be overridden below
        sb.setColor(0, prettyPrintedInput.length, jsonDigitsAndNullValueColor)
        while (index < prettyPrintedInput.length) {
            val (tokenIndex, tokenType) = prettyPrintedInput.indexOfNextToken(startIndex = index)
            when (tokenType) {
                TokenType.BOOLEAN -> sb.setBooleanColor(tokenIndex).also { endIndex ->
                    index = endIndex
                }
                TokenType.ARRAY,
                TokenType.OBJECT,
                TokenType.KEY_SEPARATOR,
                TokenType.VALUE_SEPARATOR -> {
                    sb.setColor(
                        start = tokenIndex,
                        end = tokenIndex + 1,
                        color = jsonSignElementsColor
                    )
                    index = tokenIndex + 1
                }
                TokenType.STRING -> sb.setStringColor(tokenIndex, lastTokenType)?.also { endIndex ->
                    index = endIndex + 1
                } ?: return sb
                TokenType.NONE -> return sb
            }
            lastTokenType = tokenType
        }
        return sb
    }

    private fun SpannableStringBuilder.setColor(start: Int, end: Int, color: Int): SpannableStringBuilder {
        this.setSpan(
            ChuckerForegroundColorSpan(color),
            start,
            end,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        return this
    }

    /**
     * Given the tokenIndex, attempt to format the boolean value by first checking to see if the
     * token starts with a 't' for true or not.
     *
     * Returns the index of the end of the spanned boolean value
     */
    private fun SpannableStringBuilder.setBooleanColor(tokenIndex: Int): Int {
        val endIndex = if (this[tokenIndex].equals('t', ignoreCase = true)) {
            tokenIndex + BOOLEAN_TRUE_INDEX_OFFSET
        } else {
            tokenIndex + BOOLEAN_FALSE_INDEX_OFFSET
        }
        setColor(
            start = tokenIndex,
            end = endIndex,
            color = jsonBooleanColor
        )
        return endIndex
    }

    /**
     * Given the tokenIndex and lastTokenType, attempt to format the color string by searching for
     * the next unescaped quote mark in the string. If none is found, we return null immediately
     * which should signal that the JSON string is incomplete and all further formatting should stop.
     *
     * Otherwise, we will return the index of the end of the spanned string
     */
    private fun SpannableStringBuilder.setStringColor(tokenIndex: Int, lastTokenType: TokenType? = null): Int? {
        val color = when (lastTokenType) {
            TokenType.ARRAY,
            TokenType.OBJECT,
            TokenType.VALUE_SEPARATOR,
            TokenType.NONE,
            null -> {
                jsonKeyColor
            }
            else -> {
                jsonValueColor
            }
        }

        @Suppress("TooGenericExceptionCaught", "SwallowedException")
        val endIndex =
            try {
                this.indexOfNextUnescapedQuote(tokenIndex + 1)
            } catch (e: Exception) {
                -1
            }
        // if we somehow get an incomplete string, we lose the ability to parse any other
        // tokens, so just return now
        if (endIndex < tokenIndex) {
            return null
        }
        setColor(start = tokenIndex, end = endIndex + 1, color)
        return endIndex
    }

    public class ChuckerForegroundColorSpan(color: Int) : ForegroundColorSpan(color)
}
