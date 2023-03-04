package com.chuckerteam.chucker.internal.support

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import com.chuckerteam.chucker.R
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException


public class SpanTextUtil(context: Context) {
    private val jsonKeyColor: Int
    private val jsonValueColor: Int
    private val jsonDigitsAndNullValueColor: Int
    private val jsonSignElementsColor: Int

    init {
        jsonKeyColor = ContextCompat.getColor(context, R.color.chucker_json_key_color)
        jsonValueColor = ContextCompat.getColor(context, R.color.chucker_json_value_color)
        jsonDigitsAndNullValueColor =
            ContextCompat.getColor(context, R.color.chucker_json_digit_and_null_value_color)
        jsonSignElementsColor = ContextCompat.getColor(context, R.color.chucker_json_elements_color)
    }

    public fun spanJson(input: CharSequence): SpannableStringBuilder {
        val jsonElement = try {
            JsonParser.parseString(input.toString())
        } catch (e: JsonSyntaxException) {
            Logger.warn("Json structure is invalid so it can not be formatted", e)
            return SpannableStringBuilder.valueOf(input)
        }
        return SpannableStringBuilder().also {
            printifyRecursive(it, StringBuilder(""), jsonElement)
        }
    }
    private fun printifyRecursive(
        sb: SpannableStringBuilder,
        currentIndent: StringBuilder,
        transformedJson: JsonElement
    ) {
        val indent = StringBuilder(currentIndent)
        if (transformedJson.isJsonArray) {
            printifyJsonArray(sb, indent, transformedJson)
        }
        if (transformedJson.isJsonObject) {
            printifyJsonObject(sb, indent, transformedJson)
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
                jsonSignElementsColor
            )
            return
        }
        sb.appendWithColor("${indent}[\n", jsonSignElementsColor)
        indent.append("  ")
        for (index in 0 until transformedJson.asJsonArray.size()) {
            val item = transformedJson.asJsonArray[index]
            if (item.isJsonObject || item.isJsonArray)
                printifyRecursive(sb, indent, item)
            else {
                sb.append(indent)
                sb.appendJsonValue(item)
            }
            if (index != transformedJson.asJsonArray.size() - 1)
                sb.appendWithColor(",", jsonSignElementsColor).append("\n")
        }
        val finalIndent = StringBuilder(indent.dropLast(2))
        sb.appendWithColor("\n${finalIndent}]", jsonSignElementsColor)
    }

    private fun printifyJsonObject(
        sb: SpannableStringBuilder,
        indentBuilder: StringBuilder,
        transformedJson: JsonElement
    ) {
        if (transformedJson.asJsonObject.size() == 0) {
            sb.appendWithColor(
                "{}",
                jsonSignElementsColor
            )
            return
        }
        sb.appendWithColor("${indentBuilder}{\n", jsonSignElementsColor)
        indentBuilder.append("  ")
        var index = 0
        for (item in transformedJson.asJsonObject.entrySet()) {
            sb.append(indentBuilder)
            index++
            sb.appendWithColor("\"${item.key}\"", jsonKeyColor)
                .appendWithColor(":", jsonSignElementsColor)
            if (item.value.isJsonObject || item.value.isJsonArray) {
                sb.append(" ")
                printifyRecursive(sb, indentBuilder, item.value)
            } else sb.appendJsonValue(item.value)
            if (index != transformedJson.asJsonObject.size())
                sb.appendWithColor(",", jsonSignElementsColor).append("\n")
        }
        sb.appendWithColor("\n${indentBuilder.dropLast(2)}}", jsonSignElementsColor)
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
        val color = if (isDigit || jsonValue.isJsonNull) jsonDigitsAndNullValueColor
        else jsonValueColor
        return this.appendWithColor(
            " $value",
            color
        )
    }

    public class ChuckerForegroundColorSpan(color: Int) : ForegroundColorSpan(color)
}

