package com.chuckerteam.chucker.internal.support

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FormatUtilsTest {

    @Test
    fun testFormatJson_withNullValues() {
        val parsedJson = FormatUtils.formatJson(
            """
            {
              "field": null
            }
            """.trimIndent()
        )

        assertEquals(
            """
            {
              "field": null
            }
            """.trimIndent(),
            parsedJson
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

        assertEquals(
            """
            {
              "field": ""
            }
            """.trimIndent(),
            parsedJson
        )
    }

    @Test
    fun testFormatJson_willPrettyPrint() {
        val parsedJson = FormatUtils.formatJson(
            """{ "field1": "something", "field2": "else" }"""
        )

        assertEquals(
            """
            {
              "field1": "something",
              "field2": "else"
            }
            """.trimIndent(),
            parsedJson
        )
    }
}
