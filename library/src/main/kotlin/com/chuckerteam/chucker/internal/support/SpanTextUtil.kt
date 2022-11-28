package com.chuckerteam.chucker.internal.support

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.text.isDigitsOnly
import com.google.gson.JsonElement
import com.google.gson.JsonParser


public class SpanTextUtil {
    public companion object {
        private val JSON_KEY_COLOR = Color.parseColor("#8B0057")
        private val JSON_STRING_VALUE_COLOR = Color.parseColor("#2F00FF")
        private val JSON_DIGIT_AND_NULL_VALUE_COLOR = Color.parseColor("#E84B31")
        private val JSON_SIGN_ELEMENTS_COLOR = Color.parseColor("#474747")

        public fun spanJson(input: CharSequence): SpannableStringBuilder {
            val jsonElement = try {
                JsonParser.parseString(input.toString())
            } catch (e: Exception) {
                return SpannableStringBuilder.valueOf(input)
            }
            val result = SpannableStringBuilder()
            result.append(printifyRecursive(jsonElement, ""))
            return result
        }

        private fun printifyRecursive(
            transformedJson: JsonElement,
            currentIndent: String
        ): SpannableStringBuilder {
            var indent = currentIndent
            val result = SpannableStringBuilder()
            if (transformedJson.isJsonArray) {
                if (transformedJson.asJsonArray.size() == 0)
                    return SpannableStringBuilder().appendWithColor(
                        "[]",
                        JSON_SIGN_ELEMENTS_COLOR
                    )
                result.appendWithColor("$indent[\n", JSON_SIGN_ELEMENTS_COLOR)
                indent += "  "
                for (index in 0 until transformedJson.asJsonArray.size()) {
                    val item = transformedJson.asJsonArray[index]
                    if (item.isJsonObject || item.isJsonArray) result.append(
                        printifyRecursive(
                            item,
                            indent
                        )
                    )
                    else {
                        result.append(indent)
                        result.appendJsonValue(item)
                    }
                    if (index != transformedJson.asJsonArray.size() - 1)
                        result.appendWithColor(",", JSON_SIGN_ELEMENTS_COLOR).append("\n")
                }
                if (indent.length > 1)
                    indent = indent.substring(2)
                result.appendWithColor("\n$indent]", JSON_SIGN_ELEMENTS_COLOR)
            }
            if (transformedJson.isJsonObject) {
                if (transformedJson.asJsonObject.size() == 0)
                    return SpannableStringBuilder().appendWithColor(
                        "{}",
                        JSON_SIGN_ELEMENTS_COLOR
                    )
                result.appendWithColor("$indent{\n", JSON_SIGN_ELEMENTS_COLOR)
                indent += "  "
                var index = 0
                for (item in transformedJson.asJsonObject.entrySet()) {
                    result.append(indent)
                    index++
                    result.appendWithColor("\"${item.key}\"", JSON_KEY_COLOR)
                        .appendWithColor(":", JSON_SIGN_ELEMENTS_COLOR)
                    if (item.value.isJsonObject || item.value.isJsonArray)
                        result.append(" ").append(printifyRecursive(item.value, indent))
                    else result.appendJsonValue(item.value)
                    if (index != transformedJson.asJsonObject.size())
                        result.appendWithColor(",", JSON_SIGN_ELEMENTS_COLOR).append("\n")
                }
                if (indent.length > 1)
                    indent = indent.substring(2)
                result.appendWithColor("\n$indent}", JSON_SIGN_ELEMENTS_COLOR)
            }
            return result
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
