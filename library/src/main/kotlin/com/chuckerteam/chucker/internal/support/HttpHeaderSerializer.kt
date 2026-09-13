package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.internal.data.entity.HttpHeader
import com.google.gson.JsonArray
import com.google.gson.JsonObject

internal object HttpHeaderSerializer {
    fun toJson(headers: List<HttpHeader>): String =
        try {
            JsonConverter.instance.toJson(headers)
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException") e: Exception,
        ) {
            // Gson's default path resolves the List<HttpHeader> element type
            // reflectively, which occasionally throws NPE on cold start (issue
            // #1602). Build the JSON tree explicitly so callers still get a
            // valid string that fromJson<List<HttpHeader>> can round-trip.
            buildHeadersJson(headers)
        }

    private fun buildHeadersJson(headers: List<HttpHeader>): String {
        val array = JsonArray(headers.size)
        headers.forEach { header ->
            val obj = JsonObject()
            obj.addProperty("name", header.name)
            obj.addProperty("value", header.value)
            array.add(obj)
        }
        return array.toString()
    }
}
