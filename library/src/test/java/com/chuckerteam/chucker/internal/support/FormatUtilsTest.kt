package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.internal.data.entity.HttpHeader
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class FormatUtilsTest {

    private val exampleHeadersList = listOf(
        HttpHeader("Accept", "text/html"),
        HttpHeader("Authorization", "exampleToken")
    )

    @Test
    fun `test Format Json with Null Values`() {
        val parsedJson = FormatUtils.formatJson(
            """
            {
              "field": null
            }
            """.trimIndent()
        )

        assertThat(parsedJson).isEqualTo(
            """
            {
              "field": null
            }
            """.trimIndent()
        )
    }

    @Test
    fun `test Format Json with Empty Values`() {
        val parsedJson = FormatUtils.formatJson(
            """
            {
              "field": ""
            }
            """.trimIndent()
        )

        assertThat(parsedJson).isEqualTo(
            """
            {
              "field": ""
            }
            """.trimIndent()
        )
    }

    @Test
    fun `test Format Json will Pretty Print`() {
        val parsedJson = FormatUtils.formatJson(
            """{ "field1": "something", "field2": "else" }"""
        )

        assertThat(parsedJson).isEqualTo(
            """
            {
              "field1": "something",
              "field2": "else"
            }
            """.trimIndent()
        )
    }

    @Test
    fun `test Format Headers with Null Values`() {
        val result = FormatUtils.formatHeaders(null, false)
        assertThat(result).isEmpty()
    }

    @Test
    fun `test Format Headers with Empty Values`() {
        val result = FormatUtils.formatHeaders(listOf(), false)
        assertThat(result).isEmpty()
    }

    @Test
    fun `test Format Headers without Markup`() {
        val result = FormatUtils.formatHeaders(exampleHeadersList, false)
        val expected = "Accept: text/html\nAuthorization: exampleToken\n"
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test Format Headers with Markup`() {
        val result = FormatUtils.formatHeaders(exampleHeadersList, true)
        val expected = "<b> Accept: </b>text/html <br /><b> Authorization: </b>exampleToken <br />"
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test Format ByteCount zero Bytes`() {
        val resultNonSi = FormatUtils.formatByteCount(0, false)
        val resultSi = FormatUtils.formatByteCount(0, true)
        val expected = "0 B"
        assertThat(resultNonSi).isEqualTo(expected)
        assertThat(resultSi).isEqualTo(expected)
    }

    @Test
    fun `test Format ByteCount oneKiloByte`() {
        testFormatByteCount(1024L, "1.0 kB", "1.0 KiB")
    }

    @Test
    fun `test Format ByteCount oneKiloByte Si`() {
        testFormatByteCount(1023L, "1.0 kB", "1023 B")
    }

    private fun testFormatByteCount(
        byteCountToTest: Long,
        expectedSi: String,
        expectedNonSi: String
    ) {
        val resultNonSi = FormatUtils.formatByteCount(byteCountToTest, false)
        val resultSi = FormatUtils.formatByteCount(byteCountToTest, true)
        assertThat(resultNonSi).isEqualTo(expectedNonSi)
        assertThat(resultSi).isEqualTo(expectedSi)
    }

    @Test
    fun `test Format Xml empty String`() {
        assertThat(FormatUtils.formatXml("")).isEmpty()
    }

    @Test
    fun `test Format Xml proper Xml`() {
        val xml =
            """
            <example>value</example>
            """.trimIndent()
        val expected =
            """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <example>value</example>
            
            """.trimIndent()
        assertThat(FormatUtils.formatXml(xml)).isEqualTo(expected)
    }
}
