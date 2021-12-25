package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.internal.data.entity.HttpHeader
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class FormatUtilsTest {

    private val exampleHeadersList = listOf(
        HttpHeader("Accept", "text/html"),
        HttpHeader("Authorization", "exampleToken")
    )

    @Test
    fun `JSON can have null fields`() {
        val parsedJson = FormatUtils.formatJson(
            """{ "field": null }"""
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
    fun `JSON can have empty fields`() {
        val parsedJson = FormatUtils.formatJson(
            """{ "field": "" }"""
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
    fun `JSON can be invalid`() {
        val parsedJson = FormatUtils.formatJson(
            """[{ "field": null }"""
        )

        assertThat(parsedJson).isEqualTo(
            """[{ "field": null }"""
        )
    }

    @Test
    fun `JSON object is pretty printed`() {
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
    fun `JSON array is pretty printed`() {
        val parsedJson = FormatUtils.formatJson(
            """[{ "field1": "something1", "field2": "else1" }, { "field1": "something2", "field2": "else2" }]"""
        )

        assertThat(parsedJson).isEqualTo(
            """
            [
              {
                "field1": "something1",
                "field2": "else1"
              },
              {
                "field1": "something2",
                "field2": "else2"
              }
            ]
            """.trimIndent()
        )
    }

    @Test
    fun `headers can have null values`() {
        val result = FormatUtils.formatHeaders(null, withMarkup = false)
        assertThat(result).isEmpty()
    }

    @Test
    fun `headers can have empty values`() {
        val result = FormatUtils.formatHeaders(listOf(), withMarkup = false)
        assertThat(result).isEmpty()
    }

    @Test
    fun `headers are formatted without markup`() {
        val result = FormatUtils.formatHeaders(exampleHeadersList, withMarkup = false)
        val expected = "Accept: text/html\nAuthorization: exampleToken\n"
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `headers are formatted with markup`() {
        val result = FormatUtils.formatHeaders(exampleHeadersList, withMarkup = true)
        val expected = "<b> Accept: </b>text/html <br /><b> Authorization: </b>exampleToken <br />"
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `byte count can be zero`() {
        val resultNonSi = FormatUtils.formatByteCount(0, si = false)
        val resultSi = FormatUtils.formatByteCount(0, si = true)
        val expected = "0 B"
        assertThat(resultNonSi).isEqualTo(expected)
        assertThat(resultSi).isEqualTo(expected)
    }

    @Test
    fun `1 kilobyte is formatted`() {
        testFormatByteCount(1024L, "1.0 kB", "1.0 KiB")
    }

    @Test
    fun `1023 bytes are formatted`() {
        testFormatByteCount(1023L, "1.0 kB", "1023 B")
    }

    private fun testFormatByteCount(
        byteCountToTest: Long,
        expectedSi: String,
        expectedNonSi: String
    ) {
        val resultNonSi = FormatUtils.formatByteCount(byteCountToTest, si = false)
        val resultSi = FormatUtils.formatByteCount(byteCountToTest, si = true)
        assertThat(resultNonSi).isEqualTo(expectedNonSi)
        assertThat(resultSi).isEqualTo(expectedSi)
    }

    @Test
    fun `XML can be empty`() {
        assertThat(FormatUtils.formatXml("")).isEmpty()
    }

    @Test
    fun `XML has encoding header added`() {
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
    fun `URL encoded form can be blank`() {
        assertThat(FormatUtils.formatUrlEncodedForm("    ")).isEqualTo("    ")
    }

    @Test
    fun `URL encoded form multiple parameters are formatted`() {
        val request =
            """
            sampleKey=Some%20value&someOtherKey=With%20symbols%20%25!%40%25
            """.trimIndent()
        val expected =
            """
            sampleKey: Some value
            someOtherKey: With symbols %!@%
            """.trimIndent()
        assertThat(FormatUtils.formatUrlEncodedForm(request)).isEqualTo(expected)
    }

    @Test
    fun `URL encoded form single parameter is formatted`() {
        val request =
            """
            sampleKey=Some%20value
            """.trimIndent()
        val expected =
            """
            sampleKey: Some value
            """.trimIndent()
        assertThat(FormatUtils.formatUrlEncodedForm(request)).isEqualTo(expected)
    }

    @Test
    fun `URL encoded form parameters can be without values`() {
        val request =
            """
            sampleKey=&someOtherKey=
            """.trimIndent()
        val expected =
            """
            sampleKey: 
            someOtherKey: 
            """.trimIndent()
        assertThat(FormatUtils.formatUrlEncodedForm(request)).isEqualTo(expected)
    }

    @Test
    fun `URL encoded form parameters can be invalid`() {
        val request =
            """
            sampleKey=Some%20value%someOtherKey=With%20symbols%20%25!%40%25
            """.trimIndent()
        assertThat(FormatUtils.formatUrlEncodedForm(request)).isEqualTo(request)
    }
}
