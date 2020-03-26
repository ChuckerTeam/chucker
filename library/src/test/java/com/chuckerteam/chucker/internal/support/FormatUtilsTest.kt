package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.TestTransactionFactory
import com.chuckerteam.chucker.internal.data.entity.HttpHeader
import com.google.common.truth.Truth.assertThat
import java.lang.NullPointerException
import org.junit.jupiter.api.Test

class FormatUtilsTest {

    private val exampleHeadersList = listOf(
        HttpHeader("Accept", "text/html"),
        HttpHeader("Authorization", "exampleToken")
    )

    @Test
    fun testFormatJson_withNullValues() {
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
    fun testFormatJson_withEmptyValues() {
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
    fun testFormatJson_willPrettyPrint() {
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
    fun testFormatHeaders_withNullValues() {
        val result = FormatUtils.formatHeaders(null, false)
        assertThat(result).isEmpty()
    }

    @Test
    fun testFormatHeaders_withEmptyValues() {
        val result = FormatUtils.formatHeaders(listOf(), false)
        assertThat(result).isEmpty()
    }

    @Test
    fun testFormatHeaders_withoutMarkup() {
        val result = FormatUtils.formatHeaders(exampleHeadersList, false)
        val expected = "Accept: text/html\nAuthorization: exampleToken\n"
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun testFormatHeaders_withMarkup() {
        val result = FormatUtils.formatHeaders(exampleHeadersList, true)
        val expected = "<b> Accept: </b>text/html <br /><b> Authorization: </b>exampleToken <br />"
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun testFormatByteCount_zeroBytes() {
        val resultNonSi = FormatUtils.formatByteCount(0, false)
        val resultSi = FormatUtils.formatByteCount(0, true)
        val expected = "0 B"
        assertThat(resultNonSi).isEqualTo(expected)
        assertThat(resultSi).isEqualTo(expected)
    }

    @Test
    fun testFormatByteCount_oneKiloByte() {
        testFormatByteCount(1024L, "1.0 kB", "1.0 KiB")
    }

    @Test
    fun testFormatByteCount_oneKiloByteSi() {
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
    fun testFormatXml_emptyString() {
        assertThat(FormatUtils.formatXml("")).isEmpty()
    }

    @Test
    fun testFormatXml_properXml() {
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

    @Test
    fun testGetShareCurlCommand_getMethod() {
        val actual = FormatUtils.getShareCurlCommand(TestTransactionFactory.createTransaction("GET"))
        val expected = "curl -X GET http://localhost/getUsers"
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun testGetShareCurlCommand_postMethod() {
        val actual = FormatUtils.getShareCurlCommand(TestTransactionFactory.createTransaction("POST"))
        val expected = "curl -X POST http://localhost/getUsers"
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun testFormatThrowable_containsExceptionAndMessage() {
        val actual = FormatUtils.formatThrowable(NullPointerException("NPE Test message"))
        val expected = "java.lang.NullPointerException: NPE Test message"
        assertThat(actual).contains(expected)
    }
}
