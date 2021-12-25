package com.chuckerteam.chucker.internal.support

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class JsonConverterTest {

    @Test
    fun `JSON converter is a singleton`() {
        val instance1 = JsonConverter.instance
        val instance2 = JsonConverter.instance

        assertThat(instance1).isEqualTo(instance2)
    }

    @Test
    fun `JSON object has null values serialized`() {
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
