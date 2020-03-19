package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.internal.data.entity.HttpHeader
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonClass
import org.junit.jupiter.api.Test

class MoshiUtilsTest {

    @Test
    fun testHttpHeaderList_willSerialize() {
        val httpHeaderList = listOf(
            HttpHeader("name1", "value1"),
            HttpHeader("name2", "value2")
        )
        val adapter = moshi.getHttpHeaderListJsonAdapter()
        assertThat(adapter.toJson(httpHeaderList)).isEqualTo(
            """
            [
              {
                "name": "name1",
                "value": "value1"
              },
              {
                "name": "name2",
                "value": "value2"
              }
            ]
            """.trimIndent()
        )
    }

    @Test
    fun testHttpHeaderList_willDeserialize() {
        val httpHeaderListJson =
            """
            [
              {
                "name": "name1",
                "value": "value1"
              },
              {
                "name": "name2",
                "value": "value2"
              }
            ]
            """.trimIndent()
        val adapter = moshi.getHttpHeaderListJsonAdapter()
        assertThat(adapter.fromJson(httpHeaderListJson)).isEqualTo(
            listOf(
                HttpHeader("name1", "value1"),
                HttpHeader("name2", "value2")
            )
        )
    }

    @Test
    fun testMoshiConvenienceMethods_willSerializeNulls_and_willPrettyPrint() {
        val json = moshi.adapter(NullTestClass::class.java)
            .addConvenienceMethods()
            .toJson(NullTestClass(null))
        assertThat(json).isEqualTo(
            """
            {
              "string": null
            }
            """.trimIndent()
        )
    }

    @JsonClass(generateAdapter = true)
    internal data class NullTestClass(val string: String?)
}
