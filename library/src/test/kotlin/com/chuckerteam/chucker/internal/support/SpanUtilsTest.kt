package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth
import org.junit.jupiter.api.Test

internal class SpanUtilsTest {
    @Test
    fun `JSON can have null fields`() {
        val parsedJson = SpanTextUtil.spanJson(
            """{ "field": null }"""
        )

        Truth.assertThat(parsedJson).isEqualTo(
            """
            {
              "field": null
            }
            """.trimIndent()
        )
    }

    @Test
    fun `JSON can have empty fields`() {
        val parsedJson = SpanTextUtil.spanJson(
            """{ "field": "" }"""
        )

        Truth.assertThat(parsedJson).isEqualTo(
            """
            {
              "field": ""
            }
            """.trimIndent()
        )
    }

    @Test
    fun `JSON can be invalid`() {
        val parsedJson = SpanTextUtil.spanJson(
            """[{ "field": null }"""
        )

        Truth.assertThat(parsedJson).isEqualTo(
            """[{ "field": null }"""
        )
    }

    @Test
    fun `JSON object is pretty printed`() {
        val parsedJson = SpanTextUtil.spanJson(
            """{ "field1": "something", "field2": "else" }"""
        )

        Truth.assertThat(parsedJson).isEqualTo(
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
        val parsedJson = SpanTextUtil.spanJson(
            """[{ "field1": "something1", "field2": "else1" }, { "field1": "something2", "field2": "else2" }]"""
        )

        Truth.assertThat(parsedJson).isEqualTo(
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
