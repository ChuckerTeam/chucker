package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonClass
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

    @JsonClass(generateAdapter = true)
    internal data class DateTestClass(val date: Date)
}
