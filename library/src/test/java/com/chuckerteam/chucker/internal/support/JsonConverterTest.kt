package com.chuckerteam.chucker.internal.support

import java.util.Date
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class JsonConverterTest {

    @Test
    fun testInstance_sameInstance() {
        val instance1 = JsonConverter.instance
        val instance2 = JsonConverter.instance

        assertTrue(instance1 == instance2)
    }

    @Test
    fun testGsonConfiguration_willParseDateTime() {
        val json = JsonConverter.instance.toJson(DateTestClass(Date(0)))
        assertEquals(
            """
            {
              "date": "Jan 1, 1970 1:00:00 AM"
            }
            """.trimIndent(),
            json
        )
    }

    @Test
    fun testGsonConfiguration_willUseLowerCaseWithUnderscores() {
        val json = JsonConverter.instance.toJson(NamingTestClass("aCamelCaseString"))
        assertEquals(
            """
            {
              "a_long_name_with_camel_case": "aCamelCaseString"
            }
            """.trimIndent(),
            json
        )
    }

    @Test
    fun testGsonConfiguration_willSerializeNulls() {
        val json = JsonConverter.instance.toJson(NullTestClass(null))
        assertEquals(
            """
            {
              "string": null
            }
            """.trimIndent(),
            json
        )
    }

    inner class DateTestClass(var date: Date)
    inner class NullTestClass(var string: String?)
    inner class NamingTestClass(var aLongNameWithCamelCase: String)
}
