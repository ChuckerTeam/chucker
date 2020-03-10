package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import org.junit.jupiter.api.Test

class JsonConverterTest {

    @Test
    fun testInstance_sameInstance() {
        val instance1 = JsonConverter.instance
        val instance2 = JsonConverter.instance

        assertThat(instance1).isEqualTo(instance2)
    }

    @Test
    fun testMoshiConfiguration_willParseDateTime() {
        val dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US)
        val expectedDateString = dateFormat.format(Date(0))
        val json = JsonConverter.instance.adapter(DateTestClass::class.java).addConvenienceMethods().toJson(DateTestClass(Date(0)))
        assertThat(json).isEqualTo(
            """
            {
              "date": "$expectedDateString"
            }
            """.trimIndent()
        )
    }

    @Test
    fun testMoshiConfiguration_willSerializeNulls() {
        val json = JsonConverter.instance.adapter(NullTestClass::class.java).addConvenienceMethods().toJson(NullTestClass(null))
        assertThat(json).isEqualTo(
            """
            {
              "string": null
            }
            """.trimIndent()
        )
    }

    private class DateTestClass(var date: Date)
    private class NullTestClass(var string: String?)
}
