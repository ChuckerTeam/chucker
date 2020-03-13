package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import org.junit.Test

class JsonConverterTest {

    @Test
    fun testInstance_sameInstance() {
        val instance1 = JsonConverter.instance
        val instance2 = JsonConverter.instance
        assertThat(instance1).isEqualTo(instance2)
    }

    @Test
    fun testMoshiConfiguration_willSerializeDateTime() {
        val dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US)
        val dateTestJson = JsonConverter.instance.adapter(DateTestClass::class.java)
            .toJson(DateTestClass(Date(0)))
        assertThat(dateTestJson).isEqualTo("""{"date":"${dateFormat.format(Date(0))}"}""")
    }

    @Test
    fun testMoshiConfiguration_willDeserializeDateTime() {
        val dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US)
        val dateTestClass = JsonConverter.instance.adapter(DateTestClass::class.java)
            .fromJson("""{"date":"${dateFormat.format(Date(0))}"}""")
        assertThat(dateTestClass).isEqualTo(DateTestClass(Date(0)))
    }

    @Test
    fun testMoshiConfiguration_willSerialize_null_DateTime() {
        val dateTestJson = JsonConverter.instance.adapter(DateTestClass::class.java)
            .toJson(DateTestClass(null))
        assertThat(dateTestJson).isEqualTo("{}")
    }

    @Test
    fun testMoshiConfiguration_willDeserialize_null_DateTime() {
        val dateTestClass = JsonConverter.instance.adapter(DateTestClass::class.java)
            .fromJson("""{"date":null}""")
        assertThat(dateTestClass).isEqualTo(DateTestClass(null))
    }

    @Test(expected = JsonDataException::class)
    fun testMoshiConfiguration_willDeserialize_Invalid_DateTime() {
        val dateTestClass = JsonConverter.instance.adapter(DateTestClass::class.java)
            .fromJson("""{"date":"abcd"}""")
        assertThat(dateTestClass).isEqualTo(DateTestClass(null))
    }

    @JsonClass(generateAdapter = true)
    internal data class DateTestClass(val date: Date?)
}
