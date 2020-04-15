package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class JsonConverterTest {

    @Test
    fun testInstance_sameInstance() {
        val instance1 = JsonConverter.instance
        val instance2 = JsonConverter.instance

        assertThat(instance1).isEqualTo(instance2)
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

    inner class NullTestClass(var string: String?)
}
