package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FormatUtilsTest {

    @Test
    fun testFormatJson_withNullValues() {
        val parsedJson = FormatUtils.formatJson(
            """{ "field" : null }""".trimIndent()
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
            """ { "field" : "" } """.trimIndent()
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
    fun testFormatJsonArray_willPrettyPrint() {
        val parsedJson = FormatUtils.formatJson(
                """[{"field1":"something1","field2":"else1"},{"field1":"something2","field2":"else2"}]"""
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
}
