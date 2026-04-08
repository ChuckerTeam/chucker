package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.internal.data.entity.HttpHeader
import com.google.common.truth.Truth.assertThat
import com.google.gson.reflect.TypeToken
import org.junit.jupiter.api.Test

internal class HttpHeaderSerializerTest {
    private val sampleHeaders =
        listOf(
            HttpHeader("Content-Type", "application/json"),
            HttpHeader("X-Trace", "abc\"123\n"),
        )

    private val headerListType =
        TypeToken.getParameterized(List::class.java, HttpHeader::class.java).type

    @Test
    fun `empty list serializes to an empty JSON array`() {
        val json = HttpHeaderSerializer.toJson(emptyList())

        val parsed: List<HttpHeader> = JsonConverter.instance.fromJson(json, headerListType)
        assertThat(parsed).isEmpty()
    }

    @Test
    fun `round-trips headers through the default Gson path`() {
        val json = HttpHeaderSerializer.toJson(sampleHeaders)

        val parsed: List<HttpHeader> = JsonConverter.instance.fromJson(json, headerListType)
        assertThat(parsed).containsExactlyElementsIn(sampleHeaders).inOrder()
    }

    @Test
    fun `fallback path produces JSON that round-trips back to the same headers`() {
        val fallbackJson = invokePrivateFallback(sampleHeaders)

        val parsed: List<HttpHeader> = JsonConverter.instance.fromJson(fallbackJson, headerListType)
        assertThat(parsed).containsExactlyElementsIn(sampleHeaders).inOrder()
    }

    @Test
    fun `fallback escapes control characters and quotes into valid JSON`() {
        val awkward =
            listOf(
                HttpHeader("k\"ey", "line1\nline2\tend"),
            )

        val fallbackJson = invokePrivateFallback(awkward)

        val parsed: List<HttpHeader> = JsonConverter.instance.fromJson(fallbackJson, headerListType)
        assertThat(parsed).containsExactlyElementsIn(awkward).inOrder()
    }

    private fun invokePrivateFallback(headers: List<HttpHeader>): String {
        val method =
            HttpHeaderSerializer::class.java.getDeclaredMethod("buildHeadersJson", List::class.java).apply {
                isAccessible = true
            }
        return method.invoke(HttpHeaderSerializer, headers) as String
    }
}
