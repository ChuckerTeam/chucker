package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.text.DateFormat
import java.util.*

class JsonConverterTest {

    @Test
    fun testInstance_sameInstance() {
        val instance1 = JsonConverter.instance
        val instance2 = JsonConverter.instance

        assertThat(instance1).isEqualTo(instance2)
    }

    @Test
    fun testGsonConfiguration_willParseDateTime() {
        val dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US)
        val expectedDateString = dateFormat.format(Date(0))
        val json = JsonConverter.instance.toJson(DateTestClass(Date(0)))
        assertThat(json).isEqualTo(
            """
            {
              "date": "$expectedDateString"
            }
            """.trimIndent()
        )
    }

    @Test
    fun testGsonConfiguration_willUseLowerCaseWithUnderscores() {
        val json = JsonConverter.instance.toJson(NamingTestClass("aCamelCaseString"))
        assertThat(json).isEqualTo(
            """
            {
              "a_long_name_with_camel_case": "aCamelCaseString"
            }
            """.trimIndent()
        )
    }

    @Test
    fun testGsonConfiguration_willSerializeNulls() {
        val json = JsonConverter.instance.toJson(NullTestClass(null))
        assertThat(json).isEqualTo(
            """
            {
              "string": null
            }
            """.trimIndent()
        )
    }

    inner class DateTestClass(var date: Date)
    inner class NullTestClass(var string: String?)
    inner class NamingTestClass(var aLongNameWithCamelCase: String)
}
