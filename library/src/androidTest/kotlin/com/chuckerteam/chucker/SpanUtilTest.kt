package com.chuckerteam.chucker

import android.annotation.SuppressLint
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.chuckerteam.chucker.internal.support.SpanTextUtil
import com.google.common.truth.Truth
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class SpanUtilTest {
    private lateinit var context: Context

    @Before
    public fun init() {
        context = InstrumentationRegistry.getInstrumentation().context
    }

    @SuppressLint("CheckResult")
    @Test
    public fun json_can_have_null_value() {
        val parsedJson = SpanTextUtil(context).spanJson(
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
    public fun json_can_have_empty_fields() {
        val parsedJson = SpanTextUtil(context).spanJson(
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
    public fun json_can_be_invalid() {
        val parsedJson = SpanTextUtil(context).spanJson(
            """[{ "field": null }"""
        )

        Truth.assertThat(parsedJson.toString()).isEqualTo(
            """[{ "field": null }"""
        )
    }

    @Test
    public fun json_object_is_pretty_printed() {
        val parsedJson = SpanTextUtil(context).spanJson(
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
    public fun json_array_is_pretty_printed() {
        val parsedJson = SpanTextUtil(context).spanJson(
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
