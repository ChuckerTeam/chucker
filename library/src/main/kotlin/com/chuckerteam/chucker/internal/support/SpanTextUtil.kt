package com.chuckerteam.chucker.internal.support

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.text.isDigitsOnly
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException


public class SpanTextUtil {
    public companion object {
        private val JSON_KEY_COLOR = Color.parseColor("#8B0057")
        private val JSON_STRING_VALUE_COLOR = Color.parseColor("#2F00FF")
        private val JSON_DIGIT_AND_NULL_VALUE_COLOR = Color.parseColor("#E84B31")
        private val JSON_SIGN_ELEMENTS_COLOR = Color.parseColor("#474747")

        public fun spanJson(input: CharSequence): SpannableStringBuilder {
            val jsonElement = try {
                JsonParser.parseString(input.toString())
            } catch (e: JsonSyntaxException) {
                Logger.warn("Json structure is invalid so it can not be formatted", e)
                return SpannableStringBuilder.valueOf(input)
            }
            val sb = SpannableStringBuilder()
            printifyRecursive(jsonElement, StringBuilder(""), sb)
            return sb
        }

        private fun printifyRecursive(
            transformedJson: JsonElement,
            currentIndent: StringBuilder,
            sb: SpannableStringBuilder
        ) {
            if (transformedJson.isJsonArray) {
                printifyJsonArray(sb, currentIndent, transformedJson)
            }
            if (transformedJson.isJsonObject) {
                printifyJsonObject(sb, currentIndent, transformedJson)
            }
        }

        private fun printifyJsonArray(
            sb: SpannableStringBuilder,
            indent: StringBuilder,
            transformedJson: JsonElement
        ) {
            if (transformedJson.asJsonArray.isEmpty) {
                sb.appendWithColor(
                    "[]",
                    JSON_SIGN_ELEMENTS_COLOR
                )
                return
            }
            sb.appendWithColor("$indent[\n", JSON_SIGN_ELEMENTS_COLOR)
            indent.append("  ")
            for (index in 0 until transformedJson.asJsonArray.size()) {
                val item = transformedJson.asJsonArray[index]
                if (item.isJsonObject || item.isJsonArray)
                    printifyRecursive(item, indent, sb)
                else {
                    sb.append(indent)
                    sb.appendJsonValue(item)
                }
                if (index != transformedJson.asJsonArray.size() - 1)
                    sb.appendWithColor(",", JSON_SIGN_ELEMENTS_COLOR).append("\n")
            }
            val finalIndent = StringBuilder(indent.dropLast(2))
            sb.appendWithColor("\n$finalIndent]", JSON_SIGN_ELEMENTS_COLOR)
        }

        private fun printifyJsonObject(
            sb: SpannableStringBuilder,
            indentBuilder: StringBuilder,
            transformedJson: JsonElement
        ) {
            if (transformedJson.asJsonObject.size() == 0) {
                sb.appendWithColor(
                    "{}",
                    JSON_SIGN_ELEMENTS_COLOR
                )
                return
            }
            sb.appendWithColor("${indentBuilder}{\n", JSON_SIGN_ELEMENTS_COLOR)
            indentBuilder.append("  ")
            var index = 0
            for (item in transformedJson.asJsonObject.entrySet()) {
                sb.append(indentBuilder)
                index++
                sb.appendWithColor("\"${item.key}\"", JSON_KEY_COLOR)
                    .appendWithColor(":", JSON_SIGN_ELEMENTS_COLOR)
                if (item.value.isJsonObject || item.value.isJsonArray) {
                    sb.append(" ")
                    printifyRecursive(item.value, indentBuilder, sb)
                } else sb.appendJsonValue(item.value)
                if (index != transformedJson.asJsonObject.size())
                    sb.appendWithColor(",", JSON_SIGN_ELEMENTS_COLOR).append("\n")
            }
            sb.appendWithColor("\n${indentBuilder.dropLast(2)}}", JSON_SIGN_ELEMENTS_COLOR)
        }

        private fun SpannableStringBuilder.appendWithColor(text: CharSequence, color: Int):
            SpannableStringBuilder {
            this.append(
                text, ChuckerForegroundColorSpan(color),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
            return this
        }

        private fun SpannableStringBuilder.appendJsonValue(jsonValue: JsonElement):
            SpannableStringBuilder {
            val isDigit = jsonValue.isJsonNull.not() &&
                jsonValue.asString.isNotEmpty() &&
                jsonValue.isJsonPrimitive &&
                jsonValue.asString.isDigitsOnly()
            val value = if (isDigit) jsonValue.asString else jsonValue.toString()
            val color = if (isDigit || jsonValue.isJsonNull) JSON_DIGIT_AND_NULL_VALUE_COLOR
            else JSON_STRING_VALUE_COLOR
            return this.appendWithColor(
                " $value",
                color
            )
        }
    }

    public class ChuckerForegroundColorSpan(color: Int) : ForegroundColorSpan(color)
}

