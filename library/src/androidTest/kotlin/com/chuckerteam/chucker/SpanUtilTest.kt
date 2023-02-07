package com.chuckerteam.chucker

import android.annotation.SuppressLint
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chuckerteam.chucker.internal.support.SpanTextUtil
import com.google.common.truth.Truth
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class SpanUtilTest {
    @SuppressLint("CheckResult")
    @Test
    public fun `json can have null value`() {
        val parsedJson = SpanTextUtil.spanJson(
            """{ "field": null }"""
        )
        Assert.assertEquals(
            parsedJson.toString(),
            """
            {
              "field": null
            }
            """.trimIndent()
        )
    }
    @Test
    public fun `json can have empty fields`() {
        val parsedJson = SpanTextUtil.spanJson(
            """{ "field": "" }"""
        )

        Truth.assertThat(parsedJson.toString()).isEqualTo(
            """
            {
              "field": ""
            }
            """.trimIndent()
        )
    }

    @Test
    public fun `json can be invalid`() {
        val parsedJson = SpanTextUtil.spanJson(
            """[{ "field": null }"""
        )

        Truth.assertThat(parsedJson.toString()).isEqualTo(
            """[{ "field": null }"""
        )
    }

    @Test
    public fun `json object is pretty printed`() {
        val parsedJson = SpanTextUtil.spanJson(
            """{ "field1": "something", "field2": "else" }"""
        )

        Truth.assertThat(parsedJson.toString()).isEqualTo(
            """
            {
              "field1": "something",
              "field2": "else"
            }
            """.trimIndent()
        )
    }

    @Test
    public fun `json array is pretty printed`() {
        val parsedJson = SpanTextUtil.spanJson(
            """[{ "field1": "something1", "field2": "else1" }, { "field1": "something2", "field2": "else2" }]"""
        )

        Truth.assertThat(parsedJson.toString()).isEqualTo(
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
