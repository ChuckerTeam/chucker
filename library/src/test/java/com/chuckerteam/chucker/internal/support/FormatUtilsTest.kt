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
    fun testFormatJson_withNullValues() {
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
    fun testFormatJson_withEmptyValues() {
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
    fun testFormatJson_withInvalidJson() {
        val parsedJson = FormatUtils.formatJson(
            """[{ "field": null }"""
        )

        assertThat(parsedJson).isEqualTo(
            """[{ "field": null }"""
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
    fun testFormatJsonArray_willPrettyPrint() {
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
    fun testFormatUrlEncodedForm_blankString() {
        assertThat(FormatUtils.formatUrlEncodedForm("    ")).isEqualTo("    ")
    }

    @Test
    fun testFormatUrlEncodedForm_properRequest() {
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
    fun testFormatUrlEncodedForm_singleParam() {
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
    fun testFormatUrlEncodedForm_noValues() {
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
    fun testFormatUrlEncodedForm_invalidRequest() {
        val request =
            """
            sampleKey=Some%20value%someOtherKey=With%20symbols%20%25!%40%25
            """.trimIndent()
        assertThat(FormatUtils.formatUrlEncodedForm(request)).isEqualTo(request)
    }
}
